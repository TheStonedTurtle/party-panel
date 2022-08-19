package thestonedturtle.partypanel;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.account.SessionManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.events.PartyMemberAvatar;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.events.UserPart;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import thestonedturtle.partypanel.data.GameItem;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.data.PrayerData;
import thestonedturtle.partypanel.data.Prayers;
import thestonedturtle.partypanel.data.Stats;
import thestonedturtle.partypanel.data.events.PartyBatchedChange;
import thestonedturtle.partypanel.data.events.PartyMiscChange;
import thestonedturtle.partypanel.data.events.PartyStatChange;
import thestonedturtle.partypanel.ui.PlayerPanel;
import thestonedturtle.partypanel.ui.prayer.PrayerSprites;

@Slf4j
@PluginDescriptor(
	name = "Hub Party Panel"
)
public class PartyPanelPlugin extends Plugin
{
	private static final BufferedImage ICON = ImageUtil.loadImageResource(PartyPanelPlugin.class, "icon.png");

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Getter
	@Inject
	private PartyPanelConfig config;

	@Inject
	private PartyService partyService;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	SpriteManager spriteManager;

	@Inject
	ItemManager itemManager;

	@Inject
	private WSClient wsClient;

	@Provides
	PartyPanelConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyPanelConfig.class);
	}

	@Getter
	private final Map<Long, PartyPlayer> partyMembers = new HashMap<>();

	@Getter
	private PartyPlayer myPlayer = null;

	private NavigationButton navButton;
	private boolean addedButton = false;

	private PartyPanel panel;

	// All events should be deferred to the next game tick
	private PartyBatchedChange currentChange = new PartyBatchedChange();

	@Override
	protected void startUp() throws Exception
	{
		panel = new PartyPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("Hub Party Panel")
			.icon(ICON)
			.priority(7)
			.panel(panel)
			.build();

		wsClient.registerMessage(PartyPlayer.class);
		wsClient.registerMessage(PartyBatchedChange.class);

		if (isInParty() || config.alwaysShowIcon())
		{
			clientToolbar.addNavigation(navButton);
			addedButton = true;
		}

		if (isInParty())
		{
			clientThread.invokeLater(() ->
			{
				myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager);
				partyService.send(new UserSync());
				partyService.send(partyPlayerAsBatchedChange());
			});
		}

		final Optional<Plugin> partyPlugin = pluginManager.getPlugins().stream().filter(p -> p.getName().equals("Party")).findFirst();
		if (partyPlugin.isPresent() && !pluginManager.isPluginEnabled(partyPlugin.get()))
		{
			pluginManager.setPluginEnabled(partyPlugin.get(), true);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		addedButton = false;
		partyMembers.clear();
		wsClient.unregisterMessage(PartyPlayer.class);
		wsClient.unregisterMessage(PartyBatchedChange.class);
		currentChange = new PartyBatchedChange();
		panel.getPlayerPanelMap().clear();
	}

	@Subscribe
	protected void onConfigChanged(final ConfigChanged c)
	{
		if (!c.getGroup().equals("partypanel"))
		{
			return;
		}

		if (config.alwaysShowIcon())
		{
			if (!addedButton)
			{
				clientToolbar.addNavigation(navButton);
				addedButton = true;
			}
		}
		else if (addedButton && !isInParty())
		{
			clientToolbar.removeNavigation(navButton);
			addedButton = false;
		}
		addedButton = config.alwaysShowIcon();

		switch (c.getKey())
		{
			case "autoExpandMembers":
				panel.updatePartyMembersExpand(config.autoExpandMembers());
				break;
			case "showPartyControls":
				panel.updatePartyControls();
				break;
			case "showPartyPassphrase":
				panel.syncPartyPassphraseVisibility();
				break;
			case "displayVirtualLevels":
				panel.updateDisplayVirtualLevels();
				break;
			case "displayPlayerWorlds":
				panel.updateDisplayPlayerWorlds();
				break;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged c)
	{
		if (!isInParty())
		{
			return;
		}

		if (myPlayer == null)
		{
			myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager);
			final PartyBatchedChange ce = partyPlayerAsBatchedChange();
			partyService.send(ce);
			return;
		}

		if (c.getGameState() == GameState.LOGGED_IN)
		{
			PartyMiscChange e = new PartyMiscChange(PartyMiscChange.PartyMisc.W, client.getWorld());
			if (myPlayer.getWorld() == e.getV())
			{
				return;
			}

			myPlayer.setWorld(e.getV());
			currentChange.getM().add(e);
		}

		if (c.getGameState() == GameState.LOGIN_SCREEN)
		{
			if (myPlayer.getWorld() == 0)
			{
				return;
			}

			myPlayer.setWorld(0);
			currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.W, 0));
		}
	}

	public boolean isInParty()
	{
		return partyService.isInParty();
	}

	public boolean isLocalPlayer(long id)
	{
		return partyService.getLocalMember() != null && partyService.getLocalMember().getMemberId() == id;
	}

	@Subscribe
	public void onUserPart(final UserPart event)
	{
		final PartyPlayer removed = partyMembers.remove(event.getMemberId());
		if (removed != null)
		{
			SwingUtilities.invokeLater(() -> panel.removePartyPlayer(removed));
		}

		if (addedButton && (!isInParty() || partyService.getMembers().size() == 0) && !config.alwaysShowIcon())
		{
			clientToolbar.removeNavigation(navButton);
			addedButton = false;
		}
	}

	@Subscribe
	public void onUserSync(final UserSync event)
	{
		if (!addedButton)
		{
			clientToolbar.addNavigation(navButton);
			addedButton = true;
		}

		if (myPlayer != null)
		{
			final PartyBatchedChange c = partyPlayerAsBatchedChange();
			if (c.isValid())
			{
				partyService.send(c);
			}
			return;
		}

		clientThread.invoke(() ->
		{
			myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager);
			final PartyBatchedChange c = partyPlayerAsBatchedChange();
			if (c.isValid())
			{
				partyService.send(c);
			}
		});
	}

	@Subscribe
	public void onPartyChanged(final PartyChanged event)
	{
		partyMembers.clear();
		SwingUtilities.invokeLater(panel::renderSidebar);
		myPlayer = null;

		panel.updateParty();

		if (!isInParty())
		{
			if (!config.alwaysShowIcon())
			{
				clientToolbar.removeNavigation(navButton);
				addedButton = false;
			}
			return;
		}

		config.setPreviousPartyId(event.getPassphrase());
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!isInParty() || client.getLocalPlayer() == null || partyService.getLocalMember() == null)
		{
			return;
		}

		// First time logging in or they changed accounts so resend the entire player object
		if (myPlayer == null || !Objects.equals(client.getLocalPlayer().getName(), myPlayer.getUsername()))
		{
			myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager);
			final PartyBatchedChange c = partyPlayerAsBatchedChange();
			partyService.send(c);
			return;
		}

		if (myPlayer.getStats() == null)
		{
			myPlayer.updatePlayerInfo(client, itemManager);

			for (final Skill s : Skill.values())
			{
				currentChange.getS().add(myPlayer.getStats().createPartyStatChangeForSkill(s));
			}
		}
		else
		{
			// We only need to check energy every tick as the special attack and stat levels are handled in their respective events
			final int energy = client.getEnergy();
			if (myPlayer.getStats().getRunEnergy() != energy)
			{
				myPlayer.getStats().setRunEnergy(energy);
				currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.R, energy));
			}
		}

		if (myPlayer.getPrayers() == null)
		{
			myPlayer.setPrayers(new Prayers(client));
			final Collection<Prayer> available = new ArrayList<>();
			final Collection<Prayer> enabled = new ArrayList<>();
			for (final PrayerSprites p : PrayerSprites.values())
			{
				final PrayerData data = myPlayer.getPrayers().getPrayerData().get(p.getPrayer());
				if (data.isAvailable()) {
					available.add(p.getPrayer());
				}

				if (data.isEnabled()) {
					enabled.add(p.getPrayer());
				}
			}

			currentChange.setAp(PartyBatchedChange.pack(available));
			currentChange.setEp(PartyBatchedChange.pack(enabled));
		}
		else
		{
			final Collection<Prayer> available = new ArrayList<>();
			final Collection<Prayer> enabled = new ArrayList<>();
			boolean change = false;
			for (final PrayerSprites p : PrayerSprites.values())
			{
				change = myPlayer.getPrayers().updatePrayerState(p, client) || change;

				// Store the data for this prayer regardless of if it changes since any update
				// will assume all prayers are not available & disabled
				final PrayerData data = myPlayer.getPrayers().getPrayerData().get(p.getPrayer());
				if (data.isAvailable()) {
					available.add(p.getPrayer());
				}

				if (data.isEnabled()) {
					enabled.add(p.getPrayer());
				}
			}

			// Send both arrays as bit-packed ints whenever any prayer has changed.
			if (change)
			{
				currentChange.setAp(PartyBatchedChange.pack(available));
				currentChange.setEp(PartyBatchedChange.pack(enabled));
			}
		}

		if (currentChange.isValid())
		{
			currentChange.setMemberId(partyService.getLocalMember().getMemberId()); // Add member ID before sending
			currentChange.removeDefaults();
			partyService.send(currentChange);

			currentChange = new PartyBatchedChange();
		}
	}

	@Subscribe
	public void onStatChanged(final StatChanged event)
	{
		if (myPlayer == null || myPlayer.getStats() == null || !isInParty())
		{
			return;
		}

		final Skill s = event.getSkill();
		if (myPlayer.getSkillBoostedLevel(s) == event.getBoostedLevel() &&
			myPlayer.getSkillRealLevel(s) == event.getLevel() &&
			myPlayer.getSkillExperience(s) == event.getXp())
		{
			return;
		}

		myPlayer.setSkillsBoostedLevel(event.getSkill(), event.getBoostedLevel());
		myPlayer.setSkillsRealLevel(event.getSkill(), event.getLevel());
		myPlayer.setSkillExperience(event.getSkill(), event.getXp());

		currentChange.getS().add(new PartyStatChange(event.getSkill(), event.getLevel(), event.getBoostedLevel(), event.getXp()));

		// Total level change
		if (myPlayer.getStats().getTotalLevel() != client.getTotalLevel())
		{
			myPlayer.getStats().setTotalLevel(client.getTotalLevel());
			currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.T, myPlayer.getStats().getTotalLevel()));
		}

		// Combat level change
		final int oldCombatLevel = myPlayer.getStats().getCombatLevel();
		myPlayer.getStats().recalculateCombatLevel();
		if (myPlayer.getStats().getCombatLevel() != oldCombatLevel)
		{
			currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.C, myPlayer.getStats().getCombatLevel()));
		}
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged c)
	{
		if (myPlayer == null || !isInParty())
		{
			return;
		}

		if (c.getContainerId() == InventoryID.INVENTORY.getId())
		{
			myPlayer.setInventory(GameItem.convertItemsToGameItems(c.getItemContainer().getItems(), itemManager));
			int[] items = convertItemsToArray(c.getItemContainer().getItems());
			currentChange.setI(items);
		}
		else if (c.getContainerId() == InventoryID.EQUIPMENT.getId())
		{
			myPlayer.setEquipment(GameItem.convertItemsToGameItems(c.getItemContainer().getItems(), itemManager));
			int[] items = convertItemsToArray(c.getItemContainer().getItems());
			currentChange.setE(items);
		}
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		if (myPlayer == null || myPlayer.getStats() == null || !isInParty())
		{
			return;
		}

		final int specialPercent = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
		if (specialPercent != myPlayer.getStats().getSpecialPercent())
		{
			myPlayer.getStats().setSpecialPercent(specialPercent);
			currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.S, specialPercent));
		}

		final int stamina = client.getVarbitValue(Varbits.STAMINA_EFFECT);
		if (stamina != myPlayer.getStamina())
		{
			myPlayer.setStamina(stamina);
			currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.ST, stamina));
		}

		final int poison = client.getVar(VarPlayer.POISON);
		if (poison != myPlayer.getPoison())
		{
			myPlayer.setPoison(poison);
			currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.P, poison));
		}

		final int disease = client.getVar(VarPlayer.DISEASE_VALUE);
		if (disease != myPlayer.getDisease())
		{
			myPlayer.setDisease(disease);
			currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.D, disease));
		}
	}

	@Subscribe
	public void onPartyBatchedChange(PartyBatchedChange e)
	{
		if (isLocalPlayer(e.getMemberId()))
		{
			return;
		}

		// create new PartyPlayer for this member if they don't already exist
		final PartyPlayer player = partyMembers.computeIfAbsent(e.getMemberId(), k -> new PartyPlayer(partyService.getMemberById(e.getMemberId())));

		// Create placeholder stats object
		if (player.getStats() == null && e.hasStatChange())
		{
			player.setStats(new Stats());
		}

		// Create placeholder prayer object
		if (player.getPrayers() == null && (e.getAp() != null || e.getEp() != null))
		{
			player.setPrayers(new Prayers());
		}
		clientThread.invoke(() ->
		{
			e.process(player, itemManager);

			SwingUtilities.invokeLater(() -> {
				final PlayerPanel playerPanel = panel.getPlayerPanelMap().get(e.getMemberId());
				if (playerPanel != null)
				{
					playerPanel.updatePlayerData(player, e.hasBreakingBannerChange());
					return;
				}

				panel.drawPlayerPanel(player);
			});
		});
	}

	@Subscribe
	public void onPartyMemberAvatar(PartyMemberAvatar e)
	{
		if (isLocalPlayer(e.getMemberId()) || partyMembers.get(e.getMemberId()) == null)
		{
			return;
		}

		final PartyPlayer player = partyMembers.get(e.getMemberId());
		player.getMember().setAvatar(e.getImage());
		SwingUtilities.invokeLater(() -> panel.getPlayerPanelMap().get(e.getMemberId()).getBanner().refreshStats());
	}

	public void changeParty(String passphrase)
	{
		passphrase = passphrase.replace(" ", "-").trim();
		if (passphrase.length() == 0) {
			return;
		}

		for (int i = 0; i < passphrase.length(); ++i)
		{
			char ch = passphrase.charAt(i);
			if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '-')
			{
				JOptionPane.showMessageDialog(panel.getControlsPanel(),
					"Party passphrase must be a combination of alphanumeric or hyphen characters.",
					"Invalid party passphrase",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		partyService.changeParty(passphrase);
		panel.updateParty();
	}

	public void createParty()
	{
		// Create party
		clientThread.invokeLater(() -> changeParty(partyService.generatePassphrase()));
	}

	public String getPartyPassphrase()
	{
		return partyService.getPartyPassphrase();
	}

	public void leaveParty()
	{
		partyService.changeParty(null);
		panel.updateParty();
	}

	private int[] convertItemsToArray(Item[] items)
	{
		int[] eles = new int[items.length * 2];
		for (int i = 0; i < items.length * 2; i += 2)
		{
			if (items[i / 2] == null) {
				eles[i] = -1;
				eles[i + 1] = 0;
				continue;
			}

			eles[i] = items[i / 2].getId();
			eles[i + 1] = items[i / 2].getQuantity();
		}

		return eles;
	}

	private int[] convertGameItemsToArray(GameItem[] items)
	{
		int[] eles = new int[items.length * 2];
		for (int i = 0; i < items.length * 2; i += 2)
		{
			if (items[i / 2] == null) {
				eles[i] = -1;
				eles[i + 1] = 0;
				continue;
			}

			eles[i] = items[i / 2].getId();
			eles[i + 1] = items[i / 2].getQty();
		}

		return eles;
	}

	public PartyBatchedChange partyPlayerAsBatchedChange()
	{
		final PartyBatchedChange c = new PartyBatchedChange();
		if (myPlayer == null)
		{
			return c;
		}

		// Inventories
		c.setI(convertGameItemsToArray(myPlayer.getInventory()));
		c.setE(convertGameItemsToArray(myPlayer.getEquipment()));

		// Stats
		if (myPlayer.getStats() != null)
		{
			for (final Skill s : Skill.values())
			{
				c.getS().add(myPlayer.getStats().createPartyStatChangeForSkill(s));
			}

			c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.S, myPlayer.getStats().getSpecialPercent()));
			c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.R, myPlayer.getStats().getRunEnergy()));
			c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.C, myPlayer.getStats().getCombatLevel()));
			c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.T, myPlayer.getStats().getTotalLevel()));;
		}

		// Misc
		c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.ST, myPlayer.getStamina()));
		c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.P, myPlayer.getPoison()));
		c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.D, myPlayer.getDisease()));
		c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.W, myPlayer.getWorld()));

		// Prayers
		if (myPlayer.getPrayers() != null)
		{
			final Collection<Prayer> available = new ArrayList<>();
			final Collection<Prayer> enabled = new ArrayList<>();
			for (final PrayerSprites p : PrayerSprites.values())
			{
				final PrayerData data = myPlayer.getPrayers().getPrayerData().get(p.getPrayer());
				if (data.isAvailable())
				{
					available.add(p.getPrayer());
				}

				if (data.isEnabled())
				{
					enabled.add(p.getPrayer());
				}
			}

			c.setAp(PartyBatchedChange.pack(available));
			c.setEp(PartyBatchedChange.pack(enabled));
		}

		c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.U, myPlayer.getUsername()));

		c.setMemberId(partyService.getLocalMember().getMemberId()); // Add member ID before sending
		c.removeDefaults();

		return c;
	}
}

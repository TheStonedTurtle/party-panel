package thestonedturtle.partypanel;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.account.AccountSession;
import net.runelite.client.account.SessionManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.messages.PartyMemberMessage;
import net.runelite.client.party.messages.UserJoin;
import net.runelite.client.party.messages.UserPart;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import thestonedturtle.partypanel.data.GameItem;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.data.Prayers;
import thestonedturtle.partypanel.data.events.PartyBatchedChange;
import thestonedturtle.partypanel.data.events.PartyItemsChange;
import thestonedturtle.partypanel.data.events.PartyMiscChange;
import thestonedturtle.partypanel.data.events.PartyPrayerChange;
import thestonedturtle.partypanel.data.events.PartyPrayersChange;
import thestonedturtle.partypanel.data.events.PartyProcess;
import thestonedturtle.partypanel.data.events.PartyProcessItemManager;
import thestonedturtle.partypanel.data.events.PartyStatChange;
import thestonedturtle.partypanel.data.events.PartyStatsChange;
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
	private final Map<UUID, PartyPlayer> partyMembers = new HashMap<>();

	@Getter
	private PartyPlayer myPlayer = null;

	private NavigationButton navButton;
	private boolean addedButton = false;

	private PartyPanel panel;

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
		wsClient.registerMessage(PartyItemsChange.class);
		wsClient.registerMessage(PartyMiscChange.class);
		wsClient.registerMessage(PartyPrayerChange.class);
		wsClient.registerMessage(PartyPrayersChange.class);
		wsClient.registerMessage(PartyStatChange.class);
		wsClient.registerMessage(PartyStatsChange.class);

		// If there isn't already a session open, open one
		if (!wsClient.sessionExists())
		{
			AccountSession accountSession = sessionManager.getAccountSession();
			// Use the existing account session, if it exists, otherwise generate a new session id
			UUID uuid = accountSession != null ? accountSession.getUuid() : UUID.randomUUID();
			wsClient.changeSession(uuid);
		}

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
				wsClient.send(myPlayer);
			});
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
		wsClient.unregisterMessage(PartyItemsChange.class);
		wsClient.unregisterMessage(PartyMiscChange.class);
		wsClient.unregisterMessage(PartyPrayerChange.class);
		wsClient.unregisterMessage(PartyPrayersChange.class);
		wsClient.unregisterMessage(PartyStatChange.class);
		wsClient.unregisterMessage(PartyStatsChange.class);
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

		if (c.getKey().equals("autoExpandMembers"))
		{
			panel.updatePartyMembersExpand(config.autoExpandMembers());
		}
	}

	boolean isInParty()
	{
		// TODO: Determine if this is the correct way to check if we are in a party
		return wsClient.sessionExists() && partyService.getLocalMember() != null;
	}

	@Subscribe
	public void onUserJoin(final UserJoin event)
	{
		// TODO: Figure out how to support people not using the plugin
		if (partyService.getLocalMember() == null)
		{
			return;
		}

		if (!addedButton)
		{
			clientToolbar.addNavigation(navButton);
			addedButton = true;
		}

		// Self joined
		if (event.getMemberId().equals(partyService.getLocalMember().getMemberId()))
		{
			if (myPlayer == null)
			{
				clientThread.invoke(() ->
				{
					myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager);
					wsClient.send(myPlayer);
					return true;
				});
			}
			else
			{
				// Send the entire player object to new members
				wsClient.send(myPlayer);
			}
		}
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
		wsClient.send(myPlayer);
	}

	@Subscribe
	public void onPartyChanged(final PartyChanged event)
	{
		partyMembers.clear();
		SwingUtilities.invokeLater(panel::renderSidebar);
		myPlayer = null;

		if (!isInParty() && !config.alwaysShowIcon())
		{
			clientToolbar.removeNavigation(navButton);
			addedButton = false;
		}
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!isInParty() || client.getLocalPlayer() == null)
		{
			return;
		}

		// First time logging in or they changed accounts so resend the entire player object
		if (myPlayer == null || !Objects.equals(client.getLocalPlayer().getName(), myPlayer.getUsername()))
		{
			myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager);
			wsClient.send(myPlayer);
			return;
		}

		// Batch all messages together per tick
		final List<PartyMemberMessage> changes = new ArrayList<>();

		if (myPlayer.getStats() == null)
		{
			myPlayer.updatePlayerInfo(client, itemManager);
			changes.add(new PartyStatsChange(myPlayer.getStats()));
		}
		else
		{
			// We only need to check energy every tick as the special attack and stat levels are handled in their respective events
			final int energy = client.getEnergy();
			if (myPlayer.getStats().getRunEnergy() != energy)
			{
				myPlayer.getStats().setRunEnergy(energy);
				changes.add(new PartyMiscChange(PartyMiscChange.PartyMisc.RUN, energy));
			}
		}

		if (myPlayer.getPrayers() == null)
		{
			myPlayer.setPrayers(new Prayers(client));
			changes.add(new PartyPrayersChange(myPlayer.getPrayers()));
		}
		else
		{
			for (final PrayerSprites prayer : PrayerSprites.values())
			{
				if (myPlayer.getPrayers().updatePrayerState(prayer, client))
				{
					changes.add(new PartyPrayerChange(myPlayer.getPrayers().getPrayerData().get(prayer.getPrayer())));
				}
			}
		}

		if (changes.size() == 1)
		{
			wsClient.send(changes.get(0));
			return;
		}

		wsClient.send(new PartyBatchedChange(changes));
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

		wsClient.send(new PartyStatChange(event.getSkill(), event.getLevel(), event.getBoostedLevel(), event.getXp()));

		// Total level change
		if (myPlayer.getStats().getTotalLevel() != client.getTotalLevel())
		{
			myPlayer.getStats().setTotalLevel(client.getTotalLevel());
			wsClient.send(new PartyMiscChange(PartyMiscChange.PartyMisc.TOTAL, myPlayer.getStats().getTotalLevel()));
		}

		// Combat level change
		final int oldCombatLevel = myPlayer.getStats().getCombatLevel();
		myPlayer.getStats().recalculateCombatLevel();
		if (myPlayer.getStats().getCombatLevel() != oldCombatLevel)
		{
			wsClient.send(new PartyMiscChange(PartyMiscChange.PartyMisc.COMBAT, myPlayer.getStats().getCombatLevel()));
		}
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged c)
	{
		if (!isInParty())
		{
			return;
		}

		if (c.getContainerId() == InventoryID.INVENTORY.getId())
		{
			myPlayer.setInventory(GameItem.convertItemsToGameItems(c.getItemContainer().getItems(), itemManager));
			wsClient.send(new PartyItemsChange(PartyItemsChange.PartyItemContainer.INVENTORY, c.getItemContainer().getItems()));
		}
		else if (c.getContainerId() == InventoryID.EQUIPMENT.getId())
		{
			myPlayer.setEquipment(GameItem.convertItemsToGameItems(c.getItemContainer().getItems(), itemManager));
			wsClient.send(new PartyItemsChange(PartyItemsChange.PartyItemContainer.EQUIPMENT, c.getItemContainer().getItems()));
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
			wsClient.send(new PartyMiscChange(PartyMiscChange.PartyMisc.SPECIAL, specialPercent));
		}
	}

	@Subscribe
	public void onPartyPlayer(final PartyPlayer player)
	{
		if (!isInParty())
		{
			return;
		}

		if (player.getMemberId().equals(partyService.getLocalMember().getMemberId()))
		{
			return;
		}

		player.setMember(partyService.getMemberById(player.getMemberId()));
		if (player.getMember() == null)
		{
			return;
		}

		partyMembers.put(player.getMemberId(), player);
		SwingUtilities.invokeLater(() -> panel.renderSidebar());
	}

	@Subscribe
	public void onPartyItemsChange(PartyItemsChange e)
	{
		processPartyMemberMessage(e);
	}

	@Subscribe
	public void onPartyMiscChange(PartyMiscChange e)
	{
		processPartyMemberMessage(e);
	}

	@Subscribe
	public void onPartyPrayersChange(PartyPrayersChange e)
	{
		processPartyMemberMessage(e);
	}

	@Subscribe
	public void onPartyPrayerChange(PartyPrayerChange e)
	{
		processPartyMemberMessage(e);
	}

	@Subscribe
	public void onPartyStatsChange(PartyStatsChange e)
	{
		processPartyMemberMessage(e);
	}

	@Subscribe
	public void onPartyStatChange(PartyStatChange e)
	{
		processPartyMemberMessage(e);
	}

	@Subscribe
	public void onPartyBatchedChange(PartyBatchedChange e)
	{
		if (e.getMemberId().equals(partyService.getLocalMember().getMemberId()))
		{
			// Ignore self
			return;
		}

		final PartyPlayer player = partyMembers.get(e.getMemberId());
		e.getMessages().forEach(msg -> processPartyMemberMessage(msg, false));
		panel.getPlayerPanelMap().get(e.getMemberId()).updatePlayerData(player);
	}

	private void processPartyMemberMessage(PartyMemberMessage e)
	{
		processPartyMemberMessage(e, true);
	}

	private void processPartyMemberMessage(PartyMemberMessage e, boolean update)
	{
		if (e.getMemberId().equals(partyService.getLocalMember().getMemberId()))
		{
			// Ignore self
			return;
		}

		final PartyPlayer player = partyMembers.get(e.getMemberId());
		if (e instanceof PartyProcess)
		{
			((PartyProcess) e).process(player);
		}
		else if (e instanceof PartyProcessItemManager)
		{
			((PartyProcessItemManager) e).process(player, itemManager);
		}
		else
		{
			log.warn("Unhandled party member message: {}", e);
		}

		if (update)
		{
			panel.getPlayerPanelMap().get(e.getMemberId()).updatePlayerData(player);
		}
	}
}

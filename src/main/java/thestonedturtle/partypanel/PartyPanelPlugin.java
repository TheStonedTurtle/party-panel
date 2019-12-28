package thestonedturtle.partypanel;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import jdk.internal.jline.internal.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.ws.PartyMember;
import net.runelite.client.ws.PartyService;
import net.runelite.client.ws.WSClient;
import net.runelite.http.api.ws.messages.party.UserJoin;
import net.runelite.http.api.ws.messages.party.UserPart;
import net.runelite.http.api.ws.messages.party.UserSync;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.data.Stats;

@Slf4j
@PluginDescriptor(
	name = "Party Panel"
)
public class PartyPanelPlugin extends Plugin
{
	private final static BufferedImage ICON = ImageUtil.getResourceStreamFromClass(PartyPanelPlugin.class, "icon.png");

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PartyPanelConfig config;

	@Inject
	private PartyService partyService;

	@Inject
	private WSClient wsClient;

	@Provides
	PartyPanelConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyPanelConfig.class);
	}

	private final Map<UUID, PartyPlayer> partyMembers = new HashMap<>();

	private NavigationButton navButton;
	private PartyPanel panel;
	private PartyPlayer myPlayer;
	private boolean fetchPlayerName = false;

	@Override
	protected void startUp() throws Exception
	{
		panel = new PartyPanel();
		navButton = NavigationButton.builder()
			.tooltip("Party Panel")
			.icon(ICON)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		wsClient.registerMessage(PartyPlayer.class);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		wsClient.unregisterMessage(PartyPlayer.class);
		partyMembers.clear();
	}

	boolean isInParty()
	{
		// TODO: Determine if this is the correct way to check if we are in a party
		return partyService.getLocalMember() != null;
	}

	@Subscribe
	public void onPartyPlayer(final PartyPlayer player)
	{
		if (player.equals(myPlayer))
		{
			return;
		}

		partyMembers.put(player.getMember().getMemberId(), player);
	}

	@Subscribe
	public void onUserPart(final UserPart event)
	{
		partyMembers.remove(event.getMemberId());
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
		myPlayer.setMember(partyService.getLocalMember());
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (!isInParty())
		{
			return;
		}

		if (event.getGameState() == GameState.LOGGING_IN)
		{
			fetchPlayerName = true;
		}
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!isInParty())
		{
			return;
		}

		if (fetchPlayerName && client.getLocalPlayer() != null)
		{
			fetchPlayerName = false;
			if (myPlayer == null || !myPlayer.getUsername().equals(client.getLocalPlayer().getName()))
			{
				myPlayer = new PartyPlayer(partyService.getLocalMember(), client);
				// member changed account, send new data to all members
				wsClient.send(myPlayer);
			}
		}
	}

	@Nullable
	PartyPlayer getPartyPlayerData(final UUID uuid)
	{
		if (!isInParty())
		{
			return null;
		}

		if (uuid.equals(myPlayer.getMember().getMemberId()))
		{
			return myPlayer;
		}

		return partyMembers.get(uuid);
	}
}

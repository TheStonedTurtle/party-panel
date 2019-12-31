package thestonedturtle.partypanel;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.account.AccountSession;
import net.runelite.client.account.SessionManager;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.ws.PartyService;
import net.runelite.client.ws.WSClient;
import net.runelite.http.api.ws.messages.party.UserJoin;
import net.runelite.http.api.ws.messages.party.UserPart;
import net.runelite.http.api.ws.messages.party.UserSync;
import thestonedturtle.partypanel.data.PartyPlayer;

@Slf4j
@PluginDescriptor(
	name = "Party Panel"
)
public class PartyPanelPlugin extends Plugin
{
	private final static BufferedImage ICON = ImageUtil.getResourceStreamFromClass(PartyPanelPlugin.class, "icon.png");

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PartyPanelConfig config;

	@Inject
	private PartyService partyService;

	@Inject
	private SessionManager sessionManager;

	@Inject
	SpriteManager spriteManager;

	@Inject
	private WSClient wsClient;

	@Provides
	PartyPanelConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyPanelConfig.class);
	}

	@Getter
	private final Map<UUID, PartyPlayer> partyMembers = new HashMap<>();

	private NavigationButton navButton;
	private PartyPanel panel;
	@Getter
	private PartyPlayer myPlayer = null;
	private boolean fetchPlayerName = false;
	private boolean doSync;

	@Inject
	ItemManager itemManager;

	@Override
	protected void startUp() throws Exception
	{
		panel = new PartyPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("Party Panel")
			.icon(ICON)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		wsClient.registerMessage(PartyPlayer.class);

		// If there isn't already a session open, open one
		if (!wsClient.sessionExists())
		{
			AccountSession accountSession = sessionManager.getAccountSession();
			// Use the existing account session, if it exists, otherwise generate a new session id
			UUID uuid = accountSession != null ? accountSession.getUuid() : UUID.randomUUID();
			wsClient.changeSession(uuid);
		}

		doSync = true;
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		partyMembers.clear();
		wsClient.unregisterMessage(PartyPlayer.class);
		doSync = false;
	}

	boolean isInParty()
	{
		// TODO: Determine if this is the correct way to check if we are in a party
		return wsClient.sessionExists() && partyService.getLocalMember() != null;
	}

	@Subscribe
	public void onPartyPlayer(final PartyPlayer player)
	{
		if (player.equals(myPlayer))
		{
			return;
		}

		final boolean newMember = !partyMembers.containsKey(player.getMember().getMemberId());
		partyMembers.put(player.getMember().getMemberId(), player);
		if (newMember)
		{
			final ChatMessageBuilder builder = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append(player.getMember().getName());
			if (player.getUsername() != null)
			{
				builder.append(" (")
					.append(player.getUsername())
					.append(")");
			}
			builder.append(" has joined the party!");

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.FRIENDSCHATNOTIFICATION)
				.runeLiteFormattedMessage(builder.build())
				.build());
		}
	}

	@Subscribe
	public void onUserJoin(final UserJoin event)
	{
		// TODO: Figure out how to support people not using the plugin
		if (event.getMemberId().equals(partyService.getLocalMember().getMemberId()))
		{
			if (myPlayer == null)
			{
				myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager);
			}
			wsClient.send(myPlayer);
		}
	}

	@Subscribe
	public void onUserPart(final UserPart event)
	{
		final PartyPlayer removed = partyMembers.remove(event.getMemberId());

		if (removed != null)
		{
			final ChatMessageBuilder builder = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append(removed.getMember().getName());
			if (removed.getUsername() != null)
			{
				builder.append(" (")
					.append(removed.getUsername())
					.append(")");
			}
			builder.append(" has left the party!");

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.FRIENDSCHATNOTIFICATION)
				.runeLiteFormattedMessage(builder.build())
				.build());
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
		myPlayer = null;
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

		if (doSync && !partyService.getMembers().isEmpty())
		{
			// Request sync
			final UserSync userSync = new UserSync();
			userSync.setMemberId(partyService.getLocalMember().getMemberId());
			wsClient.send(userSync);
		}

		doSync = false;
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
				myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager);
				// member changed account, send new data to all members
				wsClient.send(myPlayer);
				SwingUtilities.invokeLater(panel::refreshUI);
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

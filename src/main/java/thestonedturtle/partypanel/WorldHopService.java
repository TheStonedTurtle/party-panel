package thestonedturtle.partypanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.game.WorldService;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

@Singleton
class WorldHopService
{
	private static final int DISPLAY_SWITCHER_MAX_ATTEMPTS = 3;
	private static final String WORLD_SWITCHER_BUSY_MESSAGE = "Please finish what you're doing before using the World Switcher.";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private WorldService worldService;

	@Inject
	private ChatMessageManager chatMessageManager;

	private net.runelite.api.World quickHopTargetWorld;
	private int displaySwitcherAttempts = 0;

	void hopToWorld(final int worldId)
	{
		clientThread.invoke(() -> hop(worldId));
	}

	void processQuickHop()
	{
		assert client.isClientThread();

		if (quickHopTargetWorld == null)
		{
			return;
		}

		if (client.getWidget(InterfaceID.Worldswitcher.BUTTONS) == null)
		{
			client.openWorldHopper();

			if (++displaySwitcherAttempts >= DISPLAY_SWITCHER_MAX_ATTEMPTS)
			{
				queueFailedQuickHopMessage();
				resetQuickHop();
			}

			return;
		}

		client.hopToWorld(quickHopTargetWorld);
		resetQuickHop();
	}

	void onChatMessage(final ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE && WORLD_SWITCHER_BUSY_MESSAGE.equals(event.getMessage()))
		{
			resetQuickHop();
		}
	}

	void reset()
	{
		resetQuickHop();
	}

	private void hop(final int worldId)
	{
		assert client.isClientThread();

		if (worldId <= 0 || worldId == client.getWorld())
		{
			return;
		}

		final WorldResult worldResult = worldService.getWorlds();
		if (worldResult == null)
		{
			worldService.refresh();
			queueConsoleMessage("Unable to find world " + worldId + ". Refreshing world list.");
			return;
		}

		final World currentWorld = worldResult.findWorld(client.getWorld());
		final World targetWorld = worldResult.findWorld(worldId);
		if (targetWorld == null)
		{
			queueConsoleMessage("Unknown world " + worldId + ".");
			return;
		}

		if (currentWorld != null
			&& !currentWorld.getTypes().contains(WorldType.PVP)
			&& targetWorld.getTypes().contains(WorldType.PVP))
		{
			queueConsoleMessage("Use the World Switcher to hop to PvP worlds.");
			return;
		}

		final net.runelite.api.World rsWorld = client.createWorld();
		rsWorld.setActivity(targetWorld.getActivity());
		rsWorld.setAddress(targetWorld.getAddress());
		rsWorld.setId(targetWorld.getId());
		rsWorld.setPlayerCount(targetWorld.getPlayers());
		rsWorld.setLocation(targetWorld.getLocation());
		rsWorld.setTypes(WorldUtil.toWorldTypes(targetWorld.getTypes()));

		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			client.changeWorld(rsWorld);
			return;
		}

		queueQuickHopMessage(targetWorld.getId());
		quickHopTargetWorld = rsWorld;
		displaySwitcherAttempts = 0;
	}

	private void resetQuickHop()
	{
		displaySwitcherAttempts = 0;
		quickHopTargetWorld = null;
	}

	private void queueConsoleMessage(final String message)
	{
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.value(message)
			.build());
	}

	private void queueQuickHopMessage(final int worldId)
	{
		final String chatMessage = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("Quick-hopping to World ")
			.append(ChatColorType.HIGHLIGHT)
			.append(Integer.toString(worldId))
			.append(ChatColorType.NORMAL)
			.append("..")
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}

	private void queueFailedQuickHopMessage()
	{
		final String chatMessage = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("Failed to quick-hop after ")
			.append(ChatColorType.HIGHLIGHT)
			.append(Integer.toString(displaySwitcherAttempts))
			.append(ChatColorType.NORMAL)
			.append(" attempts.")
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}
}

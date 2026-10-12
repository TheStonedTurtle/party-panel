package thestonedturtle.partypanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.party.PartyMember;
import org.junit.Assume;
import org.junit.Test;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.ui.PlayerPanel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartyPanelDragIntegrationTest
{
	@Test
	public void realMouseCanExpandCollapseAndReorderWholeCards() throws Exception
	{
		Assume.assumeTrue("UI mouse test is opt-in", Boolean.getBoolean("partyPanel.uiTest"));
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		AtomicReference<JFrame> frameRef = new AtomicReference<>();
		AtomicReference<PartyPanel> sidebarRef = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() ->
		{
			PartyPanelConfig config = mock(PartyPanelConfig.class);
			PartyPanelPlugin plugin = mock(PartyPanelPlugin.class);
			when(plugin.getConfig()).thenReturn(config);
			plugin.spriteManager = mock(SpriteManager.class);
			plugin.itemManager = mock(ItemManager.class);
			PartyPanel sidebar = new PartyPanel(plugin);
			sidebar.drawPlayerPanel(player(1L, "Alice"));
			sidebar.drawPlayerPanel(player(2L, "Bob"));
			JFrame frame = new JFrame("Party Panel mouse test");
			frame.setContentPane(sidebar);
			frame.setSize(300, 800);
			frame.setLocation(50, 50);
			frame.setVisible(true);
			frameRef.set(frame);
			sidebarRef.set(sidebar);
		});
		try
		{
			Robot robot = new Robot();
			robot.setAutoDelay(45);
			PartyPanel sidebar = sidebarRef.get();
			PlayerPanel first = sidebar.getPlayerPanelMap().get(1L);
			PlayerPanel second = sidebar.getPlayerPanelMap().get(2L);

			AtomicReference<Point> pointRef = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() ->
			{
				Point p = first.getBanner().getLocationOnScreen();
				pointRef.set(new Point(p.x + 12, p.y + 12));
			});
			Point point = pointRef.get();
			int collapsedHeight = first.getHeight();
			click(robot, point);
			assertTrue(first.isShowInfo());
			assertFalse(first.isDragging());
			assertTrue("Expanded card should become taller", first.getHeight() > collapsedHeight);
			click(robot, point);
			assertFalse(first.isShowInfo());
			assertEquals("Card should return to its original height", collapsedHeight, first.getHeight());
			AtomicReference<Point> nameRef = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() ->
			{
				JLabel name = findLabel(first.getBanner(), "Alice");
				Point p = name.getLocationOnScreen();
				nameRef.set(new Point(p.x + 2, p.y + name.getHeight() / 2));
			});
			click(robot, nameRef.get());
			assertTrue("Username click should expand", first.isShowInfo());
			click(robot, nameRef.get());
			assertFalse("Username click should collapse", first.isShowInfo());

			AtomicReference<Point> destinationRef = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() ->
			{
				Point p = second.getLocationOnScreen();
				destinationRef.set(new Point(point.x, p.y + second.getHeight() * 3 / 4));
			});
			Point destination = destinationRef.get();
			robot.mouseMove(point.x, point.y);
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			robot.mouseMove(point.x, point.y + 10);
			robot.waitForIdle();
			assertTrue("Dragged card should be highlighted after the movement threshold", first.isDragging());
			for (int y = point.y; y <= destination.y; y += 5)
			{
				robot.mouseMove(destination.x, y);
			}
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			robot.waitForIdle();
			assertFalse("Highlight should clear on release", first.isDragging());
			assertEquals(second, first.getParent().getComponent(0));

			// An expanded card must also be draggable from its content, while a
			// normal content click must not collapse it.
			AtomicReference<Point> bodyRef = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() ->
			{
				first.toggleExpanded();
				JPanel content = (JPanel) first.getComponent(2);
				JLabel body = new JLabel("Expanded content");
				content.add(body);
				frameRef.get().validate();
				Point p = body.getLocationOnScreen();
				bodyRef.set(new Point(p.x + 4, p.y + 4));
			});
			Point body = bodyRef.get();
			click(robot, body);
			assertTrue(first.isShowInfo());
			AtomicReference<Point> aboveRef = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() ->
			{
				Point p = second.getLocationOnScreen();
				aboveRef.set(new Point(body.x, p.y + second.getHeight() / 4));
			});
			Point above = aboveRef.get();
			robot.mouseMove(body.x, body.y);
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			for (int y = body.y; y >= above.y; y -= 5)
			{
				robot.mouseMove(above.x, y);
			}
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			robot.waitForIdle();
			assertEquals(first, first.getParent().getComponent(0));
			assertTrue(first.isShowInfo());
		}
		finally
		{
			SwingUtilities.invokeAndWait(frameRef.get()::dispose);
		}
	}

	private static PartyPlayer player(long id, String name)
	{
		PartyMember member = new PartyMember(id);
		member.setDisplayName(name);
		PartyPlayer player = new PartyPlayer(member);
		player.setUsername(name);
		return player;
	}

	private static JLabel findLabel(Component root, String text)
	{
		if (root instanceof JLabel && ((JLabel) root).getText() != null && ((JLabel) root).getText().contains(text))
		{
			return (JLabel) root;
		}
		if (root instanceof Container)
		{
			for (Component child : ((Container) root).getComponents())
			{
				JLabel found = findLabel(child, text);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}

	private static void click(Robot robot, Point point)
	{
		robot.mouseMove(point.x, point.y);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		robot.waitForIdle();
	}
}

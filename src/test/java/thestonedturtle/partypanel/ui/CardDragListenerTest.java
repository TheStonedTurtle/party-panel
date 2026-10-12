package thestonedturtle.partypanel.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CardDragListenerTest
{
	@Test
	public void receivesDragsFromNestedAndNewContentWithoutConsumingClicks() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			JPanel card = new JPanel();
			JPanel nested = new JPanel();
			JLabel label = new JLabel("player");
			card.add(nested);
			nested.add(label);
			List<String> events = new ArrayList<>();
			label.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent event)
				{
					events.add("existing click");
				}
			});
			card.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent event)
				{
					events.add("ancestor click");
				}
			});
			new CardDragListener(card, e -> events.add("press"), e -> events.add("drag"), e -> events.add("release"));
			dispatch(label, MouseEvent.MOUSE_PRESSED);
			dispatch(label, MouseEvent.MOUSE_DRAGGED);
			dispatch(label, MouseEvent.MOUSE_RELEASED);
			assertEquals(java.util.Arrays.asList("press", "drag", "existing click", "release"), events);

			JLabel newLabel = new JLabel("inventory item");
			nested.add(newLabel);
			dispatch(newLabel, MouseEvent.MOUSE_DRAGGED);
			assertEquals("drag", events.get(4));
			assertFalse(newLabel.getMouseMotionListeners().length == 0);
		});
	}

	@Test
	public void smallMovementIsClickButDragIsNot()
	{
		assertEquals(true, CardDragListener.isClick(new java.awt.Point(10, 10), new java.awt.Point(12, 13)));
		assertFalse(CardDragListener.isClick(new java.awt.Point(10, 10), new java.awt.Point(10, 18)));
	}

	private static void dispatch(Component component, int type)
	{
		MouseEvent event = new MouseEvent(component, type, System.currentTimeMillis(),
			MouseEvent.BUTTON1_DOWN_MASK, 2, 2, 1, false, MouseEvent.BUTTON1);
		component.dispatchEvent(event);
	}
}

package thestonedturtle.partypanel.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/** Observes mouse gestures anywhere in a card, including rebuilt tab content. */
public class CardDragListener
{
	public static boolean isClick(Point start, Point end)
	{
		return start != null && start.distance(end) < 6;
	}

	private final MouseAdapter mouseListener;
	private final ContainerAdapter containerListener = new ContainerAdapter()
	{
		@Override
		public void componentAdded(ContainerEvent event)
		{
			attach(event.getChild());
		}

		@Override
		public void componentRemoved(ContainerEvent event)
		{
			detach(event.getChild());
		}
	};

	public CardDragListener(Component root, Consumer<MouseEvent> pressed, Consumer<MouseEvent> dragged, Consumer<MouseEvent> released)
	{
		mouseListener = new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent event)
			{
				pressed.accept(event);
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
				dragged.accept(event);
			}

			@Override
			public void mouseReleased(MouseEvent event)
			{
				released.accept(event);
			}
		};
		attach(root);
	}

	private void attach(Component component)
	{
		component.addMouseListener(mouseListener);
		component.addMouseMotionListener(mouseListener);
		if (component instanceof Container)
		{
			Container container = (Container) component;
			container.addContainerListener(containerListener);
			for (Component child : container.getComponents())
			{
				attach(child);
			}
		}
	}

	private void detach(Component component)
	{
		component.removeMouseListener(mouseListener);
		component.removeMouseMotionListener(mouseListener);
		if (component instanceof Container)
		{
			Container container = (Container) component;
			container.removeContainerListener(containerListener);
			for (Component child : container.getComponents())
			{
				detach(child);
			}
		}
	}
}

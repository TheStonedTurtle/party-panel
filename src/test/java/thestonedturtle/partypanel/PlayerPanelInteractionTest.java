package thestonedturtle.partypanel;

import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.party.PartyMember;
import org.junit.Test;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.ui.PlayerPanel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PlayerPanelInteractionTest
{
	@Test
	public void toggleExpandedReversesCardState() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			PartyMember member = new PartyMember(1L);
			member.setDisplayName("Alice");
			PlayerPanel card = new PlayerPanel(new PartyPlayer(member), mock(PartyPanelConfig.class),
				mock(SpriteManager.class), mock(ItemManager.class));
			assertFalse(card.isShowInfo());
			card.toggleExpanded();
			assertTrue(card.isShowInfo());
			card.toggleExpanded();
			assertFalse(card.isShowInfo());
		});
	}

	@Test
	public void dragOutlineAppearsOnlyDuringDrag() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			PartyMember member = new PartyMember(1L);
			PlayerPanel card = new PlayerPanel(new PartyPlayer(member), mock(PartyPanelConfig.class),
				mock(SpriteManager.class), mock(ItemManager.class));
			card.setSize(210, 100);
			BufferedImage before = paint(card);
			card.setDragging(true);
			assertTrue(card.isDragging());
			BufferedImage during = paint(card);
			assertFalse("The outline should be visible", before.getRGB(1, 1) == during.getRGB(1, 1));
			card.setDragging(false);
			assertFalse(card.isDragging());
			assertEquals(before.getRGB(1, 1), paint(card).getRGB(1, 1));
		});
	}

	private static BufferedImage paint(PlayerPanel card)
	{
		BufferedImage image = new BufferedImage(210, 100, BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D graphics = image.createGraphics();
		card.paint(graphics);
		graphics.dispose();
		return image;
	}
}

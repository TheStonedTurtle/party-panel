package thestonedturtle.partypanel.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class PlayerBannerAfkTest
{
	@Test
	public void testTooltipShowsWholeMinutes()
	{
		assertEquals("AFK for 1 minute", PlayerBanner.formatAfkTooltip(1));
		assertEquals("AFK for 2 minutes", PlayerBanner.formatAfkTooltip(2));
		assertNull(PlayerBanner.formatAfkTooltip(0));
	}

	@Test
	public void testBundledZzzIconIsCompactTransparentAndWhite() throws Exception
	{
		assertNotNull(PlayerBanner.class.getResource("zzz.png"));
		BufferedImage icon = ImageIO.read(PlayerBanner.class.getResource("zzz.png"));
		assertEquals(18, icon.getWidth());
		assertEquals(18, icon.getHeight());
		assertEquals(0, (icon.getRGB(0, 0) >>> 24) & 0xff);
		int visible = 0;
		for (int y = 0; y < icon.getHeight(); y++)
		{
			for (int x = 0; x < icon.getWidth(); x++)
			{
				int pixel = icon.getRGB(x, y);
				if ((pixel >>> 24) > 0)
				{
					visible++;
					assertEquals(0xffffff, pixel & 0xffffff);
				}
			}
		}
		assertTrue(visible > 0);
	}
}

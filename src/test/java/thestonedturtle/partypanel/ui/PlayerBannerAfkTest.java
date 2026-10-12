package thestonedturtle.partypanel.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class PlayerBannerAfkTest
{
	@Test
	public void testTooltipUsesSingularAndPluralMinutes()
	{
		assertEquals("AFK for 1 minute", PlayerBanner.formatAfkTooltip(1));
		assertEquals("AFK for 2 minutes", PlayerBanner.formatAfkTooltip(2));
		assertNull(PlayerBanner.formatAfkTooltip(0));
	}
}

package thestonedturtle.partypanel;

import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Tests that AFK config items exist with correct annotations.
 * Checks the setting defaults and the allowed threshold range.
 */
public class PartyPanelConfigAfkTest
{
	private final PartyPanelConfig defaults = new PartyPanelConfig()
	{
		@Override
		public void setPreviousPartyId(String id)
		{
		}
	};

	@Test
	public void testAfkDefaults()
	{
		assertTrue(defaults.showAfkIndicator());
		assertEquals(5, defaults.afkThresholdMinutes());
	}

	@Test
	public void testThresholdRange() throws Exception
	{
		Range range = PartyPanelConfig.class.getMethod("afkThresholdMinutes").getAnnotation(Range.class);
		assertNotNull(range);
		assertEquals(1, range.min());
		assertEquals(60, range.max());
	}
	@Test
	public void testShowAfkIndicatorMethodExists() throws Exception
	{
		Method method = PartyPanelConfig.class.getMethod("showAfkIndicator");
		ConfigItem annotation = method.getAnnotation(ConfigItem.class);
		assertNotNull("showAfkIndicator must have @ConfigItem", annotation);
		assertFalse("showAfkIndicator should not be hidden", annotation.hidden());
	}

	@Test
	public void testAfkThresholdMinutesMethodExists() throws Exception
	{
		Method method = PartyPanelConfig.class.getMethod("afkThresholdMinutes");
		ConfigItem annotation = method.getAnnotation(ConfigItem.class);
		assertNotNull("afkThresholdMinutes must have @ConfigItem", annotation);
		assertFalse("afkThresholdMinutes should not be hidden", annotation.hidden());
	}

	@Test
	public void testShowAfkIndicatorKeyname() throws Exception
	{
		Method method = PartyPanelConfig.class.getMethod("showAfkIndicator");
		ConfigItem annotation = method.getAnnotation(ConfigItem.class);
		assertEquals("showAfkIndicator", annotation.keyName());
	}

	@Test
	public void testAfkThresholdMinutesKeyname() throws Exception
	{
		Method method = PartyPanelConfig.class.getMethod("afkThresholdMinutes");
		ConfigItem annotation = method.getAnnotation(ConfigItem.class);
		assertEquals("afkThresholdMinutes", annotation.keyName());
	}

	@Test
	public void testShowAfkIndicatorReturnType() throws Exception
	{
		Method method = PartyPanelConfig.class.getMethod("showAfkIndicator");
		assertEquals("Return type must be boolean", boolean.class, method.getReturnType());
	}

	@Test
	public void testAfkThresholdMinutesReturnType() throws Exception
	{
		Method method = PartyPanelConfig.class.getMethod("afkThresholdMinutes");
		assertEquals("Return type must be int", int.class, method.getReturnType());
	}
}

package thestonedturtle.partypanel;

import net.runelite.client.config.ConfigItem;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Tests that AFK config items exist with correct annotations.
 * Checks the default-on toggle and absence of a threshold input.
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
	}

	@Test
	public void testNoThresholdSetting()
	{
		for (Method method : PartyPanelConfig.class.getDeclaredMethods())
		{
			assertNotEquals("afkThresholdMinutes", method.getName());
		}
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
	public void testShowAfkIndicatorKeyname() throws Exception
	{
		Method method = PartyPanelConfig.class.getMethod("showAfkIndicator");
		ConfigItem annotation = method.getAnnotation(ConfigItem.class);
		assertEquals("showAfkIndicator", annotation.keyName());
	}

	@Test
	public void testShowAfkIndicatorReturnType() throws Exception
	{
		Method method = PartyPanelConfig.class.getMethod("showAfkIndicator");
		assertEquals("Return type must be boolean", boolean.class, method.getReturnType());
	}

}

package thestonedturtle.partypanel;

import org.junit.Test;
import static org.junit.Assert.*;
import thestonedturtle.partypanel.data.events.PartyMiscChange;

/**
 * Tests PartyMiscChange.AFK enum exists and can be instantiated.
 * Full plugin lifecycle testing requires RuneLite client runtime.
 */
public class PartyPanelPluginAfkTest
{
	@Test
	public void testAfkMiscEnumExists()
	{
		PartyMiscChange.PartyMisc afk = PartyMiscChange.PartyMisc.AFK;
		assertNotNull(afk);
		assertEquals("AFK", afk.name());
	}

	@Test
	public void testAfkMiscChangeConstructable()
	{
		PartyMiscChange change = new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 1);
		assertNotNull(change);
		assertEquals(PartyMiscChange.PartyMisc.AFK, change.getT());
		assertEquals(Integer.valueOf(1), change.getV());
	}

	@Test
	public void testAfkMiscChangeWithValueZero()
	{
		PartyMiscChange change = new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 0);
		assertEquals(Integer.valueOf(0), change.getV());
	}

	@Test
	public void testAfkValueSemantics()
	{
		// Non-zero = AFK, zero = active
		PartyMiscChange afkTrue = new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 1);
		assertTrue(afkTrue.getV() != 0);

		PartyMiscChange afkFalse = new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 0);
		assertFalse(afkFalse.getV() != 0);
	}
}

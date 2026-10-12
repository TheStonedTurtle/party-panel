package thestonedturtle.partypanel;

import org.junit.Test;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.data.events.PartyMiscChange;
import static org.junit.Assert.*;

public class PartyPlayerAfkTest
{
	@Test
	public void testDefaultAfkIsFalse()
	{
		PartyPlayer player = createPlayer();
		assertFalse(player.isAfk());
	}

	@Test
	public void testAfkMiscChangeSetsAfkTrue()
	{
		PartyPlayer player = createPlayer();
		PartyMiscChange change = new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 1);
		change.process(player);
		assertTrue(player.isAfk());
	}

	@Test
	public void testAfkMiscChangeSetsAfkFalse()
	{
		PartyPlayer player = createPlayer();
		player.setAfk(true);
		assertTrue(player.isAfk());

		PartyMiscChange change = new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 0);
		change.process(player);
		assertFalse(player.isAfk());
	}

	@Test
	public void testAfkValueZeroMeansNotAfk()
	{
		PartyPlayer player = createPlayer();
		PartyMiscChange change = new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 0);
		change.process(player);
		assertFalse(player.isAfk());
	}

	@Test
	public void testAfkToggle()
	{
		PartyPlayer player = createPlayer();
		assertFalse(player.isAfk());

		new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 1).process(player);
		assertTrue(player.isAfk());

		new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 0).process(player);
		assertFalse(player.isAfk());
	}

	@Test
	public void testAfkMessageCarriesWholeMinutesAndClears()
	{
		PartyPlayer player = createPlayer();
		new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 2).process(player);
		assertEquals(2, player.getAfkMinutes());
		new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 0).process(player);
		assertEquals(0, player.getAfkMinutes());
	}

	private PartyPlayer createPlayer()
	{
		// Create a minimal PartyPlayer without needing a real PartyMember
		return new PartyPlayer(null);
	}
}

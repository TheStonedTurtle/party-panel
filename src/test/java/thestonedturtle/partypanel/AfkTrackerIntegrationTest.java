package thestonedturtle.partypanel;

import org.junit.Test;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.data.events.PartyBatchedChange;
import thestonedturtle.partypanel.data.events.PartyMiscChange;
import java.util.Collections;
import static org.junit.Assert.*;

/**
 * Integration tests: PartyBatchedChange correctly propagates AFK misc changes to PartyPlayer.
 */
public class AfkTrackerIntegrationTest
{
	@Test
	public void testBatchedChangeWithAfkMiscSetsPlayerAfk()
	{
		PartyPlayer player = new PartyPlayer(null);
		assertFalse(player.isAfk());

		PartyBatchedChange change = new PartyBatchedChange();
		change.setM(Collections.singletonList(new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 1)));
		change.process(player, null);

		assertTrue("AFK misc change should set player.afk to true", player.isAfk());
	}

	@Test
	public void testBatchedChangeWithAfkFalseMiscClearsPlayerAfk()
	{
		PartyPlayer player = new PartyPlayer(null);
		player.setAfk(true);
		assertTrue(player.isAfk());

		PartyBatchedChange change = new PartyBatchedChange();
		change.setM(Collections.singletonList(new PartyMiscChange(PartyMiscChange.PartyMisc.AFK, 0)));
		change.process(player, null);

		assertFalse("AFK=0 misc change should clear player.afk", player.isAfk());
	}

	@Test
	public void testBatchedChangeWithoutAfkMiscDoesNotChangeAfk()
	{
		PartyPlayer player = new PartyPlayer(null);
		player.setAfk(false);

		// Misc change with only a different type (not AFK)
		PartyBatchedChange change = new PartyBatchedChange();
		change.setM(Collections.singletonList(new PartyMiscChange(PartyMiscChange.PartyMisc.W, 5)));
		change.process(player, null);

		assertFalse("Non-AFK misc change should not affect afk state", player.isAfk());
	}

	@Test
	public void testBatchedChangeEmptyMiscDoesNotAffectAfk()
	{
		PartyPlayer player = new PartyPlayer(null);
		player.setAfk(true);

		PartyBatchedChange change = new PartyBatchedChange();
		change.setM(Collections.emptyList());
		change.process(player, null);

		assertTrue("Empty misc changes should not affect afk state", player.isAfk());
	}
}

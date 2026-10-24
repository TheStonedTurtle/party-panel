package thestonedturtle.partypanel;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlayerOrderTest
{
	@Test
	public void movesMemberBeforeAndAfterTarget()
	{
		PlayerOrder order = new PlayerOrder();
		order.reconcile(Arrays.asList(1L, 2L, 3L));
		assertTrue(order.move(3L, 1L, true));
		assertEquals(Arrays.asList(3L, 1L, 2L), order.getMembers());
		assertTrue(order.move(3L, 2L, false));
		assertEquals(Arrays.asList(1L, 2L, 3L), order.getMembers());
	}

	@Test
	public void ignoresSelfAndUnknownTargets()
	{
		PlayerOrder order = new PlayerOrder();
		order.reconcile(Arrays.asList(1L, 2L));
		assertFalse(order.move(1L, 1L, false));
		assertFalse(order.move(1L, 99L, true));
		assertEquals(Arrays.asList(1L, 2L), order.getMembers());
	}

	@Test
	public void adjacentPlacementDoesNotMoveMember()
	{
		PlayerOrder order = new PlayerOrder();
		order.reconcile(Arrays.asList(1L, 2L, 3L));
		assertFalse(order.move(1L, 2L, true));
		assertFalse(order.move(3L, 2L, false));
		assertEquals(Arrays.asList(1L, 2L, 3L), order.getMembers());
	}

	@Test
	public void retainsCustomOrderAcrossMembershipChanges()
	{
		PlayerOrder order = new PlayerOrder();
		order.reconcile(Arrays.asList(1L, 2L));
		order.move(2L, 1L, true);
		order.reconcile(Arrays.asList(1L, 2L, 3L));
		assertEquals(Arrays.asList(2L, 1L, 3L), order.getMembers());
		order.reconcile(Arrays.asList(1L, 3L));
		assertEquals(Arrays.asList(1L, 3L), order.getMembers());
		order.reconcile(Collections.emptyList());
		assertEquals(Collections.emptyList(), order.getMembers());
	}
}

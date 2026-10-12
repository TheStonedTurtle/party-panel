package thestonedturtle.partypanel;

import org.junit.Test;
import org.junit.Before;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.*;

public class AfkTrackerTest
{
	private AfkTracker tracker;

	@Before
	public void setUp()
	{
		tracker = new AfkTracker();
	}

	@Test
	public void testNotAfkImmediatelyAfterActivity()
	{
		tracker.recordActivity();
		assertFalse(tracker.isAfk(5));
	}

	@Test
	public void testAfkAfterThresholdExceeded()
	{
		// Simulate threshold minutes + 1 second of inactivity
		tracker = new AfkTracker(5 * 60 * 1000 + 1000);
		assertTrue(tracker.isAfk(5));
	}

	@Test
	public void testNotAfkBeforeThreshold()
	{
		// 4 minutes elapsed out of 5 minute threshold
		tracker = new AfkTracker(4 * 60 * 1000);
		assertFalse(tracker.isAfk(5));
	}

	@Test
	public void testAfkAtExactThresholdBoundary()
	{
		// Exactly 5 minutes elapsed (boundary)
		tracker = new AfkTracker(5 * 60 * 1000);
		assertTrue(tracker.isAfk(5));
	}

	@Test
	public void testRecordActivityResetsAfk()
	{
		// Start AFK
		tracker = new AfkTracker(10 * 60 * 1000);
		assertTrue(tracker.isAfk(5));

		// Record activity resets
		tracker.recordActivity();
		assertFalse(tracker.isAfk(5));
	}

	@Test
	public void testResetClearsAfk()
	{
		tracker = new AfkTracker(10 * 60 * 1000);
		assertTrue(tracker.isAfk(5));

		tracker.reset();
		assertFalse(tracker.isAfk(5));
	}

	@Test
	public void testCustomThreshold()
	{
		// 3 minutes elapsed, threshold is 2
		tracker = new AfkTracker(3 * 60 * 1000);
		assertTrue(tracker.isAfk(2));

		// 3 minutes elapsed, threshold is 5
		tracker = new AfkTracker(3 * 60 * 1000);
		assertFalse(tracker.isAfk(5));
	}

	@Test
	public void testOneMinuteThreshold()
	{
		// 61 seconds elapsed, threshold is 1 minute
		tracker = new AfkTracker(61 * 1000);
		assertTrue(tracker.isAfk(1));
	}

	@Test
	public void testInvalidThresholdIsClampedToOneMinute()
	{
		tracker = new AfkTracker(30 * 1000);
		assertFalse(tracker.isAfk(0));
		assertFalse(tracker.isAfk(-1));
	}

	@Test
	public void testAfkMinutesRoundToNearestWholeMinute()
	{
		AtomicLong now = new AtomicLong(1_000_000L);
		tracker = new AfkTracker(now::get);
		assertEquals(0, tracker.getAfkMinutes(1));
		now.addAndGet(60_000L);
		assertEquals(1, tracker.getAfkMinutes(1));
		now.addAndGet(29_999L);
		assertEquals(1, tracker.getAfkMinutes(1));
		now.incrementAndGet();
		assertEquals(2, tracker.getAfkMinutes(1));
		now.addAndGet(60_000L);
		assertEquals(3, tracker.getAfkMinutes(1));
		tracker.recordActivity();
		assertEquals(0, tracker.getAfkMinutes(1));
	}
}

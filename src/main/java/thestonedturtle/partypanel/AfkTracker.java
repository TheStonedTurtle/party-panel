package thestonedturtle.partypanel;

import java.util.function.Supplier;

/**
 * Tracks user mouse/keyboard activity to determine AFK status.
 * Uses an injectable clock for testability.
 * Records only event timing; the plugin shares inactivity rounded to whole minutes.
 */
public class AfkTracker
{
	private volatile long lastActivityTime;
	private final Supplier<Long> clock;

	/**
	 * Creates an AfkTracker using the system clock.
	 */
	public AfkTracker()
	{
		this(System::currentTimeMillis);
	}

	/**
	 * Creates an AfkTracker simulating the given elapsed inactivity time (for testing).
	 * @param elapsedMillis the simulated elapsed time since last activity, in milliseconds
	 */
	public AfkTracker(final long elapsedMillis)
	{
		this(System::currentTimeMillis);
		this.lastActivityTime = clock.get() - elapsedMillis;
	}

	AfkTracker(final Supplier<Long> clock)
	{
		this.clock = clock;
		this.lastActivityTime = clock.get();
	}

	/**
	 * Records user activity, resetting the idle timer.
	 */
	public void recordActivity()
	{
		lastActivityTime = clock.get();
	}

	/**
	 * Checks if the user is considered AFK based on the threshold.
	 * @param thresholdMinutes the inactivity threshold in minutes
	 * @return true if idle time exceeds threshold
	 */
	public boolean isAfk(final int thresholdMinutes)
	{
		return getAfkMinutes(thresholdMinutes) > 0;
	}

	/**
	 * Returns inactivity rounded to the nearest whole minute once AFK.
	 * Zero means active; one means at least one minute without input.
	 */
	public int getAfkMinutes(final int thresholdMinutes)
	{
		final long elapsed = clock.get() - lastActivityTime;
		if (elapsed < Math.max(1, thresholdMinutes) * 60_000L)
		{
			return 0;
		}
		final long nearestMinute = elapsed / 60_000L + (elapsed % 60_000L >= 30_000L ? 1 : 0);
		return (int) Math.min(Integer.MAX_VALUE, nearestMinute);
	}

	/**
	 * Resets the tracker to non-AFK state.
	 */
	public void reset()
	{
		lastActivityTime = clock.get();
	}
}

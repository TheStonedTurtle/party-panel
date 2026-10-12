package thestonedturtle.partypanel;

import java.util.function.Supplier;

/**
 * Tracks user mouse/keyboard activity to determine AFK status.
 * Uses an injectable clock for testability.
 * Records only event timing; the plugin shares a boolean AFK state with the party.
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
		this.lastActivityTime = clock.get();
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

	private AfkTracker(final Supplier<Long> clock)
	{
		this.clock = clock;
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
		final long elapsed = clock.get() - lastActivityTime;
		return elapsed >= Math.max(1, thresholdMinutes) * 60 * 1000L;
	}

	/**
	 * Resets the tracker to non-AFK state.
	 */
	public void reset()
	{
		lastActivityTime = clock.get();
	}
}

package thestonedturtle.partypanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Keeps the user's local card order while members arrive and leave. */
class PlayerOrder
{
	private final List<Long> members = new ArrayList<>();

	List<Long> getMembers()
	{
		return Collections.unmodifiableList(members);
	}

	void reconcile(Collection<Long> currentMembers)
	{
		members.retainAll(currentMembers);
		for (Long id : currentMembers)
		{
			if (!members.contains(id))
			{
				members.add(id);
			}
		}
	}

	boolean move(long source, long target, boolean before)
	{
		if (source == target || !members.contains(source) || !members.contains(target))
		{
			return false;
		}

		int original = members.indexOf(source);
		members.remove(original);
		int destination = members.indexOf(target) + (before ? 0 : 1);
		members.add(destination, source);
		return original != destination;
	}
}

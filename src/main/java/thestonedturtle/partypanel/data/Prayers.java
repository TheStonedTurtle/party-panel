/*
 * Copyright (c) 2020, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package thestonedturtle.partypanel.data;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.client.party.PartyMember;
import thestonedturtle.partypanel.ui.prayer.PrayerSprites;

import java.util.HashMap;
import java.util.Map;

public class Prayers
{
	/**
	 * Checks if the prayer is available to be used (checks all requirements)
	 * <ul>
	 * <li> int	Jagex prayer index to check (See {@link PrayerSprites} scriptIndex property) </li>
	 * </ul>
	 * Returns
	 * <ul>
	 * <li> int	(boolean) whether the prayer can be activated </li>
	 * </ul>
	 */
	public static final int PRAYER_IS_AVAILABLE = 10154;

	@Getter
	private final Map<Prayer, PrayerData> prayerData = new HashMap<>();

	public Prayers()
	{
		for (final Prayer p : Prayer.values())
		{
			prayerData.put(p, new PrayerData(p, p.ordinal() == 0, false, isUnlockedByDefault(p)));
		}
	}

	public Prayers(final Client client)
	{
		// Initialize all prayers if created when logged in
		if (client.getLocalPlayer() != null)
		{
			for (final PrayerSprites p : PrayerSprites.values())
			{
				updatePrayerState(p, client);
			}
		}
	}

	public boolean updatePrayerState(final PrayerSprites p, final Client client)
	{
		boolean available, unlocked;
		if (p.isUnlocked(client))
		{
			client.runScript(PRAYER_IS_AVAILABLE, p.getItemID());
			available = client.getIntStack()[0] > 0;
			unlocked = true;
		}
		else
		{
			available = false;
			unlocked = false;
		}

		boolean changed;
		PrayerData data = prayerData.get(p.getPrayer());
		if (data == null)
		{
			// enabled is handled from PartyDataService, default to false
			data = new PrayerData(p.getPrayer(), available, false, unlocked);
			changed = true;
		}
		else
		{
			changed = data.isAvailable() != available || data.isUnlocked() != unlocked;
			data.setAvailable(available);
		}

		prayerData.put(data.getPrayer(), data);
		return changed;
	}

	public static boolean isUnlockedByDefault(Prayer p)
	{
		return !p.name().equals("DEADEYE") && !p.name().equals("MYSTIC_VIGOUR");
	}

	// PartyDataService handles transmitting currently enabled prayers
	public void updateFromPartyMember(final PartyMember member)
	{
		for (final Prayer p : Prayer.values())
		{
			PrayerData data = prayerData.get(p);
			data.setEnabled(member.getPrayers().contains(p));
		}
	}
}

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

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.Prayer;
import net.runelite.api.Varbits;
import thestonedturtle.partypanel.ui.prayer.PrayerSprites;

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
	public static final int PRAYER_IS_AVAILABLE = 464;

	@Getter
	private final Map<Prayer, PrayerData> prayerData = new HashMap<>();
	private int[] prayerIds = new int[0];

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
		else
		{
			setCurrentPrayerIds(client);
		}
	}

	public boolean updatePrayerState(final PrayerSprites p, final Client client)
	{
		if (prayerIds.length == 0)
		{
			setCurrentPrayerIds(client);
		}

		assert prayerIds.length > 0;
		boolean changed, available, enabled, unlocked;

		PrayerData data = prayerData.get(p.getPrayer());
		if (p.isUnlocked(client))
		{
			client.runScript(PRAYER_IS_AVAILABLE, prayerIds[p.getScriptIndex()]);
			available = client.getIntStack()[0] > 0;
			enabled = client.isPrayerActive(p.getPrayer());
			unlocked = true;
		}
		else
		{
			available = false;
			enabled = false;
			unlocked = false;
		}


		if (data == null)
		{
			data = new PrayerData(p.getPrayer(), available, enabled, unlocked);
			changed = true;
		}
		else
		{
			changed = data.isAvailable() != available || data.isEnabled() != enabled || data.isUnlocked() != unlocked;
			data.setAvailable(available);
			data.setEnabled(enabled);
		}

		prayerData.put(data.getPrayer(), data);
		return changed;
	}

	private void setCurrentPrayerIds(Client client)
	{
		final EnumComposition prayers = getPrayerEnum(client);
		this.prayerIds = prayers.getIntVals();
	}

	private EnumComposition getPrayerEnum(Client client)
	{
		boolean deadeye = client.getVarbitValue(Varbits.PRAYER_DEADEYE_UNLOCKED) != 0;
		boolean vigour = client.getVarbitValue(Varbits.PRAYER_MYSTIC_VIGOUR_UNLOCKED) != 0;

		if (deadeye && vigour)
		{
			return client.getEnum(EnumID.PRAYERS_NORMAL_DEADEYE_MYSTIC_VIGOUR);
		}
		else if (deadeye)
		{
			return client.getEnum(EnumID.PRAYERS_NORMAL_DEADEYE);
		}
		else if (vigour)
		{
			return client.getEnum(EnumID.PRAYERS_NORMAL_MYSTIC_VIGOUR);
		}
		else
		{
			return client.getEnum(EnumID.PRAYERS_NORMAL);
		}
	}

	public static boolean isUnlockedByDefault(Prayer p)
	{
		return !p.name().equals("DEADEYE") && !p.name().equals("MYSTIC_VIGOUR");
	}
}

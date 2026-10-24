/*
 * Copyright (c) 2022, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package thestonedturtle.partypanel.data.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.runelite.api.Prayer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.party.messages.PartyMemberMessage;
import thestonedturtle.partypanel.data.PartyPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyBatchedChange extends PartyMemberMessage
{
	Collection<PartyMiscChange> m = new ArrayList<>(); // Misc Changes
	Integer ap; // Available Prayers, bit-packed & contains all available prayers on every change
	Integer up; // Unlocked Prayers, bit-packed & contains all unlocked prayers on every change

	public boolean isValid()
	{
		return (m != null && !m.isEmpty())
				|| ap != null
				|| up != null;
	}

	// Unset unneeded variables to minimize payload
	public void removeDefaults()
	{
		m = (m == null || m.isEmpty()) ? null : m;
	}

	public void process(PartyPlayer player, ItemManager itemManager)
	{
		// A lot of this data is now handled by the PartyDataService
		if (m != null)
		{
			m.forEach(change -> change.process(player));
		}

		if (ap != null || up != null)
		{
			processPrayers(player);
		}
	}

	private void processPrayers(PartyPlayer player)
	{
		// Default all prayers to not available and not enabled
		player.getPrayers().getPrayerData().forEach((idx, p) ->
		{
			p.setAvailable(false);
			p.setEnabled(false);
			p.setUnlocked(false);
		});

		// enabled prayers are handled by PartyDataService
		if (ap != null)
		{
			for (final Prayer p : unpack(ap))
			{
				player.getPrayers().getPrayerData().get(p).setAvailable(true);
			}
		}

		if (up != null)
		{
			for (final Prayer p : unpack(up))
			{
				player.getPrayers().getPrayerData().get(p).setUnlocked(true);
			}
		}
	}

	public boolean hasBreakingBannerChange()
	{
		return m != null
				&& m.stream()
				.anyMatch(e -> e.t.equals(PartyMiscChange.PartyMisc.W));
	}

	public static <E extends Enum<E>> int pack(Collection<E> items)
	{
		int i = 0;
		for (E e : items)
		{
			assert e.ordinal() < 32;
			i |= (1 << e.ordinal());
		}

		return i;
	}

	private Collection<Prayer> unpack(int pack)
	{
		final List<Prayer> out = new ArrayList<>();
		for (Prayer p : Prayer.values())
		{
			// We bitpack into an int of 32 bits, as of now that's fine but the ruinous powers could break this
			// since the prayer enum has these defined still break out early.
			if (p.ordinal() >= 32)
			{
				break;
			}

			if ((pack & (1 << p.ordinal())) != 0)
			{
				out.add(p);
			}
		}

		return out;
	}
}

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

import java.util.ArrayList;
import java.util.Collection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.party.messages.PartyMemberMessage;
import thestonedturtle.partypanel.data.PartyPlayer;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyBatchedChange extends PartyMemberMessage
{
	PartyItemsChange i; // Inventory
	PartyItemsChange e; // equipment
	Collection<PartyPrayerChange> p = new ArrayList<>(); // Prayer Changes
	Collection<PartyStatChange> s = new ArrayList<>(); // Stat Changes
	Collection<PartyMiscChange> m = new ArrayList<>(); // Misc Changes

	public boolean isValid()
	{
		return i != null
			|| e != null
			|| (p != null && p.size() > 0)
			|| (s != null && s.size() > 0)
			|| (m != null && m.size() > 0);
	}

	// Unset unneeded variables to minimize payload
	public void removeDefaults()
	{
		p = (p == null || p.size() == 0) ? null : p;
		s = (s == null || s.size() == 0) ? null : s;
		m = (m == null || m.size() == 0) ? null : m;
	}

	public void process(PartyPlayer player, ItemManager itemManager)
	{
		if (i != null)
		{
			i.process(player, itemManager);
		}

		if (e != null)
		{
			e.process(player, itemManager);
		}

		if (p != null)
		{
			p.forEach(change -> change.process(player));
		}

		if (s != null)
		{
			s.forEach(change -> change.process(player));
		}

		if (m != null)
		{
			m.forEach(change -> change.process(player));
		}
	}
}

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
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.runelite.api.Prayer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.party.messages.PartyMemberMessage;
import thestonedturtle.partypanel.data.GameItem;
import thestonedturtle.partypanel.data.PartyPlayer;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyBatchedChange extends PartyMemberMessage
{
	int[] i; // Inventory
	int[] e; // equipment
	Collection<PartyStatChange> s = new ArrayList<>(); // Stat Changes
	Collection<PartyMiscChange> m = new ArrayList<>(); // Misc Changes
	Integer ap; // Available Prayers, bit-packed & contains all available prayers on every change
	Integer ep; // Enabled Prayers, bit-packed & contains all enabled prayers on every change

	public boolean isValid()
	{
		return i != null
			|| e != null
			|| (s != null && s.size() > 0)
			|| (m != null && m.size() > 0)
			|| ap != null
			|| ep != null;
	}

	// Unset unneeded variables to minimize payload
	public void removeDefaults()
	{
		s = (s == null || s.size() == 0) ? null : s;
		m = (m == null || m.size() == 0) ? null : m;
	}

	public void process(PartyPlayer player, ItemManager itemManager)
	{
		if (i != null)
		{
			final GameItem[] gameItems = GameItem.convertItemsToGameItems(i, itemManager);
			player.setInventory(gameItems);
		}

		if (e != null)
		{
			final GameItem[] gameItems = GameItem.convertItemsToGameItems(e, itemManager);
			player.setEquipment(gameItems);
		}

		if (s != null)
		{
			s.forEach(change -> change.process(player));
		}

		if (m != null)
		{
			m.forEach(change -> change.process(player));
		}

		if (ap == null || ep == null)
		{
			return;
		}

		// Default all prayers to not available and not enabled
		player.getPrayers().getPrayerData().forEach((idx, p) -> {
			p.setAvailable(false);
			p.setEnabled(false);
		});

		for (final Prayer p : unpackActivePrayers())
		{
			player.getPrayers().getPrayerData().get(p).setAvailable(true);
		}

		for (final Prayer p : unpackEnabledPrayers())
		{
			player.getPrayers().getPrayerData().get(p).setEnabled(true);
		}
	}

	public boolean hasBreakingBannerChange()
	{
		return m != null
			&& m.stream()
				.anyMatch(e -> e.getT() == PartyMiscChange.PartyMisc.C || e.getT() == PartyMiscChange.PartyMisc.W);
	}

	public boolean hasStatChange()
	{
		return (s != null && s.size() > 0)
			|| (m != null && m.stream().anyMatch(e ->
				e.getT() == PartyMiscChange.PartyMisc.S
				|| e.getT() == PartyMiscChange.PartyMisc.R
				|| e.getT() == PartyMiscChange.PartyMisc.C
				|| e.getT() == PartyMiscChange.PartyMisc.T)
			);
	}

	public static <E extends Enum<E>> int pack(Collection<E> items) {
		int i = 0;
		for (E e : items) {
			assert e.ordinal() < 32;
			i |= (1 << e.ordinal());
		}

		return i;
	}

	private Collection<Prayer> unpack(int pack) {
		final List<Prayer> out = new ArrayList<>();
		for (Prayer p : Prayer.values()) {
			if ((pack & (1 << p.ordinal())) != 0) {
				out.add(p);
			}
		}

		return out;
	}

	public Collection<Prayer> unpackActivePrayers() {
		return unpack(ap);
	}

	public Collection<Prayer> unpackEnabledPrayers() {
		return unpack(ep);
	}
}

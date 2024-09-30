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

import lombok.AllArgsConstructor;
import lombok.Value;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.QuantityFormatter;

@Value
@AllArgsConstructor
public class GameItem
{
	int id;
	int qty;
	String name;
	boolean stackable;
	int price;

	public GameItem(final Item item, final ItemManager itemManager)
	{
		this(item.getId(), item.getQuantity(), itemManager);
	}

	public GameItem(final int id, final int qty, final ItemManager itemManager)
	{
		this.id = id;
		this.qty = qty;

		final ItemComposition c = itemManager.getItemComposition(id);

		this.name = c.getName();
		this.stackable = c.isStackable();
		this.price = itemManager.getItemPrice(c.getNote() != -1 ? c.getLinkedNoteId() : id);
	}

	public static GameItem[] convertItemsToGameItems(final int[] items, final ItemManager itemManager)
	{
		GameItem[] output = new GameItem[items.length / 2];
		for (int i = 0; i < items.length; i += 2)
		{
			if (items[i] == -1 || items[i + 1] <= 0)
			{
				output[i / 2] = null;
			}
			else
			{
				output[i / 2] = new GameItem(items[i], items[i + 1], itemManager);
			}
		}

		return output;
	}

	public static GameItem[] convertItemsToGameItems(final Item[] items, final ItemManager itemManager)
	{
		final GameItem[] output = new GameItem[items.length];
		for (int i = 0; i < items.length; i++)
		{
			final Item item = items[i];
			if (item == null || item.getId() == -1)
			{
				output[i] = null;
			}
			else
			{
				output[i] = new GameItem(item, itemManager);
			}
		}

		return output;
	}

	public String getDisplayName()
	{
		if (this.qty <= 1)
		{
			return this.name;
		}

		return this.name + " x " + QuantityFormatter.formatNumber(this.qty);
	}
}

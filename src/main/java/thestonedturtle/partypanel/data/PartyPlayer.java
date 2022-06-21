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

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.messages.PartyMemberMessage;

@Data
@EqualsAndHashCode(callSuper = true)
public class PartyPlayer extends PartyMemberMessage
{
	private transient PartyMember member;
	private String username;
	private Stats stats;
	private GameItem[] inventory;
	private GameItem[] equipment;
	private Prayers prayers;

	public PartyPlayer(final PartyMember member, final Client client, final ItemManager itemManager)
	{
		this.setMemberId(member.getMemberId());
		this.member = member;
		this.username = "";
		this.stats = null;
		this.inventory = new GameItem[28];
		this.equipment = new GameItem[EquipmentInventorySlot.AMMO.getSlotIdx() + 1];
		this.prayers = null;

		updatePlayerInfo(client, itemManager);
	}

	public void updatePlayerInfo(final Client client, final ItemManager itemManager)
	{
		// Player is logged in
		if (client.getLocalPlayer() != null)
		{
			this.username = client.getLocalPlayer().getName();
			this.stats = new Stats(client);

			final ItemContainer invi = client.getItemContainer(InventoryID.INVENTORY);
			if (invi != null)
			{
				this.inventory = GameItem.convertItemsToGameItems(invi.getItems(), itemManager);
			}

			final ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
			if (equip != null)
			{
				this.equipment = GameItem.convertItemsToGameItems(equip.getItems(), itemManager);
			}

			if (this.prayers == null)
			{
				prayers = new Prayers(client);
			}
		}
	}

	public int getSkillExperience(final Skill skill)
	{
		if (stats == null)
		{
			return 0;
		}

		return stats.getSkillEXPs().get(skill);
	}

	public int getSkillBoostedLevel(final Skill skill)
	{
		if (stats == null)
		{
			return 0;
		}

		return stats.getBoostedLevels().get(skill);
	}

	public int getSkillRealLevel(final Skill skill)
	{
		if (stats == null)
		{
			return 0;
		}

		return stats.getBaseLevels().get(skill);
	}

	public void setSkillExperience(final Skill skill, final int exp)
	{
		if (stats == null)
		{
			return;
		}

		stats.getSkillEXPs().put(skill, exp);
	}

	public void setSkillsBoostedLevel(final Skill skill, final int level)
	{
		if (stats == null)
		{
			return;
		}

		stats.getBoostedLevels().put(skill, level);
	}

	public void setSkillsRealLevel(final Skill skill, final int level)
	{
		if (stats == null)
		{
			return;
		}

		stats.getBaseLevels().put(skill, level);
	}
}

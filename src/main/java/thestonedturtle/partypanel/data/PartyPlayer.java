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
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.party.PartyMember;
import thestonedturtle.partypanel.PartyPanelPlugin;

import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode
public class PartyPlayer
{
	private transient PartyMember member;
	private Stats stats;
	private GameItem[] inventory;
	private GameItem[] equipment;
	private Prayers prayers;
	private int stamina;
	private int poison;
	private int disease;
	private int world;
	private GameItem[] runesInPouch;
	private Quiver quiver;
	// 0=Standard, 1=Ancient, 2=Lunar, 3=Arceuus
	private int spellbook;
	private transient MemberBannerSnapshot snapshot;

	public PartyPlayer()
	{
		this.member = null;
		this.stats = null;
		this.inventory = new GameItem[28];
		this.equipment = new GameItem[EquipmentInventorySlot.AMMO.getSlotIdx() + 1];
		this.prayers = null;
		this.stamina = 0;
		this.poison = 0;
		this.disease = 0;
		this.world = 0;
		this.runesInPouch = new GameItem[0];
		this.quiver = new Quiver(null, false, false);
		this.spellbook = -1;
	}

	public PartyPlayer(final PartyMember member, final ItemManager itemManager)
	{
		this();

		updateMemberInfo(member, itemManager);
	}

	public PartyPlayer(final PartyMember member, final Client client, final ItemManager itemManager, final ClientThread clientThread)
	{
		this();
		this.stamina = client.getVarbitValue(VarbitID.STAMINA_DURATION);
		this.poison = client.getVarpValue(VarPlayerID.POISON);
		this.disease = client.getVarpValue(VarPlayerID.DISEASE);
		this.world = client.getWorld();

		if (member != null)
		{
			updateMemberInfo(member, itemManager);
		}

		clientThread.invoke(() -> updatePlayerInfo(client, itemManager));
	}

	public void updatePlayerInfo(final Client client, final ItemManager itemManager)
	{
		assert client.isClientThread();

		// Player is logged in
		if (client.getLocalPlayer() != null)
		{
			this.stats = new Stats(client);

			final ItemContainer invi = client.getItemContainer(InventoryID.INV);
			if (invi != null)
			{
				final List<Item> runesInPouch = PartyPanelPlugin.getRunePouchContents(client);
				this.runesInPouch = GameItem.convertItemsToGameItems(runesInPouch.toArray(Item[]::new), itemManager);

				boolean hasQuiverInInventory = false;
				for (final Item item : invi.getItems())
				{
					if (PartyPanelPlugin.DIZANAS_QUIVER_IDS.contains(item.getId()))
					{
						hasQuiverInInventory = true;
						break;
					}
				}
				quiver.setInInventory(hasQuiverInInventory);
			}

			final ItemContainer equip = client.getItemContainer(InventoryID.WORN);
			if (equip != null)
			{
				final Item cape = equip.getItem(EquipmentInventorySlot.CAPE.getSlotIdx());
				boolean isWearingQuiver = cape != null && PartyPanelPlugin.DIZANAS_QUIVER_IDS.contains(cape.getId());
				quiver.setBeingWorn(isWearingQuiver);
			}

			if (this.prayers == null)
			{
				prayers = new Prayers(client);
			}
		}

		if (quiver.isSlotVisible())
		{
			final int quiverAmmoId = client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO);
			final int quiverAmmoCount = client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT);
			if (quiverAmmoId == -1 || quiverAmmoCount == 0)
			{
				quiver.setQuiverAmmo(null);
			}
			else
			{
				quiver.setQuiverAmmo(new GameItem(quiverAmmoId, quiverAmmoCount, itemManager));
			}
		}
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
		return getSkillRealLevel(skill, false);
	}

	public int getSkillRealLevel(final Skill skill, final boolean allowVirtualLevels)
	{
		if (stats == null)
		{
			return 0;
		}

		return Math.min(stats.getBaseLevels().get(skill), allowVirtualLevels ? 126 : 99);
	}

	public boolean updateMemberInfo(final PartyMember newData, final ItemManager itemManager)
	{
		final PartyMember oldMember = member;
		member = newData;

		if (member.getInv() != null)
		{
			inventory = GameItem.convertItemsToGameItems(member.getInv(), itemManager);
			for (final GameItem item : inventory)
			{
				if (item == null)
				{
					continue;
				}

				if (PartyPanelPlugin.DIZANAS_QUIVER_IDS.contains(item.getId()))
				{
					quiver.setInInventory(true);
					break;
				}
			}
		}


		if (member.getWorn() != null)
		{
			equipment = GameItem.convertItemsToGameItems(member.getWorn(), itemManager);
			for (final GameItem item : equipment)
			{
				if (item == null)
				{
					continue;
				}

				if (PartyPanelPlugin.DIZANAS_QUIVER_IDS.contains(item.getId()))
				{
					quiver.setBeingWorn(true);
				}
			}
		}

		if (prayers == null)
		{
			prayers = new Prayers();
		}

		if (member.getPrayers() != null)
		{
			prayers.updateFromPartyMember(member);
		}

		if (stats == null)
		{
			stats = new Stats();
		}
		stats.handleMemberUpdate(member);

		final MemberBannerSnapshot newSnapshot = MemberBannerSnapshot.of(member, stats.getCombatLevel());
		final boolean hasBreakingChange = oldMember == null || !newSnapshot.equals(snapshot);

		snapshot = newSnapshot;

		return hasBreakingChange;
	}

	public String getUsername()
	{
		final String username = member == null ? null : member.getDisplayName();

		if (Objects.equals(username, "<unknown>"))
		{
			return null;
		}

		return username;
	}
}

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
package thestonedturtle.partypanel.ui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import thestonedturtle.partypanel.data.GameItem;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.data.PrayerData;
import thestonedturtle.partypanel.ui.equipment.EquipmentPanelSlot;
import thestonedturtle.partypanel.ui.equipment.PlayerEquipmentPanel;
import thestonedturtle.partypanel.ui.prayer.PlayerPrayerPanel;
import thestonedturtle.partypanel.ui.prayer.PrayerSlot;
import thestonedturtle.partypanel.ui.skills.PlayerSkillsPanel;
import thestonedturtle.partypanel.ui.skills.SkillPanelSlot;

@Getter
public class PlayerPanel extends JPanel
{
	private static final Dimension IMAGE_SIZE = new Dimension(24, 24);

	private PartyPlayer player;
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;

	private final PlayerBanner banner;
	private final PlayerInventoryPanel inventoryPanel;
	private final PlayerEquipmentPanel equipmentPanel;
	private final PlayerSkillsPanel skillsPanel;
	private final PlayerPrayerPanel prayersPanel;

	public PlayerPanel(final PartyPlayer selectedPlayer, final SpriteManager spriteManager, final ItemManager itemManager)
	{
		this.player = selectedPlayer;
		this.spriteManager = spriteManager;
		this.itemManager = itemManager;

		this.banner = new PlayerBanner(selectedPlayer, spriteManager);
		this.inventoryPanel = new PlayerInventoryPanel(selectedPlayer.getInventory(), itemManager);
		this.equipmentPanel = new PlayerEquipmentPanel(selectedPlayer.getEquipment(), spriteManager, itemManager);
		this.skillsPanel = new PlayerSkillsPanel(selectedPlayer, spriteManager, itemManager);
		this.prayersPanel = new PlayerPrayerPanel(selectedPlayer.getPrayers(), spriteManager);

		final JPanel view = new JPanel();
		final MaterialTabGroup tabGroup = new MaterialTabGroup(view);
		tabGroup.setBorder(new EmptyBorder(10, 0, 10, 0));

		final MaterialTab inventory = new MaterialTab(createImageIcon(spriteManager.getSprite(SpriteID.TAB_INVENTORY, 0)), tabGroup, inventoryPanel);
		inventory.setToolTipText("Inventory");
		tabGroup.addTab(inventory);

		final MaterialTab equipment = new MaterialTab(createImageIcon(spriteManager.getSprite(SpriteID.TAB_EQUIPMENT, 0)), tabGroup, equipmentPanel);
		equipment.setToolTipText("Equipment");
		tabGroup.addTab(equipment);

		final MaterialTab prayers = new MaterialTab(createImageIcon(spriteManager.getSprite(SpriteID.TAB_PRAYER, 0)), tabGroup, prayersPanel);
		prayers.setToolTipText("Prayers");
		tabGroup.addTab(prayers);

		final MaterialTab skills = new MaterialTab(createImageIcon(spriteManager.getSprite(SpriteID.TAB_STATS, 0)), tabGroup, skillsPanel);
		skills.setToolTipText("Skills");
		tabGroup.addTab(skills);

		setLayout(new DynamicGridLayout(0, 1));
		add(banner);
		add(tabGroup);
		add(view);

		tabGroup.select(inventory);

		revalidate();
		repaint();
	}

	private ImageIcon createImageIcon(BufferedImage image)
	{
		return new ImageIcon(ImageUtil.resizeImage(image, IMAGE_SIZE.width, IMAGE_SIZE.height));
	}

	// TODO add smarter ways to update data
	public void changePlayer(final PartyPlayer newPlayer)
	{
		final boolean newUser = !newPlayer.getMemberId().equals(player.getMemberId());

		player = newPlayer;
		banner.setPlayer(player);
		inventoryPanel.updateInventory(player.getInventory());

		for (final EquipmentInventorySlot equipSlot : EquipmentInventorySlot.values())
		{
			GameItem item = null;
			if (player.getEquipment().length > equipSlot.getSlotIdx())
			{
				item = player.getEquipment()[equipSlot.getSlotIdx()];
			}

			final EquipmentPanelSlot slot = this.equipmentPanel.getPanelMap().get(equipSlot);
			if (item != null)
			{
				final AsyncBufferedImage img = itemManager.getImage(item.getId(), item.getQty(), item.isStackable());
				slot.setGameItem(item, img);

				// Ensure item is set when image loads
				final GameItem finalItem = item;
				img.onLoaded(() -> slot.setGameItem(finalItem, img));
			}
			else
			{
				slot.setGameItem(null, null);
			}
		}

		if (newUser)
		{
			banner.recreatePanel();
		}

		if (player.getStats() != null)
		{
			banner.refreshStats();
			for (final Skill s : Skill.values())
			{
				if (s.equals(Skill.OVERALL))
				{
					continue;
				}

				final SkillPanelSlot panel = skillsPanel.getPanelMap().get(s);
				panel.updateBoostedLevel(player.getStats().getBoostedLevels().get(s));
				panel.updateBaseLevel(player.getStats().getBaseLevels().get(s));
			}
			skillsPanel.getTotalLevelPanel().updateTotalLevel(player.getStats().getTotalLevel());
		}

		if (player.getPrayers() != null)
		{
			for (final Map.Entry<Prayer, PrayerSlot> entry : prayersPanel.getSlotMap().entrySet())
			{
				final PrayerData data = player.getPrayers().getPrayerData().get(entry.getKey());
				if (data != null)
				{
					entry.getValue().updatePrayerData(data);
				}
			}
		}
	}
}

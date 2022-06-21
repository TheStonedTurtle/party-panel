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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Experience;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.client.game.AlternateSprites;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import thestonedturtle.partypanel.PartyPanelConfig;
import thestonedturtle.partypanel.data.GameItem;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.data.PrayerData;
import thestonedturtle.partypanel.ui.equipment.EquipmentPanelSlot;
import thestonedturtle.partypanel.ui.equipment.PlayerEquipmentPanel;
import thestonedturtle.partypanel.ui.prayer.PlayerPrayerPanel;
import thestonedturtle.partypanel.ui.prayer.PrayerSlot;
import thestonedturtle.partypanel.ui.skills.PlayerSkillsPanel;

@Getter
public class PlayerPanel extends JPanel
{
	private static final Dimension IMAGE_SIZE = new Dimension(24, 24);
	private static final Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private static final Color BACKGROUND_HOVER_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private static final BufferedImage EXPAND_ICON = ImageUtil.loadImageResource(PlayerPanel.class, "expand.png");

	private static final int VENOM_THRESHOLD = 1000000;
	private static final BufferedImage HEART_DISEASE;
	private static final BufferedImage HEART_POISON;
	private static final BufferedImage HEART_VENOM;

	static
	{
		HEART_DISEASE = ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.DISEASE_HEART);
		HEART_POISON = ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.POISON_HEART);
		HEART_VENOM = ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.VENOM_HEART);
	}

	private PartyPlayer player;
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;

	private final PlayerBanner banner;
	private final PlayerInventoryPanel inventoryPanel;
	private final PlayerEquipmentPanel equipmentPanel;
	private final PlayerSkillsPanel skillsPanel;
	private final PlayerPrayerPanel prayersPanel;

	private final PartyPanelConfig config;

	@Setter
	private boolean showInfo;

	public PlayerPanel(final PartyPlayer selectedPlayer, final PartyPanelConfig config,
					   final SpriteManager spriteManager, final ItemManager itemManager)
	{
		this.player = selectedPlayer;
		this.config = config;
		this.spriteManager = spriteManager;
		this.itemManager = itemManager;
		this.showInfo = config.autoExpandMembers();
		this.banner = new PlayerBanner(selectedPlayer, showInfo, config.displayPlayerWorlds(), spriteManager);
		this.inventoryPanel = new PlayerInventoryPanel(selectedPlayer.getInventory(), itemManager);
		this.equipmentPanel = new PlayerEquipmentPanel(selectedPlayer.getEquipment(), spriteManager, itemManager);
		this.skillsPanel = new PlayerSkillsPanel(selectedPlayer, config.displayVirtualLevels(), spriteManager);
		this.prayersPanel = new PlayerPrayerPanel(selectedPlayer, spriteManager);

		// Non-optimal way to attach a mouse listener to
		// the entire panel, but easy to implement
		JPanel statsPanel = this.banner.getStatsPanel();
		JLabel expandIcon = this.banner.getExpandIcon();
		Component[] list = new Component[statsPanel.getComponentCount()+1];
		System.arraycopy(statsPanel.getComponents(), 0, list, 0, list.length-1);
		list[list.length-1] = banner;

		for (Component comp : list)
		{
			if (comp instanceof JPanel)
			{
				comp.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent e)
					{
						if (e.getButton() == MouseEvent.BUTTON1)
						{
							ImageIcon retrieve = (ImageIcon)expandIcon.getIcon();
							BufferedImage buffered = (BufferedImage)retrieve.getImage();

							showInfo = !showInfo;
							expandIcon.setIcon(new ImageIcon(ImageUtil.rotateImage(buffered, Math.PI)));
							updatePanel();
						}
					}

					@Override
					public void mouseEntered(MouseEvent e)
					{
						banner.setBackground(BACKGROUND_HOVER_COLOR);
						statsPanel.setBackground(BACKGROUND_HOVER_COLOR);
					}

					@Override
					public void mouseExited(MouseEvent e)
					{
						banner.setBackground(BACKGROUND_COLOR);
						statsPanel.setBackground(BACKGROUND_COLOR);
					}
				});
			}
		}

		updatePanel();

		revalidate();
		repaint();
	}

	private void addTab(final MaterialTabGroup tabGroup, final int spriteID, final JPanel panel, final String tooltip)
	{
		spriteManager.getSpriteAsync(spriteID, 0, img ->
			SwingUtilities.invokeLater(() ->
			{
				final MaterialTab tab = new MaterialTab(createImageIcon(img), tabGroup, panel);
				tab.setToolTipText(tooltip);
				tabGroup.addTab(tab);
				tabGroup.revalidate();
				tabGroup.repaint();

				if (spriteID == SpriteID.TAB_INVENTORY)
				{
					tabGroup.select(tab);
				}
			}));
	}

	private ImageIcon createImageIcon(BufferedImage image)
	{
		return new ImageIcon(ImageUtil.resizeImage(image, IMAGE_SIZE.width, IMAGE_SIZE.height));
	}

	// TODO add smarter ways to update data
	public void updatePlayerData(PartyPlayer newPlayer, boolean hasBreakingBannerChange)
	{
		player = newPlayer;
		banner.setPlayer(player);

		if (hasBreakingBannerChange)
		{
			banner.recreatePanel();
		}

		inventoryPanel.updateInventory(player.getInventory());

		for (final EquipmentInventorySlot equipSlot : EquipmentInventorySlot.values())
		{
			GameItem item = null;
			if (player.getEquipment().length > equipSlot.getSlotIdx())
			{
				item = player.getEquipment()[equipSlot.getSlotIdx()];
			}

			final EquipmentPanelSlot slot = this.equipmentPanel.getPanelMap().get(equipSlot);
			if (item != null && slot != null)
			{
				final AsyncBufferedImage img = itemManager.getImage(item.getId(), item.getQty(), item.isStackable());
				slot.setGameItem(item, img);

				// Ensure item is set when image loads
				final GameItem finalItem = item;
				img.onLoaded(() -> slot.setGameItem(finalItem, img));
			}
			else if (slot != null)
			{
				slot.setGameItem(null, null);
			}
		}

		if (player.getStats() != null)
		{
			banner.refreshStats();
			int totalLevel = 0;
			long totalXp = 0;
			for (final Skill s : Skill.values())
			{
				if (s.equals(Skill.OVERALL))
				{
					continue;
				}

				int xp = player.getSkillExperience(s);
				totalLevel += Experience.getLevelForXp(xp);
				totalXp += xp;

				updateSkill(s);
			}
			skillsPanel.getTotalLevelPanel().updateTotalLevel(config.displayVirtualLevels() ? totalLevel : player.getStats().getTotalLevel(), totalXp);
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

			prayersPanel.updatePrayerRemaining(player.getSkillBoostedLevel(Skill.PRAYER), player.getSkillRealLevel(Skill.PRAYER));
		}

		BufferedImage heart = null;
		if (player.getPoison() >= VENOM_THRESHOLD)
		{
			heart = HEART_VENOM;
		}
		else if (player.getPoison() > 0)
		{
			heart = HEART_POISON;
		}
		else if (player.getDisease() > 0)
		{
			heart = HEART_DISEASE;
		}
		banner.setCurrentHeart(heart, spriteManager);

		banner.setUsingStamIcon(player.getStamina() > 0, spriteManager);
	}

	public void updatePanel()
	{
		this.removeAll();
		if (showInfo)
		{
			this.setBorder(new CompoundBorder(
					new MatteBorder(2, 2, 2, 2, new Color(87, 80, 64)),
					new EmptyBorder(0, 0, 5,  0)
			));
		}
		else
		{
			this.setBorder(new MatteBorder(2, 2, 2, 2, new Color(87, 80, 64)));
		}

		final JPanel view = new JPanel();
		view.setBorder(new EmptyBorder(5, 5, 0,  5));
		final MaterialTabGroup tabGroup = new MaterialTabGroup(view);
		tabGroup.setBorder(new EmptyBorder(10, 0, 4, 0));

		addTab(tabGroup, SpriteID.TAB_INVENTORY, inventoryPanel, "Inventory");
		addTab(tabGroup, SpriteID.TAB_EQUIPMENT, equipmentPanel, "Equipment");
		addTab(tabGroup, SpriteID.TAB_PRAYER, prayersPanel, "Prayers");
		addTab(tabGroup, SpriteID.TAB_STATS, skillsPanel, "Skills");

		setLayout(new DynamicGridLayout(0, 1));

		add(banner);
		if (this.showInfo)
		{
			add(tabGroup);
			add(view);
		}

		revalidate();
		repaint();
	}

	public void updateSkill(Skill s)
	{
		skillsPanel.updateSkill(player, s, config.displayVirtualLevels());
	}

	public void updateDisplayVirtualLevels()
	{
		int totalLevel = 0;
		long totalXp = 0;
		for (final Skill s : Skill.values())
		{
			if (s.equals(Skill.OVERALL))
			{
				continue;
			}

			int xp = player.getSkillExperience(s);
			totalLevel += Experience.getLevelForXp(xp);
			totalXp += xp;

			updateSkill(s);
		}

		skillsPanel.getTotalLevelPanel().updateTotalLevel(config.displayVirtualLevels() ? totalLevel : player.getStats().getTotalLevel(), totalXp);
	}

	public void updateDisplayPlayerWorlds()
	{
		banner.updateWorld(player.getWorld(), config.displayPlayerWorlds());
	}
}

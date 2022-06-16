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
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import thestonedturtle.partypanel.data.PartyPlayer;

public class PlayerBanner extends JPanel
{
	private static final Dimension STAT_ICON_SIZE = new Dimension(18, 18);
	private static final Dimension ICON_SIZE = new Dimension(Constants.ITEM_SPRITE_WIDTH-6, Constants.ITEM_SPRITE_HEIGHT-4);
	private static final BufferedImage EXPAND_ICON = ImageUtil.loadImageResource(PlayerPanel.class, "expand.png");
	private static final String SPECIAL_ATTACK_NAME = "Special Attack";
	private static final String RUN_ENERGY_NAME = "Run Energy";

	@Getter
	private final JPanel statsPanel = new JPanel();
	private final JLabel iconLabel = new JLabel();
	private final Map<String, JLabel> statLabels = new HashMap<>();
	@Getter
	private final JLabel expandIcon = new JLabel();

	private ImageIcon expandIconUp;
	private ImageIcon expandIconDown;

	@Setter
	@Getter
	private PartyPlayer player;
	private boolean checkIcon;

	public PlayerBanner(final PartyPlayer player, boolean expanded, SpriteManager spriteManager)
	{
		super();
		this.player = player;

		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 14, 68));
		this.setBorder(new EmptyBorder(5, 5, 0,  5));

		statsPanel.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 25));
		statsPanel.setLayout(new GridLayout(0, 4));
		statsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		statsPanel.setOpaque(true);

		expandIconDown = new ImageIcon(EXPAND_ICON);
		expandIconUp = new ImageIcon(ImageUtil.rotateImage(EXPAND_ICON, Math.PI));
		if (expanded)
		{
			expandIcon.setIcon(expandIconUp);
		}
		else
		{
			expandIcon.setIcon(expandIconDown);
		}

		statsPanel.add(createIconPanel(spriteManager, SpriteID.SKILL_HITPOINTS, Skill.HITPOINTS.getName(), String.valueOf(player.getSkillBoostedLevel(Skill.HITPOINTS))));
		statsPanel.add(createIconPanel(spriteManager, SpriteID.SKILL_PRAYER, Skill.PRAYER.getName(), String.valueOf(player.getSkillBoostedLevel(Skill.PRAYER))));
		statsPanel.add(createIconPanel(spriteManager, SpriteID.MULTI_COMBAT_ZONE_CROSSED_SWORDS, SPECIAL_ATTACK_NAME, player.getStats() == null ? "0" : String.valueOf(player.getStats().getSpecialPercent())));
		statsPanel.add(createIconPanel(spriteManager, SpriteID.MINIMAP_ORB_RUN_ICON, RUN_ENERGY_NAME, player.getStats() == null ? "0" : String.valueOf(player.getStats().getRunEnergy())));

		recreatePanel();
	}

	// True = arrow up; False = arrow down
	public void setExpandIcon(boolean direction)
	{
		if (direction)
		{
			expandIcon.setIcon(expandIconUp);
		}
		else
		{
			expandIcon.setIcon(expandIconDown);
		}
	}

	public void recreatePanel()
	{
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 1.0;
		c.ipady = 4;

		// Add avatar label regardless of if one exists just to have UI matching
		iconLabel.setBorder(new MatteBorder(1, 1, 1, 1, ColorScheme.DARKER_GRAY_HOVER_COLOR));
		iconLabel.setPreferredSize(ICON_SIZE);
		iconLabel.setMinimumSize(ICON_SIZE);
		iconLabel.setOpaque(false);

		checkIcon = player.getMember().getAvatar() == null;
		if (!checkIcon)
		{
			addIcon();
		}

		add(iconLabel, c);
		c.gridx++;

		final JPanel nameContainer = new JPanel(new GridLayout(1, 1));
		nameContainer.setBorder(new EmptyBorder(0, 5, 0, 0));
		nameContainer.setOpaque(false);

		final JLabel usernameLabel = new JLabel();
		usernameLabel.setLayout(new OverlayLayout(usernameLabel));
		usernameLabel.setHorizontalTextPosition(JLabel.LEFT);
		if (player.getUsername() == null)
		{
			usernameLabel.setText("Not logged in");
		}
		else
		{
			final String levelText = player.getStats() == null ? "" : " (level-" + player.getStats().getCombatLevel() + ")";
			usernameLabel.setText(player.getUsername() + levelText);
		}

		expandIcon.setAlignmentX(Component.RIGHT_ALIGNMENT);
		usernameLabel.add(expandIcon, BorderLayout.EAST);
		nameContainer.add(usernameLabel);

		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(nameContainer, c);

		refreshStats();
		c.gridy++;
		c.weightx = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		add(statsPanel, c);

		revalidate();
		repaint();
	}

	private void addIcon()
	{
		final BufferedImage resized = ImageUtil.resizeImage(player.getMember().getAvatar(), Constants.ITEM_SPRITE_WIDTH-8, Constants.ITEM_SPRITE_HEIGHT-4);
		iconLabel.setIcon(new ImageIcon(resized));
	}

	public void refreshStats()
	{
		if (checkIcon)
		{
			if (player.getMember().getAvatar() != null)
			{
				addIcon();
				checkIcon = false;
			}
		}

		statLabels.getOrDefault(Skill.HITPOINTS.getName(), new JLabel()).setText(String.valueOf(player.getSkillBoostedLevel(Skill.HITPOINTS)));
		statLabels.getOrDefault(Skill.PRAYER.getName(), new JLabel()).setText(String.valueOf(player.getSkillBoostedLevel(Skill.PRAYER)));
		statLabels.getOrDefault(SPECIAL_ATTACK_NAME, new JLabel()).setText(player.getStats() == null ? "0" : String.valueOf(player.getStats().getSpecialPercent()));
		statLabels.getOrDefault(RUN_ENERGY_NAME, new JLabel()).setText(player.getStats() == null ? "0" : String.valueOf(player.getStats().getRunEnergy()));

		statsPanel.revalidate();
		statsPanel.repaint();
	}

	private JPanel createIconPanel(final SpriteManager spriteManager, final int spriteID, final String name,
		final String value)
	{
		final JLabel iconLabel = new JLabel();
		iconLabel.setPreferredSize(STAT_ICON_SIZE);
		spriteManager.getSpriteAsync(spriteID, 0, img ->
			SwingUtilities.invokeLater(() ->
			{
				if (spriteID == SpriteID.SKILL_PRAYER)
				{
					iconLabel.setIcon(new ImageIcon(ImageUtil.resizeImage(img, STAT_ICON_SIZE.width+2, STAT_ICON_SIZE.height+2)));
				}
				else
				{
					iconLabel.setIcon(new ImageIcon(ImageUtil.resizeImage(img, STAT_ICON_SIZE.width, STAT_ICON_SIZE.height)));
				}
				iconLabel.revalidate();
				iconLabel.repaint();
			}));

		final JLabel textLabel = new JLabel(value);
		textLabel.setHorizontalAlignment(JLabel.CENTER);
		textLabel.setHorizontalTextPosition(JLabel.CENTER);
		statLabels.put(name, textLabel);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(iconLabel, BorderLayout.WEST);
		panel.add(textLabel, BorderLayout.CENTER);
		panel.setOpaque(false);
		panel.setToolTipText(name);

		return panel;
	}
}

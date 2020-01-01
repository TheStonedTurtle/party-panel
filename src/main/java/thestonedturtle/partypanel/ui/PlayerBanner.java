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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import thestonedturtle.partypanel.data.PartyPlayer;

public class PlayerBanner extends JPanel
{
	private static final Dimension STAT_ICON_SIZE = new Dimension(18, 18);
	private static final Dimension ICON_SIZE = new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT);

	private final BufferedImage hitpointsIcon;
	private final BufferedImage prayIcon;
	private final BufferedImage specialAttackIcon;
	private final JPanel statsPanel = new JPanel();

	@Setter
	@Getter
	private PartyPlayer player;

	public PlayerBanner(final PartyPlayer player, SpriteManager spriteManager)
	{
		super();
		this.player = player;

		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 75));
		this.setBorder(new CompoundBorder(
			new MatteBorder(2, 2, 2, 2, ColorScheme.DARK_GRAY_HOVER_COLOR),
			new EmptyBorder(5, 5, 5,  5)
		));

		statsPanel.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 30));
		statsPanel.setLayout(new DynamicGridLayout(1, 3));
		statsPanel.setOpaque(false);

		hitpointsIcon = spriteManager.getSprite(SpriteID.SKILL_HITPOINTS, 0);
		prayIcon = spriteManager.getSprite(SpriteID.SKILL_PRAYER, 0);
		specialAttackIcon = spriteManager.getSprite(SpriteID.MULTI_COMBAT_ZONE_CROSSED_SWORDS, 0);

		recreatePanel();
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
		final JLabel iconLabel = new JLabel();
		iconLabel.setBorder(new MatteBorder(1, 1, 1, 1, ColorScheme.DARKER_GRAY_HOVER_COLOR));
		iconLabel.setPreferredSize(ICON_SIZE);
		iconLabel.setMinimumSize(ICON_SIZE);
		iconLabel.setOpaque(false);

		if (player.getMember().getAvatar() != null)
		{
			final BufferedImage resized = ImageUtil.resizeImage(player.getMember().getAvatar(), Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT);
			iconLabel.setIcon(new ImageIcon(resized));
		}

		add(iconLabel, c);
		c.gridx++;

		final JPanel nameContainer = new JPanel(new GridLayout(2, 1));
		nameContainer.setBorder(new EmptyBorder(0, 10, 0, 0));
		nameContainer.setOpaque(false);

		final JLabel usernameLabel = new JLabel();
		usernameLabel.setHorizontalTextPosition(JLabel.LEFT);
		if (player.getUsername() == null)
		{
			usernameLabel.setText("Not logged in");
		}
		else
		{
			final String levelText = player.getStats() == null ? "" : " (Lvl - " + player.getStats().getCombatLevel() + ")";
			usernameLabel.setText(player.getUsername() + levelText);
		}

		final JLabel discordNameLabel = new JLabel(player.getMember().getName());
		discordNameLabel.setHorizontalTextPosition(JLabel.LEFT);

		nameContainer.add(usernameLabel);
		nameContainer.add(discordNameLabel);

		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(nameContainer, c);

		recreateStatsPanel();
		c.gridy++;
		c.weightx = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		add(statsPanel, c);
	}

	public void recreateStatsPanel()
	{
		statsPanel.removeAll();

		statsPanel.add(createIconTextLabel(hitpointsIcon, player.getSkillBoostedLevel(Skill.HITPOINTS)));
		statsPanel.add(createIconTextLabel(prayIcon, player.getSkillBoostedLevel(Skill.PRAYER)));
		statsPanel.add(createIconTextLabel(specialAttackIcon, player.getStats() == null ? 0 : player.getStats().getSpecialPercent()));
	}

	private JPanel createIconTextLabel(final BufferedImage icon, final int value)
	{
		final JLabel iconLabel = new JLabel();
		iconLabel.setPreferredSize(STAT_ICON_SIZE);
		iconLabel.setIcon(new ImageIcon(ImageUtil.resizeImage(icon, STAT_ICON_SIZE.width, STAT_ICON_SIZE.height)));

		final JLabel textLabel = new JLabel(String.valueOf(value));

		final JPanel panel = new JPanel();
		panel.add(iconLabel);
		panel.add(textLabel);
		panel.setOpaque(false);

		return panel;
	}
}

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
import java.util.HashMap;
import java.util.Map;
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

	private final JPanel statsPanel = new JPanel();
	private final JLabel iconLabel = new JLabel();
	private final Map<String, JLabel> statLabels = new HashMap<>();

	@Setter
	@Getter
	private PartyPlayer player;
	private boolean checkIcon;

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

		final BufferedImage hpIcon = spriteManager.getSprite(SpriteID.SKILL_HITPOINTS, 0);
		final JPanel hp = createIconTextLabel(Skill.HITPOINTS.getName(), hpIcon, String.valueOf(player.getSkillBoostedLevel(Skill.HITPOINTS)));
		hp.setToolTipText("Hitpoints");
		statsPanel.add(hp);

		final BufferedImage prayIcon = spriteManager.getSprite(SpriteID.SKILL_PRAYER, 0);
		final JPanel pray = createIconTextLabel(Skill.PRAYER.getName(), prayIcon, String.valueOf(player.getSkillBoostedLevel(Skill.PRAYER)));
		pray.setToolTipText("Prayer");
		statsPanel.add(pray);

		final BufferedImage specialAttackIcon = spriteManager.getSprite(SpriteID.MULTI_COMBAT_ZONE_CROSSED_SWORDS, 0);
		final JPanel special = createIconTextLabel("SPECIAL", specialAttackIcon, player.getStats() == null ? "0%" : String.valueOf(player.getStats().getSpecialPercent()) + "%");
		special.setToolTipText("Special Attack");
		statsPanel.add(special);

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

		refreshStats();
		c.gridy++;
		c.weightx = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		add(statsPanel, c);
	}

	private void addIcon()
	{
		final BufferedImage resized = ImageUtil.resizeImage(player.getMember().getAvatar(), Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT);
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

		statLabels.get(Skill.HITPOINTS.getName()).setText(String.valueOf(player.getSkillBoostedLevel(Skill.HITPOINTS)));
		statLabels.get(Skill.PRAYER.getName()).setText(String.valueOf(player.getSkillBoostedLevel(Skill.PRAYER)));
		statLabels.get("SPECIAL").setText(player.getStats() == null ? "0%" : String.valueOf(player.getStats().getSpecialPercent()) + "%");

		statsPanel.revalidate();
		statsPanel.repaint();
	}

	private JPanel createIconTextLabel(final String name, final BufferedImage icon, final String value)
	{
		final JLabel iconLabel = new JLabel();
		iconLabel.setPreferredSize(STAT_ICON_SIZE);
		iconLabel.setIcon(new ImageIcon(ImageUtil.resizeImage(icon, STAT_ICON_SIZE.width, STAT_ICON_SIZE.height)));

		final JLabel textLabel = new JLabel(value);
		statLabels.put(name, textLabel);

		final JPanel panel = new JPanel();
		panel.add(iconLabel);
		panel.add(textLabel);
		panel.setOpaque(false);

		return panel;
	}
}

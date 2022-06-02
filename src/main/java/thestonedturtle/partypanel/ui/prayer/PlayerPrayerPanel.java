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
package thestonedturtle.partypanel.ui.prayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.data.PrayerData;
import thestonedturtle.partypanel.data.Prayers;

public class PlayerPrayerPanel extends JPanel
{
	private static final Dimension PANEL_SIZE = new Dimension(PluginPanel.PANEL_WIDTH - 14, 296);
	private static final Color BACKGROUND = new Color(62, 53, 41);

	private static final int MAX_COLUMNS = 5;

	@Getter
	private final Map<Prayer, PrayerSlot> slotMap = new HashMap<>();
	private final JLabel remainingLabel = new JLabel();

	public PlayerPrayerPanel(final PartyPlayer player, final SpriteManager spriteManager)
	{
		super();

		setLayout(new BorderLayout());

		setBackground(BACKGROUND);
		setPreferredSize(PANEL_SIZE);

		add(createPrayerContainer(player.getPrayers(), spriteManager), BorderLayout.NORTH);
		add(createPrayerRemainingPanel(spriteManager), BorderLayout.SOUTH);
		updatePrayerRemaining(player.getSkillBoostedLevel(Skill.PRAYER), player.getSkillRealLevel(Skill.PRAYER));
	}

	private JPanel createPrayerContainer(final Prayers prayer, final SpriteManager spriteManager)
	{
		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setPreferredSize(new Dimension(PANEL_SIZE.width, PANEL_SIZE.height - 25));
		panel.setOpaque(false);

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = .5;
		c.weightx = .5;
		c.ipadx = 2;
		c.ipady = 2;
		c.anchor = GridBagConstraints.CENTER;

		// Creates and adds the Prayers to the panel
		for (final PrayerSprites p : PrayerSprites.values())
		{
			final PrayerSlot slot = new PrayerSlot(p, spriteManager);

			if (prayer != null)
			{
				final PrayerData data = prayer.getPrayerData().get(p.getPrayer());
				if (data != null)
				{
					slot.updatePrayerData(data);
				}
			}

			slotMap.put(p.getPrayer(), slot);

			if (c.gridx == MAX_COLUMNS)
			{
				c.gridx = 0;
				c.gridy++;
			}
			panel.add(slot, c);
			c.gridx++;
		}

		return panel;
	}

	private JPanel createPrayerRemainingPanel(final SpriteManager spriteManager)
	{
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 4;
		c.gridwidth = 1;

		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		panel.setPreferredSize(new Dimension(PANEL_SIZE.width, 25));

		final JLabel iconLabel = new JLabel();
		iconLabel.setOpaque(false);
		spriteManager.addSpriteTo(iconLabel, SpriteID.UNKNOWN_PRAYER_ICON, 0);
		iconLabel.setHorizontalAlignment(JLabel.RIGHT);

		remainingLabel.setFont(FontManager.getRunescapeSmallFont());
		remainingLabel.setForeground(ColorScheme.BRAND_ORANGE);
		remainingLabel.setVerticalAlignment(JLabel.CENTER);
		remainingLabel.setHorizontalTextPosition(JLabel.LEFT);
		remainingLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
		remainingLabel.setOpaque(false);

		panel.add(iconLabel, c);
		c.gridx++;
		panel.add(remainingLabel, c);

		return panel;
	}

	public void updatePrayerRemaining(final int remaining, final int maximum)
	{
		remainingLabel.setText(remaining + "/" + maximum);
	}
}

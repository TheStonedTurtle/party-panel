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
package thestonedturtle.partypanel.ui.skills;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import thestonedturtle.partypanel.ImgUtil;

public class TotalPanelSlot extends JPanel
{
	private final JLabel levelLabel = new JLabel();
	private final BufferedImage background;

	@Override
	protected  void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(background, 0, 0, null);
	}

	TotalPanelSlot(final int totalLevel, final SpriteManager spriteManager)
	{
		super();
		setOpaque(false);

		background = ImgUtil.combineImages(
			SkillPanelSlot.resize(spriteManager.getSprite(SpriteID.STATS_TILE_HALF_LEFT_BLACK, 0)),
			SkillPanelSlot.resize(spriteManager.getSprite(SpriteID.STATS_TILE_HALF_RIGHT_BLACK, 0))
		);

		setPreferredSize(SkillPanelSlot.PANEL_FULL_SIZE);
		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;

		final JLabel textLabel = new JLabel("Total level:");
		textLabel.setFont(FontManager.getRunescapeSmallFont());
		textLabel.setForeground(Color.YELLOW);
		add(textLabel, c);

		if (totalLevel > 0)
		{
			levelLabel.setText(String.valueOf(totalLevel));
		}
		levelLabel.setFont(FontManager.getRunescapeSmallFont());
		levelLabel.setForeground(Color.YELLOW);
		c.gridy++;
		add(levelLabel, c);
	}

	public void updateTotalLevel(final int level)
	{
		levelLabel.setText(String.valueOf(level));
		levelLabel.repaint();
	}
}

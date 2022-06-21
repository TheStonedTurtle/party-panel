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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import thestonedturtle.partypanel.ImgUtil;

public class SkillPanelSlot extends JPanel
{
	private static final Dimension PANEL_HALF_SIZE = new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT + 4);
	static final Dimension PANEL_FULL_SIZE = new Dimension(PANEL_HALF_SIZE.width * 2, PANEL_HALF_SIZE.height);

	private final JLabel boostedLabel = new JLabel();
	private final JLabel baseLabel = new JLabel();
	private BufferedImage background;
	private BufferedImage skillHalf;
	private BufferedImage statHalf;

	@Getter
	@Setter
	private int skillEXP;

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (background == null)
		{
			return;
		}

		g.drawImage(background, 0, 0, null);
	}

	private void updateBackgroundImage()
	{
		if (skillHalf != null && statHalf != null)
		{
			background = ImgUtil.combineImages(skillHalf, statHalf);
			this.repaint();
		}
	}

	SkillPanelSlot(final int boostedLevel, final int baseLevel, final int exp)
	{
		super();
		setOpaque(false);

		setPreferredSize(PANEL_FULL_SIZE);
		setLayout(new BorderLayout());

		final JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridBagLayout());
		textPanel.setPreferredSize(PANEL_HALF_SIZE);
		textPanel.setOpaque(false);

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = .5;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;

		boostedLabel.setText(String.valueOf(boostedLevel));
		boostedLabel.setVerticalAlignment(JLabel.CENTER);
		boostedLabel.setHorizontalAlignment(JLabel.LEFT);
		boostedLabel.setFont(FontManager.getRunescapeSmallFont());
		boostedLabel.setForeground(Color.YELLOW);
		boostedLabel.setBorder(new EmptyBorder(6, 3, 0, 0));
		c.anchor = GridBagConstraints.NORTHWEST;
		textPanel.add(boostedLabel, c);

		baseLabel.setVerticalAlignment(JLabel.CENTER);
		baseLabel.setHorizontalAlignment(JLabel.RIGHT);
		baseLabel.setFont(FontManager.getRunescapeSmallFont());
		baseLabel.setForeground(Color.YELLOW);
		updateBaseLevel(baseLevel); // Call so the correct right border is used

		skillEXP = exp;

		c.anchor = GridBagConstraints.SOUTHEAST;
		c.gridy++;
		textPanel.add(baseLabel, c);

		add(textPanel, BorderLayout.EAST);
	}

	void initImages(final BufferedImage skillIcon, final SpriteManager spriteManager)
	{
		spriteManager.getSpriteAsync(SpriteID.STATS_TILE_HALF_LEFT, 0, img ->
		{
			skillHalf = ImgUtil.overlapImages(skillIcon, SkillPanelSlot.resize(img), false);
			updateBackgroundImage();
		});
		spriteManager.getSpriteAsync(SpriteID.STATS_TILE_HALF_RIGHT_WITH_SLASH, 0, img ->
		{
			statHalf = SkillPanelSlot.resize(img);
			updateBackgroundImage();
		});
	}

	static BufferedImage resize(final BufferedImage img)
	{
		return ImageUtil.resizeImage(img, PANEL_HALF_SIZE.width, PANEL_HALF_SIZE.height);
	}

	public void updateBaseLevel(final int baseLevel)
	{
		// Adjust offset depending on amount of characters to make the UI look nicer
		int rightPadding = 9;
		if (baseLevel > 99)
		{
			rightPadding = 2;
		}
		else if (baseLevel > 9)
		{
			rightPadding = 6;
		}

		baseLabel.setText(String.valueOf(baseLevel));
		baseLabel.setBorder(new EmptyBorder(0, 0, 4, rightPadding));
		baseLabel.repaint();
	}

	public void updateBoostedLevel(final int boostedLevel)
	{
		boostedLabel.setText(String.valueOf(boostedLevel));
		boostedLabel.repaint();
	}
}

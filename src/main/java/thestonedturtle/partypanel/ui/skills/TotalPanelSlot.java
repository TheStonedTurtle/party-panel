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

import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import thestonedturtle.partypanel.ImgUtil;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

public class TotalPanelSlot extends JPanel
{
	private static final int SLOT_HEIGHT = 22;
	private static final int SLOT_WIDTH = PlayerSkillsPanel.PANEL_SIZE.width + 7;
	private static final int _1_5TH_SLOT_WIDTH = SLOT_WIDTH / 5;

	private final JLabel levelLabel = new JLabel();
	private BufferedImage background;
	private BufferedImage backgroundLeft;
	private BufferedImage backgroundRight;
	private BufferedImage backgroundCenter;

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
		if (backgroundLeft != null && backgroundRight != null && backgroundCenter != null)
		{
			final BufferedImage temp = ImgUtil.combineImages(backgroundLeft, backgroundCenter);
			background = ImgUtil.combineImages(temp, backgroundRight);
			this.repaint();
		}
	}

	TotalPanelSlot(final int totalLevel, final SpriteManager spriteManager)
	{
		super();
		setOpaque(false);

		spriteManager.getSpriteAsync(SpriteID.Miscgraphics2._4, 0, img ->
		{
			backgroundLeft = resize_edge(img);
			updateBackgroundImage();
		});
		spriteManager.getSpriteAsync(SpriteID.Miscgraphics2._5, 0, img ->
		{
			backgroundRight = resize_edge(img);
			updateBackgroundImage();
		});
		spriteManager.getSpriteAsync(SpriteID.Miscgraphics2._6, 0, img ->
		{
			backgroundCenter = resize_middle(img);
			updateBackgroundImage();
		});

		setPreferredSize(new Dimension(PlayerSkillsPanel.PANEL_SIZE.width, SLOT_HEIGHT));
		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(3, 0, 0, 0); // shift text down a bit to look centered

		final JLabel textLabel = new JLabel("Total level:");
		textLabel.setFont(FontManager.getRunescapeSmallFont());
		textLabel.setForeground(Color.YELLOW);
		textLabel.setAlignmentY(BOTTOM_ALIGNMENT);
		add(textLabel, c);

		if (totalLevel > 0)
		{
			levelLabel.setText(String.valueOf(totalLevel));
		}
		levelLabel.setFont(FontManager.getRunescapeSmallFont());
		levelLabel.setForeground(Color.YELLOW);
		levelLabel.setAlignmentY(BOTTOM_ALIGNMENT);
		c.gridx++;
		add(levelLabel, c);
	}

	private BufferedImage resize_edge(final BufferedImage img)
	{
		return ImageUtil.resizeImage(img, _1_5TH_SLOT_WIDTH, SLOT_HEIGHT);
	}

	private BufferedImage resize_middle(final BufferedImage img)
	{
		return ImageUtil.resizeImage(img, _1_5TH_SLOT_WIDTH * 3, SLOT_HEIGHT);
	}

	public void updateTotalLevel(final int level)
	{
		levelLabel.setText(String.valueOf(level));
		levelLabel.repaint();
	}
}

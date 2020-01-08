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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import net.runelite.api.Constants;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.Text;
import thestonedturtle.partypanel.ImgUtil;
import thestonedturtle.partypanel.data.PrayerData;

public class PrayerSlot extends JLabel
{
	private static final Dimension SIZE = new Dimension(Constants.ITEM_SPRITE_WIDTH, 45);

	private final BufferedImage unavailableImage;
	private final BufferedImage availableImage;
	private final BufferedImage activatedImage;

	private PrayerData data;

	public PrayerSlot(final PrayerSprites sprites, final SpriteManager spriteManager)
	{
		this.unavailableImage = spriteManager.getSprite(sprites.getUnavailable(), 0);
		this.availableImage = spriteManager.getSprite(sprites.getAvailable(), 0);
		final BufferedImage activated = spriteManager.getSprite(SpriteID.ACTIVATED_PRAYER_BACKGROUND, 0);
		this.activatedImage = ImgUtil.overlapImages(availableImage, activated);

		setToolTipText(Text.titleCase(sprites.getPrayer()));
		setVerticalAlignment(JLabel.CENTER);
		setHorizontalAlignment(JLabel.CENTER);
		setPreferredSize(SIZE);

		data = new PrayerData(sprites.getPrayer(), false, false);
		updatePrayerData(data);
	}

	public void updatePrayerData(final PrayerData updatedData)
	{
		if (!data.getPrayer().equals(updatedData.getPrayer()))
		{
			return;
		}

		data = updatedData;

		BufferedImage icon = data.isAvailable() ? availableImage : unavailableImage;
		if (data.isActivated())
		{
			icon = activatedImage;
		}

		setIcon(new ImageIcon(icon));

		revalidate();
		repaint();
	}
}

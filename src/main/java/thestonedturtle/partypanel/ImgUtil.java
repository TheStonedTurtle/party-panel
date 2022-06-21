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
package thestonedturtle.partypanel;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImgUtil
{
	/**
	 * Combines the two images into one, from left to right using the first images specs.
	 * @param left image to center over background
	 * @param right image to overlap
	 * @return overlapped image
	 */
	public static BufferedImage combineImages(final BufferedImage left, final BufferedImage right)
	{
		BufferedImage combined = new BufferedImage(left.getWidth() + right.getWidth(), left.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = combined.createGraphics();
		g2d.drawImage(left, 0, 0, null);
		g2d.drawImage(right, left.getWidth(), 0, null);
		g2d.dispose();

		return combined;
	}

	/**
	 * Overlaps the foreground image centered over the background image.
	 * @param foreground image to center over background
	 * @param background image to overlap
	 * @return overlapped image
	 */
	public static BufferedImage overlapImages(final BufferedImage foreground, final BufferedImage background, boolean isEquipment)
	{
		final int centeredX = background.getWidth() / 2 - foreground.getWidth() / 2;
		final int centeredY = background.getHeight() / 2 - foreground.getHeight() / 2;

		BufferedImage combined = new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = combined.createGraphics();
		g2d.drawImage(background, 0, 0, null);

		// For some reason equipment icons are offset and need to be adjusted
		if (isEquipment)
		{
			g2d.drawImage(foreground, centeredX + 2, centeredY, null);
		}
		else
		{
			g2d.drawImage(foreground, centeredX, centeredY, null);
		}

		g2d.dispose();

		return combined;
	}
}

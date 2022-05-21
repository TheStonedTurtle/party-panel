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
package thestonedturtle.partypanel.ui.equipment;

import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import lombok.Getter;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import thestonedturtle.partypanel.ImgUtil;
import thestonedturtle.partypanel.data.GameItem;

public class EquipmentPanelSlot extends JLabel
{
	private final int IMAGE_SIZE = 48; // Background is squared at 32x32, we want 50% bigger so 48x48
	private final BufferedImage background;
	private final BufferedImage placeholder;
	@Getter
	private GameItem item = null;

	EquipmentPanelSlot(final GameItem item, final BufferedImage image, final BufferedImage background, final BufferedImage placeholder)
	{
		super();

		this.background = background;
		this.placeholder = ImageUtil.resizeImage(ImgUtil.overlapImages(placeholder, background, false), IMAGE_SIZE, IMAGE_SIZE);

		setVerticalAlignment(JLabel.CENTER);
		setHorizontalAlignment(JLabel.CENTER);
		setGameItem(item, image);
	}

	public void setGameItem(final GameItem item, final BufferedImage image)
	{
		this.item = item;

		if (item == null || image == null)
		{
			setIcon(new ImageIcon(placeholder));
			setToolTipText(null);
			return;
		}

		setIcon(new ImageIcon(ImageUtil.resizeImage(ImgUtil.overlapImages(image, background, true), IMAGE_SIZE, IMAGE_SIZE)));
		String name = item.getName();
		if (item.getQty() > 1)
		{
			name += " x " + QuantityFormatter.formatNumber(item.getQty());
		}
		setToolTipText(name);
	}
}

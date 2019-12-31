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

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import net.runelite.api.Constants;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.util.QuantityFormatter;
import thestonedturtle.partypanel.data.GameItem;

public class PlayerInventoryPanel extends JPanel
{
	private final static Dimension INVI_SLOT_SIZE = new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT);
	private final static Color INVI_BACKGROUND = new Color(62, 53, 41);
	private final static Color INVI_BORDER_COLOR = new Color(87, 80, 64);
	private final static Border INVI_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createMatteBorder(3, 3, 3, 3, INVI_BORDER_COLOR),
		BorderFactory.createEmptyBorder(2, 2, 2, 2)
	);

	public PlayerInventoryPanel(final GameItem[] items, final ItemManager itemManager)
	{
		super();

		setLayout(new DynamicGridLayout(7, 4, 2, 2));
		setBackground(INVI_BACKGROUND);
		setBorder(INVI_BORDER);

		for (final GameItem i : items)
		{
			final JLabel label = new JLabel();
			label.setMinimumSize(INVI_SLOT_SIZE);
			label.setPreferredSize(INVI_SLOT_SIZE);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setHorizontalAlignment(JLabel.CENTER);

			if (i != null)
			{
				String name = i.getName();
				if (i.getQty() > 1)
				{
					name += " x " + QuantityFormatter.formatNumber(i.getQty());
				}
				label.setToolTipText("<html>" + name + "</html>");
				label.setIcon(new ImageIcon(itemManager.getImage(i.getId(), i.getQty(), i.isStackable())));
			}

			add(label);
		}

		revalidate();
		repaint();
	}
}

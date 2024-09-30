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
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import org.apache.commons.lang3.ArrayUtils;
import thestonedturtle.partypanel.PartyPanelPlugin;
import thestonedturtle.partypanel.data.GameItem;

public class PlayerInventoryPanel extends JPanel
{
	private static final Dimension INVI_SLOT_SIZE = new Dimension(50, 42);
	private static final Dimension PANEL_SIZE = new Dimension(PluginPanel.PANEL_WIDTH - 14, 296);
	private static final Color INVI_BACKGROUND = new Color(62, 53, 41);

	private final ItemManager itemManager;

	public PlayerInventoryPanel(final GameItem[] items, final GameItem[] runePouchContents, final ItemManager itemManager)
	{
		super();

		this.itemManager = itemManager;

		setLayout(new DynamicGridLayout(7, 4, 2, 2));
		setBackground(INVI_BACKGROUND);
		setPreferredSize(PANEL_SIZE);

		updateInventory(items, runePouchContents);
	}

	public void updateInventory(final GameItem[] items, final GameItem[] runePouchContents)
	{
		this.removeAll();

		for (final GameItem i : items)
		{
			final JLabel label = new JLabel();
			label.setMinimumSize(INVI_SLOT_SIZE);
			label.setPreferredSize(INVI_SLOT_SIZE);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setHorizontalAlignment(JLabel.CENTER);

			if (i != null)
			{
				String tooltip;
				if (ArrayUtils.contains(PartyPanelPlugin.RUNEPOUCH_ITEM_IDS, i.getId()))
				{
					tooltip = getRunePouchHoverText(i, runePouchContents);
				}
				else
				{
					tooltip = i.getDisplayName();
				}
				label.setToolTipText(tooltip);
				itemManager.getImage(i.getId(), i.getQty(), i.isStackable()).addTo(label);
			}

			add(label);
		}

		for (int i = getComponentCount(); i < 28; i++)
		{
			final JLabel label = new JLabel();
			label.setMinimumSize(INVI_SLOT_SIZE);
			label.setPreferredSize(INVI_SLOT_SIZE);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setHorizontalAlignment(JLabel.CENTER);
			add(label);
		}

		revalidate();
		repaint();
	}

	public String getRunePouchHoverText(final GameItem runePouch, final GameItem[] contents)
	{
		final String contentNames = Arrays.stream(contents)
			.filter(Objects::nonNull)
			.map(GameItem::getDisplayName)
			.collect(Collectors.joining("<br>"));

		if (contentNames.isEmpty())
		{
			return runePouch.getDisplayName();
		}

		return "<html>"
			+ runePouch.getDisplayName()
			+ "<br><br>"
			+ contentNames
			+ "</html>";
	}
}

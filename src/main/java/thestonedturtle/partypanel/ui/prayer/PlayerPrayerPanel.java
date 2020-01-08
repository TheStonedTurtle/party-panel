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

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import thestonedturtle.partypanel.data.PrayerData;
import thestonedturtle.partypanel.data.Prayers;

public class PlayerPrayerPanel extends JPanel
{
	private static final Dimension PANEL_SIZE = new Dimension(PluginPanel.PANEL_WIDTH - 10, 300);
	private static final Color BACKGROUND = new Color(62, 53, 41);
	private static final Color BORDER_COLOR = new Color(87, 80, 64);
	private static final Border BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createMatteBorder(3, 3, 3, 3, BORDER_COLOR),
		BorderFactory.createEmptyBorder(2, 2, 2, 2)
	);

	@Getter
	private final Map<Prayer, PrayerSlot> slotMap = new HashMap<>();

	public PlayerPrayerPanel(final Prayers prayer, final SpriteManager spriteManager)
	{
		super();

		setLayout(new DynamicGridLayout(6, 5, 2, 2));
		setBackground(BACKGROUND);
		setBorder(BORDER);
		setPreferredSize(PANEL_SIZE);

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
			add(slot);
		}
	}
}

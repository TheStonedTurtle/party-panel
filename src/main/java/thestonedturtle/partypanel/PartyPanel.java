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

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JLabel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.ui.PlayerBanner;
import thestonedturtle.partypanel.ui.PlayerInventoryPanel;
import thestonedturtle.partypanel.ui.equipment.PlayerEquipmentPanel;
import thestonedturtle.partypanel.ui.skills.PlayerSkillsPanel;

class PartyPanel extends PluginPanel
{
	private enum PartyPanelViewType
	{
		BANNER,
		PLAYER
	}

	private static final Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private static final Color BACKGROUND_HOVER_COLOR = ColorScheme.DARK_GRAY_HOVER_COLOR;

	private final PartyPanelPlugin plugin;
	private final Map<UUID, PlayerBanner> bannerMap = new HashMap<>();

	private PartyPanelViewType viewType = PartyPanelViewType.BANNER;
	private PartyPlayer selectedPlayer = null;

	@Inject
	PartyPanel(final PartyPanelPlugin plugin)
	{
		super();
		this.plugin = plugin;
	}

	void refreshUI()
	{
		this.removeAll();
		switch (viewType)
		{
			case BANNER:
				showBannerView();
				break;
			case PLAYER:
				showPlayerView();
				break;
		}
	}

	/**
	 * Shows all members of the party, excluding the local player, in banner view. See {@link PlayerBanner)
	 */
	void showBannerView()
	{
		viewType = PartyPanelViewType.BANNER;
		removeAll();

		final Collection<PartyPlayer> players = plugin.getPartyMembers().values()
			.stream()
			// Sort by username, if it doesn't exist use their discord name
			.sorted(Comparator.comparing(o -> o.getUsername() == null ? o.getMember().getName() : o.getUsername()))
			.collect(Collectors.toList());

		for (final PartyPlayer player : players)
		{
			final PlayerBanner banner = new PlayerBanner(player, plugin.spriteManager);
			banner.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					if (e.getButton() == MouseEvent.BUTTON1)
					{
						selectedPlayer = player;
						viewType = PartyPanelViewType.PLAYER;
						showPlayerView();
					}
				}

				@Override
				public void mouseEntered(MouseEvent e)
				{
					banner.setBackground(BACKGROUND_HOVER_COLOR);
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					banner.setBackground(BACKGROUND_COLOR);
				}
			});
			add(banner);
			bannerMap.put(player.getMember().getMemberId(), banner);
		}

		if (getComponentCount() == 0)
		{
			add(new JLabel("There are no members in your party"));
		}

		this.revalidate();
		this.repaint();
	}

	void showPlayerView()
	{
		if (selectedPlayer == null)
		{
			showBannerView();
		}

		removeAll();
		add(createPlayerTitle());
		add(new PlayerBanner(selectedPlayer, plugin.spriteManager));
		add(new PlayerInventoryPanel(selectedPlayer.getInventory(), plugin.itemManager));
		add(new PlayerEquipmentPanel(selectedPlayer.getEquipment(), plugin.spriteManager, plugin.itemManager));
		add(new PlayerSkillsPanel(selectedPlayer, plugin.spriteManager, plugin.itemManager));

		this.revalidate();
		this.repaint();
	}

	// Title element for Loot breakdown view
	private JButton createPlayerTitle()
	{
		final JButton label = new JButton("Back to party overview");
		label.setFocusable(false);
		label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				label.setBackground(BACKGROUND_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				label.setBackground(BACKGROUND_COLOR);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					showBannerView();
				}
			}
		});

		return label;
	}

	void updatePartyPlayer(final PartyPlayer player)
	{
		switch (viewType)
		{
			case BANNER:
				final PlayerBanner panel = bannerMap.get(player.getMember().getMemberId());
				if (panel == null)
				{
					// New member, recreate entire view
					showBannerView();
					return;
				}

				final boolean usernameChanged = Objects.equals(player.getUsername(), panel.getPlayer().getUsername());
				panel.setPlayer(player);
				if (usernameChanged)
				{
					panel.recreateStatsPanel();
				}
				else
				{
					panel.recreatePanel();
				}
				break;
			case PLAYER:
				if (player.equals(selectedPlayer))
				{
					showPlayerView();
				}
				break;
		}
	}
}

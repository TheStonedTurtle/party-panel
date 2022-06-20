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
import java.awt.BorderLayout;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.ui.PlayerBanner;
import thestonedturtle.partypanel.ui.PlayerPanel;

class PartyPanel extends PluginPanel
{
	private final PartyPanelPlugin plugin;
	@Getter
	private final HashMap<UUID, PlayerPanel> playerPanelMap = new HashMap<>();
	private final JPanel basePanel;

	@Inject
	PartyPanel(final PartyPanelPlugin plugin)
	{
		super(false);
		this.plugin = plugin;
		this.setLayout(new BorderLayout());

		basePanel = new JPanel();
		basePanel.setBorder(new EmptyBorder(BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET));
		basePanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

		// Wrap content to anchor to top and prevent expansion
		final JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(basePanel, BorderLayout.NORTH);
		final JScrollPane scrollPane = new JScrollPane(northPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		this.add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Shows all members of the party, excluding the local player. See {@link PlayerBanner)
	 */
	void renderSidebar()
	{
		basePanel.removeAll();

		// Sort by their RSN first; If it doesn't exist sort by their Discord name instead
		final List<PartyPlayer> players = plugin.getPartyMembers().values()
			.stream()
			.sorted(Comparator.comparing(o -> o.getUsername() == null ? o.getMember().getName() : o.getUsername()))
			.collect(Collectors.toList());

		for (final PartyPlayer player : players)
		{
			drawPlayerPanel(player);
		}

		if (getComponentCount() == 0)
		{
			basePanel.add(new JLabel("There are no members in your party"));
		}

		basePanel.revalidate();
		basePanel.repaint();
	}

	void drawPlayerPanel(PartyPlayer player)
	{
		playerPanelMap.computeIfAbsent(player.getMemberId(), (k) ->
				new PlayerPanel(player, plugin.getConfig().autoExpandMembers(), plugin.spriteManager, plugin.itemManager)).updatePlayerData(player);
		basePanel.add(playerPanelMap.get(player.getMemberId()));
		basePanel.revalidate();
		basePanel.repaint();
	}

	void removePartyPlayer(final PartyPlayer player)
	{
		if (player != null)
		{
			playerPanelMap.remove(player.getMemberId());
			renderSidebar();
		}
	}

	void updatePartyMembersExpand(boolean expand)
	{
		for (PlayerPanel panel : playerPanelMap.values())
		{
			panel.setShowInfo(expand);
			panel.getBanner().setExpandIcon(expand);
			panel.updatePanel();
		}
	}
}

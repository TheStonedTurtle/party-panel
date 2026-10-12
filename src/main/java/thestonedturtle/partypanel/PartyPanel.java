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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import thestonedturtle.partypanel.data.PartyPlayer;
import thestonedturtle.partypanel.ui.CardDragListener;
import thestonedturtle.partypanel.ui.ControlsPanel;
import thestonedturtle.partypanel.ui.PlayerPanel;

class PartyPanel extends PluginPanel
{
	private final PartyPanelPlugin plugin;
	@Getter
	private final HashMap<Long, PlayerPanel> playerPanelMap = new HashMap<>();
	private final JPanel basePanel = new JPanel();
	private final PlayerOrder playerOrder = new PlayerOrder();
	private final JScrollPane scrollPane;
	private Long draggedMember;
	private Point dragOrigin;
	private boolean dragStarted;
	private boolean pressedOnBanner;
	private final JPanel passphrasePanel = new JPanel();
	private final JLabel passphraseLabel = new JLabel();
	@Getter
	private final ControlsPanel controlsPanel;

	@Inject
	PartyPanel(final PartyPanelPlugin plugin)
	{
		super(false);
		this.plugin = plugin;
		this.setLayout(new BorderLayout());

		basePanel.setBorder(new EmptyBorder(BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET));
		basePanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

		final JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(BORDER_OFFSET, 2, BORDER_OFFSET, 2));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		passphrasePanel.setBorder(new EmptyBorder(4, 0, 0, 0));
		passphrasePanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

		final JLabel passphraseTopLabel = new JLabel("Party Passphrase");
		passphraseTopLabel.setForeground(Color.WHITE);
		passphraseTopLabel.setHorizontalTextPosition(JLabel.CENTER);
		passphraseTopLabel.setHorizontalAlignment(JLabel.CENTER);

		final JMenuItem copyOpt = new JMenuItem("Copy Passphrase");
		copyOpt.addActionListener(e ->
		{
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(passphraseLabel.getText()), null);
		});

		final JPopupMenu copyPopup = new JPopupMenu();
		copyPopup.setBorder(new EmptyBorder(5, 5, 5, 5));
		copyPopup.add(copyOpt);

		passphraseLabel.setText(plugin.getPartyPassphrase());
		passphraseLabel.setHorizontalTextPosition(JLabel.CENTER);
		passphraseLabel.setHorizontalAlignment(JLabel.CENTER);
		passphraseLabel.setComponentPopupMenu(copyPopup);

		passphrasePanel.add(passphraseTopLabel);
		passphrasePanel.add(passphraseLabel);
		syncPartyPassphraseVisibility();

		controlsPanel = new ControlsPanel(plugin);
		topPanel.add(controlsPanel);
		topPanel.add(passphrasePanel);

		this.add(topPanel, BorderLayout.NORTH);

		// Wrap content to anchor to top and prevent expansion
		final JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(basePanel, BorderLayout.NORTH);
		scrollPane = new JScrollPane(northPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		this.add(scrollPane, BorderLayout.CENTER);
	}

	void clearSidebar()
	{
		playerPanelMap.values().forEach(p -> p.setDragging(false));
		basePanel.removeAll();
		playerPanelMap.clear();
		playerOrder.reconcile(playerPanelMap.keySet());
		draggedMember = null;
		dragOrigin = null;
		dragStarted = false;
		pressedOnBanner = false;

		basePanel.revalidate();
		basePanel.repaint();
	}

	/**
	 * Shows all members of the party, excluding the local player. See {@link thestonedturtle.partypanel.ui.PlayerBanner )
	 */
	void renderSidebar()
	{
		// Sort by their RSN first; If it doesn't exist sort by their Discord name instead
		final List<PartyPlayer> players = plugin.getPartyMembers().values()
			.stream()
			.sorted(Comparator.comparing(o -> Strings.isNullOrEmpty(o.getUsername()) ? o.getMember().getDisplayName() : o.getUsername()))
			.collect(Collectors.toList());

		for (final PartyPlayer player : players)
		{
			drawPlayerPanel(player);
		}

		if (playerPanelMap.isEmpty())
		{
			basePanel.add(new JLabel("There are no members in your party"));
		}

		basePanel.revalidate();
		basePanel.repaint();
	}

	void drawPlayerPanel(PartyPlayer player)
	{
		drawPlayerPanel(player, false);
	}

	void drawPlayerPanel(PartyPlayer player, boolean hasBreakingBannerChange)
	{
		PlayerPanel panel = playerPanelMap.get(player.getMember().getMemberId());
		if (panel != null)
		{
			panel.updatePlayerData(player, true);
			return;
		}

		panel = new PlayerPanel(player, plugin.getConfig(), plugin.spriteManager, plugin.itemManager);
		playerPanelMap.put(player.getMember().getMemberId(), panel);
		panel.updatePlayerData(player, hasBreakingBannerChange);
		installCardDrag(panel, player.getMember().getMemberId());
		playerOrder.reconcile(playerPanelMap.keySet());
		refreshPlayerOrder();
	}

	void removePartyPlayer(final PartyPlayer player)
	{
		if (player != null)
		{
			final PlayerPanel p = playerPanelMap.remove(player.getMember().getMemberId());
			if (p != null)
			{
				p.setDragging(false);
				if (draggedMember != null && draggedMember == player.getMember().getMemberId())
				{
					draggedMember = null;
					dragOrigin = null;
					dragStarted = false;
					pressedOnBanner = false;
				}
				playerOrder.reconcile(playerPanelMap.keySet());
				refreshPlayerOrder();
			}
		}
	}

	private void refreshPlayerOrder()
	{
		basePanel.removeAll();
		for (Long id : playerOrder.getMembers())
		{
			basePanel.add(playerPanelMap.get(id));
		}
		if (playerPanelMap.isEmpty())
		{
			basePanel.add(new JLabel("There are no members in your party"));
		}
		basePanel.revalidate();
		basePanel.repaint();
	}

	private void moveCard(long memberId)
	{
		// Preserve components and selected tabs; only change layout order.
		final PlayerPanel moving = playerPanelMap.get(memberId);
		basePanel.setComponentZOrder(moving, playerOrder.getMembers().indexOf(memberId));
		basePanel.doLayout();
		basePanel.repaint();
	}

	private void installCardDrag(PlayerPanel panel, long memberId)
	{
		final MouseAdapter drag = new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent event)
			{
				if (SwingUtilities.isLeftMouseButton(event))
				{
				if (draggedMember != null)
				{
					PlayerPanel previous = playerPanelMap.get(draggedMember);
					if (previous != null)
					{
						previous.setDragging(false);
					}
				}
					draggedMember = memberId;
					dragOrigin = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), basePanel);
					dragStarted = false;
					pressedOnBanner = SwingUtilities.isDescendingFrom(event.getComponent(), panel.getBanner());
				}
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
				if (draggedMember == null || draggedMember != memberId)
				{
					return;
				}

				final Point point = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), basePanel);
				if (!dragStarted)
				{
					if (CardDragListener.isClick(dragOrigin, point))
					{
						return;
					}
					dragStarted = true;
					panel.setDragging(true);
				}
				final int scrollY = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), scrollPane.getViewport()).y;
				if (scrollY < 24)
				{
					scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue() - 12);
				}
				else if (scrollY > scrollPane.getViewport().getHeight() - 24)
				{
					scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue() + 12);
				}

				for (Long target : playerOrder.getMembers())
				{
					if (target == memberId)
					{
						continue;
					}
					final PlayerPanel other = playerPanelMap.get(target);
					if (other.getY() <= point.y && point.y < other.getY() + other.getHeight())
					{
						boolean before = point.y < other.getY() + other.getHeight() / 2;
						if (playerOrder.move(memberId, target, before))
						{
							moveCard(memberId);
						}
						break;
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent event)
			{
				if (draggedMember != null && draggedMember == memberId)
				{
				final Point released = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), basePanel);
				final boolean toggle = pressedOnBanner && !dragStarted && CardDragListener.isClick(dragOrigin, released);
				panel.setDragging(false);
					draggedMember = null;
					dragOrigin = null;
					dragStarted = false;
					pressedOnBanner = false;
					if (toggle)
					{
						panel.toggleExpanded();
					}
				}
			}
		};
		new CardDragListener(panel, drag::mousePressed, drag::mouseDragged, drag::mouseReleased);
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

	public void updatePartyControls()
	{
		controlsPanel.setVisible(plugin.getConfig().showPartyControls());
	}

	public void syncPartyPassphraseVisibility()
	{
		passphraseLabel.setText(plugin.getPartyPassphrase());
		passphrasePanel.setVisible(plugin.getConfig().showPartyPassphrase() && plugin.isInParty());
	}

	public void updateParty()
	{
		controlsPanel.updateControls();
		syncPartyPassphraseVisibility();
	}

	public void updateDisplayVirtualLevels()
	{
		playerPanelMap.values().forEach(PlayerPanel::updateDisplayVirtualLevels);
	}

	public void updateDisplayPlayerWorlds()
	{
		playerPanelMap.values().forEach(PlayerPanel::updateDisplayPlayerWorlds);
	}
}

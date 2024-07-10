/*
 * Copyright (c) 2021, Jonathan Rousseau <https://github.com/JoRouss>
 * Copyright (c) 2022, TheStonedTurtle <https://github.com/TheStonedTurtle>
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

import java.util.Locale;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import thestonedturtle.partypanel.PartyPanelPlugin;

// A copy of the controls from the `net.runelite.client.plugins.party.PartyPanel` class
public class ControlsPanel extends JPanel
{
	private static final String BTN_CREATE_TEXT = "Create party";
	private static final String BTN_LEAVE_TEXT = "Leave party";

	private final JButton startButton = new JButton();
	private final JButton joinPartyButton = new JButton();
	private final JButton rejoinPartyButton = new JButton();
	private final JButton copyPartyIdButton = new JButton();

	private final PartyPanelPlugin plugin;

	public ControlsPanel(PartyPanelPlugin plugin)
	{
		this.plugin = plugin;
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 2, 4, 2);

		c.gridx = 0;
		c.gridy = 0;
		this.add(startButton, c);

		c.gridx = 1;
		c.gridy = 0;
		this.add(joinPartyButton, c);

		c.gridx = 1;
		c.gridy = 0;
		this.add(copyPartyIdButton, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		this.add(rejoinPartyButton, c);

		startButton.setText(plugin.isInParty() ? BTN_LEAVE_TEXT : BTN_CREATE_TEXT);
		startButton.setFocusable(false);

		joinPartyButton.setText("Join party");
		joinPartyButton.setFocusable(false);

		rejoinPartyButton.setText("Join previous party");
		rejoinPartyButton.setFocusable(false);

		copyPartyIdButton.setText("Copy passphrase");
		copyPartyIdButton.setFocusable(false);

		startButton.addActionListener(e ->
		{
			if (plugin.isInParty())
			{
				// Leave party
				final int result = JOptionPane.showOptionDialog(startButton,
					"Are you sure you want to leave the party?",
					"Leave party?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null, new String[]{"Yes", "No"}, "No");

				if (result == JOptionPane.YES_OPTION)
				{
					plugin.leaveParty();
				}
			}
			else
			{
				plugin.createParty();
			}
		});

		joinPartyButton.addActionListener(e ->
		{
			if (!plugin.isInParty())
			{
				String s = (String) JOptionPane.showInputDialog(
					joinPartyButton,
					"Please enter the party passphrase:",
					"Party Passphrase",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					"");

				if (s == null)
				{
					return;
				}

				s = s.toLowerCase(Locale.US);


				plugin.changeParty(s);
			}
		});

		rejoinPartyButton.addActionListener(e ->
		{
			if (!plugin.isInParty())
			{
				plugin.changeParty(plugin.getConfig().previousPartyId());
			}
		});

		copyPartyIdButton.addActionListener(e ->
		{
			if (plugin.isInParty())
			{
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(plugin.getPartyPassphrase()), null);
			}
		});

		updateControls();
	}

	public void updateControls()
	{
		startButton.setText(plugin.isInParty() ? BTN_LEAVE_TEXT : BTN_CREATE_TEXT);
		joinPartyButton.setVisible(!plugin.isInParty());
		rejoinPartyButton.setVisible(!plugin.isInParty());
		copyPartyIdButton.setVisible(plugin.isInParty());

		if (!plugin.getConfig().showPartyControls())
		{
			this.setVisible(false);
		}
	}
}

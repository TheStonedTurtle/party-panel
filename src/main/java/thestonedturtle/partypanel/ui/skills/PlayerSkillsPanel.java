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
package thestonedturtle.partypanel.ui.skills;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import thestonedturtle.partypanel.data.PartyPlayer;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.runelite.api.Skill.AGILITY;
import static net.runelite.api.Skill.ATTACK;
import static net.runelite.api.Skill.CONSTRUCTION;
import static net.runelite.api.Skill.COOKING;
import static net.runelite.api.Skill.CRAFTING;
import static net.runelite.api.Skill.DEFENCE;
import static net.runelite.api.Skill.FARMING;
import static net.runelite.api.Skill.FIREMAKING;
import static net.runelite.api.Skill.FISHING;
import static net.runelite.api.Skill.FLETCHING;
import static net.runelite.api.Skill.HERBLORE;
import static net.runelite.api.Skill.HITPOINTS;
import static net.runelite.api.Skill.HUNTER;
import static net.runelite.api.Skill.MAGIC;
import static net.runelite.api.Skill.MINING;
import static net.runelite.api.Skill.PRAYER;
import static net.runelite.api.Skill.RANGED;
import static net.runelite.api.Skill.RUNECRAFT;
import static net.runelite.api.Skill.SAILING;
import static net.runelite.api.Skill.SLAYER;
import static net.runelite.api.Skill.SMITHING;
import static net.runelite.api.Skill.STRENGTH;
import static net.runelite.api.Skill.THIEVING;
import static net.runelite.api.Skill.WOODCUTTING;

@Getter
public class PlayerSkillsPanel extends JPanel
{
	/**
	 * Skills ordered in the way they should be displayed in the panel.
	 */
	private static final List<Skill> SKILLS = ImmutableList.of(
			ATTACK, HITPOINTS, MINING,
			STRENGTH, AGILITY, SMITHING,
			DEFENCE, HERBLORE, FISHING,
			RANGED, THIEVING, COOKING,
			PRAYER, CRAFTING, FIREMAKING,
			MAGIC, FLETCHING, WOODCUTTING,
			RUNECRAFT, SLAYER, FARMING,
			CONSTRUCTION, HUNTER, SAILING
	);

	private static final ImmutableMap<Skill, Integer> SPRITE_MAP;

	static
	{
		final ImmutableMap.Builder<Skill, Integer> map = ImmutableMap.builder();
		map.put(Skill.ATTACK, SpriteID.Staticons.ATTACK);
		map.put(Skill.STRENGTH, SpriteID.Staticons.STRENGTH);
		map.put(Skill.DEFENCE, SpriteID.Staticons.DEFENCE);
		map.put(Skill.RANGED, SpriteID.Staticons.RANGED);
		map.put(Skill.PRAYER, SpriteID.Staticons.PRAYER);
		map.put(Skill.MAGIC, SpriteID.Staticons.MAGIC);
		map.put(Skill.HITPOINTS, SpriteID.Staticons.HITPOINTS);
		map.put(Skill.AGILITY, SpriteID.Staticons.AGILITY);
		map.put(Skill.HERBLORE, SpriteID.Staticons.HERBLORE);
		map.put(Skill.THIEVING, SpriteID.Staticons.THIEVING);
		map.put(Skill.CRAFTING, SpriteID.Staticons.CRAFTING);
		map.put(Skill.FLETCHING, SpriteID.Staticons.FLETCHING);
		map.put(Skill.MINING, SpriteID.Staticons.MINING);
		map.put(Skill.SMITHING, SpriteID.Staticons.SMITHING);
		map.put(Skill.FISHING, SpriteID.Staticons.FISHING);
		map.put(Skill.COOKING, SpriteID.Staticons.COOKING);
		map.put(Skill.FIREMAKING, SpriteID.Staticons.FIREMAKING);
		map.put(Skill.WOODCUTTING, SpriteID.Staticons.WOODCUTTING);
		map.put(Skill.RUNECRAFT, SpriteID.Staticons2.RUNECRAFT);
		map.put(Skill.SLAYER, SpriteID.Staticons2.SLAYER);
		map.put(Skill.FARMING, SpriteID.Staticons2.FARMING);
		map.put(Skill.CONSTRUCTION, SpriteID.Staticons2.CONSTRUCTION);
		map.put(Skill.HUNTER, SpriteID.Staticons2.HUNTER);
		map.put(Skill.SAILING, SpriteID.Staticons2.SAILING);
		SPRITE_MAP = map.build();
	}

	protected static final Dimension PANEL_SIZE = new Dimension(PluginPanel.PANEL_WIDTH - 14, 296);

	private final Map<Skill, SkillPanelSlot> panelMap = new HashMap<>();
	private final TotalPanelSlot totalLevelPanel;

	private final JPanel skillsPanel = new JPanel();

	public PlayerSkillsPanel(final PartyPlayer player, final boolean displayVirtualLevels, final SpriteManager spriteManager)
	{
		super();

		this.setMinimumSize(PANEL_SIZE);
		this.setPreferredSize(PANEL_SIZE);
		this.setBackground(new Color(62, 53, 41));

		this.setLayout(new DynamicGridLayout(2, 1, 0, 0));

		skillsPanel.setLayout(new DynamicGridLayout(8, 3, 2, 0));
		skillsPanel.setBackground(new Color(62, 53, 41));
		int totalLevel = 0;
		for (final Skill s : SKILLS)
		{
			int realLevel = player.getSkillRealLevel(s, displayVirtualLevels);
			final SkillPanelSlot slot = new SkillPanelSlot(player.getSkillBoostedLevel(s), realLevel);
			panelMap.put(s, slot);
			skillsPanel.add(slot);
			spriteManager.getSpriteAsync(SPRITE_MAP.get(s), 0, img -> SwingUtilities.invokeLater(() -> slot.initImages(img, spriteManager)));

			updateSkill(player, s, displayVirtualLevels); // Call to ensure tooltip is correct

			totalLevel += realLevel;
		}
		this.add(skillsPanel);


		// Add 9 since hp starts at 10
		totalLevel = player.getStats() == null ? (9 + Skill.values().length) : totalLevel;
		totalLevelPanel = new TotalPanelSlot(totalLevel, spriteManager);
		this.add(totalLevelPanel);
	}

	public void updateSkill(final PartyPlayer player, final Skill s, final boolean displayVirtualLevels)
	{
		int boosted = s == Skill.HITPOINTS ? 10 : 1;
		int baseLevel = s == Skill.HITPOINTS ? 10 : 1;
		if (player.getStats() != null)
		{
			boosted = player.getSkillBoostedLevel(s);
			baseLevel = player.getSkillRealLevel(s, displayVirtualLevels);
		}

		final SkillPanelSlot panel = panelMap.get(s);
		panel.updateBoostedLevel(boosted);
		panel.updateBaseLevel(baseLevel);
		panel.setToolTipText(s.getName());
	}
}

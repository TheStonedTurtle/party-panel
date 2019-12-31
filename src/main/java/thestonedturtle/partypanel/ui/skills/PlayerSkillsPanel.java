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
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import lombok.Getter;
import net.runelite.api.Skill;
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
import static net.runelite.api.Skill.SLAYER;
import static net.runelite.api.Skill.SMITHING;
import static net.runelite.api.Skill.STRENGTH;
import static net.runelite.api.Skill.THIEVING;
import static net.runelite.api.Skill.WOODCUTTING;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import thestonedturtle.partypanel.data.PartyPlayer;

public class PlayerSkillsPanel extends JPanel
{
	/**
	 * Real skills, ordered in the way they should be displayed in the panel.
	 */
	private static final List<Skill> SKILLS = ImmutableList.of(
		ATTACK, HITPOINTS, MINING,
		STRENGTH, AGILITY, SMITHING,
		DEFENCE, HERBLORE, FISHING,
		RANGED, THIEVING, COOKING,
		PRAYER, CRAFTING, FIREMAKING,
		MAGIC, FLETCHING, WOODCUTTING,
		RUNECRAFT, SLAYER, FARMING,
		CONSTRUCTION, HUNTER
	);

	private static final ImmutableMap<Skill, Integer> SPRITE_MAP;
	static
	{
		final ImmutableMap.Builder<Skill, Integer> map = ImmutableMap.builder();
		map.put(Skill.ATTACK, SpriteID.SKILL_ATTACK);
		map.put(Skill.STRENGTH, SpriteID.SKILL_STRENGTH);
		map.put(Skill.DEFENCE, SpriteID.SKILL_DEFENCE);
		map.put(Skill.RANGED, SpriteID.SKILL_RANGED);
		map.put(Skill.PRAYER, SpriteID.SKILL_PRAYER);
		map.put(Skill.MAGIC, SpriteID.SKILL_MAGIC);
		map.put(Skill.HITPOINTS, SpriteID.SKILL_HITPOINTS);
		map.put(Skill.AGILITY, SpriteID.SKILL_AGILITY);
		map.put(Skill.HERBLORE, SpriteID.SKILL_HERBLORE);
		map.put(Skill.THIEVING, SpriteID.SKILL_THIEVING);
		map.put(Skill.CRAFTING, SpriteID.SKILL_CRAFTING);
		map.put(Skill.FLETCHING, SpriteID.SKILL_FLETCHING);
		map.put(Skill.MINING, SpriteID.SKILL_MINING);
		map.put(Skill.SMITHING, SpriteID.SKILL_SMITHING);
		map.put(Skill.FISHING, SpriteID.SKILL_FISHING);
		map.put(Skill.COOKING, SpriteID.SKILL_COOKING);
		map.put(Skill.FIREMAKING, SpriteID.SKILL_FIREMAKING);
		map.put(Skill.WOODCUTTING, SpriteID.SKILL_WOODCUTTING);
		map.put(Skill.RUNECRAFT, SpriteID.SKILL_RUNECRAFT);
		map.put(Skill.SLAYER, SpriteID.SKILL_SLAYER);
		map.put(Skill.FARMING, SpriteID.SKILL_FARMING);
		map.put(Skill.CONSTRUCTION, SpriteID.SKILL_CONSTRUCTION);
		map.put(Skill.HUNTER, SpriteID.SKILL_HUNTER);
		SPRITE_MAP = map.build();
	}

	private static final Dimension PANEL_SIZE = new Dimension(PluginPanel.PANEL_WIDTH - 10, 300);
	private static final Color PANEL_BORDER_COLOR = new Color(87, 80, 64);
	private static final Border PANEL_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createMatteBorder(3, 3, 3, 3, PANEL_BORDER_COLOR),
		BorderFactory.createEmptyBorder(2, 2, 2, 2)
	);

	@Getter
	private final Map<Skill, SkillPanelSlot> panelMap = new HashMap<>();

	public PlayerSkillsPanel(final PartyPlayer player, final SpriteManager spriteManager, final ItemManager itemManager)
	{
		super();

		this.setMinimumSize(PANEL_SIZE);
		this.setPreferredSize(PANEL_SIZE);
		this.setBorder(PANEL_BORDER);
		this.setBackground(new Color(62, 53, 41));
		this.setLayout(new DynamicGridLayout(8, 3, 0, 0));

		for (final Skill s : SKILLS)
		{
			final SkillPanelSlot slot = new SkillPanelSlot(player.getSkillBoostedLevel(s), player.getSkillRealLevel(s), spriteManager.getSprite(SPRITE_MAP.get(s), 0), spriteManager);
			panelMap.put(s, slot);
			this.add(slot);
		}

		final int totalLevel = player.getStats() == null ? -1 : player.getStats().getTotalLevel();
		this.add(new TotalPanelSlot(totalLevel, spriteManager));
	}
}

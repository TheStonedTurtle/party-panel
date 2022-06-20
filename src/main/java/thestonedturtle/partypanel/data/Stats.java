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
package thestonedturtle.partypanel.data;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import thestonedturtle.partypanel.data.events.PartyStatChange;

@Getter
@Setter
public class Stats
{
	private final Map<Skill, Integer> baseLevels = new HashMap<>();
	private final Map<Skill, Integer> boostedLevels = new HashMap<>();
	private final Map<Skill, Integer> skillEXPs = new HashMap<>();
	private int specialPercent;
	private int runEnergy;
	private int combatLevel;
	private int totalLevel;

	public Stats(final Client client)
	{
		final int[] bases = client.getRealSkillLevels();
		final int[] boosts = client.getBoostedSkillLevels();
		for (final Skill s : Skill.values())
		{
			baseLevels.put(s, bases[s.ordinal()]);
			boostedLevels.put(s, boosts[s.ordinal()]);
			skillEXPs.put(s, client.getSkillExperience(s));
		}

		combatLevel = Experience.getCombatLevel(
			baseLevels.get(Skill.ATTACK),
			baseLevels.get(Skill.STRENGTH),
			baseLevels.get(Skill.DEFENCE),
			baseLevels.get(Skill.HITPOINTS),
			baseLevels.get(Skill.MAGIC),
			baseLevels.get(Skill.RANGED),
			baseLevels.get(Skill.PRAYER)
		);

		specialPercent = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
		totalLevel = client.getTotalLevel();
		runEnergy = client.getEnergy();
	}

	public int recalculateCombatLevel()
	{
		combatLevel = Experience.getCombatLevel(
			baseLevels.get(Skill.ATTACK),
			baseLevels.get(Skill.STRENGTH),
			baseLevels.get(Skill.DEFENCE),
			baseLevels.get(Skill.HITPOINTS),
			baseLevels.get(Skill.MAGIC),
			baseLevels.get(Skill.RANGED),
			baseLevels.get(Skill.PRAYER)
		);

		return combatLevel;
	}

	public PartyStatChange createPartyStatChangeForSkill(Skill s)
	{
		return new PartyStatChange(s, baseLevels.get(s), boostedLevels.get(s), skillEXPs.get(s));
	}
}

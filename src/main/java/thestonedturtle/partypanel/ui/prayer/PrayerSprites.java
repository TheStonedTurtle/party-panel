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

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;

@AllArgsConstructor
@Getter
public enum PrayerSprites
{
	THICK_SKIN(Prayer.THICK_SKIN, SpriteID.PRAYER_THICK_SKIN, SpriteID.PRAYER_THICK_SKIN_DISABLED, 0),
	BURST_OF_STRENGTH(Prayer.BURST_OF_STRENGTH, SpriteID.PRAYER_BURST_OF_STRENGTH, SpriteID.PRAYER_BURST_OF_STRENGTH_DISABLED, 1),
	CLARITY_OF_THOUGHT(Prayer.CLARITY_OF_THOUGHT, SpriteID.PRAYER_CLARITY_OF_THOUGHT, SpriteID.PRAYER_CLARITY_OF_THOUGHT_DISABLED, 2),
	SHARP_EYE(Prayer.SHARP_EYE, SpriteID.PRAYER_SHARP_EYE, SpriteID.PRAYER_SHARP_EYE_DISABLED, 18),
	MYSTIC_WILL(Prayer.MYSTIC_WILL, SpriteID.PRAYER_MYSTIC_WILL, SpriteID.PRAYER_MYSTIC_WILL_DISABLED, 19),

	ROCK_SKIN(Prayer.ROCK_SKIN, SpriteID.PRAYER_ROCK_SKIN, SpriteID.PRAYER_ROCK_SKIN_DISABLED, 3),
	SUPERHUMAN_STRENGTH(Prayer.SUPERHUMAN_STRENGTH, SpriteID.PRAYER_SUPERHUMAN_STRENGTH, SpriteID.PRAYER_SUPERHUMAN_STRENGTH_DISABLED, 4),
	IMPROVED_REFLEXES(Prayer.IMPROVED_REFLEXES, SpriteID.PRAYER_IMPROVED_REFLEXES, SpriteID.PRAYER_IMPROVED_REFLEXES_DISABLED, 5),
	RAPID_RESTORE(Prayer.RAPID_RESTORE, SpriteID.PRAYER_RAPID_RESTORE, SpriteID.PRAYER_RAPID_RESTORE_DISABLED, 6),
	RAPID_HEAL(Prayer.RAPID_HEAL, SpriteID.PRAYER_RAPID_HEAL, SpriteID.PRAYER_RAPID_HEAL_DISABLED, 7),

	PROTECT_ITEM(Prayer.PROTECT_ITEM, SpriteID.PRAYER_PROTECT_ITEM, SpriteID.PRAYER_PROTECT_ITEM_DISABLED, 8),
	HAWK_EYE(Prayer.HAWK_EYE, SpriteID.PRAYER_HAWK_EYE, SpriteID.PRAYER_HAWK_EYE_DISABLED, 20),
	MYSTIC_LORE(Prayer.MYSTIC_LORE, SpriteID.PRAYER_MYSTIC_LORE, SpriteID.PRAYER_MYSTIC_LORE_DISABLED, 21),
	STEEL_SKIN(Prayer.STEEL_SKIN, SpriteID.PRAYER_STEEL_SKIN, SpriteID.PRAYER_STEEL_SKIN_DISABLED, 9),
	ULTIMATE_STRENGTH(Prayer.ULTIMATE_STRENGTH, SpriteID.PRAYER_ULTIMATE_STRENGTH, SpriteID.PRAYER_ULTIMATE_STRENGTH_DISABLED, 10),

	INCREDIBLE_REFLEXES(Prayer.INCREDIBLE_REFLEXES, SpriteID.PRAYER_INCREDIBLE_REFLEXES, SpriteID.PRAYER_INCREDIBLE_REFLEXES_DISABLED, 11),
	PROTECT_FROM_MAGIC(Prayer.PROTECT_FROM_MAGIC, SpriteID.PRAYER_PROTECT_FROM_MAGIC, SpriteID.PRAYER_PROTECT_FROM_MAGIC_DISABLED, 12),
	PROTECT_FROM_MISSILES(Prayer.PROTECT_FROM_MISSILES, SpriteID.PRAYER_PROTECT_FROM_MISSILES, SpriteID.PRAYER_PROTECT_FROM_MISSILES_DISABLED, 13),
	PROTECT_FROM_MELEE(Prayer.PROTECT_FROM_MELEE, SpriteID.PRAYER_PROTECT_FROM_MELEE, SpriteID.PRAYER_PROTECT_FROM_MELEE_DISABLED, 14),
	EAGLE_EYE(Prayer.EAGLE_EYE, SpriteID.PRAYER_EAGLE_EYE, SpriteID.PRAYER_EAGLE_EYE_DISABLED, 22)
		{
			@Override
			public boolean isUnlocked(Client client)
			{
				return !DEADEYE.isUnlocked(client);
			}
		},
	DEADEYE(Prayer.DEADEYE, SpriteID.PRAYER_DEADEYE, SpriteID.PRAYER_DEADEYE_DISABLED, 22)
		{
			@Override
			public boolean isUnlocked(Client client)
			{
				boolean inLms = client.getVarbitValue(Varbits.IN_LMS) != 0;
				boolean deadeye = client.getVarbitValue(Varbits.PRAYER_DEADEYE_UNLOCKED) != 0;
				return deadeye && !inLms;
			}
		},

	MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT, SpriteID.PRAYER_MYSTIC_MIGHT, SpriteID.PRAYER_MYSTIC_MIGHT_DISABLED, 23)
		{
			@Override
			public boolean isUnlocked(Client client)
			{
				return !MYSTIC_VIGOUR.isUnlocked(client);
			}
		},
	MYSTIC_VIGOUR(Prayer.MYSTIC_VIGOUR, SpriteID.PRAYER_MYSTIC_VIGOUR, SpriteID.PRAYER_MYSTIC_VIGOUR_DISABLED, 23)
		{
			@Override
			public boolean isUnlocked(Client client)
			{
				boolean inLms = client.getVarbitValue(Varbits.IN_LMS) != 0;
				boolean vigour = client.getVarbitValue(Varbits.PRAYER_MYSTIC_VIGOUR_UNLOCKED) != 0;
				return vigour && !inLms;
			}
		},
	RETRIBUTION(Prayer.RETRIBUTION, SpriteID.PRAYER_RETRIBUTION, SpriteID.PRAYER_RETRIBUTION_DISABLED, 15),
	REDEMPTION(Prayer.REDEMPTION, SpriteID.PRAYER_REDEMPTION, SpriteID.PRAYER_REDEMPTION_DISABLED, 16),
	SMITE(Prayer.SMITE, SpriteID.PRAYER_SMITE, SpriteID.PRAYER_SMITE_DISABLED, 17),
	PRESERVE(Prayer.PRESERVE, SpriteID.PRAYER_PRESERVE, SpriteID.PRAYER_PRESERVE_DISABLED, 28),

	CHIVALRY(Prayer.CHIVALRY, SpriteID.PRAYER_CHIVALRY, SpriteID.PRAYER_CHIVALRY_DISABLED, 24),
	PIETY(Prayer.PIETY, SpriteID.PRAYER_PIETY, SpriteID.PRAYER_PIETY_DISABLED, 25),
	RIGOUR(Prayer.RIGOUR, SpriteID.PRAYER_RIGOUR, SpriteID.PRAYER_RIGOUR_DISABLED, 26),
	AUGURY(Prayer.AUGURY, SpriteID.PRAYER_AUGURY, SpriteID.PRAYER_AUGURY_DISABLED, 27),
	;

	private final Prayer prayer;
	private final int available;
	private final int unavailable;
	private final int scriptIndex;

	public boolean isUnlocked(Client client)
	{
		return true;
	}
}

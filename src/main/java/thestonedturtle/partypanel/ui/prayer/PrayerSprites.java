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
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarbitID;

@AllArgsConstructor
@Getter
public enum PrayerSprites
{
	THICK_SKIN(Prayer.THICK_SKIN, SpriteID.Prayeron.THICK_SKIN, SpriteID.Prayeroff.THICK_SKIN_DISABLED, 20803),
	BURST_OF_STRENGTH(Prayer.BURST_OF_STRENGTH, SpriteID.Prayeron.BURST_OF_STRENGTH, SpriteID.Prayeroff.BURST_OF_STRENGTH_DISABLED, 20804),
	CLARITY_OF_THOUGHT(Prayer.CLARITY_OF_THOUGHT, SpriteID.Prayeron.CLARITY_OF_THOUGHT, SpriteID.Prayeroff.CLARITY_OF_THOUGHT_DISABLED, 20805),
	SHARP_EYE(Prayer.SHARP_EYE, SpriteID.Prayeron.SHARP_EYE, SpriteID.Prayeroff.SHARP_EYE_DISABLED, 20821),
	MYSTIC_WILL(Prayer.MYSTIC_WILL, SpriteID.Prayeron.MYSTIC_WILL, SpriteID.Prayeroff.MYSTIC_WILL_DISABLED, 20822),

	ROCK_SKIN(Prayer.ROCK_SKIN, SpriteID.Prayeron.ROCK_SKIN, SpriteID.Prayeroff.ROCK_SKIN_DISABLED, 20806),
	SUPERHUMAN_STRENGTH(Prayer.SUPERHUMAN_STRENGTH, SpriteID.Prayeron.SUPERHUMAN_STRENGTH, SpriteID.Prayeroff.SUPERHUMAN_STRENGTH_DISABLED, 20807),
	IMPROVED_REFLEXES(Prayer.IMPROVED_REFLEXES, SpriteID.Prayeron.IMPROVED_REFLEXES, SpriteID.Prayeroff.IMPROVED_REFLEXES_DISABLED, 20808),
	RAPID_RESTORE(Prayer.RAPID_RESTORE, SpriteID.Prayeron.RAPID_RESTORE, SpriteID.Prayeroff.RAPID_RESTORE_DISABLED, 20809),
	RAPID_HEAL(Prayer.RAPID_HEAL, SpriteID.Prayeron.RAPID_HEAL, SpriteID.Prayeroff.RAPID_HEAL_DISABLED, 20810),

	PROTECT_ITEM(Prayer.PROTECT_ITEM, SpriteID.Prayeron.PROTECT_ITEM, SpriteID.Prayeroff.PROTECT_ITEM_DISABLED, 20811),
	HAWK_EYE(Prayer.HAWK_EYE, SpriteID.Prayeron.HAWK_EYE, SpriteID.Prayeroff.HAWK_EYE_DISABLED, 20823),
	MYSTIC_LORE(Prayer.MYSTIC_LORE, SpriteID.Prayeron.MYSTIC_LORE, SpriteID.Prayeroff.MYSTIC_LORE_DISABLED, 20824),
	STEEL_SKIN(Prayer.STEEL_SKIN, SpriteID.Prayeron.STEEL_SKIN, SpriteID.Prayeroff.STEEL_SKIN_DISABLED, 20812),
	ULTIMATE_STRENGTH(Prayer.ULTIMATE_STRENGTH, SpriteID.Prayeron.ULTIMATE_STRENGTH, SpriteID.Prayeroff.ULTIMATE_STRENGTH_DISABLED, 20813),

	INCREDIBLE_REFLEXES(Prayer.INCREDIBLE_REFLEXES, SpriteID.Prayeron.INCREDIBLE_REFLEXES, SpriteID.Prayeroff.INCREDIBLE_REFLEXES_DISABLED, 20814),
	PROTECT_FROM_MAGIC(Prayer.PROTECT_FROM_MAGIC, SpriteID.Prayeron.PROTECT_FROM_MAGIC, SpriteID.Prayeroff.PROTECT_FROM_MAGIC_DISABLED, 20815),
	PROTECT_FROM_MISSILES(Prayer.PROTECT_FROM_MISSILES, SpriteID.Prayeron.PROTECT_FROM_MISSILES, SpriteID.Prayeroff.PROTECT_FROM_MISSILES_DISABLED, 20816),
	PROTECT_FROM_MELEE(Prayer.PROTECT_FROM_MELEE, SpriteID.Prayeron.PROTECT_FROM_MELEE, SpriteID.Prayeroff.PROTECT_FROM_MELEE_DISABLED, 20817),
	EAGLE_EYE(Prayer.EAGLE_EYE, SpriteID.Prayeron.EAGLE_EYE, SpriteID.Prayeroff.EAGLE_EYE_DISABLED, 20825)
			{
				@Override
				public boolean isUnlocked(Client client)
				{
					return !DEADEYE.isUnlocked(client);
				}
			},
	DEADEYE(Prayer.DEADEYE, SpriteID.Prayeron.DEADEYE, SpriteID.Prayeroff.DEADEYE_DISABLED, 25939)
			{
				@Override
				public boolean isUnlocked(Client client)
				{

					boolean inLms = client.getVarbitValue(VarbitID.BR_INGAME) != 0;
					boolean deadeye = client.getVarbitValue(VarbitID.PRAYER_DEADEYE_UNLOCKED) != 0;
					return deadeye && !inLms;
				}
			},

	MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT, SpriteID.Prayeron.MYSTIC_MIGHT, SpriteID.Prayeroff.MYSTIC_MIGHT_DISABLED, 20826)
			{
				@Override
				public boolean isUnlocked(Client client)
				{
					return !MYSTIC_VIGOUR.isUnlocked(client);
				}
			},
	MYSTIC_VIGOUR(Prayer.MYSTIC_VIGOUR, SpriteID.Prayeron.MYSTIC_VIGOUR, SpriteID.Prayeroff.MYSTIC_VIGOUR_DISABLED, 27034)
			{
				@Override
				public boolean isUnlocked(Client client)
				{
					boolean inLms = client.getVarbitValue(VarbitID.BR_INGAME) != 0;
					boolean vigour = client.getVarbitValue(VarbitID.PRAYER_MYSTIC_VIGOUR_UNLOCKED) != 0;
					return vigour && !inLms;
				}
			},
	RETRIBUTION(Prayer.RETRIBUTION, SpriteID.Prayeron.RETRIBUTION, SpriteID.Prayeroff.RETRIBUTION_DISABLED, 20818),
	REDEMPTION(Prayer.REDEMPTION, SpriteID.Prayeron.REDEMPTION, SpriteID.Prayeroff.REDEMPTION_DISABLED, 20819),
	SMITE(Prayer.SMITE, SpriteID.Prayeron.SMITE, SpriteID.Prayeroff.SMITE_DISABLED, 20820),
	PRESERVE(Prayer.PRESERVE, SpriteID.Prayeron.PRESERVE, SpriteID.Prayeroff.PRESERVE_DISABLED, 20831),

	CHIVALRY(Prayer.CHIVALRY, SpriteID.Prayeron.CHIVALRY, SpriteID.Prayeroff.CHIVALRY_DISABLED, 20828),
	PIETY(Prayer.PIETY, SpriteID.Prayeron.PIETY, SpriteID.Prayeroff.PIETY_DISABLED, 20829)
			{
				@Override
				public boolean isUnlocked(Client client)
				{
					return !ZEAL.isUnlocked(client);
				}
			},
	ZEAL(Prayer.ZEAL, SpriteID.Prayeron.ZEAL, SpriteID.Prayeroff.ZEAL_DISABLED, 22350)
			{
				@Override
				public boolean isUnlocked(Client client)
				{
					return client.getVarbitValue(VarbitID.PRAYER_ZEAL_UNLOCKED) == 0;
				}
			},
	RIGOUR(Prayer.RIGOUR, SpriteID.Prayeron.RIGOUR, SpriteID.Prayeroff.RIGOUR_DISABLED, 20827),
	AUGURY(Prayer.AUGURY, SpriteID.Prayeron.AUGURY, SpriteID.Prayeroff.AUGURY_DISABLED, 20830),
	;

	private final Prayer prayer;
	private final int available;
	private final int unavailable;
	// Technically each prayer uses an item ID to run against the `PRAYER_ISAVAILABLE` script
	// However, Jagex can repurpose these IDs for discontinued holiday items so using the ItemID gameval enum would be confusing
	private final int itemID;

	public boolean isUnlocked(Client client)
	{
		return true;
	}
}

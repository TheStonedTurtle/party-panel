/*
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
package thestonedturtle.partypanel.data.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.party.messages.PartyMemberMessage;
import thestonedturtle.partypanel.data.PartyPlayer;

// Used for updating stuff that is just a single integer value and doesn't fit into the other classes
@Value
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class PartyMiscChange extends PartyMemberMessage implements PartyProcess
{
	PartyMisc type;
	int value;

	public enum PartyMisc {
		SPECIAL,
		RUN,
		COMBAT,
		TOTAL;
	}

	@Override
	public void process(PartyPlayer p)
	{
		switch (type)
		{
			case SPECIAL:
				p.getStats().setSpecialPercent(value);
				break;
			case COMBAT:
				p.getStats().setCombatLevel(value);
				break;
			case TOTAL:
				p.getStats().setTotalLevel(value);
				break;
			case RUN:
				p.getStats().setRunEnergy(value);
				break;
			default:
				log.warn("Unhandled misc change type for event: {}", this);
		}
	}
}

/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.targethandlers;

import org.l2j.gameserver.mobius.gameserver.handler.ITargetTypeHandler;
import org.l2j.gameserver.mobius.gameserver.model.L2Object;
import org.l2j.gameserver.mobius.gameserver.model.L2Party;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;
import org.l2j.gameserver.mobius.gameserver.model.skills.targets.TargetType;

/**
 * Something like target self, but party. Used in aura skills.
 * @author Nik
 */
public class MyParty implements ITargetTypeHandler
{
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.MY_PARTY;
	}
	
	@Override
	public L2Object getTarget(L2Character activeChar, L2Object selectedTarget, Skill skill, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		if ((selectedTarget != null) && selectedTarget.isPlayer() && (selectedTarget != activeChar))
		{
			final L2Party party = activeChar.getParty();
			final L2Party targetParty = selectedTarget.getActingPlayer().getParty();
			if ((party != null) && (targetParty != null) && (party.getLeaderObjectId() == targetParty.getLeaderObjectId()))
			{
				return selectedTarget;
			}
		}
		
		return activeChar;
	}
}
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
package org.l2j.gameserver.model.stats.functions;

import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.conditions.Condition;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.stats.Stats;

/**
 * Returns the initial value divided the function value, if the condition are met.
 *
 * @author Zoey76
 */
public class FuncDiv extends AbstractFunction {
    public FuncDiv(Stats stat, int order, Object owner, double value, Condition applayCond) {
        super(stat, order, owner, value, applayCond);
    }

    @Override
    public double calc(L2Character effector, L2Character effected, Skill skill, double initVal) {
        if ((getApplayCond() == null) || getApplayCond().test(effector, effected, skill)) {
            try {
                return initVal / getValue();
            } catch (Exception e) {
                LOG.warn(FuncDiv.class.getSimpleName() + ": Division by zero: " + getValue() + "!");
            }
        }
        return initVal;
    }
}

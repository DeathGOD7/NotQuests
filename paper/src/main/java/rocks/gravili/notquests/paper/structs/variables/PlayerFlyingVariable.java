/*
 * NotQuests - A Questing plugin for Minecraft Servers
 * Copyright (C) 2022 Alessio Gravili
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package rocks.gravili.notquests.paper.structs.variables;

import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.structs.QuestPlayer;

import java.util.List;

public class PlayerFlyingVariable extends Variable<Boolean> {
    public PlayerFlyingVariable(NotQuests main) {
        super(main);
        setCanSetValue(true);
    }

    @Override
    public Boolean getValue(QuestPlayer questPlayer, Object... objects) {
        if (questPlayer != null) {
            return questPlayer.getPlayer().isFlying();
        } else {
            return false;
        }
    }

    @Override
    public boolean setValueInternally(Boolean newValue, QuestPlayer questPlayer, Object... objects) {
        if (questPlayer != null) {
            questPlayer.getPlayer().setFlying(newValue);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public List<String> getPossibleValues(QuestPlayer questPlayer, Object... objects) {
        return null;
    }

    @Override
    public String getPlural() {
        return "Flying";
    }

    @Override
    public String getSingular() {
        return "Flying";
    }
}

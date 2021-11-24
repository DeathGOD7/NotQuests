/*
 * NotQuests - A Questing plugin for Minecraft Servers
 * Copyright (C) 2021 Alessio Gravili
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

package rocks.gravili.notquests.Structs.Objectives;


import org.bukkit.entity.Player;
import rocks.gravili.notquests.NotQuests;
import rocks.gravili.notquests.Structs.Quest;

public class TriggerCommandObjective extends Objective {

    private final NotQuests main;
    private final String triggerName;


    public TriggerCommandObjective(NotQuests main, final Quest quest, final int objectiveID, String triggerName, int amountToTrigger) {
        super(main, quest, objectiveID, amountToTrigger);
        this.triggerName = triggerName;
        this.main = main;
    }

    public TriggerCommandObjective(NotQuests main, Quest quest, int objectiveNumber, int progressNeeded) {
        super(main, quest, objectiveNumber, progressNeeded);
        final String questName = quest.getQuestName();

        this.main = main;
        triggerName = main.getDataManager().getQuestsData().getString("quests." + questName + ".objectives." + objectiveNumber + ".specifics.triggerName");

    }

    @Override
    public String getObjectiveTaskDescription(final String eventualColor, final Player player) {
        return main.getLanguageManager().getString("chat.objectives.taskDescription.triggerCommand.base", player)
                .replaceAll("%EVENTUALCOLOR%", eventualColor)
                .replaceAll("%TRIGGERNAME%", "" + getTriggerName());
    }

    @Override
    public void save() {
        main.getDataManager().getQuestsData().set("quests." + getQuest().getQuestName() + ".objectives." + getObjectiveID() + ".specifics.triggerName", getTriggerName());
    }

    public final String getTriggerName() {
        return triggerName;
    }


}
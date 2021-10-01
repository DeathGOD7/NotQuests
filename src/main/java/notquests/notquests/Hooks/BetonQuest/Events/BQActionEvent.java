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

package notquests.notquests.Hooks.BetonQuest.Events;

import notquests.notquests.NotQuests;
import notquests.notquests.Structs.ActiveQuest;
import notquests.notquests.Structs.Quest;
import notquests.notquests.Structs.QuestPlayer;
import notquests.notquests.Structs.Triggers.Action;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class BQActionEvent extends QuestEvent {

    private final NotQuests main;
    private Action action;
    private Quest quest;

    /**
     * Creates new instance of the event. The event should parse instruction
     * string without doing anything else. If anything goes wrong, throw
     * {@link InstructionParseException} with error message describing the
     * problem.
     *
     * @param instruction the Instruction object representing this event; you need to
     *                    extract all required data from it and throw
     *                    {@link InstructionParseException} if there is anything wrong
     * @throws InstructionParseException when the is an error in the syntax or argument parsing
     */
    public BQActionEvent(Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        this.main = NotQuests.getInstance();

        final String actionName = instruction.getPart(1);

        String questName = "";

        try {
            questName = instruction.getPart(2);
        } catch (InstructionParseException ignored) {

        }

        if (!questName.isBlank()) {
            boolean foundQuest = false;
            for (final Quest quest : main.getQuestManager().getAllQuests()) {
                if (quest.getQuestName().equalsIgnoreCase(questName)) {
                    this.quest = quest;
                    foundQuest = true;
                }
            }
            if (!foundQuest) {
                throw new InstructionParseException("NotQuests Quest with the name '" + questName + "' does not exist - even though you have specified it. Specifying a quest name is not necessary.");
            }
        }


        boolean foundAction = false;
        for (final Action action : main.getQuestManager().getAllActions()) {
            if (action.getActionName().equalsIgnoreCase(actionName)) {
                this.action = action;
                foundAction = true;
                break;
            }
        }

        if (!foundAction) {
            throw new InstructionParseException("NotQuests Action with the name '" + actionName + "' does not exist.");
        }

    }

    @Override
    protected Void execute(String playerID) throws QuestRuntimeException {

        final Player player = PlayerConverter.getPlayer(playerID);


        //execute action here
        if (player != null && action != null) {
            if (quest != null) {
                final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(player.getUniqueId());
                if (questPlayer != null) {
                    if (questPlayer.getActiveQuests().size() > 0) {
                        for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                            if (activeQuest.getQuest().getQuestName().equalsIgnoreCase(this.quest.getQuestName())) {
                                action.execute(player, activeQuest);
                            }
                        }
                    }
                }
            } else {
                action.execute(player);
            }

        } else {
            main.getLogManager().log(Level.WARNING, "Error executing action (triggered by BetonQuests) - action or player was not found.");
            throw new QuestRuntimeException("Error executing NotQuests action (triggered by BetonQuests) - action or player was not found.");
        }

        return null;
    }
}

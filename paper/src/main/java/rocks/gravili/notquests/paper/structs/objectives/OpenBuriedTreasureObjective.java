/*
 * NotQuests - A Questing plugin for Minecraft Servers
 * Copyright (C) 2021-2022 Alessio Gravili
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

package rocks.gravili.notquests.paper.structs.objectives;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.structs.ActiveObjective;
import rocks.gravili.notquests.paper.structs.QuestPlayer;


public class OpenBuriedTreasureObjective extends Objective {



    public OpenBuriedTreasureObjective(NotQuests main) {
        super(main);
    }

    public static void handleCommands(NotQuests main, PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> addObjectiveBuilder) {
        manager.command(addObjectiveBuilder
                .argument(IntegerArgument.<CommandSender>newBuilder("amount").withMin(1), ArgumentDescription.of("Amount of buried treasures to open."))
                .handler((context) -> {
                    final int amount = context.get("amount");


                    OpenBuriedTreasureObjective openBuriedTreasureObjective = new OpenBuriedTreasureObjective(main);
                    openBuriedTreasureObjective.setProgressNeeded(amount);

                    main.getObjectiveManager().addObjective(openBuriedTreasureObjective, context);
                }));
    }


    @Override
    public String getObjectiveTaskDescription(final QuestPlayer questPlayer) {
        return main.getLanguageManager().getString("chat.objectives.taskDescription.openBuriedTreasure.base", questPlayer);
    }

    @Override
    public void save(FileConfiguration configuration, String initialPath) {
    }

    @Override
    public void load(FileConfiguration configuration, String initialPath) {
    }


    @Override
    public void onObjectiveUnlock(final ActiveObjective activeObjective, final boolean unlockedDuringPluginStartupQuestLoadingProcess) {
    }
    @Override
    public void onObjectiveCompleteOrLock(final ActiveObjective activeObjective, final boolean lockedOrCompletedDuringPluginStartupQuestLoadingProcess, final boolean completed) {
    }


}

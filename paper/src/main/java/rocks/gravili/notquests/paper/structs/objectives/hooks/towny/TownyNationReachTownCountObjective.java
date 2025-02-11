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

package rocks.gravili.notquests.paper.structs.objectives.hooks.towny;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.structs.ActiveObjective;
import rocks.gravili.notquests.paper.structs.QuestPlayer;
import rocks.gravili.notquests.paper.structs.objectives.Objective;

import java.util.Map;

public class TownyNationReachTownCountObjective extends Objective {

    private boolean countPreviousTowns = true;

    public TownyNationReachTownCountObjective(NotQuests main) {
        super(main);
    }

    public static void handleCommands(NotQuests main, PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> addObjectiveBuilder) {
        if (!main.getIntegrationsManager().isTownyEnabled()) {
            return;
        }

        manager.command(addObjectiveBuilder
                .argument(IntegerArgument.<CommandSender>newBuilder("amount").withMin(1), ArgumentDescription.of("Minimum amounts of towns"))
                .flag(
                        manager.flagBuilder("doNotCountPreviousTowns")
                                .withDescription(ArgumentDescription.of("Makes it so only additional towns from the time of unlocking this Objective will count (and previous/existing counts will not count, so it starts from zero)"))
                )
                .handler((context) -> {
                    int amount = context.get("amount");
                    final boolean countPreviousTowns = !context.flags().isPresent("doNotCountPreviousTowns");


                    TownyNationReachTownCountObjective townyNationReachTownCountObjective = new TownyNationReachTownCountObjective(main);
                    townyNationReachTownCountObjective.setProgressNeeded(amount);
                    townyNationReachTownCountObjective.setCountPreviousTowns(countPreviousTowns);

                    main.getObjectiveManager().addObjective(townyNationReachTownCountObjective, context);
                }));
    }

    public final boolean isCountPreviousTowns() {
        return countPreviousTowns;
    }

    public void setCountPreviousTowns(final boolean countPreviousTowns) {
        this.countPreviousTowns = countPreviousTowns;
    }

    public final long getAmountOfTownsToReach() {
        return getProgressNeeded();
    }

    @Override
    public String getObjectiveTaskDescription(final QuestPlayer questPlayer) {
        return main.getLanguageManager().getString("chat.objectives.taskDescription.townyNationReachTownCount.base", questPlayer, Map.of(
                "%AMOUNT%", "" + getAmountOfTownsToReach()
        ));
    }

    @Override
    public void save(FileConfiguration configuration, String initialPath) {
        configuration.set(initialPath + ".specifics.countPreviousTowns", isCountPreviousTowns());
    }

    @Override
    public void load(FileConfiguration configuration, String initialPath) {
        countPreviousTowns = configuration.getBoolean(initialPath + ".specifics.countPreviousTowns");
    }

    @Override
    public void onObjectiveUnlock(final ActiveObjective activeObjective, final boolean unlockedDuringPluginStartupQuestLoadingProcess) {
        if(activeObjective.getCurrentProgress() != 0){
            return;
        }

        if (!main.getIntegrationsManager().isTownyEnabled() || !isCountPreviousTowns()) {
            return;
        }

        final Player player = activeObjective.getQuestPlayer().getPlayer();
        if (player == null) {
            return;
        }

        Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
        if (resident == null) {
            return;
        }

        Town town = resident.getTownOrNull();
        if (town == null) {
            return;
        }

        Nation nation = town.getNationOrNull();
        if (nation == null) {
            return;
        }

        activeObjective.addProgress(nation.getNumTowns());
    }

    @Override
    public void onObjectiveCompleteOrLock(final ActiveObjective activeObjective, final boolean lockedOrCompletedDuringPluginStartupQuestLoadingProcess, final boolean completed) {
    }

}

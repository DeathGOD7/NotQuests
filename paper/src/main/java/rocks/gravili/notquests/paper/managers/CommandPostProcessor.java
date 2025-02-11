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

package rocks.gravili.notquests.paper.managers;

import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.services.types.ConsumerService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import rocks.gravili.notquests.paper.NotQuests;

public class CommandPostProcessor<C> implements CommandPostprocessor<C> {
    private final NotQuests main;

    public CommandPostProcessor(final NotQuests main){
        this.main = main;
    }

    @Override
    public void accept(@NotNull final CommandPostprocessingContext<C> context) {
        if(main.getDataManager().isDisabled() && !(context.getCommand().getArguments().size() >= 3 && (context.getCommand().getArguments().get(2).getName().equalsIgnoreCase("enablePluginAndSaving") || context.getCommand().getArguments().get(2).getName().equalsIgnoreCase("disablePluginAndSaving") )) ){
            if(context.getCommandContext().getSender() instanceof final CommandSender commandSender){
                main.getDataManager().sendPluginDisabledMessage(commandSender);
            }
            ConsumerService.interrupt();
        }
    }

}
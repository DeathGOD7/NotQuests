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

package rocks.gravili.notquests.commands;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import rocks.gravili.notquests.NotQuests;
import rocks.gravili.notquests.conversation.ConversationPlayer;
import rocks.gravili.notquests.structs.ActiveObjective;
import rocks.gravili.notquests.structs.ActiveQuest;
import rocks.gravili.notquests.structs.Quest;
import rocks.gravili.notquests.structs.QuestPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandNotQuests implements CommandExecutor, TabCompleter {
    private final NotQuests main;

    private final List<String> completions = new ArrayList<>();

    private final Component firstLevelCommands;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CommandNotQuests(NotQuests main) {
        this.main = main;

        firstLevelCommands = Component.text("NotQuests Player Commands:", NamedTextColor.BLUE, TextDecoration.BOLD)
                .append(Component.newline())
                .append(miniMessage.parse("<YELLOW>/nquests <GOLD>take <DARK_AQUA>[Quest Name]").clickEvent(ClickEvent.suggestCommand("/nquests take ")).hoverEvent(HoverEvent.showText(Component.text("Takes/Starts a Quest", NamedTextColor.GREEN))))
                .append(Component.newline())
                .append(miniMessage.parse("<YELLOW>/nquests <GOLD>abort <DARK_AQUA>[Quest Name]").clickEvent(ClickEvent.suggestCommand("/nquests abort ")).hoverEvent(HoverEvent.showText(Component.text("Fails a Quest", NamedTextColor.GREEN))))
                .append(Component.newline())
                .append(miniMessage.parse("<YELLOW>/nquests <GOLD>preview <DARK_AQUA>[Quest Name]").clickEvent(ClickEvent.suggestCommand("/nquests preview ")).hoverEvent(HoverEvent.showText(Component.text("Shows more information about a Quest", NamedTextColor.GREEN))))
                .append(Component.newline())
                .append(miniMessage.parse("<YELLOW>/nquests <GOLD>activeQuests").clickEvent(ClickEvent.runCommand("/nquests activeQuests")).hoverEvent(HoverEvent.showText(Component.text("Shows all your active Quests", NamedTextColor.GREEN))))
                .append(Component.newline())
                .append(miniMessage.parse("<YELLOW>/nquests <GOLD>progress <DARK_AQUA>[Quest Name]").clickEvent(ClickEvent.suggestCommand("/nquests progress ")).hoverEvent(HoverEvent.showText(Component.text("Shows the progress of an active Quest", NamedTextColor.GREEN))))
                .append(Component.newline())
                .append(miniMessage.parse("<YELLOW>/nquests <GOLD>questPoints").clickEvent(ClickEvent.runCommand("/nquests questPoints")).hoverEvent(HoverEvent.showText(Component.text("Shows how many Quest Points you have", NamedTextColor.GREEN))))
                .append(Component.newline()

                );


    }

    public final String convert(final String old) { //Converts minimessage to legacy
        return main.getUtilManager().miniMessageToLegacyWithSpigotRGB(old);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final Audience audience = main.adventure().sender(sender);
        if (sender instanceof final Player player) {
            final boolean guiEnabled = main.getConfiguration().isUserCommandsUseGUI();
            if (sender.hasPermission("notquests.use")) {
                final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer((player.getUniqueId()));
                if (args.length == 0) {
                    if (guiEnabled) {
                        String[] guiSetup = {
                                "zxxxxxxxx",
                                "x0123456x",
                                "x789abcdx",
                                "xefghijkx",
                                "xlmnopqrx",
                                "xxxxxxxxx"
                        };
                        InventoryGui gui = new InventoryGui(main, player, convert(main.getLanguageManager().getString("gui.main.title", player)), guiSetup);
                        gui.setFiller(new ItemStack(Material.AIR, 1)); // fill the empty slots with this

                        gui.addElement(new StaticGuiElement('8',
                                new ItemStack(Material.CHEST),
                                0,
                                click -> {
                                    player.chat("/notquests take");

                                    return true; // returning true will cancel the click event and stop taking the item
                                },
                                convert(main.getLanguageManager().getString("gui.main.button.takequest.text", player))

                        ));
                        gui.addElement(new StaticGuiElement('a',
                                new ItemStack(Material.REDSTONE_BLOCK),
                                0,
                                click -> {
                                    player.chat("/notquests abort");
                                    return true; // returning true will cancel the click event and stop taking the item
                                },
                                convert(main.getLanguageManager().getString("gui.main.button.abortquest.text", player))
                        ));
                        gui.addElement(new StaticGuiElement('c',
                                new ItemStack(Material.SPYGLASS),
                                0,
                                click -> {
                                    player.chat("/notquests preview");
                                    return true; // returning true will cancel the click event and stop taking the item
                                },
                                convert(main.getLanguageManager().getString("gui.main.button.previewquest.text", player))
                        ));

                        gui.addElement(new StaticGuiElement('o',
                                new ItemStack(Material.LADDER),
                                0,
                                click -> {
                                    player.chat("/notquests activeQuests");
                                    return true; // returning true will cancel the click event and stop taking the item
                                },
                                convert(main.getLanguageManager().getString("gui.main.button.activequests.text", player))
                        ));
                        if (questPlayer != null) {
                            gui.addElement(new StaticGuiElement('z',
                                    new ItemStack(Material.SUNFLOWER),
                                    0,
                                    click -> {
                                        return true; // returning true will cancel the click event and stop taking the item
                                    },
                                    convert(main.getLanguageManager().getString("gui.main.button.questpoints.text", player, questPlayer))
                            ));
                        } else {
                            gui.addElement(new StaticGuiElement('z',
                                    new ItemStack(Material.SUNFLOWER),
                                    0,
                                    click -> {
                                        return true; // returning true will cancel the click event and stop taking the item
                                    },
                                    convert(main.getLanguageManager().getString("gui.main.button.questpoints.text", player).replace("%QUESTPOINTS%", "??"))

                            ));
                        }

                        gui.show(player);
                    } else {
                        audience.sendMessage(Component.empty());
                        main.adventure().sender(sender).sendMessage(firstLevelCommands);
                    }
                } else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("activequests")) {
                        if (questPlayer != null) {
                            if (guiEnabled) {
                                String[] guiSetup = {
                                        "zxxxxxxxx",
                                        "xgggggggx",
                                        "xgggggggx",
                                        "xgggggggx",
                                        "xgggggggx",
                                        "pxxxxxxxn"
                                };
                                InventoryGui gui = new InventoryGui(main, player, convert(main.getLanguageManager().getString("gui.activeQuests.title", player)), guiSetup);
                                gui.setFiller(new ItemStack(Material.AIR, 1)); // fill the empty slots with this


                                int count = 0;
                                GuiElementGroup group = new GuiElementGroup('g');

                                for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {

                                    final ItemStack materialToUse;
                                    if (!activeQuest.isCompleted()) {
                                        materialToUse = activeQuest.getQuest().getTakeItem();
                                    } else {
                                        materialToUse = new ItemStack(Material.EMERALD_BLOCK);
                                    }

                                    if (main.getConfiguration().showQuestItemAmount) {
                                        count++;
                                    }


                                    group.addElement(new StaticGuiElement('e',
                                            materialToUse,
                                            count, // Display a number as the item count
                                            click -> {
                                                player.chat("/notquests progress " + activeQuest.getQuest().getQuestName());
                                                //click.getEvent().getWhoClicked().sendMessage(ChatColor.RED + "I am Redstone!");
                                                return true; // returning true will cancel the click event and stop taking the item

                                            },
                                            convert(main.getLanguageManager().getString("gui.activeQuests.button.activeQuestButton.text", player, activeQuest))


                                    ));

                                }


                                gui.addElement(group);

                                // Previous page
                                gui.addElement(new GuiPageElement('p', new ItemStack(Material.SPECTRAL_ARROW), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)"));

                                // Next page
                                gui.addElement(new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)"));


                                gui.show(player);
                            } else {
                                audience.sendMessage(miniMessage.parse(
                                        main.getLanguageManager().getString("chat.active-quests-label", player)
                                ));
                                int counter = 1;
                                for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                                    audience.sendMessage(miniMessage.parse(
                                            "<GREEN>" + counter + ". <YELLOW>" + activeQuest.getQuest().getQuestFinalName()
                                    ));
                                    counter += 1;
                                }
                            }

                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    main.getLanguageManager().getString("chat.no-quests-accepted", player)
                            ));
                        }
                    } else if (args[0].equalsIgnoreCase("take")) {
                        if (guiEnabled) {
                            String[] guiSetup = {
                                    "zxxxxxxxx",
                                    "xgggggggx",
                                    "xgggggggx",
                                    "xgggggggx",
                                    "xgggggggx",
                                    "pxxxxxxxn"
                            };
                            InventoryGui gui = new InventoryGui(main, player, convert(main.getLanguageManager().getString("gui.takeQuestChoose.title", player)), guiSetup);
                            gui.setFiller(new ItemStack(Material.AIR, 1)); // fill the empty slots with this

                            int count = 0;
                            GuiElementGroup group = new GuiElementGroup('g');

                            for (final Quest quest : main.getQuestManager().getAllQuests()) {
                                if (quest.isTakeEnabled()) {
                                    final ItemStack materialToUse = quest.getTakeItem();

                                    if (main.getConfiguration().showQuestItemAmount) {
                                        count++;
                                    }

                                    String displayName = convert(quest.getQuestFinalName());

                                    displayName = main.getLanguageManager().getString("gui.takeQuestChoose.button.questPreview.questNamePrefix", player, quest) + displayName;

                                    if (questPlayer != null && questPlayer.hasAcceptedQuest(quest)) {
                                        displayName += main.getLanguageManager().getString("gui.takeQuestChoose.button.questPreview.acceptedSuffix", player, quest);
                                    }
                                    String description = "";
                                    if (!quest.getQuestDescription().isBlank()) {

                                        description = main.getLanguageManager().getString("gui.takeQuestChoose.button.questPreview.questDescriptionPrefix", player, quest)
                                                + quest.getQuestDescription(main.getConfiguration().guiQuestDescriptionMaxLineLength
                                        );
                                    }

                                    group.addElement(new StaticGuiElement('e',
                                            materialToUse,
                                            count, // Display a number as the item count
                                            click -> {
                                                player.chat("/notquests preview " + quest.getQuestName());
                                                //click.getEvent().getWhoClicked().sendMessage(ChatColor.RED + "I am Redstone!");
                                                return true; // returning true will cancel the click event and stop taking the item

                                            },
                                            convert(displayName),
                                            convert(description),
                                            convert(main.getLanguageManager().getString("gui.takeQuestChoose.button.questPreview.bottomText", player))


                                    ));

                                }


                            }


                            gui.addElement(group);

                            // Previous page
                            gui.addElement(new GuiPageElement('p', new ItemStack(Material.SPECTRAL_ARROW), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)"));

                            // Next page
                            gui.addElement(new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)"));


                            gui.show(player);
                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    "<RED>Please specify the <AQUA>name of the quest</AQUA> you wish to take.\n"
                                            + "<YELLOW>/nquests <GOLD>take <DARK_AQUA>[Quest Name]"
                            ));

                        }

                    } else if (args[0].equalsIgnoreCase("abort")) {
                        if (questPlayer != null) {
                            if (guiEnabled) {
                                String[] guiSetup = {
                                        "zxxxxxxxx",
                                        "xgggggggx",
                                        "xgggggggx",
                                        "xgggggggx",
                                        "xgggggggx",
                                        "pxxxxxxxn"
                                };
                                InventoryGui gui = new InventoryGui(main, player, convert(main.getLanguageManager().getString("gui.abortQuestChoose.title", player)), guiSetup);
                                gui.setFiller(new ItemStack(Material.AIR, 1)); // fill the empty slots with this

                                GuiElementGroup group = new GuiElementGroup('g');


                                int count = 0;

                                for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {

                                    final ItemStack materialToUse;
                                    if (!activeQuest.isCompleted()) {
                                        materialToUse = activeQuest.getQuest().getTakeItem();
                                    } else {
                                        materialToUse = new ItemStack(Material.EMERALD_BLOCK);
                                    }


                                    if (main.getConfiguration().showQuestItemAmount) {
                                        count++;
                                    }

                                    group.addElement(new StaticGuiElement('e',
                                            materialToUse,
                                            count, // Display a number as the item count
                                            click -> {
                                                player.chat("/notquests abort " + activeQuest.getQuest().getQuestName());
                                                //click.getEvent().getWhoClicked().sendMessage(ChatColor.RED + "I am Redstone!");
                                                return true; // returning true will cancel the click event and stop taking the item

                                            },
                                            convert(main.getLanguageManager().getString("gui.abortQuestChoose.button.abortQuestPreview.text", player, activeQuest))


                                    ));

                                }


                                gui.addElement(group);

                                // Previous page
                                gui.addElement(new GuiPageElement('p', new ItemStack(Material.SPECTRAL_ARROW), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)"));

                                // Next page
                                gui.addElement(new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)"));


                                gui.show(player);
                            } else {
                                audience.sendMessage(miniMessage.parse(
                                        "<RED>Please specify the <AQUA>name of the quest</AQUA> you wish to abort (fail).\n"
                                                + "<YELLOW>/nquests <GOLD>abort <DARK_AQUA>[Quest Name]"
                                ));
                            }
                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    main.getLanguageManager().getString("chat.no-quests-accepted", player)
                            ));
                        }


                    } else if (args[0].equalsIgnoreCase("preview")) {
                        if (guiEnabled) {
                            String[] guiSetup = {
                                    "zxxxxxxxx",
                                    "xgggggggx",
                                    "xgggggggx",
                                    "xgggggggx",
                                    "xgggggggx",
                                    "pxxxxxxxn"
                            };
                            InventoryGui gui = new InventoryGui(main, player, convert(main.getLanguageManager().getString("gui.previewQuestChoose.title", player)), guiSetup);
                            gui.setFiller(new ItemStack(Material.AIR, 1)); // fill the empty slots with this

                            int count = 0;
                            GuiElementGroup group = new GuiElementGroup('g');

                            for (final Quest quest : main.getQuestManager().getAllQuests()) {
                                if (quest.isTakeEnabled()) {
                                    final ItemStack materialToUse = quest.getTakeItem();


                                    if (main.getConfiguration().showQuestItemAmount) {
                                        count++;
                                    }
                                    String displayName = quest.getQuestFinalName();

                                    displayName = main.getLanguageManager().getString("gui.previewQuestChoose.button.questPreview.questNamePrefix", player) + displayName;

                                    if (questPlayer != null && questPlayer.hasAcceptedQuest(quest)) {
                                        displayName += main.getLanguageManager().getString("gui.previewQuestChoose.button.questPreview.acceptedSuffix", player);
                                    }
                                    String description = "";
                                    if (!quest.getQuestDescription().isBlank()) {
                                        description = main.getLanguageManager().getString("gui.previewQuestChoose.button.questPreview.questDescriptionPrefix", player)
                                                + quest.getQuestDescription(main.getConfiguration().guiQuestDescriptionMaxLineLength);
                                    }

                                    group.addElement(new StaticGuiElement('e',
                                            materialToUse,
                                            count, // Display a number as the item count
                                            click -> {
                                                player.chat("/notquests preview " + quest.getQuestName());
                                                return true; // returning true will cancel the click event and stop taking the item

                                            },
                                            convert(displayName),
                                            convert(description),
                                            convert(main.getLanguageManager().getString("gui.previewQuestChoose.button.questPreview.bottomText", player))

                                    ));

                                }

                            }


                            gui.addElement(group);

                            // Previous page
                            gui.addElement(new GuiPageElement('p', new ItemStack(Material.SPECTRAL_ARROW), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)"));

                            // Next page
                            gui.addElement(new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)"));


                            gui.show(player);
                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    "<RED>Please specify the <AQUA>name of the quest</AQUA> you wish to preview.\n"
                                            + "<YELLOW>/nquests <GOLD>preview <DARK_AQUA>[Quest Name]"
                            ));
                        }

                    } else if (args[0].equalsIgnoreCase("progress")) {
                        audience.sendMessage(miniMessage.parse(
                                "<RED>Please specify the <AQUA>name of the quest</AQUA> you wish to see your progress for.\n"
                                        + "<YELLOW>/nquests <GOLD>progress <DARK_AQUA>[Quest Name]"
                        ));
                    } else if (args[0].equalsIgnoreCase("questPoints")) {
                        if (questPlayer != null) {
                            audience.sendMessage(miniMessage.parse(
                                    main.getLanguageManager().getString("chat.questpoints.query", player, questPlayer)
                            ));
                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    main.getLanguageManager().getString("chat.questpoints.none", player)
                            ));
                        }
                    } else {
                        audience.sendMessage(miniMessage.parse(
                                main.getLanguageManager().getString("chat.wrong-command-usage", player)
                        ));
                    }
                } else if (args.length >= 2 && args[0].equalsIgnoreCase("continueConversation")) {
                    if (args[0].equalsIgnoreCase("continueConversation")) {
                        handleConversation(player, args);
                        return true;
                    }
                } else if (args.length == 2) {
                    //Accept Quest
                    if (args[0].equalsIgnoreCase("take")) {
                        final Quest quest = main.getQuestManager().getQuest(args[1]);
                        if (quest != null) {

                            if (!quest.isTakeEnabled() && !main.getQuestManager().isPlayerCloseToCitizenOrArmorstandWithQuest(player, quest)) {
                                audience.sendMessage(miniMessage.parse(
                                        main.getLanguageManager().getString("chat.take-disabled-accept", player, quest)
                                ));
                                return true;
                            }
                            final String result = main.getQuestPlayerManager().acceptQuest(player, quest, true, true);
                            if (!result.equals("accepted")) {
                                audience.sendMessage(miniMessage.parse(
                                        result
                                ));
                            } else {
                                audience.sendMessage(miniMessage.parse(
                                        main.getLanguageManager().getString("chat.quest-successfully-accepted", player, quest)
                                ));
                                if (!quest.getQuestDescription().isBlank()) {
                                    audience.sendMessage(miniMessage.parse(
                                            main.getLanguageManager().getString("chat.quest-description", player, quest)
                                    ));
                                } else {
                                    audience.sendMessage(miniMessage.parse(
                                            main.getLanguageManager().getString("chat.missing-quest-description", player)
                                    ));
                                }
                            }
                            return true;

                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    main.getLanguageManager().getString("chat.quest-does-not-exist", player).replace("%QUESTNAME%", args[1])
                            ));
                        }
                    } else if (args[0].equalsIgnoreCase("abort")) {

                        if (questPlayer != null && questPlayer.getActiveQuests().size() > 0) {
                            final String activeQuestName = args[1];
                            AtomicBoolean failedSuccessfully = new AtomicBoolean(false);
                            final ArrayList<ActiveQuest> questsToFail = new ArrayList<>();
                            for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                                if (activeQuest.getQuest().getQuestName().equalsIgnoreCase(activeQuestName)) {
                                    questsToFail.add(activeQuest);
                                }
                            }

                            if (questsToFail.isEmpty()) {
                                audience.sendMessage(miniMessage.parse(
                                        convert(main.getLanguageManager().getString("chat.quest-not-active-error", player).replace("%QUESTNAME%", activeQuestName))
                                ));
                                return true;
                            }

                            if (guiEnabled) {
                                String[] guiSetup = {
                                        "zxxxxxxxx",
                                        "x0123456x",
                                        "x789abcdx",
                                        "xefghijkx",
                                        "xlmnopqrx",
                                        "xxxxxxxxx"
                                };
                                InventoryGui gui = new InventoryGui(main, player, convert(main.getLanguageManager().getString("gui.abortQuest.title", player)), guiSetup);
                                gui.setFiller(new ItemStack(Material.AIR, 1)); // fill the empty slots with this

                                final ActiveQuest activeQuest = questsToFail.get(0);


                                gui.addElement(new StaticGuiElement('9',
                                        new ItemStack(Material.GREEN_WOOL),
                                        1, // Display a number as the item count
                                        click -> {
                                            questPlayer.failQuest(activeQuest);
                                            failedSuccessfully.set(true);
                                            questsToFail.clear();
                                            if (!failedSuccessfully.get()) {
                                                audience.sendMessage(miniMessage.parse(
                                                        main.getLanguageManager().getString("chat.quest-not-active-error", player, activeQuest)
                                                ));
                                            }
                                            audience.sendMessage(miniMessage.parse(
                                                    main.getLanguageManager().getString("chat.quest-aborted", player, activeQuest)
                                            ));

                                            //click.getEvent().getWhoClicked().sendMessage(ChatColor.RED + "I am Redstone!");
                                            gui.close();
                                            return true; // returning true will cancel the click event and stop taking the item

                                        },
                                        convert(main.getLanguageManager().getString("gui.abortQuest.button.confirmAbort.text", player, activeQuest))


                                ));
                                gui.addElement(new StaticGuiElement('b',
                                        new ItemStack(Material.RED_WOOL),
                                        1, // Display a number as the item count
                                        click -> {
                                            gui.close();
                                            //click.getEvent().getWhoClicked().sendMessage(ChatColor.RED + "I am Redstone!");
                                            return true; // returning true will cancel the click event and stop taking the item

                                        },
                                        convert(main.getLanguageManager().getString("gui.abortQuest.button.cancelAbort.text", player))
                                ));

                                gui.show(player);
                            } else {

                                for (final ActiveQuest activeQuest : questsToFail) {
                                    questPlayer.failQuest(activeQuest);
                                    failedSuccessfully.set(true);
                                    audience.sendMessage(miniMessage.parse(
                                            main.getLanguageManager().getString("chat.quest-aborted", player, activeQuest)
                                    ));

                                }
                                questsToFail.clear();
                                if (!failedSuccessfully.get()) {
                                    audience.sendMessage(miniMessage.parse(
                                            main.getLanguageManager().getString("chat.quest-not-active-error", player).replace("%QUESTNAME%", activeQuestName)
                                    ));
                                }
                            }

                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    main.getLanguageManager().getString("chat.no-quests-accepted", player)
                            ));
                        }


                    } else if (args[0].equalsIgnoreCase("preview")) {
                        Quest quest = main.getQuestManager().getQuest(args[1]);
                        if (quest != null) {

                            if (!quest.isTakeEnabled() && !main.getQuestManager().isPlayerCloseToCitizenOrArmorstandWithQuest(player, quest)) {
                                audience.sendMessage(miniMessage.parse(
                                        main.getLanguageManager().getString("chat.take-disabled-preview", player, quest)
                                ));
                                return true;
                            }
                            if (guiEnabled) {
                                String[] guiSetup = {
                                        "zxxxxxxxx",
                                        "x0123456x",
                                        "x789abcdx",
                                        "xefghijkx",
                                        "xlmnopqrx",
                                        "xxxxxxxxx"
                                };


                                InventoryGui gui = new InventoryGui(main, player, convert(main.getLanguageManager().getString("gui.previewQuest.title", player, quest, questPlayer)), guiSetup);
                                gui.setFiller(new ItemStack(Material.AIR, 1)); // fill the empty slots with this


                                if (main.getConfiguration().isGuiQuestPreviewDescription_enabled()) {
                                    String description = main.getLanguageManager().getString("gui.previewQuest.button.description.empty", player, questPlayer);
                                    if (!quest.getQuestDescription().isBlank()) {
                                        description = quest.getQuestDescription(main.getConfiguration().guiQuestDescriptionMaxLineLength);
                                    }
                                    gui.addElement(new StaticGuiElement(main.getConfiguration().getGuiQuestPreviewDescription_slot(),
                                            new ItemStack(Material.BOOKSHELF),
                                            1, // Display a number as the item count
                                            click -> {
                                                return true; // returning true will cancel the click event and stop taking the item
                                            },
                                            convert(main.getLanguageManager().getString("gui.previewQuest.button.description.text", player, questPlayer)
                                                    .replace("%QUESTDESCRIPTION%", description))


                                    ));
                                }


                                if (main.getConfiguration().isGuiQuestPreviewRewards_enabled()) {
                                    String rewards = main.getQuestManager().getQuestRewards(quest);
                                    if (rewards.isBlank()) {
                                        rewards = main.getLanguageManager().getString("gui.previewQuest.button.rewards.empty", player);
                                    }

                                    gui.addElement(new StaticGuiElement(main.getConfiguration().getGuiQuestPreviewRewards_slot(),
                                            new ItemStack(Material.EMERALD),
                                            1, // Display a number as the item count
                                            click -> {

                                                return true; // returning true will cancel the click event and stop taking the item

                                            },
                                            convert(main.getLanguageManager().getString("gui.previewQuest.button.rewards.text", player, quest, questPlayer)
                                                    .replace("%QUESTREWARDS%", rewards))


                                    ));
                                }


                                if (main.getConfiguration().isGuiQuestPreviewRequirements_enabled()) {
                                    String requirements = main.getQuestManager().getQuestRequirements(quest);
                                    if (requirements.isBlank()) {
                                        requirements = main.getLanguageManager().getString("gui.previewQuest.button.requirements.empty", player, questPlayer);
                                    }

                                    gui.addElement(new StaticGuiElement(main.getConfiguration().getGuiQuestPreviewRequirements_slot(),
                                            new ItemStack(Material.IRON_BARS),
                                            1, // Display a number as the item count
                                            click -> {

                                                return true; // returning true will cancel the click event and stop taking the item

                                            },
                                            convert(main.getLanguageManager().getString("gui.previewQuest.button.requirements.text", player, quest, questPlayer)
                                                    .replace("%QUESTREQUIREMENTS%", requirements))


                                    ));
                                }


                                gui.addElement(new StaticGuiElement('g',
                                        new ItemStack(Material.GREEN_WOOL),
                                        1, // Display a number as the item count
                                        click -> {
                                            player.chat("/notquests take " + quest.getQuestName());

                                            //click.getEvent().getWhoClicked().sendMessage(ChatColor.RED + "I am Redstone!");
                                            gui.close();
                                            return true; // returning true will cancel the click event and stop taking the item

                                        },
                                        convert(main.getLanguageManager().getString("gui.previewQuest.button.confirmTake.text", player, quest, questPlayer))

                                ));
                                gui.addElement(new StaticGuiElement('i',
                                        new ItemStack(Material.RED_WOOL),
                                        1, // Display a number as the item count
                                        click -> {
                                            gui.close();
                                            //click.getEvent().getWhoClicked().sendMessage(ChatColor.RED + "I am Redstone!");
                                            return true; // returning true will cancel the click event and stop taking the item

                                        },
                                        convert(main.getLanguageManager().getString("gui.previewQuest.button.cancelTake.text", player, quest, questPlayer))

                                ));


                                gui.show(player);
                            } else {
                                main.getQuestManager().sendSingleQuestPreview(player, quest);
                            }

                            return true;

                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    main.getLanguageManager().getString("chat.quest-does-not-exist", player, questPlayer).replace("%QUESTNAME%", args[1])
                            ));
                        }
                    } else if (args[0].equalsIgnoreCase("progress")) {
                        ActiveQuest requestedActiveQuest = null;
                        if (questPlayer != null) {
                            for (ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                                if (activeQuest.getQuest().getQuestName().equalsIgnoreCase(args[1])) {
                                    requestedActiveQuest = activeQuest;
                                }
                            }
                            if (requestedActiveQuest != null) {
                                if (guiEnabled) {
                                    String[] guiSetup = {
                                            "zxxxxxxxx",
                                            "xgggggggx",
                                            "xgggggggx",
                                            "xgggggggx",
                                            "xgggggggx",
                                            "pxxxxxxxn"
                                    };

                                    InventoryGui gui = new InventoryGui(main, player, convert(main.getLanguageManager().getString("gui.progress.title", player, requestedActiveQuest, questPlayer)), guiSetup);
                                    gui.setFiller(new ItemStack(Material.AIR, 1)); // fill the empty slots with this


                                    //int count = 0;
                                    GuiElementGroup group = new GuiElementGroup('g');

                                    for (final ActiveObjective activeObjective : requestedActiveQuest.getActiveObjectives()) {

                                        final Material materialToUse = Material.PAPER;

                                        //count++;
                                        int count = activeObjective.getObjectiveID();
                                        if (!main.getConfiguration().showObjectiveItemAmount) {
                                            count = 0;
                                        }
                                        if (activeObjective.isUnlocked()) {

                                            String descriptionToDisplay = main.getLanguageManager().getString("gui.progress.button.unlockedObjective.description-empty", player);
                                            if (!activeObjective.getObjective().getObjectiveDescription().isBlank()) {
                                                descriptionToDisplay = activeObjective.getObjective().getObjectiveDescription(main.getConfiguration().guiObjectiveDescriptionMaxLineLength);
                                            }

                                            group.addElement(new StaticGuiElement('e',
                                                    new ItemStack(materialToUse),
                                                    count, // Display a number as the item count
                                                    click -> {
                                                        return true; // returning true will cancel the click event and stop taking the item

                                                    },
                                                    convert(
                                                            main.getLanguageManager().getString("gui.progress.button.unlockedObjective.text", player, activeObjective, questPlayer)
                                                                    .replace("%OBJECTIVEDESCRIPTION%", descriptionToDisplay)
                                                                    .replace("%ACTIVEOBJECTIVEDESCRIPTION%", main.getQuestManager().getObjectiveTaskDescription(activeObjective.getObjective(), false, player))
                                                    )


                                            ));
                                        } else {


                                            group.addElement(new StaticGuiElement('e',
                                                    new ItemStack(materialToUse),
                                                    activeObjective.getObjectiveID(), // Display a number as the item count
                                                    click -> {
                                                        return true; // returning true will cancel the click event and stop taking the item

                                                    },
                                                    convert(main.getLanguageManager().getString("gui.progress.button.lockedObjective.text", player, activeObjective))


                                            ));
                                        }


                                    }
                                    //count++;
                                    for (final ActiveObjective activeObjective : requestedActiveQuest.getCompletedObjectives()) {

                                        final Material materialToUse = Material.FILLED_MAP;

                                        //count++;

                                        int count = activeObjective.getObjectiveID();
                                        if (!main.getConfiguration().showObjectiveItemAmount) {
                                            count = 0;
                                        }


                                        String descriptionToDisplay = main.getLanguageManager().getString("gui.progress.button.completedObjective.description-empty", player, activeObjective, questPlayer);
                                        if (!activeObjective.getObjective().getObjectiveDescription().isBlank()) {
                                            descriptionToDisplay = activeObjective.getObjective().getObjectiveDescription(main.getConfiguration().guiObjectiveDescriptionMaxLineLength);
                                        }


                                        group.addElement(new StaticGuiElement('e',
                                                new ItemStack(materialToUse),
                                                count, // Display a number as the item count
                                                click -> {
                                                    return true; // returning true will cancel the click event and stop taking the item

                                                },
                                                convert(
                                                        main.getLanguageManager().getString("gui.progress.button.completedObjective.text", player, activeObjective, questPlayer)
                                                                .replace("%OBJECTIVEDESCRIPTION%", descriptionToDisplay)
                                                                .replace("%COMPLETEDOBJECTIVEDESCRIPTION%", main.getQuestManager().getObjectiveTaskDescription(activeObjective.getObjective(), true, player))
                                                )


                                        ));


                                    }


                                    gui.addElement(group);

                                    // Previous page
                                    gui.addElement(new GuiPageElement('p', new ItemStack(Material.SPECTRAL_ARROW), GuiPageElement.PageAction.PREVIOUS, convert(main.getLanguageManager().getString("gui.progress.button.previousPage.text", player))));

                                    // Next page
                                    gui.addElement(new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, convert(main.getLanguageManager().getString("gui.progress.button.nextPage.text", player))));


                                    gui.show(player);
                                } else {
                                    audience.sendMessage(miniMessage.parse(
                                            "<GREEN>Completed Objectives for Quest <AQUA>" + requestedActiveQuest.getQuest().getQuestFinalName() + "<YELLOW>:"
                                    ));
                                    main.getQuestManager().sendCompletedObjectivesAndProgress(player, requestedActiveQuest);
                                    audience.sendMessage(miniMessage.parse(
                                            "<GREEN>Active Objectives for Quest <AQUA>" + requestedActiveQuest.getQuest().getQuestFinalName() + "<YELLOW>:"
                                    ));
                                    main.getQuestManager().sendActiveObjectivesAndProgress(player, requestedActiveQuest);
                                }

                            } else {
                                audience.sendMessage(miniMessage.parse(
                                        main.getLanguageManager().getString("chat.quest-not-found-or-does-not-exist", player, questPlayer).replace("%QUESTNAME%", args[1])
                                ));
                                audience.sendMessage(miniMessage.parse(
                                        main.getLanguageManager().getString("chat.active-quests-label", player, questPlayer)
                                ));
                                int counter = 1;
                                for (ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                                    audience.sendMessage(miniMessage.parse(
                                            "<GREEN>" + counter + ". <YELLOW>" + activeQuest.getQuest().getQuestFinalName()
                                    ));
                                    counter += 1;
                                }
                            }
                        } else {
                            audience.sendMessage(miniMessage.parse(
                                    main.getLanguageManager().getString("chat.no-quests-accepted", player)
                            ));
                        }

                    } else {
                        audience.sendMessage(miniMessage.parse(
                                main.getLanguageManager().getString("chat.wrong-command-usage", player)
                        ));
                    }

                } else {
                    audience.sendMessage(miniMessage.parse(
                            main.getLanguageManager().getString("chat.too-many-arguments", player)
                    ));
                }
            } else {
                audience.sendMessage(miniMessage.parse(
                        main.getLanguageManager().getString("chat.wrong-command-usage", player).replace("%PERMISSION%", "notquests.use")
                ));
            }
        } else {
            audience.sendMessage(miniMessage.parse(
                    "<RED>Only players can run this command! Try <AQUA>/notquestsadmin</AQUA>."
            ));
        }

        return true;
    }



    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        completions.clear();
        main.getDataManager().partialCompletions.clear();

        if (sender instanceof Player player) {
            if (sender.hasPermission("notquests.use")) {


                if (args.length == 1) {
                    completions.addAll(Arrays.asList("take", "abort", "preview", "activeQuests", "progress", "questPoints"));
                    StringUtil.copyPartialMatches(args[args.length - 1], completions, main.getDataManager().partialCompletions);
                    return main.getDataManager().partialCompletions;

                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("take")) {
                        for (Quest quest : main.getQuestManager().getAllQuests()) {
                            if (quest.isTakeEnabled()) {
                                completions.add(quest.getQuestName());
                            }
                        }
                        StringUtil.copyPartialMatches(args[args.length - 1], completions, main.getDataManager().partialCompletions);
                        return main.getDataManager().partialCompletions;
                    } else if (args[0].equalsIgnoreCase("abort")) {
                        final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(player.getUniqueId());
                        if (questPlayer != null) {
                            for (final ActiveQuest quest : questPlayer.getActiveQuests()) {
                                completions.add(quest.getQuest().getQuestName());
                            }
                        }

                        StringUtil.copyPartialMatches(args[args.length - 1], completions, main.getDataManager().partialCompletions);
                        return main.getDataManager().partialCompletions;
                    } else if (args[0].equalsIgnoreCase("preview")) {
                        for (Quest quest : main.getQuestManager().getAllQuests()) {
                            if (quest.isTakeEnabled()) {
                                completions.add(quest.getQuestName());
                            }
                        }
                        StringUtil.copyPartialMatches(args[args.length - 1], completions, main.getDataManager().partialCompletions);
                        return main.getDataManager().partialCompletions;
                    } else if (args[0].equalsIgnoreCase("progress")) {
                        final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(player.getUniqueId());
                        if (questPlayer != null) {
                            for (ActiveQuest quest : questPlayer.getActiveQuests()) {
                                completions.add(quest.getQuest().getQuestName());
                            }
                            StringUtil.copyPartialMatches(args[args.length - 1], completions, main.getDataManager().partialCompletions);
                            return main.getDataManager().partialCompletions;
                        }

                    }

                }

            }
        }


        StringUtil.copyPartialMatches(args[args.length - 1], completions, main.getDataManager().partialCompletions);
        return main.getDataManager().partialCompletions; //returns the possibility's to the client


    }


    private void handleConversation(Player player, String[] args) {
        final StringBuilder option = new StringBuilder();

        int counter = 0;
        for (String arg : args) {
            counter++;
            if (counter == args.length) {
                option.append(arg);
            } else if (counter > 1) {
                option.append(arg).append(" ");
            }
        }

        final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(player.getUniqueId());
        if (main.getQuestPlayerManager().getQuestPlayer(player.getUniqueId()) == null) {
            return;
        }


        //Check if the player has an open conversation
        final ConversationPlayer conversationPlayer = main.getConversationManager().getOpenConversation(player.getUniqueId());
        if (conversationPlayer != null) {
            conversationPlayer.chooseOption(option.toString());
        } else {
            questPlayer.sendDebugMessage("Tried to choose conversation option, but the conversationPlayer was not found! Active conversationPlayers count: " + NotQuestColors.highlightGradient + main.getConversationManager().getOpenConversations().size());
            questPlayer.sendDebugMessage("All active conversationPlayers: " + NotQuestColors.highlightGradient + main.getConversationManager().getOpenConversations().toString());
            questPlayer.sendDebugMessage("Current QuestPlayer: " + questPlayer);


        }

    }


}

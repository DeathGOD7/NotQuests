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

package rocks.gravili.notquests.Conversation;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import rocks.gravili.notquests.Commands.NotQuestColors;
import rocks.gravili.notquests.NotQuests;
import rocks.gravili.notquests.Structs.QuestPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class ConversationManager {
    private final NotQuests main;
    private final ArrayList<Conversation> conversations;
    private final Speaker playerSpeaker;

    private final HashMap<UUID, ConversationPlayer> openConversations;

    final ArrayList<ConversationLine> linesForOneFile = new ArrayList<>();
    final ArrayList<Speaker> speakersForOneFile = new ArrayList<>();


    public ConversationManager(final NotQuests main) {
        this.main = main;
        conversations = new ArrayList<>();

        openConversations = new HashMap<>();

        playerSpeaker = new Speaker("You");
        playerSpeaker.setPlayer(true);


        //playConversation(Bukkit.getPlayer("NoeX"), createTestConversation());
        loadConversationsFromConfig();
    }

    public ConversationPlayer getOpenConversation(final UUID uuid) {
        return openConversations.get(uuid);
    }

    public final HashMap<UUID, ConversationPlayer> getOpenConversations() {
        return openConversations;
    }

    public Conversation createTestConversation() {
        final Conversation testConversation = new Conversation("test", 0);

        final Speaker gustav = new Speaker("Gustav");


        final ConversationLine gustav1 = new ConversationLine(gustav, "gustav1", "Hello, I'm Gustav! What's your name?");

        final ConversationLine player1 = new ConversationLine(playerSpeaker, "player1", "I'm player!");
        final ConversationLine player2 = new ConversationLine(playerSpeaker, "player2", "None of your business!");

        gustav1.addNext(player1);
        gustav1.addNext(player2);

        final ConversationLine gustav2 = new ConversationLine(gustav, "gustav2", "Nice to meet you!");
        final ConversationLine gustav3 = new ConversationLine(gustav, "gustav3", "Yeah, fuck you!");

        player1.addNext(gustav2);

        player2.addNext(gustav3);

        testConversation.addStarterConversationLine(gustav1);

        return testConversation;
    }


    public void playConversation(final Player player, final Conversation conversation) {
        final QuestPlayer questPlayer = main.getQuestPlayerManager().getOrCreateQuestPlayer(player.getUniqueId());


        if (getOpenConversation(questPlayer.getUUID()) == null) {
            ConversationPlayer conversationPlayer = new ConversationPlayer(main, questPlayer, player, conversation);
            openConversations.put(questPlayer.getUUID(), conversationPlayer);

            conversationPlayer.play();
        } else {
            main.adventure().player(player).sendMessage(
                    MiniMessage.miniMessage().parse(
                            NotQuestColors.errorGradient + "You are already in a conversation!"
                    )
            );
        }

    }


    public void loadConversationsFromConfig() {
        conversations.clear();
        openConversations.clear();

        File conversationsFolder = new File(main.getDataFolder().getPath() + "/conversations/");
        if (!conversationsFolder.exists()) {
            main.getLogManager().log(Level.INFO, "Conversations Folder not found. Creating a new one...");

            if (!conversationsFolder.mkdirs()) {
                main.getLogManager().log(Level.SEVERE, "There was an error creating the NotQuests conversations folder");
                main.getDataManager().disablePluginAndSaving("There was an error creating the NotQuests conversations folder.");
                return;
            }

        }

        for (File conversationFile : main.getUtilManager().listFilesRecursively(conversationsFolder)) {
            linesForOneFile.clear();
            speakersForOneFile.clear();
            main.getLogManager().log(Level.INFO, "Reading conversation file <AQUA>" + conversationFile.getName() + "</AQUA>...");

            final YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(conversationFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                main.getLogManager().log(Level.WARNING, "Failed reading conversation file <AQUA>" + conversationFile.getName() + "</AQUA>. It's being skipped.");
                continue;
            }

            final int npcID = config.getInt("npcID", -1);

            final Conversation conversation = new Conversation(conversationFile.getName().replaceAll(".yml", ""), npcID);


            //First add all speakers
            final ArrayList<Speaker> allSpeakers = new ArrayList<>();
            final ConfigurationSection speakersAndLinesConfigurationSection = config.getConfigurationSection("Lines");
            if (speakersAndLinesConfigurationSection != null) {
                for (final String speakerName : speakersAndLinesConfigurationSection.getKeys(false)) {
                    final Speaker speaker = new Speaker(speakerName);
                    final String color = speakersAndLinesConfigurationSection.getString(speakerName + ".color", "<WHITE>");
                    speaker.setColor(color);

                    if (speakerName.equalsIgnoreCase("player")) {
                        speaker.setPlayer(true);
                    }
                    allSpeakers.add(speaker);
                    speakersForOneFile.add(speaker);


                    final ConfigurationSection speakerLines = speakersAndLinesConfigurationSection.getConfigurationSection(speakerName);

                    if (speakerLines == null) {
                        main.getLogManager().log(Level.WARNING, "No lines found for conversation <AQUA>" + conversationFile.getName() + "</AQUA>.");

                        continue;
                    }

                    for (final String line : speakerLines.getKeys(false)) {
                        if (line.equals("color")) {
                            continue;
                        }

                    }

                }

            }

            final ArrayList<ConversationLine> conversationLines = new ArrayList<>();


            final String starterLines = config.getString("start", "");

            for (final String starterLine : starterLines.split(",")) {
                final String message = config.getString("Lines." + starterLine + ".text", "-");

                if (message.equals("-")) {
                    main.getLogManager().log(Level.WARNING, "Warning: couldn't find message for starter line <AQUA>" + starterLine + "</AQUA> of conversation <AQUA>" + conversationFile.getName() + "</AQUA>");

                }

                Speaker foundSpeaker = null;
                for (final Speaker speaker : allSpeakers) {
                    if (speaker.getSpeakerName().equals(starterLine.split("\\.")[0])) {
                        foundSpeaker = speaker;
                    }
                }

                if (foundSpeaker == null) {
                    main.getLogManager().log(Level.WARNING, "Warning: couldn't find speaker for a conversation line. Skipping...");
                    continue;
                }

                ConversationLine startLine = new ConversationLine(foundSpeaker, starterLine.split("\\.")[1], message);
                conversationLines.add(startLine);

                conversation.addStarterConversationLine(startLine);
            }


            //Now here we have all starter conversation lines. We need to dive deep into them!

            linesForOneFile.addAll(conversationLines);
            deepDiveAndConnectStarterLines(conversationLines, config);


            conversations.add(conversation);

        }
    }


    public void deepDiveAndConnectStarterLines(final ArrayList<ConversationLine> lines, final YamlConfiguration config) {

        for (final ConversationLine conversationLine : lines) {

            final String fullIdentifier = conversationLine.getFullIdentifier();

            main.getLogManager().log(Level.INFO, "Deep diving conversation line <AQUA>" + fullIdentifier + "</AQUA>...");


            final String nextString = config.getString("Lines." + fullIdentifier + ".next", "");
            if (!nextString.isBlank()) {
                //Dive deep
                final ArrayList<ConversationLine> keepDiving = new ArrayList<>();

                for (final String nextLineFullIdentifier : nextString.split(",")) {

                    final String message = config.getString("Lines." + nextLineFullIdentifier + ".text", "");
                    final String next = config.getString("Lines." + nextLineFullIdentifier + ".next", "");

                    main.getLogManager().log(Level.INFO, "Deep diving next string <AQUA>" + nextLineFullIdentifier + "</AQUA> for conversation line <AQUA>" + fullIdentifier + "</AQUA>...");
                    main.getLogManager().log(Level.INFO, "---- Message: <AQUA>" + message + "</AQUA> | Next: <AQUA>" + next + "</AQUA>");


                    boolean alreadyExists = false;
                    for (final ConversationLine existingLine : linesForOneFile) {
                        final String fullExistingLineIdenfifier = existingLine.getFullIdentifier();
                        if (nextLineFullIdentifier.equalsIgnoreCase(fullExistingLineIdenfifier)) {
                            alreadyExists = true;
                            conversationLine.addNext(existingLine);
                            continue;
                        }
                    }
                    if (!alreadyExists) {

                        final String nextLineSpeakerName = nextLineFullIdentifier.split("\\.")[0];
                        main.getLogManager().log(Level.INFO, "---- Trying to find speaker: <AQUA>" + nextLineSpeakerName + "</AQUA>...");

                        Speaker foundSpeaker = null;
                        for (final Speaker speaker : speakersForOneFile) {
                            if (speaker.getSpeakerName().equals(nextLineSpeakerName)) {
                                foundSpeaker = speaker;
                            }

                        }


                        if (foundSpeaker == null) {
                            main.getLogManager().log(Level.WARNING, "Warning: couldn't find speaker for next conversation line <AQUA>" + nextLineFullIdentifier + "</AQUA>. Skipping line...");
                            continue;
                        }

                        main.getLogManager().log(Level.INFO, "---- Speaker: <AQUA>" + foundSpeaker.getSpeakerName() + "</AQUA> | Is Player?: <AQUA>" + foundSpeaker.isPlayer() + "</AQUA>");


                        ConversationLine newLine = new ConversationLine(foundSpeaker, nextLineFullIdentifier.split("\\.")[1], message);
                        conversationLine.addNext(newLine);
                        linesForOneFile.add(newLine);

                        if (!next.isBlank()) {
                            keepDiving.add(newLine);
                        }

                    }

                }


                //Dive again to next level
                deepDiveAndConnectStarterLines(keepDiving, config);

            }

        }
    }


    public final ArrayList<Conversation> getAllConversations() {
        return conversations;
    }

    public void stopConversation(final ConversationPlayer conversationPlayer) {
        openConversations.remove(conversationPlayer.getQuestPlayer().getUUID());
    }


}
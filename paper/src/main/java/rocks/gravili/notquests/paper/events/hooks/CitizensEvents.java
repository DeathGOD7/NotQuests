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

package rocks.gravili.notquests.paper.events.hooks;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.FollowTrait;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.conversation.Conversation;
import rocks.gravili.notquests.paper.structs.ActiveObjective;
import rocks.gravili.notquests.paper.structs.ActiveQuest;
import rocks.gravili.notquests.paper.structs.QuestPlayer;
import rocks.gravili.notquests.paper.structs.objectives.DeliverItemsObjective;
import rocks.gravili.notquests.paper.structs.objectives.TalkToNPCObjective;
import rocks.gravili.notquests.paper.structs.objectives.hooks.citizens.EscortNPCObjective;
import rocks.gravili.notquests.paper.structs.triggers.ActiveTrigger;
import rocks.gravili.notquests.paper.structs.triggers.types.NPCDeathTrigger;

import java.util.Locale;

public class CitizensEvents implements Listener {
    private final NotQuests main;

    public CitizensEvents(NotQuests main) {
        this.main = main;
    }


    @EventHandler
    private void onNPCDeathEvent(NPCDeathEvent event) {
        final NPC npc = event.getNPC();

        for (final QuestPlayer questPlayer : main.getQuestPlayerManager().getQuestPlayers()) {
            if (questPlayer.getActiveQuests().size() > 0) {
                for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                    for (final ActiveTrigger activeTrigger : activeQuest.getActiveTriggers()) {
                        if (activeTrigger.getTrigger().getTriggerType().equals("NPCDEATH")) {
                            if (((NPCDeathTrigger) activeTrigger.getTrigger()).getNpcToDieID() == npc.getId()) {
                                if (activeTrigger.getTrigger().getApplyOn() == 0) { //Quest and not Objective

                                    if (activeTrigger.getTrigger().getWorldName().equalsIgnoreCase("ALL")) {
                                        activeTrigger.addAndCheckTrigger(activeQuest);
                                    } else {
                                        final Player player = Bukkit.getPlayer(questPlayer.getUniqueId());
                                        if (player != null && player.getWorld().getName().equalsIgnoreCase(activeTrigger.getTrigger().getWorldName())) {
                                            activeTrigger.addAndCheckTrigger(activeQuest);
                                        }
                                    }
                                }

                            } else if (activeTrigger.getTrigger().getApplyOn() >= 1) { //Objective and not Quest
                                final ActiveObjective activeObjective = activeQuest.getActiveObjectiveFromID(activeTrigger.getTrigger().getApplyOn());
                                if (activeObjective != null && activeObjective.isUnlocked()) {

                                    if (activeTrigger.getTrigger().getWorldName().equalsIgnoreCase("ALL")) {
                                        activeTrigger.addAndCheckTrigger(activeQuest);
                                    } else {
                                        final Player player = Bukkit.getPlayer(questPlayer.getUniqueId());
                                        if (player != null && player.getWorld().getName().equalsIgnoreCase(activeTrigger.getTrigger().getWorldName())) {
                                            activeTrigger.addAndCheckTrigger(activeQuest);
                                        }
                                    }


                                }

                            }


                        }
                    }


                }
            }
        }
    }

    @EventHandler
    private void onNPCClickEvent(NPCRightClickEvent event) { //Disconnect objectives
        final NPC npc = event.getNPC();
        final Player player = event.getClicker();
        final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(player.getUniqueId());
        boolean handledObjective = false;
        if (questPlayer != null) {
            if (questPlayer.getActiveQuests().size() > 0) {
                for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                    for (final ActiveObjective activeObjective : activeQuest.getActiveObjectives()) {
                        if (activeObjective.isUnlocked()) {
                            if (activeObjective.getObjective() instanceof final DeliverItemsObjective deliverItemsObjective) {
                                if (deliverItemsObjective.getRecipientNPCID() == npc.getId()) {
                                    for (final ItemStack itemStack : player.getInventory().getContents()) {
                                        if (itemStack != null) {

                                            if (deliverItemsObjective.isDeliverAnyItem() || deliverItemsObjective.getItemToDeliver().getType().equals(itemStack.getType())) {
                                                if (!deliverItemsObjective.isDeliverAnyItem() && deliverItemsObjective.getItemToDeliver().getItemMeta() != null && !deliverItemsObjective.getItemToDeliver().getItemMeta().equals(itemStack.getItemMeta())) {
                                                    continue;
                                                }

                                                final long progressLeft = activeObjective.getProgressNeeded() - activeObjective.getCurrentProgress();

                                                if (progressLeft == 0) {
                                                    continue;
                                                }

                                                handledObjective = true;
                                                if (progressLeft < itemStack.getAmount()) { //We can finish it with this itemStack
                                                    itemStack.setAmount((itemStack.getAmount() - (int) progressLeft));
                                                    activeObjective.addProgress(progressLeft, npc.getId());
                                                    player.sendMessage(main.parse(
                                                            "<GREEN>You have delivered <highlight>" + progressLeft + "</highlight> items to <highlight>" + npc.getName()
                                                    ));
                                                    break;
                                                } else {
                                                    player.getInventory().removeItem(itemStack);
                                                    activeObjective.addProgress(itemStack.getAmount(), npc.getId());
                                                    player.sendMessage(main.parse(
                                                            "<GREEN>You have delivered <highlight>" + itemStack.getAmount() + "</highlight> items to <highlight>" + npc.getName()
                                                    ));
                                                }
                                            }
                                        }

                                    }

                                }
                            } else if (activeObjective.getObjective() instanceof final TalkToNPCObjective talkToNPCObjective) {
                                if (talkToNPCObjective.getNPCtoTalkID() != -1 && talkToNPCObjective.getNPCtoTalkID() == npc.getId()) {
                                    activeObjective.addProgress(1, npc.getId());
                                    player.sendMessage(main.parse(
                                            "<GREEN>You talked to <highlight>" + npc.getName()
                                    ));
                                    handledObjective = true;
                                }
                            } else if (activeObjective.getObjective() instanceof final EscortNPCObjective escortNPCObjective) {
                                if (escortNPCObjective.getNpcToEscortToID() == npc.getId()) {
                                    final NPC npcToEscort = CitizensAPI.getNPCRegistry().getById(escortNPCObjective.getNpcToEscortID());
                                    if (npcToEscort != null) {
                                        if (npcToEscort.isSpawned() && (npcToEscort.getEntity().getLocation().distance(player.getLocation()) < 6)) {
                                            activeObjective.addProgress(1, npc.getId());
                                            player.sendMessage(main.parse(
                                                    "<GREEN>You have successfully delivered the NPC <highlight>" + npcToEscort.getName()
                                            ));
                                            handledObjective = true;
                                            FollowTrait followerTrait = null;
                                            for (final Trait trait : npcToEscort.getTraits()) {
                                                if (trait.getName().toLowerCase(Locale.ROOT).contains("follow")) {
                                                    followerTrait = (FollowTrait) trait;
                                                }
                                            }
                                            if (followerTrait != null) {
                                                npc.removeTrait(followerTrait.getClass());
                                            }

                                            npcToEscort.despawn();
                                        } else {
                                            player.sendMessage(main.parse(
                                                    "<RED>The NPC you have to escort is not close enough to you!"
                                            ));
                                        }
                                    }


                                }
                            }
                            //Eventually trigger CompletionNPC Objective Completion if the objective is not set to complete automatically (so, if getCompletionNPCID() is not -1)
                            if (activeObjective.getObjective().getCompletionNPCID() != -1) {
                                activeObjective.addProgress(0, npc.getId());
                            }
                        }

                    }
                    activeQuest.removeCompletedObjectives(true);
                }
                questPlayer.removeCompletedQuests();
            }
        }

        //Return if another action already happened
        if (handledObjective) {
            return;
        }

        //Quest Preview
        main.getQuestManager().sendQuestsPreviewOfQuestShownNPCs(npc, questPlayer);

        //Conversations
        final Conversation foundConversation = main.getConversationManager().getConversationForNPCID(npc.getId());
        if (foundConversation != null) {
            main.getConversationManager().playConversation(questPlayer, foundConversation);
        }


    }

    @EventHandler
    private void onCitizensEnable(CitizensEnableEvent e) {
        main.getLogManager().info("Processing Citizens Enable Event...");
        main.getIntegrationsManager().getCitizensManager().registerQuestGiverTrait();


    }

    @EventHandler
    private void onCitizensReload(CitizensReloadEvent e) {
        main.getLogManager().info("Processing Citizens Reload Event...");

        main.getIntegrationsManager().getCitizensManager().registerQuestGiverTrait();

    }
}

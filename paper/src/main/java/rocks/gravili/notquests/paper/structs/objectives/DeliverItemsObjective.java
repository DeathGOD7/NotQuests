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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.commands.arguments.MaterialOrHandArgument;
import rocks.gravili.notquests.paper.commands.arguments.wrappers.MaterialOrHand;
import rocks.gravili.notquests.paper.structs.ActiveObjective;
import rocks.gravili.notquests.paper.structs.Quest;
import rocks.gravili.notquests.paper.structs.QuestPlayer;

import java.util.*;

public class DeliverItemsObjective extends Objective {

    private int recipientNPCID = -1;
    private UUID recipientArmorStandUUID = null;
    private boolean deliverAnyItem = false;

    private ItemStack itemToDeliver = null;
    private String nqItemName = "";



    //For Citizens NPCs
    public DeliverItemsObjective(NotQuests main) {
        super(main);
    }

    public void setNQItem(final String nqItemName){
        this.nqItemName = nqItemName;
    }
    public final String getNQItem(){
        return nqItemName;
    }

    public static void handleCommands(NotQuests main, PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> addObjectiveBuilder) {
        manager.command(addObjectiveBuilder
                .argument(MaterialOrHandArgument.of("material", main), ArgumentDescription.of("Material of the item which needs to be delivered."))
                .argument(IntegerArgument.<CommandSender>newBuilder("amount").withMin(1), ArgumentDescription.of("Amount of items which need to be delivered."))
                .argument(StringArgument.<CommandSender>newBuilder("NPC or Armorstand").withSuggestionsProvider((context, lastString) -> {
                    ArrayList<String> completions = new ArrayList<>();
                    if (main.getIntegrationsManager().isCitizensEnabled()) {
                        for (final NPC npc : CitizensAPI.getNPCRegistry().sorted()) {
                            completions.add("" + npc.getId());
                        }
                    }
                    completions.add("armorstand");
                    final List<String> allArgs = context.getRawInput();
                    main.getUtilManager().sendFancyCommandCompletion(context.getSender(), allArgs.toArray(new String[0]), "[Recipient NPC ID / 'armorstand']", "");

                    return completions;
                }).build(), ArgumentDescription.of("ID of the Citizens NPC or 'armorstand' to whom the items should be delivered."))
                .handler((context) -> {
                    final Quest quest = context.get("quest");
                    final int amountToDeliver = context.get("amount");

                    boolean deliverAnyItem = false;

                    final MaterialOrHand materialOrHand = context.get("material");
                    ItemStack itemToDeliver;
                    if (materialOrHand.hand) { //"hand"
                        if (context.getSender() instanceof Player player) {
                            itemToDeliver = player.getInventory().getItemInMainHand();
                        } else {
                            context.getSender().sendMessage(main.parse(
                                    "<error>This must be run by a player."
                            ));
                            return;
                        }
                    } else {
                        if (materialOrHand.material.equalsIgnoreCase("any")) {
                            deliverAnyItem = true;
                            itemToDeliver = null;
                        } else {
                            itemToDeliver = main.getItemsManager().getItemStack(materialOrHand.material);
                        }
                    }


                    final String npcIDOrArmorstand = context.get("NPC or Armorstand");


                    if (!npcIDOrArmorstand.equalsIgnoreCase("armorstand")) {
                        if (!main.getIntegrationsManager().isCitizensEnabled()) {
                            context.getSender().sendMessage(main.parse(
                                    "<error>Error: Any kind of NPC stuff has been disabled, because you don't have the Citizens plugin installed on your server. You need to install the Citizens plugin in order to use Citizen NPCs. You can, however, use armor stands as an alternative. To do that, just enter 'armorstand' instead of the NPC ID."
                            ));
                            return;
                        }
                        int npcID;
                        try {
                            npcID = Integer.parseInt(npcIDOrArmorstand);
                        } catch (NumberFormatException e) {
                            context.getSender().sendMessage(
                                    main.parse(
                                            "<error>Invalid NPC ID."
                                    )
                            );
                            return;
                        }
                        DeliverItemsObjective deliverItemsObjective = new DeliverItemsObjective(main);
                        if(main.getItemsManager().getItem(materialOrHand.material) != null){
                            deliverItemsObjective.setNQItem(main.getItemsManager().getItem(materialOrHand.material).getItemName());
                        }else{
                            deliverItemsObjective.setItemToDeliver(itemToDeliver);
                        }

                        deliverItemsObjective.setProgressNeeded(amountToDeliver);
                        deliverItemsObjective.setRecipientNPCID(npcID);
                        deliverItemsObjective.setDeliverAnyItem(deliverAnyItem);

                        main.getObjectiveManager().addObjective(deliverItemsObjective, context);
                    } else {//Armorstands
                        if (context.getSender() instanceof Player player) {


                            Random rand = new Random();
                            int randomNum = rand.nextInt((Integer.MAX_VALUE - 1) + 1) + 1;

                            if(main.getItemsManager().getItem(materialOrHand.material) != null){
                                main.getDataManager().getItemStackCache().put(randomNum, main.getItemsManager().getItem(materialOrHand.material).getItemName());
                            }else{
                                main.getDataManager().getItemStackCache().put(randomNum, itemToDeliver);
                            }



                            ItemStack itemStack = new ItemStack(Material.PAPER, 1);
                            //give a specialitem. clicking an armorstand with that special item will remove the pdb.

                            NamespacedKey key = new NamespacedKey(main.getMain(), "notquests-item");
                            NamespacedKey QuestNameKey = new NamespacedKey(main.getMain(), "notquests-questname");

                            NamespacedKey ItemStackKey = new NamespacedKey(main.getMain(), "notquests-itemstackcache");
                            NamespacedKey ItemStackAmountKey = new NamespacedKey(main.getMain(), "notquests-itemstackamount");
                            NamespacedKey deliverAnyKey = new NamespacedKey(main.getMain(), "notquests-anyitemstack");


                            ItemMeta itemMeta = itemStack.getItemMeta();
                            List<Component> lore = new ArrayList<>();

                            assert itemMeta != null;

                            itemMeta.getPersistentDataContainer().set(QuestNameKey, PersistentDataType.STRING, quest.getQuestName());
                            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 7);


                            itemMeta.getPersistentDataContainer().set(ItemStackKey, PersistentDataType.INTEGER, randomNum);
                            itemMeta.getPersistentDataContainer().set(ItemStackAmountKey, PersistentDataType.INTEGER, amountToDeliver);

                            if (deliverAnyItem) {
                                itemMeta.getPersistentDataContainer().set(deliverAnyKey, PersistentDataType.BYTE, (byte) 1);
                            } else {
                                itemMeta.getPersistentDataContainer().set(deliverAnyKey, PersistentDataType.BYTE, (byte) 0);
                            }


                            itemMeta.displayName(main.parse(
                                    "<LIGHT_PURPLE>Add DeliverItems Objective to Armor Stand"
                            ));

                            lore.add(main.parse(
                                    "<WHITE>Right-click an Armor Stand to add the following objective to it:"
                            ));
                            lore.add(main.parse(
                                    "<YELLOW>DeliverItems <WHITE>Objective of Quest <highlight>" + quest.getQuestName() + "</highlight>."
                            ));

                            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                            itemMeta.lore(lore);

                            itemStack.setItemMeta(itemMeta);

                            player.getInventory().addItem(itemStack);

                            context.getSender().sendMessage(
                                    main.parse(
                                            "<success>You have been given an item with which you can add the DeliverItems Objective to an armor stand. Check your inventory!"
                                    )
                            );


                        } else {
                            context.getSender().sendMessage(
                                    main.parse(
                                            "<error>Must be a player!"
                                    )
                            );
                        }
                    }


                }));
    }

    public final boolean isDeliverAnyItem() {
        return deliverAnyItem;
    }

    public void setDeliverAnyItem(final boolean deliverAnyItem) {
        this.deliverAnyItem = deliverAnyItem;
    }

    public void setItemToDeliver(final ItemStack itemToDeliver) {
        this.itemToDeliver = itemToDeliver;
    }

    public void setRecipientNPCID(final int recipientNPCID) {
        this.recipientNPCID = recipientNPCID;
    }

    public void setRecipientArmorStandUUID(final UUID recipientArmorStandUUID) {
        this.recipientArmorStandUUID = recipientArmorStandUUID;
    }

    @Override
    public void onObjectiveUnlock(final ActiveObjective activeObjective, final boolean unlockedDuringPluginStartupQuestLoadingProcess) {
    }
    @Override
    public void onObjectiveCompleteOrLock(final ActiveObjective activeObjective, final boolean lockedOrCompletedDuringPluginStartupQuestLoadingProcess, final boolean completed) {
    }

    public final ItemStack getItemToDeliver() {
        if(!getNQItem().isBlank()){
            return main.getItemsManager().getItem(getNQItem()).getItemStack().clone();
        }else{
            return itemToDeliver;
        }
    }

    //Probably never used, because we use the objective progress instead
    public final long getAmountToDeliver() {
        return super.getProgressNeeded();
    }

    public final int getRecipientNPCID() {
        return recipientNPCID;
    }

    public final UUID getRecipientArmorStandUUID() {
        return recipientArmorStandUUID;
    }

    @Override
    public String getObjectiveTaskDescription(final QuestPlayer questPlayer) {
        final String displayName;
        if (!isDeliverAnyItem()) {
            if (getItemToDeliver().getItemMeta() != null) {
                displayName = PlainTextComponentSerializer.plainText().serializeOr(getItemToDeliver().getItemMeta().displayName(), getItemToDeliver().getType().name());
            } else {
                displayName = getItemToDeliver().getType().name();
            }
        } else {
            displayName = "Any";
        }

        String itemType = isDeliverAnyItem() ? "Any" : getItemToDeliver().getType().name().toLowerCase(Locale.ROOT);


        String toReturn;
        if (!displayName.isBlank()) {
            toReturn = main.getLanguageManager().getString("chat.objectives.taskDescription.deliverItems.base", questPlayer, Map.of(
                    "%ITEMTODELIVERTYPE%", itemType,
                    "%ITEMTODELIVERNAME%", displayName,
                    "%(%", "(",
                    "%)%", "<RESET>)"
            ));
        } else {
            toReturn = main.getLanguageManager().getString("chat.objectives.taskDescription.deliverItems.base", questPlayer, Map.of(
                    "%ITEMTODELIVERTYPE%", itemType,
                    "%ITEMTODELIVERNAME%", "",
                    "%(%", "",
                    "%)%", ""
            ));
        }


        if (main.getIntegrationsManager().isCitizensEnabled() && getRecipientNPCID() != -1) {
            final NPC npc = CitizensAPI.getNPCRegistry().getById(getRecipientNPCID());
            if (npc != null) {
                toReturn += "\n" + main.getLanguageManager().getString("chat.objectives.taskDescription.deliverItems.deliver-to-npc", questPlayer, Map.of(
                        "%NPCNAME%", npc.getName()
                ));
            } else {
                toReturn += "\n" + main.getLanguageManager().getString("chat.objectives.taskDescription.deliverItems.deliver-to-npc-not-available", questPlayer);
            }
        } else {

            if (getRecipientNPCID() != -1) {
                toReturn += main.getLanguageManager().getString("chat.objectives.taskDescription.deliverItems.deliver-to-npc-citizens-not-found", questPlayer);
            } else { //Armor Stands
                final UUID armorStandUUID = getRecipientArmorStandUUID();
                if (armorStandUUID != null) {
                    toReturn += "\n" + main.getLanguageManager().getString("chat.objectives.taskDescription.deliverItems.deliver-to-armorstand", questPlayer, Map.of(
                            "%ARMORSTANDNAME%", main.getArmorStandManager().getArmorStandName(armorStandUUID)
                    ));
                } else {
                    toReturn += "\n" + main.getLanguageManager().getString("chat.objectives.taskDescription.deliverItems.deliver-to-armorstand-not-available", questPlayer);
                }
            }

        }
        return toReturn;
    }

    @Override
    public void save(FileConfiguration configuration, String initialPath) {
        if(!getNQItem().isBlank()){
            configuration.set(initialPath + ".specifics.nqitem", getNQItem());
        }else {
            configuration.set(initialPath + ".specifics.itemToCollect.itemstack", getItemToDeliver());
        }

        configuration.set(initialPath + ".specifics.recipientNPCID", getRecipientNPCID());
        if (getRecipientArmorStandUUID() != null) {
            configuration.set(initialPath + ".specifics.recipientArmorStandID", getRecipientArmorStandUUID().toString());
        } else {
            configuration.set(initialPath + ".specifics.recipientArmorStandID", null);
        }
        configuration.set(initialPath + ".specifics.deliverAnyItem", isDeliverAnyItem());
    }

    @Override
    public void load(FileConfiguration configuration, String initialPath) {
        this.nqItemName = configuration.getString(initialPath + ".specifics.nqitem", "");
        if(nqItemName.isBlank()){
            itemToDeliver = configuration.getItemStack(initialPath + ".specifics.itemToCollect.itemstack");
        }

        recipientNPCID = configuration.getInt(initialPath + ".specifics.recipientNPCID");

        if (recipientNPCID != -1) {
            recipientArmorStandUUID = null;
        } else {
            final String armorStandUUIDString = configuration.getString(initialPath + ".specifics.recipientArmorStandID");
            if (armorStandUUIDString != null) {
                recipientArmorStandUUID = UUID.fromString(armorStandUUIDString);
            } else {
                recipientArmorStandUUID = null;
            }
        }

        deliverAnyItem = configuration.getBoolean(initialPath + ".specifics.deliverAnyItem", false);

    }
}
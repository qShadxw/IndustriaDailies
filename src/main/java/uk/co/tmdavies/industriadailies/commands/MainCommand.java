package uk.co.tmdavies.industriadailies.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.objects.Quest;
import uk.co.tmdavies.industriadailies.utils.Utils;

import java.util.List;

public class MainCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("industriadailies")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("option", StringArgumentType.string())
                .then(Commands.argument("quest_id",  StringArgumentType.string())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer sender = source.getPlayer();

                    if (sender == null) {
                        IndustriaDailies.LOGGER.error("MainCommand sender is null");
                        return 0;
                    }

                    Player target = EntityArgument.getPlayer(context, "player");
                    String argument = StringArgumentType.getString(context, "option");
                    String questId = StringArgumentType.getString(context, "quest_id");

                    switch (argument.toLowerCase()) {
                        case "complete":
                            if (questId == null || questId.isEmpty()) {
                                sender.sendSystemMessage(Component.literal("No questId provided."));
                                break;
                            }

                            boolean isCompleted = IndustriaDailies.manager.completeQuest(target, questId);

                            if (!isCompleted) {
                                sender.sendSystemMessage(Component.literal("Quest could not be completed."));
                                break;
                            }

                            Quest quest = IndustriaDailies.manager.getPersonalQuestFromId(target, questId);

                            if (quest == null) {
                                break;
                            }

                            if (quest.isCompleted()) {
                                break;
                            }

                            target.sendSystemMessage(Utils.Chat("Completed quest %s [%s]", quest.getObjective(), quest.getId()));

                            if (quest.getRewardItemId().equals("irs")) {
                                IndustriaDailies.neoNetworkIRS.giveMoney(target, quest.getRewardItemAmount(), String.format("Completed %s", quest.getId()));
                                break;
                            }

                            target.getInventory().add(quest.getReward());
                            break;
                        case "delete":
                            if (!IndustriaDailies.manager.hasQuests(target)) {
                                sender.sendSystemMessage(Component.literal(String.format("%s does not have quests to delete.", target.getName())));
                                break;
                            }
                            IndustriaDailies.manager.resetPlayer(target);
                            sender.sendSystemMessage(Component.literal(String.format("%s's quests has been deleted.", target.getName())));
                            target.sendSystemMessage(Component.literal("Your quests have been deleted."));

                            break;
                        case "give":
                            if (IndustriaDailies.manager.hasQuests(target)) {
                                sender.sendSystemMessage(Component.literal(String.format("%s already has quests.", target.getName())));

                                break;
                            }
                            sender.sendSystemMessage(Component.literal(String.format("Giving %s their daily missions.", target.getName())));
                            IndustriaDailies.manager.generateQuestsForPlayer(target);

                            break;
                        case "what":
                            List<Quest> personalQuests = IndustriaDailies.manager.getPersonalQuests(target);

                            for (Quest q : personalQuests) {
                                sender.sendSystemMessage(Utils.Chat("Id: %s, RequiredItem: %s, Completed: %s", q.getId(), q.getAmountNeeded() + "x" + q.getItemNeeded(), q.isCompleted()));
                            }

                            break;
                        default:
                            sender.sendSystemMessage(Component.literal(String.format("Argument %s is not valid. [delete, give]", argument)));

                            break;
                    }

                    return 1;
                }))))
        );
    }
}

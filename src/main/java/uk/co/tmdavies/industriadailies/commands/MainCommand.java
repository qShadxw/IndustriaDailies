package uk.co.tmdavies.industriadailies.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.objects.Quest;
import uk.co.tmdavies.industriadailies.uis.ChestUIController;
import uk.co.tmdavies.industriadailies.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainCommand {

    public static String commandName = "industriadailies";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(commandName)
                .then(Commands.literal("complete")
                        .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("quest_id", StringArgumentType.string())
                                .executes(MainCommand::completeOption)))
                )
                .then(Commands.literal("completeall")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::completeAllOption))
                )
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::giveOption))
                )
                .then(Commands.literal("delete")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::deleteOption))
                )
                .then(Commands.literal("what")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::whatOption))
                )
                .then(Commands.literal("open")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::openOption))
                )
        );
    }

    public static Object[] extractContext(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extraction = new Object[] {
                context.getSource(),
                context.getSource().getPlayer(),
                EntityArgument.getPlayer(context, "player")
        };

        if (extraction[1] == null) {
            IndustriaDailies.LOGGER.error("MainCommand sender is null");

            return null;
        }

        return extraction;
    }

    public static int completeOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extract = extractContext(context);

        if (extract == null) {
            return 0;
        }

        ServerPlayer sender = (ServerPlayer) extract[1];
        Player target = (Player) extract[2];
        String questId = StringArgumentType.getString(context, "quest_id");

        if (questId == null || questId.isEmpty()) {
            sender.sendSystemMessage(Component.literal("No questId provided."));
            return 0;
        }

        boolean isCompleted = IndustriaDailies.manager.completeQuest(target, questId, target.getItemInHand(InteractionHand.MAIN_HAND));

        if (!isCompleted) {
            sender.sendSystemMessage(Component.literal("Quest could not be completed."));
            return 0;
        }

        Quest quest = IndustriaDailies.manager.getPersonalQuestFromId(target, questId);

        if (quest == null) {
            return 0;
        }

        if (quest.isCompleted()) {
            return 0;
        }

        target.sendSystemMessage(Utils.Chat("Completed quest %s [%s]", quest.getObjective(), quest.getId()));
        IndustriaDailies.manager.setQuestAsCompleted(target, questId);

        if (quest.getRewardItemId().equals("irs")) {
            IndustriaDailies.neoNetworkIRS.giveMoney(target, quest.getRewardItemAmount(), String.format("Completed %s", quest.getId()));
            return 0;
        }

        target.getInventory().add(quest.getReward());
        return 1;
    }

    public static int completeAllOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extraction = extractContext(context);

        if (extraction == null) {
            return 0;
        }

        ServerPlayer sender = (ServerPlayer) extraction[1];
        Player target = (Player) extraction[1];

        if (!IndustriaDailies.manager.hasQuests(target)) {
            sender.sendSystemMessage(Utils.Chat("%s does not have quests to complete.", target.getName()));
            return 1;
        }

        List<Quest> playerQuests = IndustriaDailies.manager.getPersonalQuests(target);
        List<Quest> completedQuests = new ArrayList<>();

        target.getInventory().items.forEach(item -> {
            if (item.toString().equals("0 minecraft:air")) {
                return;
            }

            playerQuests.forEach(quest -> {
                if (!IndustriaDailies.manager.completeQuest(target, quest.getId(), item)) {
                    return;
                }
                completedQuests.add(quest);
            });
        });

        completedQuests.forEach(quest -> {
            IndustriaDailies.LOGGER.info("Completed Quest");
            target.sendSystemMessage(Utils.Chat("Completed quest %s [%s]", quest.getObjective(), quest.getId()));
            IndustriaDailies.manager.setQuestAsCompleted(target, quest.getId());
        });
        return 1;
    }

    public static int deleteOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extraction = extractContext(context);

        if (extraction == null) {
            return 0;
        }

        ServerPlayer sender = (ServerPlayer) extraction[1];
        Player target = (Player) extraction[2];

        if (!IndustriaDailies.manager.hasQuests(target)) {
            sender.sendSystemMessage(Utils.Chat("%s does not have quests to delete.", target.getName()));
            return 1;
        }

        IndustriaDailies.manager.resetPlayer(target);
        sender.sendSystemMessage(Component.literal(String.format("%s's quests has been deleted.", target.getName())));
        target.sendSystemMessage(Component.literal("Your quests have been deleted."));
        return 1;
    }

    public static int giveOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extraction = extractContext(context);

        if (extraction == null) {
            return 0;
        }

        ServerPlayer sender = (ServerPlayer) extraction[1];
        Player target = (Player) extraction[2];

        if (IndustriaDailies.manager.hasQuests(target)) {
            sender.sendSystemMessage(Component.literal(String.format("%s already has quests.", target.getName())));

            return 0;
        }

        sender.sendSystemMessage(Component.literal(String.format("Giving %s their daily missions.", target.getName())));
        IndustriaDailies.manager.generateQuestsForPlayer(target);
        return 1;
    }

    public static int whatOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extraction = extractContext(context);

        if (extraction == null) {
            return 0;
        }

        ServerPlayer sender = (ServerPlayer) extraction[1];
        Player target = (Player) extraction[2];


        List<Quest> personalQuests = IndustriaDailies.manager.getPersonalQuests(target);

        for (Quest q : personalQuests) {
            sender.sendSystemMessage(Utils.Chat("Id: %s, RequiredItem: %s, Completed: %s", q.getId(), q.getAmountNeeded() + "x" + q.getItemNeeded(), q.isCompleted()));
        }

        return 1;
    }

    public static int openOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extraction = extractContext(context);

        if (extraction == null) {
            return 0;
        }

        Player target = (Player) extraction[1];

        ChestUIController.openDailies(target, 54);
        return 1;
    }

}

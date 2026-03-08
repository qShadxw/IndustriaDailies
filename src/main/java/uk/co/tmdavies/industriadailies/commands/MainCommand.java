package uk.co.tmdavies.industriadailies.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.text2speech.Narrator;
import com.sun.jdi.connect.Connector;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.objects.DefinedPositions;
import uk.co.tmdavies.industriadailies.objects.Quest;
import uk.co.tmdavies.industriadailies.savedata.TargetDataStorage;
import uk.co.tmdavies.industriadailies.uis.ChestUIController;
import uk.co.tmdavies.industriadailies.utils.Utils;
import static uk.co.tmdavies.industriadailies.IndustriaDailies.LOGGER;
import static uk.co.tmdavies.industriadailies.IndustriaDailies.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainCommand {

    public static String commandName = "neodailies";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(commandName).requires(source -> source.hasPermission(2))
                .then(
                        Commands.literal("complete")
                        .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("quest_id", StringArgumentType.string())
                                .executes(MainCommand::completeOption)))
                )
                .then(
                        Commands.literal("completeall")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::completeAllOption))
                )
                .then(
                        Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::giveOption))
                )
                .then(
                        Commands.literal("giveSet")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("stringId", StringArgumentType.greedyString())
                                                .executes(MainCommand::giveSetOption))
                                )
                )
                .then(
                        Commands.literal("completeSet")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("stringId", StringArgumentType.greedyString())
                                                .executes(MainCommand::isCompleteOption))
                                )
                )
                .then(
                        Commands.literal("delete")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::deleteOption))
                )
                .then(
                        Commands.literal("deleteSet")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("questid", StringArgumentType.greedyString())
                                                .executes(MainCommand::deleteSetQuest))
                                )
                )
                .then(
                        Commands.literal("what")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::whatOption))
                )
                .then(
                        Commands.literal("open")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MainCommand::openOption))
                )
                .then(
                        Commands.literal("newdefinedpos")
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .then(Commands.argument("dist", IntegerArgumentType.integer())
                                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                                        .executes(MainCommand::newDefinedPos))
                                        )
                                )
                )
                .then(
                        Commands.literal("newquest")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("id", StringArgumentType.string())
                                                .then(Commands.argument("objective", StringArgumentType.string())
                                                        .then(Commands.argument("itemNeeded", StringArgumentType.string())
                                                                .then(Commands.argument("amountNeeded", IntegerArgumentType.integer())
                                                                        .then(Commands.argument("rewarditemId", StringArgumentType.string())
                                                                                .then(Commands.argument("rewarditemAmount", IntegerArgumentType.integer())
                                                                                        .then(Commands.argument("weight", IntegerArgumentType.integer())
                                                                                                .then(Commands.argument("handInPos", Vec3Argument.vec3())
                                                                                                        .then(Commands.argument("required", StringArgumentType.string())
                                                                                                                .then(Commands.argument("talkTo", StringArgumentType.string())
                                                                                                                        .then(Commands.argument("handIn", StringArgumentType.string())
                                                                                                                                .executes(MainCommand::newQuest))
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                )
        );
    }

    public static Object[] extractContext(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extraction = new Object[] {
                context.getSource(),
                context.getSource().getPlayer(),
                EntityArgument.getPlayer(context, "player")
        };
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

        for (int i = 0; i < playerQuests.size(); i++)
        {
            if (target.getInventory().contains(playerQuests.get(i).getItemNeededAsItemstack())) {
                if (target.getInventory().getItem(target.getInventory().findSlotMatchingItem(playerQuests.get(i).getItemNeededAsItemstack())).getCount() >= playerQuests.get(i).getAmountNeeded()) {
                    if (completedQuests.contains(playerQuests.get(i)) || playerQuests.get(i).isCompleted()) {
                        continue;
                    } else {
                        completedQuests.add(playerQuests.get(i));
                        playerQuests.get(i).setCompleted(true);
                        target.getInventory().removeItem(target.getInventory().findSlotMatchingItem(playerQuests.get(i).getItemNeededAsItemstack()), playerQuests.get(i).getAmountNeeded());
                    }
                }
            }
        }

        /*
        target.getInventory().items.forEach(item -> {
            if (item.toString().equals("0 minecraft:air")) {
                return;
            }



            playerQuests.forEach(quest -> {
                if (completedQuests.contains(quest) && !IndustriaDailies.manager.completeQuest(target, quest.getId(), item) ) {
                    return;
                }
                completedQuests.add(quest);
            });
        });
        */

        completedQuests.forEach(quest -> {
            IndustriaDailies.LOGGER.info("Completed Quest");
            target.sendSystemMessage(Utils.Chat("Completed quest %s [%s]", quest.getObjective(), quest.getId()));
            IndustriaDailies.manager.setQuestAsCompleted(target, quest.getId());
            target.getInventory().add(quest.getReward());
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

    public static int giveSetOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Object[] extraction = extractContext(context);

        if (extraction == null) {
            return 0;
        }

        Player target = (Player) extraction[2];

        if (target == null) {
            LOGGER.error("Tried to give a quest to a null player ");
            return 0;
        }

        String name = StringArgumentType.getString(context, "stringId");

        if (manager.addSetQuest(name, target))
        {
            return 0;
        }

        Utils.displayTitle(target, "Quest Accepted!", ChatFormatting.GOLD);
        Utils.playSound(target, SoundEvents.AMETHYST_BLOCK_RESONATE);

        manager.saveSaveData(target.getServer());
        return 1;
    }

    public static int deleteSetQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Player p = EntityArgument.getPlayer(context, "player");
        String questid = StringArgumentType.getString(context, "questid");

        for (int i = 0; i < manager.getPlayerSetQuests(p).size(); i++)
        {
            if (Objects.equals(manager.getPlayerSetQuests(p).get(i).getId(), questid))
            {
                manager.getPlayerSetQuests(p).remove(i);
                return 1;
            }
        }

        return 0;
    }

    public static int isCompleteOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        Player target = EntityArgument.getPlayer(context, "player");
        String questId = StringArgumentType.getString(context, "stringId");

        LOGGER.info(questId);
        if (questId == null || questId.isEmpty()) {
            LOGGER.error("No quest ID provided");
            return 0;
        }


        Quest quest = IndustriaDailies.manager.getPlayersSetQuest(target, questId);
        LOGGER.info(quest.questName);


        if (manager.fullQuestCheckComplete(target, quest, target))
        {
            Utils.displayTitle(target, "Quest Completed!", ChatFormatting.GOLD);
            Utils.playSound(target, SoundEvents.PLAYER_LEVELUP);
        }


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

        ChestUIController.openQuests(target, 54);
        return 1;
    }

    public static int newDefinedPos(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        Vec3 newPos = Vec3Argument.getVec3(context, "pos");
        if (newPos == null)
        {
            return 0;
        }

        int maxDist = IntegerArgumentType.getInteger(context, "dist");
        if (maxDist < 0)
        {
            return 0;
        }

        String name = StringArgumentType.getString(context, "name");
        if (name == null)
        {
            return 0;
        }

        DefinedPositions.posistions.add(new DefinedPositions(newPos, maxDist, name));
        context.getSource().sendSuccess(() -> Component.literal("Made new DefinedPosition"), false);

        new Thread(() -> {
            TargetDataStorage.posSave(context.getSource().getServer());
        }).start();

        return 1;
    }

    public static int newQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        String name = StringArgumentType.getString(context, "name");
        String id = StringArgumentType.getString(context, "id");
        String objective = StringArgumentType.getString(context, "objective");
        String itemNeeded = StringArgumentType.getString(context, "itemNeeded");
        int amountNeeded = IntegerArgumentType.getInteger(context, "amountNeeded");
        String rewarditemId = StringArgumentType.getString(context, "rewarditemId");
        int rewarditemAmount = IntegerArgumentType.getInteger(context, "rewarditemAmount");
        int weight = IntegerArgumentType.getInteger(context, "weight");
        Vec3 handInPos = Vec3Argument.getVec3(context, "handInPos");
        String required = StringArgumentType.getString(context, "required");
        String[] realRequired;
        try
        {
            realRequired = required.split(",");
        } catch (RuntimeException e) {
            context.getSource().sendSuccess(() -> Component.literal("required field invalid"), false);
            return 0;
        }
        String talkTo = StringArgumentType.getString(context, "talkTo");
        String handIn = StringArgumentType.getString(context, "handIn");

        manager.setQuests.add(new Quest(name, id, objective, itemNeeded, amountNeeded, rewarditemId, rewarditemAmount, weight, handInPos, realRequired, talkTo, handIn));

        manager.saveSaveData(context.getSource().getServer());

        context.getSource().sendSuccess(() -> Component.literal("Quest " + name + " has been created"), false);

        return 1;
    }

}

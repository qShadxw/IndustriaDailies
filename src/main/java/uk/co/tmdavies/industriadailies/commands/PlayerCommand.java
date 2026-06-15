package uk.co.tmdavies.industriadailies.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import uk.co.tmdavies.industriadailies.uis.ChestUIController;

public class PlayerCommand {
    public static String commandName = "quests";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(commandName)
                .executes(PlayerCommand::openOption)
        );
    }

    public static int openOption(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        Player player = context.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        ChestUIController.openQuests(player, 54);
        return 1;
    }
}

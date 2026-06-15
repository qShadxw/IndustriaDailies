package uk.co.tmdavies.industriadailies.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import uk.co.tmdavies.industriadailies.IndustriaDailies;

import java.io.IOException;

public class Utils {
    public static Component Chat(String message, Object... args) {
        return Component.literal(String.format(message.replace("&", "§"), args));
    }

    public static void displayTitle(ServerPlayer sp, String text, ChatFormatting col) {
        Component title = Component.literal(text).withStyle(col);
        ClientboundSetTitlesAnimationPacket animationPacket = new ClientboundSetTitlesAnimationPacket(10, 70, 20);
        ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(title);

        sp.connection.send(animationPacket);
        sp.connection.send(titlePacket);
    }

    public static void displayTitle(Player player, String text, ChatFormatting col) {
        displayTitle((ServerPlayer) player, text, col);
    }

    public static void playSound(ServerPlayer sp, SoundEvent se) {
        try (ServerLevel level = sp.serverLevel()) {
            level.playSound(
                    null,
                    sp.getX(),
                    sp.getY(),
                    sp.getZ(),
                    se,
                    SoundSource.MASTER,
                    10.0F,
                    1.0F
            );
        } catch (IOException exception) {
            IndustriaDailies.LOGGER.info("Error playing sound.");
            exception.printStackTrace();
        }
    }

    public static void playSound(Player p, SoundEvent se) {
        playSound((ServerPlayer) p, se);
    }
}

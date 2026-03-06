package uk.co.tmdavies.industriadailies.savedata;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class ModDataPath {
    public static Path getQuestDataFile(MinecraftServer server) {
        return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data").resolve("industriadailies").resolve("questData.json");
    }

    public static Path getPlayerDataFile(MinecraftServer server) {
        return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data").resolve("industriadailies").resolve("playerData.json");
    }

}

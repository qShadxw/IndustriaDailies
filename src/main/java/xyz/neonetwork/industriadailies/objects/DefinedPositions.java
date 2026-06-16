package xyz.neonetwork.industriadailies.objects;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import xyz.neonetwork.industriadailies.savedata.TargetDataStorage;

import java.util.ArrayList;

public class DefinedPositions {
    public static ArrayList<DefinedPositions> posistions;
    public Vec3 pos;
    public double maxDist;
    public String name;

    public DefinedPositions(Vec3 pos, double maxDist, String name) {
        this.pos = pos;
        this.maxDist = maxDist;
        this.name = name;
    }

    public static void init(MinecraftServer server) {
        DefinedPositions.posistions = new ArrayList<>();
        DefinedPositions.posistions = TargetDataStorage.posLoad(server);
    }
}


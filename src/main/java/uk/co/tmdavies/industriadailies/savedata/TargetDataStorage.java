package uk.co.tmdavies.industriadailies.savedata;

import com.google.common.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import uk.co.tmdavies.industriadailies.objects.Manager;
import uk.co.tmdavies.industriadailies.objects.Quest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static uk.co.tmdavies.industriadailies.IndustriaDailies.manager;

public class TargetDataStorage {
    private static final Type LIST_TYPE = new TypeToken<List<Quest>>() {}.getType();
    private static final Type HASH_TYPE = new TypeToken<HashMap<String, ArrayList<Quest>>>() {}.getType();

    public static void playerSave(MinecraftServer server)
    {
        try{
            Path file = ModDataPath.getPlayerDataFile(server);

            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String json;
            if (manager.playerSetQuests.isEmpty())
            {
                json = ModJson.GSON.toJson(new HashMap<String, ArrayList<Quest>>());
            }
            else
            {
                json = ModJson.GSON.toJson(manager.playerSetQuests);
            }

            Files.writeString(file, json);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static HashMap<String, ArrayList<Quest>> playerLoad(MinecraftServer server)
    {
        try{
            Path file = ModDataPath.getPlayerDataFile(server);

            if (Files.exists(file) == false)
            {
                playerSave(server);
                return new HashMap<String, ArrayList<Quest>>();
            }

            String json = Files.readString(file);
            if (json.length() < 4) return new HashMap<String, ArrayList<Quest>>();

            HashMap<String, ArrayList<Quest>> data = ModJson.GSON.fromJson(json, HASH_TYPE);

            return data != null ? data : new HashMap<String, ArrayList<Quest>>();

        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<String, ArrayList<Quest>>();
        }
    }

    public static void questSave(MinecraftServer server)
    {
        try{
            Path file = ModDataPath.getQuestDataFile(server);

            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String json;
            if (manager.setQuests.isEmpty())
            {
                json = ModJson.GSON.toJson(new ArrayList<Quest>());
            }
            else
            {
                json = ModJson.GSON.toJson(manager.setQuests);
            }


            Files.writeString(file, json);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static ArrayList<Quest> questLoad(MinecraftServer server)
    {
        try{
            Path file = ModDataPath.getQuestDataFile(server);

            if (Files.exists(file) == false)
            {
                questSave(server);
                return new ArrayList<Quest>();
            }

            String json = Files.readString(file);
            if (json.length() < 4) return new ArrayList<Quest>();

            ArrayList<Quest> data = ModJson.GSON.fromJson(json, LIST_TYPE);

            return data != null ? data : new ArrayList<>();

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

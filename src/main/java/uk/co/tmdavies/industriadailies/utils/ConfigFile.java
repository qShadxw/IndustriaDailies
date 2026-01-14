package uk.co.tmdavies.industriadailies.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import uk.co.tmdavies.industriadailies.IndustriaDailies;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ConfigFile {

    private final String path;
    private final String fileName;
    private File file;

    private JsonObject jsonObj;

    public ConfigFile(String name) {
        if (!name.endsWith(".json")) {
            name = name + ".json";
        }

        this.path = "./config/industriadailies";
        this.fileName = name;
        this.file = new File(this.path + "/" + this.fileName);

        checkDir();
        checkFile();
    }

    public void checkDir() {
        File dir = new File(this.path);

        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void checkFile() {
        if (file.exists()) {
            return;
        }

        try {
            file.createNewFile();
        } catch (IOException exception) {
            IndustriaDailies.LOGGER.error("Error creating config file.");
            exception.printStackTrace();
        }
    }

    public void loadConfig() {
        this.file = new File(this.path + "/" + this.fileName);

        if (file.length() == 0) {
            setDefaults();
        }

        try (FileInputStream inputStream = new FileInputStream(this.path + "/" + this.fileName)) {
            this.jsonObj = JsonParser.parseString(IOUtils.toString(inputStream, Charset.defaultCharset())).getAsJsonObject();
        } catch (IOException e) {
            IndustriaDailies.LOGGER.error("Error loading config file. Continuing to create new...");
        }

        //verboseConfig();
    }

    public void setDefaults() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Main Object
        JsonObject questsObject = new JsonObject();

        // First Template Section Object
        JsonObject templateFirstSection = new JsonObject();
        templateFirstSection.addProperty("Weight", 50);

        JsonObject firstQuest = new JsonObject();
        firstQuest.addProperty("Id", "NN_Section_1_Quest_1");
        firstQuest.addProperty("Objective", "Get 10 Porkchops");
        firstQuest.addProperty("Item-Needed", "minecraft:porkchop");
        firstQuest.addProperty("Amount-Needed", "10");
        firstQuest.addProperty("Weight", 15);

        JsonObject secondQuest = new JsonObject();
        secondQuest.addProperty("Id", "NN_Section_1_Quest_2");
        secondQuest.addProperty("Objective", "Kill {Random} Player");
        secondQuest.addProperty("Item-Needed", "minecraft:player_head");
        secondQuest.addProperty("Amount-Needed", "1");
        secondQuest.addProperty("Weight", 20);

        templateFirstSection.add("Quest1", firstQuest);
        templateFirstSection.add("Quest2", secondQuest);

        // Second Template Section Object
        JsonObject templateSecondSection = new JsonObject();
        templateSecondSection.addProperty("Weight", 50);

        templateSecondSection.add("Quest1", firstQuest);
        templateSecondSection.add("Quest2", secondQuest);

        JsonObject sectionObject = new JsonObject();

        sectionObject.add("Section1", templateFirstSection);
        sectionObject.add("Section2", templateSecondSection);

        questsObject.add("Quests", sectionObject);

        try (FileWriter writer = new FileWriter(this.path + "/" + this.fileName)) {
            gson.toJson(questsObject, writer);
        } catch (IOException exception) {
            IndustriaDailies.LOGGER.error("Failed to write json file defaults.");
            exception.printStackTrace();
        }
    }

    public JsonObject getConfig() {
        if (jsonObj == null) {
            IndustriaDailies.LOGGER.error("Config was not loaded before getting.");

            return null;
        }

        return jsonObj;
    }

    public JsonObject get(String path) {
        if (jsonObj == null) {
            IndustriaDailies.LOGGER.error("Config was not loaded before grabbing data.");

            return null;
        }

        return jsonObj.getAsJsonObject(path);
    }

    public boolean isModEnabled() {
        if (jsonObj == null) {
            IndustriaDailies.LOGGER.error("Config was not loaded before grabbing data.");

            return true;
        }

        return jsonObj.get("Enabled").getAsBoolean();
    }

    public void verboseConfig() {
        if (jsonObj == null) {
            return;
        }

        JsonObject questsObject = jsonObj.getAsJsonObject("Quests");

        IndustriaDailies.LOGGER.info("Config Details:");

        for (String key : jsonObj.keySet()) {
            IndustriaDailies.LOGGER.info("Root: {}", key);
        }

        for (String key : questsObject.keySet()) {
            IndustriaDailies.LOGGER.info("Quest: {}", key);
        }

        List<JsonObject> sectionObjects = new ArrayList<>();

        for (String key : questsObject.keySet()) {
            JsonObject sectionObject = questsObject.getAsJsonObject(key);

            sectionObjects.add(sectionObject);

            for (String key2 : sectionObject.keySet()) {

                IndustriaDailies.LOGGER.info("{}: {}: {}", key, key2, sectionObject.get(key2));
            }
        }

        for (JsonObject sectionObject : sectionObjects) {
            for (String key : sectionObject.keySet()) {
                if (key.equals("Weight")) {
                    continue;
                }

                JsonObject questObject = sectionObject.getAsJsonObject(key);

                for (String key2 : questObject.keySet()) {
                    IndustriaDailies.LOGGER.info("{}: {}: {}", key, key2, questObject.get(key2));
                }
            }
        }

    }

}

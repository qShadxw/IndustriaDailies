package uk.co.tmdavies.industriadailies.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import uk.co.tmdavies.industriadailies.IndustriaDailies;

import java.io.*;
import java.nio.charset.Charset;

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

        verboseConfig();
    }

    public void setDefaults() {


        try (Writer writer = new FileWriter(this.path + "/" + this.fileName)) {
            writer.write(this.jsonObj.toString());
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

    public Object get(String path) {
        if (jsonObj == null) {
            IndustriaDailies.LOGGER.error("Config was not loaded before grabbing data.");

            return null;
        }

        return jsonObj.get(path);
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

        IndustriaDailies.LOGGER.info("Config Details:");
    }

}

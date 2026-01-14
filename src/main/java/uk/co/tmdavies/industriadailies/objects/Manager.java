package uk.co.tmdavies.industriadailies.objects;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import uk.co.tmdavies.industriadailies.IndustriaDailies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Manager {

    private final HashMap<String, List<Quest>> playerQuests;
    private final List<QuestSection> questSections;

    public Manager() {
        playerQuests = new HashMap<>();
        questSections = new ArrayList<>();
    }

    public boolean initPlayer(Player player) {
        if (playerQuests.containsKey(player.getStringUUID())) {
            return false;
        }
        playerQuests.put(player.getStringUUID(), new ArrayList<>());
        return true;
    }

    public HashMap<String, List<Quest>> getPlayerQuests() {
        return playerQuests;
    }

    public List<QuestSection> getQuestSections() {
        return questSections;
    }

    public boolean addSection(QuestSection questSection) {
        if (questSections.contains(questSection)) {
            return false;
        }

        questSections.add(questSection);
        return true;
    }

    public boolean removeSection(QuestSection questSection) {
        if (!questSections.contains(questSection)) {
            return false;
        }

        questSections.remove(questSection);
        return true;
    }

    public boolean addQuest(Player player, Quest quest) {
        if (!playerQuests.containsKey(player.getStringUUID())) {
            return false;
        }

        List<Quest> quests = playerQuests.get(player.getStringUUID());

        if (quests.contains(quest)) {
            return false;
        }

        quests.add(quest);
        playerQuests.replace(player.getStringUUID(), quests);
        return true;
    }

    public boolean removeQuest(Player player, Quest quest) {
        if (!playerQuests.containsKey(player.getStringUUID())) {
            return false;
        }

        List<Quest> quests = playerQuests.get(player.getStringUUID());

        if (!quests.contains(quest)) {
            return false;
        }

        quests.remove(quest);
        playerQuests.replace(player.getStringUUID(), quests);
        return true;
    }

    public void loadQuests() {
        IndustriaDailies.LOGGER.info("Loading quests...");

        JsonObject questsObject = IndustriaDailies.configFile.get("Quests");
        List<JsonObject> sectionObjects = new ArrayList<>();

        // Grabbing Section, List of Quest JsonObjects
        HashMap<JsonObject, List<JsonObject>> sections = new HashMap<>();

        // Grabbing Sections
        questsObject.keySet().forEach(key -> {
            IndustriaDailies.LOGGER.info("Loading quests for key: " + key);
            sectionObjects.add(questsObject.getAsJsonObject(key));
        });

        // Grabbing Quests
        sectionObjects.forEach(section -> {
            IndustriaDailies.LOGGER.info("Loading quests for section: " + section);
            sections.put(section, new ArrayList<>());
            section.keySet().forEach(key -> {
                if (key.equals("Weight")) {
                    return;
                }
               JsonObject questObject = section.getAsJsonObject(key);
               sections.get(section).add(questObject);
            });
        });

        // Init Quests+QuestSections
        for (Map.Entry<JsonObject, List<JsonObject>> entry : sections.entrySet()) {
            IndustriaDailies.LOGGER.info("Loading section " + entry.getKey());
            JsonObject sectionObject = entry.getKey();
            List<JsonObject> questObject = entry.getValue();
            List<Quest> questList = new ArrayList<>();

            for (JsonObject quests : questObject) {
                Quest quest = new Quest(quests.get("Id").getAsString(), quests.get("Objective").getAsString(), quests.get("Item-Needed").getAsString(), quests.get("Amount-Needed").getAsInt(), quests.get("Weight").getAsInt());
                questList.add(quest);
            }

            questSections.add(new QuestSection(sectionObject.get("Weight").getAsInt(), questList));
        }

    }

    public void verboseManager() {
        IndustriaDailies.LOGGER.info("Verbose...");

        for (Map.Entry<String, List<Quest>> entry : playerQuests.entrySet()) {
            IndustriaDailies.LOGGER.info("UUID: {}", entry.getKey());
            for (Quest quest : entry.getValue()) {
                IndustriaDailies.LOGGER.info("[{}]: {}", entry.getKey(), quest.toString());
            }
        }

        for (QuestSection questSection : questSections) {
            IndustriaDailies.LOGGER.info("Weight: {}",  questSection.getWeight());
            for (Quest quest : questSection.getQuests()) {
                IndustriaDailies.LOGGER.info("Id: {}, Objective: {}, Item-Needed: {}, Amount-Needed: {}, Weight: {}", quest.id(), quest.objective(), quest.itemNeeded(), quest.amountNeeded(), quest.weight());
            }
        }
    }

}

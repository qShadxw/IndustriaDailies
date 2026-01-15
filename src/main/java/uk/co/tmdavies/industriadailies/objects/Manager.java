package uk.co.tmdavies.industriadailies.objects;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import uk.co.tmdavies.industriadailies.IndustriaDailies;

import java.util.*;

public class Manager {

    private final Random random;

    private final HashMap<String, List<Quest>> playerQuests;
    private final List<QuestSection> questSections;

    public Manager() {
        random = new Random();
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

    public List<Quest> getPersonalQuests(Player player) {
        if (!playerQuests.containsKey(player.getStringUUID())) {
            return new ArrayList<>();
        }

        return playerQuests.get(player.getStringUUID());
    }

    public Quest getPersonalQuestFromId(Player player, String id) {
        if (!playerQuests.containsKey(player.getStringUUID())) {
            return null;
        }

        IndustriaDailies.LOGGER.info("Get Quest from id " + id);

        for (Quest quests : getPersonalQuests(player)) {
            if (quests.getId().equals(id)) {
                return quests;
            }
        }

        return null;
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

    public boolean resetPlayer(Player player) {
        if (!playerQuests.containsKey(player.getStringUUID())) {
            return false;
        }

        playerQuests.remove(player.getStringUUID());
        return true;
    }

    public void loadQuests() {
        IndustriaDailies.LOGGER.info("Loading quests...");

        JsonObject questsObject = IndustriaDailies.configFile.get("Quests");
        List<JsonObject> sectionObjects = new ArrayList<>();

        // Section, List of Quest JsonObjects
        HashMap<JsonObject, List<JsonObject>> sections = new HashMap<>();

        // Grabbing Sections
        questsObject.keySet().forEach(key -> sectionObjects.add(questsObject.getAsJsonObject(key)));

        // Grabbing Quests
        sectionObjects.forEach(section -> {
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
            JsonObject sectionObject = entry.getKey();
            List<JsonObject> questObject = entry.getValue();
            List<Quest> questList = new ArrayList<>();

            for (JsonObject quests : questObject) {
                JsonObject rewardObject = quests.getAsJsonObject("Reward");
                Quest quest = new Quest(
                        quests.get("Id").getAsString(),
                        quests.get("Objective").getAsString(),
                        quests.get("Item-Needed").getAsString(),
                        quests.get("Amount-Needed").getAsInt(),
                        rewardObject.get("ItemId").getAsString(),
                        rewardObject.get("Amount").getAsInt(),
                        quests.get("Weight").getAsInt(),
                        false
                );
                questList.add(quest);
            }

            questSections.add(new QuestSection(sectionObject.get("Weight").getAsInt(), questList));
        }

    }

    public Quest getRandomQuest() {
        IndustriaDailies.LOGGER.info("Getting random quest...");

        // Getting Section
        int totalSectionWeight = 0;

        for (QuestSection questSection : questSections) {
            totalSectionWeight += questSection.weight();
        }

        int randomSectionNumber = random.nextInt(totalSectionWeight);
        int cumulativeSection = 0;
        QuestSection questSection = null;

        for (QuestSection questSections : questSections) {
            cumulativeSection += questSections.weight();
            if (randomSectionNumber < cumulativeSection) {
                questSection = questSections;
                break;
            }
        }

        if (questSection == null) {
            IndustriaDailies.LOGGER.error("Random Weight Section came back as null.");
            return null;
        }

        // Getting Quest
        int totalQuestWeight = 0;

        for (Quest quest : questSection.quests()) {
            totalQuestWeight += quest.getWeight();
        }

        int randomQuestNumber = random.nextInt(totalQuestWeight);
        int cumulativeQuest = 0;
        Quest quest = null;

        for (Quest quests : questSection.quests()) {
            cumulativeQuest += quests.getWeight();
            if (randomQuestNumber < cumulativeQuest) {
                quest = quests;
                break;
            }
        }

        if (quest == null) {
            IndustriaDailies.LOGGER.error("Random Weight Quest came back as null.");
            return null;
        }

        return quest;
    }

    public void generateQuestsForPlayer(Player player) {
        List<Quest> quests = getPersonalQuests(player);
        boolean temp;

        for (int i = 0; quests.size() < 3; i++) {
            temp = false;
            Quest quest = this.getRandomQuest();

            for (Quest questList : quests) {
                if (Objects.equals(questList.getId(), quest.getId())) {
                    temp = true;
                }
            }

            if (temp) {
                continue;
            }

            quests.add(quest);
        }

        playerQuests.put(player.getStringUUID(), quests);
    }

    public boolean completeQuest(Player player, String questId) {
        if (!playerQuests.containsKey(player.getStringUUID())) {
            return false;
        }

        List<Quest> personalQuests =  playerQuests.get(player.getStringUUID());
        Quest quest = null;

        for (Quest quests : personalQuests) {
            if (Objects.equals(quests.getId(), questId)) {
                quest = quests;
                break;
            }
        }

        if (quest == null) {
            return false;
        }

        ItemStack item = player.getMainHandItem();
        ResourceLocation id = item.getItem().builtInRegistryHolder().key().location();

        if (!id.toString().equals(quest.getItemNeeded())) {
            return false;
        }

        int itemCount = item.getCount();

        if (itemCount < quest.getAmountNeeded()) {
            return false;
        }

        if ((itemCount - quest.getAmountNeeded()) <= 0) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            item.setCount(itemCount - quest.getAmountNeeded());
            player.setItemInHand(InteractionHand.MAIN_HAND, item);
        }

        return true;
    }

    public void setQuestAsCompleted(Player player, String questId) {
        Quest quest = getPersonalQuestFromId(player, questId);

        playerQuests.get(player.getStringUUID()).remove(quest);

        quest.setCompleted(true);

        playerQuests.get(player.getStringUUID()).add(quest);
    }

    public boolean hasQuests(Player player) {
        return playerQuests.containsKey(player.getStringUUID());
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
            IndustriaDailies.LOGGER.info("Weight: {}",  questSection.weight());
            for (Quest quest : questSection.quests()) {
                IndustriaDailies.LOGGER.info("Id: {}, Objective: {}, Item-Needed: {}, Amount-Needed: {}, Weight: {}", quest.getId(), quest.getObjective(), quest.getItemNeeded(), quest.getAmountNeeded(), quest.getWeight());
            }
        }
    }

}

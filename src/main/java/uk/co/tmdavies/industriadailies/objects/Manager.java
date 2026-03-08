package uk.co.tmdavies.industriadailies.objects;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.savedata.TargetDataStorage;

import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Manager {

    private final Random random;

    private final HashMap<String, List<Quest>> playerQuests;
    public HashMap<String, ArrayList<Quest>> playerSetQuests;
    private final List<QuestSection> questSections;
    public ArrayList<Quest> setQuests;

    public Manager() {
        random = new Random();
        playerQuests = new HashMap<>();
        questSections = new ArrayList<>();
        playerSetQuests = new HashMap<>();
        setQuests = new ArrayList<>();
    }

    public boolean initPlayer(Player player) {
        if (playerQuests.containsKey(player.getStringUUID())) {
            return false;
        }
        playerQuests.put(player.getStringUUID(), new ArrayList<>());
        return true;
    }

    public void initSaveData(MinecraftServer server)
    {
        playerSetQuests = TargetDataStorage.playerLoad(server);
        setQuests = TargetDataStorage.questLoad(server);
    }

    public void saveSaveData(MinecraftServer server)
    {
        new Thread(() -> {
            TargetDataStorage.playerSave(server);
            TargetDataStorage.questSave(server);
        }).start();
    }



    public boolean fullQuestCheckComplete(Player player, Quest quest, Entity target)
    {
        if (quest == null) return false;
        if (quest.isCompleted()) return false;

        if (player.getInventory().contains(quest.getItemNeededAsItemstack())) {
            if (player.getInventory().getItem(player.getInventory().findSlotMatchingItem(quest.getItemNeededAsItemstack())).getCount() >= quest.getAmountNeeded()) {
                quest.setCompleted(true);
                player.getInventory().removeItem(player.getInventory().findSlotMatchingItem(quest.getItemNeededAsItemstack()), quest.getAmountNeeded());
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }

        if (((quest.getTalkTo() != null) && !quest.getTalkTo().isEmpty()) && !quest.hasTalkedTo) {
            //LOGGER.info("here " + quest.getTalkTo());
            return false;
        }

        this.setSetQuestAsCompleted(player, quest.getId());
        if (quest.getRewardItemId().equals("irs")) {
            IndustriaDailies.neoNetworkIRS.giveMoney(player, quest.getRewardItemAmount(), String.format("Completed %s", quest.getId()));
        }
        else {
            player.getInventory().add(quest.getReward());
        }
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        Objective objective = scoreboard.getObjective(quest.getId());
        if (objective != null)
        {
            ScoreAccess score = scoreboard.getOrCreatePlayerScore(player, objective);
            score.add(1);
        }

        this.saveSaveData(player.getServer());
        return true;
    }

    public boolean addSetQuest(String id, Player p)
    {
        for (int i = 0; i < setQuests.size(); i++)
        {
            if (Objects.equals(setQuests.get(i).getId(), id))
            {
                playerSetQuests.computeIfAbsent(p.getStringUUID(), k -> new ArrayList<>());

                if (playerSetQuests.get(p.getStringUUID()).contains(setQuests.get(i))) return true;

                playerSetQuests.get(p.getStringUUID()).add(setQuests.get(i).copy());
                return false;
            }
        }

        return true;
    }

    public Quest getSetQuest(String id)
    {
        for (int i = 0; i < setQuests.size(); i++)
        {
            if (Objects.equals(setQuests.get(i).getId(), id))
            {
                return setQuests.get(i);
            }
        }

        return null;
    }

    public Quest getPlayersSetQuest(Player p, String id)
    {
        if (!playerSetQuests.containsKey(p.getStringUUID())) {
            return null;
        }

        for (Quest quests : getPlayerSetQuests(p)) {
            if (quests.getId().equals(id)) {
                return quests;
            }
        }

        return null;
    }

    public void setSetQuestAsCompleted(Player player, String questId) {
        Quest quest = getPlayersSetQuest(player, questId);
        playerSetQuests.get(player.getStringUUID()).remove(quest);

        /*for (int i = 0; i < playerSetQuests.get(player.getStringUUID()).size(); i++)
        {
            if (Objects.equals(playerSetQuests.get(player.getStringUUID()).get(i).getId(), questId))
            {
                playerSetQuests.get(player.getStringUUID()).get(i).setCompleted(true);
            }
        }*/

        quest.setCompleted(true);
        playerSetQuests.get(player.getStringUUID()).add(quest);
    }

    public boolean completeSetQuest(Player player, String questId, ItemStack item) {
        return completeSetQuests(player, questId, item, null);
    }

    public boolean completeSetQuests(Player player, String questId, ItemStack item, UUID runner) {
        if (!playerSetQuests.containsKey(player.getStringUUID())) {
            LOGGER.info("Here");
            return false;
        }
        LOGGER.info("Here1");

        List<Quest> setQuests =  playerSetQuests.get(player.getStringUUID());
        Quest quest = null;

        for (Quest quests : setQuests) {
            if (Objects.equals(quests.getId(), questId)) {
                if (quests.getHandIn() != null && !(quests.checkHandIn(runner.toString()))) return false;
                LOGGER.info("Here2");
                quest = quests;
                break;
            }
        }
        if (quest == null) {
            LOGGER.info("Here3");
            return false;
        }

        ResourceLocation id = item.getItem().builtInRegistryHolder().key().location();
        LOGGER.info("Here4");

        if (!id.toString().equals(quest.getItemNeeded()) && quest.getItemNeeded() != "null") {
            LOGGER.info("Here5");
            return false;
        }

        int itemCount = item.getCount();
        LOGGER.info("Here5.5");

        if (itemCount < quest.getAmountNeeded() && quest.getAmountNeeded() != -1) {
            LOGGER.info("Here6");
            return false;
        }

        if(quest.getAmountNeeded() != -1) item.setCount(Math.max((itemCount - quest.getAmountNeeded()), 0));
        LOGGER.info("Here 7");

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

    public List<Quest> getPlayerSetQuests(Player player) {
        if (!playerSetQuests.containsKey(player.getStringUUID())) {
            return new ArrayList<>();
        }

        return playerSetQuests.get(player.getStringUUID());
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

            quests.add(quest.copy());
        }

        playerQuests.put(player.getStringUUID(), quests);
    }

    public boolean completeQuest(Player player, String questId, ItemStack item) {
        if (!playerQuests.containsKey(player.getStringUUID())) {
            return false;
        }

        if (getPersonalQuestFromId(player, questId).isCompleted()) {
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

        ResourceLocation id = item.getItem().builtInRegistryHolder().key().location();

        if (!id.toString().equals(quest.getItemNeeded())) {
            return false;
        }

        int itemCount = item.getCount();

        if (itemCount < quest.getAmountNeeded()) {
            return false;
        }

        item.setCount(Math.max((itemCount - quest.getAmountNeeded()), 0));

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

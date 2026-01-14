package uk.co.tmdavies.industriadailies.objects;

import java.util.ArrayList;
import java.util.List;

public class QuestSection {

    private final int weight;
    private final List<Quest> quests;

    public QuestSection(int weight) {
        this.weight = weight;
        this.quests = new ArrayList<>();
    }

    public QuestSection(int weight, List<Quest> quests) {
        this.weight = weight;
        this.quests = quests;
    }

    public int getWeight() {
        return weight;
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public boolean addQuest(Quest quest) {
        if (quests.contains(quest)) {
            return false;
        }

        return quests.add(quest);
    }

    public boolean removeQuest(Quest quest) {
        return quests.remove(quest);
    }
}

package uk.co.tmdavies.industriadailies.objects;

import java.util.ArrayList;
import java.util.List;

public record QuestSection(int weight, List<Quest> quests) {

    public QuestSection(int weight) {
        this(weight, new ArrayList<>());
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

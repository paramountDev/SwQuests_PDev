package dev.paramountdev.swquest_pdev.models;

import java.util.List;

public class QuestGroup {

    private final String id;
    private final String name;
    private final List<Quest> quests;
    private final List<Reward> rewards;

    public QuestGroup(String id, String name, List<Quest> quests, List<Reward> rewards) {
        this.id = id;
        this.name = name;
        this.quests = quests;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public Quest getFirstQuest() {
        return quests.isEmpty() ? null : quests.get(0);
    }

    public Quest getNextQuest(String questId) {
        for (int i = 0; i < quests.size(); i++) {
            if (quests.get(i).getId().equals(questId) && i + 1 < quests.size()) {
                return quests.get(i + 1);
            }
        }
        return null;
    }

    public boolean isLastQuest(String questId) {
        return !quests.isEmpty() && quests.get(quests.size() - 1).getId().equals(questId);
    }
}



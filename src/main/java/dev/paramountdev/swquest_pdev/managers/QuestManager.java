package dev.paramountdev.swquest_pdev.managers;

import dev.paramountdev.swquest_pdev.SwQuest_PDev;
import dev.paramountdev.swquest_pdev.menu.QuestMenu;
import dev.paramountdev.swquest_pdev.models.ItemRequirement;
import dev.paramountdev.swquest_pdev.models.MobKillRequirement;
import dev.paramountdev.swquest_pdev.models.PlayerKillRequirement;
import dev.paramountdev.swquest_pdev.models.Quest;
import dev.paramountdev.swquest_pdev.models.QuestGroup;
import dev.paramountdev.swquest_pdev.models.Requirement;
import dev.paramountdev.swquest_pdev.models.Reward;
import dev.paramountdev.swquest_pdev.models.RewardRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class QuestManager implements Listener {

    private final Map<UUID, Map<String, String>> activeQuests = new HashMap<>(); // ююид groupId questId
    private final Map<UUID, Map<String, Integer>> questProgress = new HashMap<>();
    private final Map<UUID, Set<String>> completedGroups = new HashMap<>();
    private final Map<UUID, Set<String>> completedQuests = new HashMap<>();
    private String prefix = SwQuest_PDev.getPrefix();

    private final Logger logger = Bukkit.getLogger();
    private FileConfiguration questConfig;

    private final Map<String, QuestGroup> questGroups = new LinkedHashMap<>();
    private final List<String> groupOrder = new ArrayList<>();


    public QuestManager(File dataFolder) {
        loadQuests(dataFolder);
    }

    public void loadQuests(File dataFolder) {
        File file = new File(dataFolder, "config.yml");
        if (!file.exists()) {
            logger.warning("Файл config.yml не найден!");
        }
        questConfig = YamlConfiguration.loadConfiguration(file);

        if (!questConfig.isConfigurationSection("quest_groups")) {
            return;
        }

        groupOrder.clear();
        for (String groupId : questConfig.getConfigurationSection("quest_groups").getKeys(false)) {
            String base = "quest_groups." + groupId;

            String name = questConfig.getString(base + ".name", groupId);
            List<Quest> quests = new ArrayList<>();

            List<Map<?, ?>> questList = questConfig.getMapList(base + ".quests");
            for (Map<?, ?> questData : questList) {
                String id = (String) questData.get("id");
                String questName = questData.containsKey("name")
                        ? (String) questData.get("name")
                        : id;
                String description = questData.containsKey("description")
                        ? (String) questData.get("description")
                        : "Нет описания";


                List<Requirement> requirements = new ArrayList<>();
                if (questData.containsKey("required_items")) {
                    Map<String, Object> items = (Map<String, Object>) questData.get("required_items");
                    for (Map.Entry<String, Object> entry : items.entrySet()) {
                        String key = entry.getKey().toUpperCase();
                        int amount = (int) entry.getValue();

                        switch (key) {
                            case "NIGHT_VISION_POTION":
                                requirements.add(new ItemRequirement(Material.POTION, amount, PotionType.NIGHT_VISION));
                                break;
                            case "POTION_OF_SWIFTNESS":
                                requirements.add(new ItemRequirement(Material.POTION, amount, PotionType.SPEED));
                                break;
                            case "POTION_OF_STRENGTH":
                                requirements.add(new ItemRequirement(Material.POTION, amount, PotionType.STRENGTH));
                                break;
                            default:
                                Material mat = Material.matchMaterial(key);
                                if (mat != null) {
                                    requirements.add(new ItemRequirement(mat, amount, null));
                                }
                                break;
                        }
                    }
                }


                if (questData.containsKey("required_player_kills")) {
                    int kills = (int) questData.get("required_player_kills");
                    requirements.add(new PlayerKillRequirement(kills));
                }

                if (questData.containsKey("required_mob_kills")) {
                    Map<String, Object> mobs = (Map<String, Object>) questData.get("required_mob_kills");
                    for (Map.Entry<String, Object> entry : mobs.entrySet()) {
                        try {
                            EntityType type = EntityType.valueOf(entry.getKey().toUpperCase());
                            int count = (int) entry.getValue();
                            requirements.add(new MobKillRequirement(type, count));
                        } catch (IllegalArgumentException ex) {
                            logger.warning("Неизвестный тип моба: " + entry.getKey());
                        }
                    }
                }


                quests.add(new Quest(id, questName, description, requirements));
            }

            List<Reward> groupRewards = new ArrayList<>();
            if (questConfig.isList(base + ".group_rewards")) {
                List<Map<?, ?>> rewardsList = questConfig.getMapList(base + ".group_rewards");
                for (Map<?, ?> rewardData : rewardsList) {
                    String type = (String) rewardData.get("type");
                    String value = String.valueOf(rewardData.get("value"));
                    Reward reward = RewardRegistry.create(type, value);
                    if (reward != null) {
                        groupRewards.add(reward);
                    }
                }
            } else if (questConfig.isConfigurationSection(base + ".group_rewards")) {
                for (String type : questConfig.getConfigurationSection(base + ".group_rewards").getKeys(false)) {
                    String value = String.valueOf(questConfig.get(base + ".group_rewards." + type));
                    Reward reward = RewardRegistry.create(type, value);
                    if (reward != null) {
                        groupRewards.add(reward);
                    }
                }
            }

            questGroups.put(groupId, new QuestGroup(groupId, name, quests, groupRewards));
            groupOrder.add(groupId);
        }
    }

    public void completeQuest(Player player, String groupId, String questId) {
        completedQuests.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(questId);

        QuestGroup group = questGroups.get(groupId);
        if (group == null) return;

        Quest next = group.getNextQuest(questId);
        if (next != null) {
            activeQuests.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(groupId, next.getId());
            player.sendMessage("");
            player.sendMessage(prefix + ChatColor.AQUA + "Открылся новый квест: " + next.getName());
            player.sendMessage("");
        }
    }

    public Map<String, QuestGroup> getQuestGroups() {
        return questGroups;
    }

    public Set<String> getCompletedQuests(UUID playerId) {
        return completedQuests.getOrDefault(playerId, new HashSet<>());
    }


    public List<QuestGroup> getGroupsInOrder() {
        List<QuestGroup> ordered = new ArrayList<>();
        for (String id : groupOrder) {
            QuestGroup g = questGroups.get(id);
            if (g != null) ordered.add(g);
        }
        return ordered;
    }

    public void completeGroup(Player player, String groupId) {
        QuestGroup group = questGroups.get(groupId);
        if (group == null) return;
        Set<String> completed = completedQuests.getOrDefault(player.getUniqueId(), new HashSet<>());
        boolean allCompleted = group.getQuests().stream().allMatch(q -> completed.contains(q.getId()));
        if (!allCompleted) {
            player.sendMessage("");
            player.sendMessage(prefix + ChatColor.RED + "Вы ещё не завершили все квесты этой группы!");
            player.sendMessage("");
            return;
        }
        if (completedGroups.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).contains(groupId)) {
            player.sendMessage("");
            player.sendMessage(prefix + ChatColor.YELLOW + "Вы уже завершили эту группу.");
            player.sendMessage("");
            return;
        }
        completedGroups.get(player.getUniqueId()).add(groupId);
        player.sendMessage("");
        player.sendMessage(prefix + ChatColor.GOLD + "Вы завершили группу: " + group.getName());
        player.sendMessage("");
        markGroupRewardClaimed(player, groupId);
        for (Reward reward : group.getRewards()) {
            reward.giveTo(player);
        }
    }

    private final Map<UUID, Set<String>> claimedGroupRewards = new HashMap<>();

    public boolean isGroupRewardClaimed(Player player, String groupId) {
        return claimedGroupRewards.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(groupId);
    }

    public void markGroupRewardClaimed(Player player, String groupId) {
        claimedGroupRewards.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(groupId);
    }


    public int getProgress(UUID playerId, String questId) {
        return questProgress.getOrDefault(playerId, new HashMap<>()).getOrDefault(questId, 0);
    }

    public void addProgress(UUID playerId, String questId, int amount, int max) {
        questProgress.computeIfAbsent(playerId, k -> new HashMap<>());
        int current = getProgress(playerId, questId);
        int newValue = Math.min(current + amount, max);
        questProgress.get(playerId).put(questId, newValue);
    }

    public void setProgress(UUID playerId, String questId, int value) {
        questProgress.computeIfAbsent(playerId, k -> new HashMap<>()).put(questId, value);
    }

    public boolean isGroupCompleted(Player player, String groupId) {
        QuestGroup group = questGroups.get(groupId);
        if (group == null) return false;

        UUID playerId = player.getUniqueId();
        Set<String> completedQuests = getCompletedQuests(playerId);

        // Проверяем, выполнены ли все квесты группы
        return group.getQuests().stream()
                .allMatch(q -> completedQuests.contains(q.getId()));
    }


    public void openQuestMenu(Player player) {
        QuestMenu.openQuestMenu(player);
    }

    public Map<UUID, Map<String, String>> getActiveQuests() {
        return activeQuests;
    }

}

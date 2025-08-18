package dev.paramountdev.swquest_pdev.menu;

import dev.paramountdev.swquest_pdev.SwQuest_PDev;
import dev.paramountdev.swquest_pdev.managers.QuestManager;
import dev.paramountdev.swquest_pdev.models.ItemRequirement;
import dev.paramountdev.swquest_pdev.models.MobKillRequirement;
import dev.paramountdev.swquest_pdev.models.PlayerKillRequirement;
import dev.paramountdev.swquest_pdev.models.Quest;
import dev.paramountdev.swquest_pdev.models.QuestGroup;
import dev.paramountdev.swquest_pdev.models.Requirement;
import dev.paramountdev.swquest_pdev.models.Reward;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class QuestMenu implements Listener {

    private static final QuestManager questManager = SwQuest_PDev.getQuestManager();
    private String prefix = SwQuest_PDev.getPrefix();
    private static String menuName = "§6§lМеню Квестов";

    private static final Material[] SHULKER_COLORS = {
            Material.WHITE_SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.PINK_SHULKER_BOX,
            Material.LIME_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.RED_SHULKER_BOX
    };

    public static void openQuestMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, menuName);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        int groupIndex = 0;

        boolean previousCompleted = true;

        for (QuestGroup group : questManager.getGroupsInOrder()) {
            int rowStart = groupIndex * 9;
            if (rowStart >= inv.getSize()) break;

            int slot = rowStart + 1;
            boolean groupUnlocked = previousCompleted;
            boolean firstIncompleteFound = false;

            for (Quest quest : group.getQuests()) {
                boolean completed = questManager.getCompletedQuests(player.getUniqueId()).contains(quest.getId());
                ItemStack questItem;

                if (completed) {
                    questItem = new ItemStack(getQuestIcon(quest));
                    ItemMeta questMeta = questItem.getItemMeta();
                    questMeta.setDisplayName(ChatColor.GOLD + "§l" + quest.getName());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + quest.getDescription());
                    lore.add("");
                    for (Requirement req : quest.getRequirements()) {
                        lore.add("§7Прогресс: " + ChatColor.AQUA + "§l" + req.getProgress(player));
                    }
                    lore.add("");
                    lore.add("§7Статус: " + ChatColor.GREEN + "§lВыполнено!");
                    questMeta.setLore(lore);
                    questMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    questMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    questItem.setItemMeta(questMeta);
                } else if (!firstIncompleteFound && groupUnlocked) {
                    questItem = new ItemStack(getQuestIcon(quest));

                    ItemMeta questMeta = questItem.getItemMeta();
                    questMeta.setDisplayName(ChatColor.GOLD + "§l" + quest.getName());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + quest.getDescription());
                    lore.add("");

                    for (Requirement req : quest.getRequirements()) {
                        lore.add("§7Прогресс: " + ChatColor.AQUA + "§l" + req.getProgress(player));
                    }

                    lore.add("");
                    lore.add("§7Статус: " + ChatColor.RED + "§lНе выполнено.");
                    lore.add("");
                    lore.add("");
                    questMeta.setLore(lore);
                    questItem.setItemMeta(questMeta);

                    firstIncompleteFound = true;
                } else {
                    questItem = new ItemStack(Material.BARRIER);
                    ItemMeta meta = questItem.getItemMeta();
                    meta.setDisplayName(ChatColor.DARK_RED + "§lНЕДОСТУПНО");
                    meta.setLore(Collections.singletonList(ChatColor.GRAY + "Завершите предыдущие квесты"));
                    questItem.setItemMeta(meta);
                }

                inv.setItem(slot++, questItem);
            }

            previousCompleted = group.getQuests().stream()
                    .allMatch(q -> questManager.getCompletedQuests(player.getUniqueId()).contains(q.getId()));

            int shulkerSlot = rowStart + 8;
            Material shulkerColor = SHULKER_COLORS[groupIndex % SHULKER_COLORS.length];
            ItemStack shulker = new ItemStack(shulkerColor);
            ItemMeta shulkerMeta = shulker.getItemMeta();
            shulkerMeta.setDisplayName("§b§l" + group.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.YELLOW + " Выполненные квесты:");
            boolean hasCompleted = false;
            for (Quest q : group.getQuests()) {
                if (questManager.getCompletedQuests(player.getUniqueId()).contains(q.getId())) {
                    lore.add(ChatColor.GREEN + " - " + q.getName());
                    hasCompleted = true;
                }
            }
            if (!hasCompleted) {
                lore.add(ChatColor.GRAY + "Нет выполненных квестов");
            }

            if (!previousCompleted) {
                lore.add("");
                lore.add(ChatColor.DARK_RED + "§lНельзя завершить: не все квесты выполнены!");
                lore.add("");
            } else {
                lore.add("");
            }

            shulkerMeta.setLore(lore);
            shulker.setItemMeta(shulkerMeta);
            inv.setItem(shulkerSlot, shulker);

            groupIndex++;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
        player.openInventory(inv);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if (e.getView().getTitle().equals(menuName)) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || meta.getDisplayName() == null) return;

            if (clicked.getType().toString().endsWith("SHULKER_BOX")) {
                String groupName = ChatColor.stripColor(meta.getDisplayName());
                QuestGroup group = questManager.getQuestGroups().values().stream()
                        .filter(g -> g.getName().equalsIgnoreCase(groupName))
                        .findFirst().orElse(null);

                if (group != null) {
                    UUID playerId = player.getUniqueId();

                    // Проверяем, завершены ли все квесты
                    boolean allCompleted = group.getQuests().stream()
                            .allMatch(q -> questManager.getCompletedQuests(playerId).contains(q.getId()));

                    if (allCompleted) {
                        // Проверяем, не получал ли игрок награду за группу
                        if (questManager.isGroupRewardClaimed(player, group.getId())) {
                            player.sendMessage(prefix + ChatColor.YELLOW + "Вы уже получили награду за эту группу!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            return;
                        }

                        // Отмечаем, что награда получена
                        questManager.markGroupRewardClaimed(player, group.getId());

                        // Выдаём награды
                        for (Reward reward : group.getRewards()) {
                            reward.giveTo(player);
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        player.closeInventory();
                    } else {
                        player.sendMessage(prefix + ChatColor.RED + "Вы не выполнили все квесты этой группы!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    }
                }
                return;
            }

            String questName = ChatColor.stripColor(meta.getDisplayName());
            QuestGroup group = questManager.getQuestGroups().values().stream()
                    .filter(g -> g.getQuests().stream().anyMatch(q -> q.getName().equalsIgnoreCase(questName)))
                    .findFirst().orElse(null);

            if (group != null) {
                Quest quest = group.getQuests().stream()
                        .filter(q -> q.getName().equalsIgnoreCase(questName))
                        .findFirst().orElse(null);

                if (quest != null && clicked.getType() == getQuestIcon(quest)) {
                    Quest firstIncomplete = group.getQuests().stream()
                            .filter(q -> !questManager.getCompletedQuests(player.getUniqueId()).contains(q.getId()))
                            .findFirst().orElse(null);

                    if (firstIncomplete == null || !firstIncomplete.getId().equals(quest.getId())) {
                        player.sendMessage(prefix + ChatColor.RED + "Сначала нужно выполнить предыдущие квесты!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        return;
                    }

                    for (Requirement req : quest.getRequirements()) {
                        req.consumeFrom(player);
                        openQuestMenu(player);
                    }

                    if (quest.canBeCompletedBy(player)) {
                        quest.complete(player);
                        questManager.completeQuest(player, group.getId(), quest.getId());
                        openQuestMenu(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    } else {
                        player.sendMessage(prefix + ChatColor.RED + "Не выполнены условия для прохождения квеста!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    }
                }
            }
        }
    }

    private static Material getQuestIcon(Quest quest) {
        if (quest.getRequirements().isEmpty()) {
            return Material.BOOK;
        }

        Requirement req = quest.getRequirements().get(0);

        if (req instanceof ItemRequirement) {
            return ((ItemRequirement) req).getMaterial();
        }

        if (req instanceof MobKillRequirement) {
            EntityType mob = ((MobKillRequirement) req).getMobType();
            try {
                return Material.valueOf(mob.name() + "_SPAWN_EGG");
            } catch (IllegalArgumentException e) {
                return Material.LEGACY_MONSTER_EGG;
            }
        }


        if (req instanceof PlayerKillRequirement) {
            return Material.NETHERITE_SWORD;
        }


        return Material.BOOK;
    }


    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
}

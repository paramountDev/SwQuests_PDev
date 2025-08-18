package dev.paramountdev.swquest_pdev.models;

import dev.paramountdev.swquest_pdev.SwQuest_PDev;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobKillRequirement implements Requirement, Listener {

    private final EntityType type;
    private final int amount;
    private final Map<UUID, Integer> kills = new HashMap<>();

    public MobKillRequirement(EntityType type, int amount) {
        this.type = type;
        this.amount = amount;
        Bukkit.getPluginManager().registerEvents(this, SwQuest_PDev.getInstance());
    }

    @Override
    public boolean isMetBy(Player player) {
        return kills.getOrDefault(player.getUniqueId(), 0) >= amount;
    }

    @Override
    public void consumeFrom(Player player) {
    }

    @Override
    public String getDescription() {
        return "Убить " + amount + " мобов " + type.name();
    }

    @Override
    public String getProgress(Player player) {
        int current = Math.min(kills.getOrDefault(player.getUniqueId(), 0), amount);
        return current + "/" + amount;
    }

    @Override
    public void addProgress(Player player, int amt) {
        kills.put(player.getUniqueId(),
                Math.min(getCurrentProgress(player) + amt, amount));
    }

    @Override
    public int getCurrentProgress(Player player) {
        return kills.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public int getMaxProgress() {
        return amount;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getType() == type && e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            UUID uuid = killer.getUniqueId();
            addProgress(killer, 1);
        }
    }
    public EntityType getMobType() {
        return type;
    }

}

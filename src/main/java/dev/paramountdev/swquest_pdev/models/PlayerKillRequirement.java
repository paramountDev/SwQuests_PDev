package dev.paramountdev.swquest_pdev.models;

import dev.paramountdev.swquest_pdev.SwQuest_PDev;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerKillRequirement implements Requirement, Listener {

    private final int amount;
    private final Map<UUID, Integer> kills = new HashMap<>();

    public PlayerKillRequirement(int amount) {
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
        return "Убить " + amount + " игроков";
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
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            UUID uuid = killer.getUniqueId();
            addProgress(killer, 1);
        }
    }
}

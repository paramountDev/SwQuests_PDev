package dev.paramountdev.swquest_pdev.models;

import org.bukkit.entity.Player;

public interface Requirement {
    boolean isMetBy(Player player);
    void consumeFrom(Player player);
    String getProgress(Player player);
    void addProgress(Player player, int amount);
    int getCurrentProgress(Player player);
    int getMaxProgress();
    String getDescription();
}


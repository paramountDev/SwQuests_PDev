package dev.paramountdev.swquest_pdev.models;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemRequirement implements Requirement {

    private final Material material;
    private final int amount;
    private final Map<UUID, Integer> delivered = new HashMap<>();
    private final PotionType potionType;

    public ItemRequirement(Material material, int amount, PotionType potionType) {
        this.material = material;
        this.amount = amount;
        this.potionType = potionType;
    }

    @Override
    public boolean isMetBy(Player player) {
        int current = getCurrentProgress(player);

        int inInventory = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                inInventory += item.getAmount();
                if (current + inInventory >= amount) return true;
            }
        }
        return current >= amount;
    }


    @Override
    public void consumeFrom(Player player) {
        int need = amount - getCurrentProgress(player);
        int have = countItems(player, material);

        int toRemove = Math.min(have, need);
        if (toRemove > 0) {
            removeItems(player, material, toRemove);
            addProgress(player, toRemove);
        }
    }

    private int countItems(Player player, Material mat) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, Material mat, int amount) {
        int toRemove = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                int amt = item.getAmount();
                if (amt > toRemove) {
                    item.setAmount(amt - toRemove);
                    break;
                } else {
                    player.getInventory().removeItem(item);
                    toRemove -= amt;
                    if (toRemove <= 0) break;
                }
            }
        }
    }

    @Override
    public String getProgress(Player player) {
        int current = Math.min(delivered.getOrDefault(player.getUniqueId(), 0), amount);
        return current + "/" + amount;
    }

    @Override
    public void addProgress(Player player, int amt) {
        delivered.put(player.getUniqueId(),
                Math.min(getCurrentProgress(player) + amt, amount));
    }

    @Override
    public int getCurrentProgress(Player player) {
        return delivered.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public int getMaxProgress() {
        return amount;
    }

    @Override
    public String getDescription() {
        return amount + " x " + material.name();
    }

    public Material getMaterial() {
        return material;
    }

}


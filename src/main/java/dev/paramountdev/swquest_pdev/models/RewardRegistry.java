package dev.paramountdev.swquest_pdev.models;

import dev.paramountdev.swquest_pdev.SwQuest_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RewardRegistry {

    private static final Map<String, Function<String, Reward>> rewardFactories = new HashMap<>();
    private static Economy economy;
    private static String prefix = SwQuest_PDev.getPrefix();

    static {
        rewardFactories.put("MONEY", value -> player -> {
            if (economy != null) {
                double amount = Double.parseDouble(value);
                economy.depositPlayer(player, amount);
                player.sendMessage(prefix + "§aВы получили §e" + amount + "$");
                player.sendMessage("");
            } else {
                player.sendMessage(prefix + "§cЭкономика не подключена! Награда в деньгах не выдана.");
                player.sendMessage("");
            }
        });

        rewardFactories.put("EXPERIENCE", value -> player -> {
            int amount = Integer.parseInt(value);
            player.giveExp(amount);
            player.sendMessage(prefix + "§aВы получили §e" + amount + " §aопыта");
            player.sendMessage("");
        });

        rewardFactories.put("ITEM", value -> player -> {
            String[] parts = value.split(":");
            if (parts.length != 2) {
                player.sendMessage(prefix + "§cНеверный формат награды ITEM. Пример: DIAMOND:5");
                player.sendMessage("");
                return;
            }
            try {
                Material material = Material.valueOf(parts[0].toUpperCase());
                int amount = Integer.parseInt(parts[1]);
                ItemStack item = new ItemStack(material, amount);
                player.getInventory().addItem(item);
                player.sendMessage(prefix + "§aВы получили §e" + amount + " §a" + material.name());
                player.sendMessage("");
            } catch (IllegalArgumentException e) {
                player.sendMessage(prefix + "§cНеизвестный предмет: " + parts[0]);
                player.sendMessage("");
            }
        });
    }

    public static Reward create(String type, String value) {
        Function<String, Reward> factory = rewardFactories.get(type.toUpperCase());
        if (factory != null) {
            return factory.apply(value);
        }
        return null;
    }

    public static void setEconomy(Economy eco) {
        economy = eco;
    }
}


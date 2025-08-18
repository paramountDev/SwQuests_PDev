package dev.paramountdev.swquest_pdev;

import dev.paramountdev.swquest_pdev.commands.QuestCommand;
import dev.paramountdev.swquest_pdev.managers.QuestManager;
import dev.paramountdev.swquest_pdev.menu.QuestMenu;
import dev.paramountdev.swquest_pdev.models.RewardRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SwQuest_PDev extends JavaPlugin {

    private static SwQuest_PDev instance;
    private QuestManager questManager;
    private static String prefix = "§5§l[§6§lQuests§5§l]§r ";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        instance = this;
        questManager = new QuestManager(getDataFolder());

        if (!setupEconomy()) {
            getLogger().warning("Vault не найден или экономика не подключена!");
        }

        setUpListeners();
        setUpCommands();

        sendSignatureToConsole("enabled");
    }

    @Override
    public void onDisable() {
        sendSignatureToConsole("disabled");
    }


    private void setUpCommands(){
        QuestCommand questCommand = new QuestCommand(questManager);
        getCommand("swquest").setExecutor(questCommand);
        getCommand("swquest").setTabCompleter(questCommand);
    }

    private void setUpListeners() {
        Bukkit.getPluginManager().registerEvents(questManager, this);
        Bukkit.getPluginManager().registerEvents(new QuestMenu(), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        RewardRegistry.setEconomy(rsp.getProvider());
        return true;
    }





    public static String getPrefix() {
        return prefix;
    }

    public static QuestManager getQuestManager() {
        return instance.questManager;
    }

    public static SwQuest_PDev getInstance() {
        return instance;
    }






    private void sendSignatureToConsole(String pluginStatus) {
        final String reset = "\u001B[0m";
        final int width = 58;
        final String borderColor = "\u001B[95m";

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

        sendEmptyLinesToConsole(8, console);

        console.sendMessage(borderColor + "╔" + "═".repeat(width) + "╗" + reset);

        console.sendMessage(colorizeCenteredText("⚡️ SwQuest Plugin " + pluginStatus + " ⚡️", width, "\u001B[35m", "\u001B[32m", "\u001B[35m") + reset);
        console.sendMessage(colorizeCenteredText("Made by ParamountDev", width, "\u001B[35m", "\u001B[95m", "\u001B[35m") + reset);
        sendEmptyLinesToConsole(1, console);
        console.sendMessage(colorizeCenteredText("✉️  Telegram: https://t.me/paramount1_dev ✉️", width, "\u001B[96m", "\u001B[97m", "\u001B[96m") + reset);
        console.sendMessage(colorizeCenteredText("💰 FunPay: https://funpay.com/uk/users/14397429/ 💰", width, "\u001B[32m", "\u001B[97m", "\u001B[32m") + reset);

        console.sendMessage(borderColor + "╚" + "═".repeat(width) + "╝" + reset);

        sendEmptyLinesToConsole(8, console);
    }

    private void sendEmptyLinesToConsole(int count, ConsoleCommandSender console) {
        for (int i = 0; i < count; i++) {
            console.sendMessage("");
        }
    }
    private String colorizeCenteredText(String text, int width, String colorStart, String colorMiddle, String colorEnd) {
        String cleanText = text.replaceAll("\u001B\\[[;\\d]*m", "");
        int textLength = cleanText.length();

        int totalPadding = width - textLength;
        int paddingLeft = totalPadding / 2;
        int paddingRight = totalPadding - paddingLeft;

        StringBuilder colored = new StringBuilder();
        colored.append(" ".repeat(Math.max(0, paddingLeft)));

        if (textLength == 0) {
            colored.append(" ".repeat(textLength));
        } else if (textLength == 1) {
            colored.append(colorStart).append(text);
        } else {
            String firstChar = text.substring(0, 1);
            String middleChars = text.substring(1, text.length() - 1);
            String lastChar = text.substring(text.length() - 1);

            colored.append(colorStart).append(firstChar);
            colored.append(colorMiddle != null ? colorMiddle : colorStart).append(middleChars);
            if (colorEnd != null) {
                colored.append(colorEnd).append(lastChar);
            } else {
                colored.append(colorStart).append(lastChar);
            }
        }

        colored.append(" ".repeat(Math.max(0, paddingRight)));
        return colored.toString();
    }
}

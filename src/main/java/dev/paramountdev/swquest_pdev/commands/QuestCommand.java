package dev.paramountdev.swquest_pdev.commands;

import dev.paramountdev.swquest_pdev.SwQuest_PDev;
import dev.paramountdev.swquest_pdev.managers.QuestManager;
import dev.paramountdev.swquest_pdev.models.QuestGroup;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QuestCommand implements CommandExecutor, TabCompleter {

    private final dev.paramountdev.swquest_pdev.managers.QuestManager questManager;
    private String prefix = SwQuest_PDev.getPrefix();

    public QuestCommand(QuestManager questManager) {
        this.questManager = questManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("");
                sender.sendMessage( prefix + ChatColor.RED + "Команда доступна только игрокам.");
                sender.sendMessage("");
                return true;
            }
            Player player = (Player) sender;
            questManager.openQuestMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("swq.admin")) {
                sender.sendMessage("");
                sender.sendMessage(prefix + ChatColor.RED + "У вас нет прав для выполнения этой команды.");
                sender.sendMessage("");
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                return true;
            }

            questManager.loadQuests(SwQuest_PDev.getInstance().getDataFolder());
            sender.sendMessage("");
            sender.sendMessage(prefix + ChatColor.GREEN + "Конфиг квестов перезагружен.");
            sender.sendMessage("");
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.0F, 1.0F);
            return true;
        }

        if (args[0].equalsIgnoreCase("reward")) {
            if (!sender.hasPermission("swq.admin")) {
                sender.sendMessage("");
                sender.sendMessage(prefix + ChatColor.RED + "У вас нет прав для выполнения этой команды.");
                sender.sendMessage("");
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("");
                sender.sendMessage(prefix + ChatColor.YELLOW + "Использование: /quests reward <ник> <группа/квест>");
                sender.sendMessage("");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("");
                sender.sendMessage(prefix + ChatColor.RED + "Игрок не найден.");
                sender.sendMessage("");
                return true;
            }

            String id = args[2];

            if (questManager.getQuestGroups().containsKey(id)) {
                questManager.completeGroup(target, id);
                sender.sendMessage("");
                sender.sendMessage(prefix + ChatColor.GREEN + "Выдана награда за группу " + id + " игроку " + target.getName());
                sender.sendMessage("");
                return true;
            }

            for (var entry : questManager.getQuestGroups().entrySet()) {
                QuestGroup group = entry.getValue();
                if (group.getQuests().stream().anyMatch(q -> q.getId().equalsIgnoreCase(id))) {
                    questManager.completeQuest(target, group.getId(), id);
                    sender.sendMessage("");
                    sender.sendMessage(prefix + ChatColor.GREEN + "Выдана награда за квест " + id + " игроку " + target.getName());
                    sender.sendMessage("");
                    return true;
                }
            }
            sender.sendMessage("");
            sender.sendMessage(prefix + ChatColor.RED + "Квест или группа с таким ID не найдены.");
            sender.sendMessage("");
            return true;
        }


        if (args[0].equalsIgnoreCase("author")) {
            sendAuthorMessage((Player) sender);
            return true;
        }
        sender.sendMessage("");
        sender.sendMessage(prefix + ChatColor.RED + "Неизвестная подкоманда.");
        sender.sendMessage("");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("reload", "author", "reward"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reward")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("reward")) {
            completions.addAll(questManager.getQuestGroups().keySet());
            questManager.getQuestGroups().values().forEach(g ->
                    g.getQuests().forEach(q -> completions.add(q.getName()))
            );
        }




        return completions;
    }


    private void sendAuthorMessage(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "§m                                                      ");
        player.sendMessage(ChatColor.DARK_GREEN + "   §6✦ §eSwQuest §6by §aParamountDev §6✦");
        player.sendMessage(ChatColor.DARK_GREEN + "§m                                                      ");
        player.sendMessage("");

        player.sendMessage("§7Contact for help or order plugin:");
        TextComponent funpay = new TextComponent("§6• §bFunPay Profile");
        funpay.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://funpay.com/uk/users/14397429/"));
        funpay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Open FunPay Profile").create()));
        player.spigot().sendMessage(funpay);
        TextComponent telegram = new TextComponent("§6• §bTelegram: @paramount1_dev");
        telegram.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/paramount1_dev"));
        telegram.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Open Telegram").create()));
        player.spigot().sendMessage(telegram);

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "§m                                                      ");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
    }
}


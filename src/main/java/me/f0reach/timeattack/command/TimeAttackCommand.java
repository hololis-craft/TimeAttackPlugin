package me.f0reach.timeattack.command;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.command.subcommand.*;
import me.f0reach.timeattack.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * メインコマンドハンドラ (/timeattack, /ta)
 */
public class TimeAttackCommand implements CommandExecutor, TabCompleter {
    private final PluginMain plugin;
    private final Map<String, SubCommand> subCommands;

    public TimeAttackCommand(PluginMain plugin) {
        this.plugin = plugin;
        this.subCommands = new LinkedHashMap<>();

        // サブコマンドを登録
        registerSubCommand(new SetupCommand(plugin));
        registerSubCommand(new CreateCommand(plugin));
        registerSubCommand(new DeleteCommand(plugin));
        registerSubCommand(new TeamCommand(plugin));
        registerSubCommand(new StartCommand(plugin));
        registerSubCommand(new StatusCommand(plugin));
        registerSubCommand(new CompleteCommand(plugin));
        registerSubCommand(new ResetCommand(plugin));
        registerSubCommand(new ReloadCommand(plugin));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();

        // helpコマンド
        if (subCommandName.equals("help")) {
            showHelp(sender);
            return true;
        }

        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null) {
            MessageUtil.sendError((Player) sender, "不明なサブコマンド: " + subCommandName);
            MessageUtil.sendInfo((Player) sender, "/ta help でヘルプを表示");
            return true;
        }

        // 権限チェック
        if (!subCommand.hasPermission(sender)) {
            MessageUtil.sendError((Player) sender, "このコマンドを実行する権限がありません");
            return true;
        }

        // サブコマンドの引数を作成（サブコマンド名を除く）
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            return subCommand.execute(sender, subArgs);
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing command: " + e.getMessage());
            e.printStackTrace();
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "コマンド実行中にエラーが発生しました");
            }
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            // サブコマンド名の補完
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            for (SubCommand subCommand : subCommands.values()) {
                if (subCommand.hasPermission(sender) &&
                    subCommand.getName().toLowerCase().startsWith(partial)) {
                    completions.add(subCommand.getName());
                }
            }

            if ("help".startsWith(partial)) {
                completions.add("help");
            }

            return completions;
        }

        if (args.length > 1) {
            // サブコマンドのタブ補完
            String subCommandName = args[0].toLowerCase();
            SubCommand subCommand = subCommands.get(subCommandName);

            if (subCommand != null && subCommand.hasPermission(sender)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subArgs);
            }
        }

        return Collections.emptyList();
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== TimeAttack コマンドヘルプ ===").color(NamedTextColor.GOLD));

        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand.hasPermission(sender)) {
                sender.sendMessage(Component.text(subCommand.getUsage()).color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("  " + subCommand.getDescription()).color(NamedTextColor.GRAY));
            }
        }
    }
}

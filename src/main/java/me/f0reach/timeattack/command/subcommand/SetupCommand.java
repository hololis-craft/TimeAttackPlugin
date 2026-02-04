package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

/**
 * /ta setup <seed> - ゲームのシードを設定
 */
public class SetupCommand extends SubCommand {

    public SetupCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public String getDescription() {
        return "タイムアタックのシードを設定します";
    }

    @Override
    public String getUsage() {
        return "/ta setup <seed|random>";
    }

    @Override
    public String getPermission() {
        return "timeattack.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "使用方法: " + getUsage());
            }
            return false;
        }

        long seed;
        if (args[0].equalsIgnoreCase("random")) {
            seed = new Random().nextLong();
        } else {
            try {
                seed = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                // 文字列をシードに変換
                seed = args[0].hashCode();
            }
        }

        plugin.getConfigManager().setCurrentSeed(seed);

        if (sender instanceof Player player) {
            MessageUtil.sendSuccess(player, "シードを設定しました: " + seed);
        } else {
            sender.sendMessage("シードを設定しました: " + seed);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("random", "<seed>");
        }
        return List.of();
    }
}

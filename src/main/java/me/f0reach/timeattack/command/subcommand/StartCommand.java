package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /ta start - ゲームを開始
 */
public class StartCommand extends SubCommand {

    public StartCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "タイムアタックを開始します";
    }

    @Override
    public String getUsage() {
        return "/ta start";
    }

    @Override
    public String getPermission() {
        return "timeattack.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 開始可能かチェック
        String error = plugin.getGameManager().canStartGame();
        if (error != null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, error);
            } else {
                sender.sendMessage("エラー: " + error);
            }
            return false;
        }

        // ゲーム開始
        boolean success = plugin.getGameManager().startGame();

        if (success) {
            if (sender instanceof Player player) {
                MessageUtil.sendSuccess(player, "ゲームを開始しました");
            }
        } else {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "ゲームの開始に失敗しました");
            }
        }

        return success;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}

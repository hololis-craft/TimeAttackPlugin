package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /ta reset - ゲームをリセット
 */
public class ResetCommand extends SubCommand {

    public ResetCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "ゲームの状態をリセットします（ワールドとチームは保持）";
    }

    @Override
    public String getUsage() {
        return "/ta reset";
    }

    @Override
    public String getPermission() {
        return "timeattack.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getGameManager().resetGame();

        if (sender instanceof Player player) {
            MessageUtil.sendSuccess(player, "ゲームをリセットしました");
        } else {
            sender.sendMessage("ゲームをリセットしました");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}

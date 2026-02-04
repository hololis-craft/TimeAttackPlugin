package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /ta reload - 設定をリロード
 */
public class ReloadCommand extends SubCommand {

    public ReloadCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "設定ファイルをリロードします";
    }

    @Override
    public String getUsage() {
        return "/ta reload";
    }

    @Override
    public String getPermission() {
        return "timeattack.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getConfigManager().reload();
        MessageUtil.setPrefix(plugin.getConfigManager().getMessagePrefix());

        if (sender instanceof Player player) {
            MessageUtil.sendSuccess(player, "設定をリロードしました");
        } else {
            sender.sendMessage("設定をリロードしました");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}

package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /ta teamcreate <name> - 新しいチームを作成（管理者専用）
 */
public class TeamCreateCommand extends SubCommand {

    public TeamCreateCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "teamcreate";
    }

    @Override
    public String getDescription() {
        return "新しいチームを作成します";
    }

    @Override
    public String getUsage() {
        return "/ta teamcreate <name>";
    }

    @Override
    public String getPermission() {
        return "timeattack.team.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "使用方法: " + getUsage());
            } else {
                sender.sendMessage("使用方法: " + getUsage());
            }
            return false;
        }

        String teamName = args[0];

        // チーム名のバリデーション
        if (!teamName.matches("^[a-zA-Z0-9_-]+$")) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム名は英数字、アンダースコア、ハイフンのみ使用できます");
            } else {
                sender.sendMessage("チーム名は英数字、アンダースコア、ハイフンのみ使用できます");
            }
            return false;
        }

        Team team = plugin.getTeamManager().createTeam(teamName);
        if (team == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム「" + teamName + "」は既に存在します");
            } else {
                sender.sendMessage("チーム「" + teamName + "」は既に存在します");
            }
            return false;
        }

        if (sender instanceof Player player) {
            MessageUtil.sendSuccess(player, "チーム「" + teamName + "」を作成しました");
        } else {
            sender.sendMessage("チーム「" + teamName + "」を作成しました");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}

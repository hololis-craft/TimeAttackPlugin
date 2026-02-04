package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /ta delete <team> - チームとワールドを削除
 */
public class DeleteCommand extends SubCommand {

    public DeleteCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "チームとそのワールドを削除します";
    }

    @Override
    public String getUsage() {
        return "/ta delete <team>";
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

        String teamName = args[0];

        // チームが存在するか確認
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
            }
            return false;
        }

        if (sender instanceof Player player) {
            MessageUtil.sendInfo(player, "チーム「" + teamName + "」を削除中...");
        }

        // チームを削除（ワールドも削除される）
        boolean success = plugin.getTeamManager().deleteTeam(teamName);

        if (success) {
            if (sender instanceof Player player) {
                MessageUtil.sendSuccess(player, "チーム「" + teamName + "」を削除しました");
            }
        } else {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チームの削除に失敗しました");
            }
        }

        return success;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return plugin.getTeamManager().getAllTeams().stream()
                .map(Team::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}

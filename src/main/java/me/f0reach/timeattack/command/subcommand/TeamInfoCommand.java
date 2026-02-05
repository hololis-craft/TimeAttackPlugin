package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /ta teaminfo <team> - チームの詳細情報を表示
 */
public class TeamInfoCommand extends SubCommand {

    public TeamInfoCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "teaminfo";
    }

    @Override
    public String getDescription() {
        return "チームの詳細情報を表示します";
    }

    @Override
    public String getUsage() {
        return "/ta teaminfo <team>";
    }

    @Override
    public String getPermission() {
        return "timeattack.team.info";
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
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
            } else {
                sender.sendMessage("チーム「" + teamName + "」が存在しません");
            }
            return false;
        }

        var status = plugin.getGameManager().getTeamStatus(team);

        if (sender instanceof Player player) {
            MessageUtil.sendInfo(player, status);
        } else {
            sender.sendMessage(status);
        }
        return true;
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

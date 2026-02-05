package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * /ta teamlist - 全チーム一覧を表示
 */
public class TeamListCommand extends SubCommand {

    public TeamListCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "teamlist";
    }

    @Override
    public String getDescription() {
        return "全チームの一覧を表示します";
    }

    @Override
    public String getUsage() {
        return "/ta teamlist";
    }

    @Override
    public String getPermission() {
        return "timeattack.team.list";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        var teams = plugin.getTeamManager().getAllTeams();

        if (teams.isEmpty()) {
            if (sender instanceof Player player) {
                MessageUtil.sendInfo(player, "チームがありません");
            } else {
                sender.sendMessage("チームがありません");
            }
            return true;
        }

        sender.sendMessage("§6=== チーム一覧 ===");
        for (Team team : teams) {
            String worldStatus = team.hasWorldSet() ? "§a[ワールド有]" : "§c[ワールド無]";
            sender.sendMessage("§e" + team.getName() + " §7(" + team.getMemberCount() + "人) " + worldStatus);

            // メンバー一覧
            for (UUID memberId : team.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                String memberName = member != null ? member.getName() : memberId.toString().substring(0, 8) + "...";
                String online = member != null && member.isOnline() ? "§a●" : "§c○";
                sender.sendMessage("  " + online + " " + memberName);
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}

package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * /ta teamrandom - バイパス権限を持たない全オンラインプレイヤーをランダムにチームへ振り分け（管理者専用）
 */
public class TeamRandomCommand extends SubCommand {

    public TeamRandomCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "teamrandom";
    }

    @Override
    public String getDescription() {
        return "全プレイヤーをランダムにチームへ振り分けます";
    }

    @Override
    public String getUsage() {
        return "/ta teamrandom";
    }

    @Override
    public String getPermission() {
        return "timeattack.team.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // チームが存在するか確認
        if (plugin.getTeamManager().getTeamCount() == 0) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チームがありません。先にチームを作成してください");
            } else {
                sender.sendMessage("チームがありません。先にチームを作成してください");
            }
            return false;
        }

        // ランダム振り分けを実行
        Map<Player, Team> assignments = plugin.getTeamManager().randomAssignAllPlayers();

        if (assignments.isEmpty()) {
            if (sender instanceof Player player) {
                MessageUtil.sendInfo(player, "振り分け対象のプレイヤーがいません");
            } else {
                sender.sendMessage("振り分け対象のプレイヤーがいません");
            }
            return true;
        }

        // 結果を報告
        if (sender instanceof Player player) {
            MessageUtil.sendSuccess(player, assignments.size() + "人のプレイヤーをチームに振り分けました");
        } else {
            sender.sendMessage(assignments.size() + "人のプレイヤーをチームに振り分けました");
        }

        // 振り分けられた各プレイヤーに通知
        for (Map.Entry<Player, Team> entry : assignments.entrySet()) {
            Player assignedPlayer = entry.getKey();
            Team team = entry.getValue();
            MessageUtil.sendSuccess(assignedPlayer, "チーム「" + team.getName() + "」に振り分けられました");
        }

        // チームごとのサマリーを表示
        sender.sendMessage("§6=== 振り分け結果 ===");
        for (Team team : plugin.getTeamManager().getAllTeams()) {
            long countInThisAssignment = assignments.entrySet().stream()
                .filter(e -> e.getValue().getName().equals(team.getName()))
                .count();
            if (countInThisAssignment > 0) {
                sender.sendMessage("§e" + team.getName() + ": §7+" + countInThisAssignment +
                    "人 (計" + team.getMemberCount() + "人)");
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}

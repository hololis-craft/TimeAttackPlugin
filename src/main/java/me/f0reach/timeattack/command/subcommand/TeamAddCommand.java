package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /ta teamadd <player> <team> - プレイヤーをチームに追加（管理者専用）
 */
public class TeamAddCommand extends SubCommand {

    public TeamAddCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "teamadd";
    }

    @Override
    public String getDescription() {
        return "プレイヤーをチームに追加します";
    }

    @Override
    public String getUsage() {
        return "/ta teamadd <player> <team>";
    }

    @Override
    public String getPermission() {
        return "timeattack.team.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "使用方法: " + getUsage());
            } else {
                sender.sendMessage("使用方法: " + getUsage());
            }
            return false;
        }

        String playerName = args[0];
        String teamName = args[1];

        // ターゲットプレイヤーを検索
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "プレイヤー「" + playerName + "」が見つかりません（オンラインである必要があります）");
            } else {
                sender.sendMessage("プレイヤー「" + playerName + "」が見つかりません（オンラインである必要があります）");
            }
            return false;
        }

        // チームの存在確認
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
            } else {
                sender.sendMessage("チーム「" + teamName + "」が存在しません");
            }
            return false;
        }

        // チームが満員かどうか確認
        if (plugin.getTeamManager().isTeamFull(teamName)) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム「" + teamName + "」は満員です");
            } else {
                sender.sendMessage("チーム「" + teamName + "」は満員です");
            }
            return false;
        }

        // 既に同じチームに所属しているか確認
        Team currentTeam = plugin.getTeamManager().getPlayerTeam(targetPlayer.getUniqueId());
        if (currentTeam != null && currentTeam.getName().equals(teamName)) {
            if (sender instanceof Player player) {
                MessageUtil.sendWarning(player, "プレイヤー「" + playerName + "」は既にチーム「" + teamName + "」に所属しています");
            } else {
                sender.sendMessage("プレイヤー「" + playerName + "」は既にチーム「" + teamName + "」に所属しています");
            }
            return true;
        }

        // プレイヤーをチームに追加（以前のチームからは自動的に削除される）
        boolean success = plugin.getTeamManager().addPlayer(targetPlayer.getUniqueId(), teamName);
        if (success) {
            if (sender instanceof Player player) {
                MessageUtil.sendSuccess(player, "プレイヤー「" + playerName + "」をチーム「" + teamName + "」に追加しました");
            } else {
                sender.sendMessage("プレイヤー「" + playerName + "」をチーム「" + teamName + "」に追加しました");
            }
            // 対象プレイヤーに通知
            MessageUtil.sendInfo(targetPlayer, "チーム「" + teamName + "」に追加されました");
        } else {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チームへの追加に失敗しました");
            } else {
                sender.sendMessage("チームへの追加に失敗しました");
            }
        }
        return success;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // オンラインプレイヤーを提案
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            // 満員でないチームを提案
            String partial = args[1].toLowerCase();
            return plugin.getTeamManager().getAllTeams().stream()
                .filter(team -> !plugin.getTeamManager().isTeamFull(team.getName()))
                .map(Team::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }

        return List.of();
    }
}

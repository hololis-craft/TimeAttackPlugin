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
 * /ta teamremove <player> - プレイヤーをチームから削除（管理者専用）
 */
public class TeamRemoveCommand extends SubCommand {

    public TeamRemoveCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "teamremove";
    }

    @Override
    public String getDescription() {
        return "プレイヤーをチームから削除します";
    }

    @Override
    public String getUsage() {
        return "/ta teamremove <player>";
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

        String playerName = args[0];

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

        // プレイヤーがチームに所属しているか確認
        Team currentTeam = plugin.getTeamManager().getPlayerTeam(targetPlayer.getUniqueId());
        if (currentTeam == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "プレイヤー「" + playerName + "」はチームに所属していません");
            } else {
                sender.sendMessage("プレイヤー「" + playerName + "」はチームに所属していません");
            }
            return false;
        }

        String teamName = currentTeam.getName();
        boolean success = plugin.getTeamManager().removePlayer(targetPlayer.getUniqueId());

        if (success) {
            if (sender instanceof Player player) {
                MessageUtil.sendSuccess(player, "プレイヤー「" + playerName + "」をチーム「" + teamName + "」から削除しました");
            } else {
                sender.sendMessage("プレイヤー「" + playerName + "」をチーム「" + teamName + "」から削除しました");
            }
            // 対象プレイヤーに通知
            MessageUtil.sendInfo(targetPlayer, "チーム「" + teamName + "」から削除されました");
        } else {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チームからの削除に失敗しました");
            } else {
                sender.sendMessage("チームからの削除に失敗しました");
            }
        }
        return success;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // チームに所属しているプレイヤーのみを提案
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                .filter(p -> plugin.getTeamManager().hasTeam(p.getUniqueId()))
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }

        return List.of();
    }
}

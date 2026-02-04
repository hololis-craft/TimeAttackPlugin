package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.GameState;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import me.f0reach.timeattack.util.TimeUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /ta status [team] - ゲーム状態を確認
 */
public class StatusCommand extends SubCommand {

    public StatusCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "ゲームの状態を確認します";
    }

    @Override
    public String getUsage() {
        return "/ta status [team]";
    }

    @Override
    public String getPermission() {
        return "timeattack.status";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            // 特定のチームの状態を表示
            String teamName = args[0];
            Team team = plugin.getTeamManager().getTeam(teamName);

            if (team == null) {
                if (sender instanceof Player player) {
                    MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
                }
                return false;
            }

            showTeamStatus(sender, team);
            return true;
        }

        // 全体の状態を表示
        showGlobalStatus(sender);
        return true;
    }

    private void showGlobalStatus(CommandSender sender) {
        GameState globalState = plugin.getGameManager().getGameState();
        long seed = plugin.getConfigManager().getCurrentSeed();

        sender.sendMessage("§6=== タイムアタック状態 ===");
        sender.sendMessage("§eゲーム状態: §f" + getStateDisplayName(globalState));

        if (seed != 0) {
            sender.sendMessage("§eシード: §f" + seed);
        } else {
            sender.sendMessage("§eシード: §c未設定");
        }

        var teams = plugin.getTeamManager().getAllTeams();
        sender.sendMessage("§eチーム数: §f" + teams.size());

        if (!teams.isEmpty()) {
            sender.sendMessage("");
            sender.sendMessage("§6--- チーム状況 ---");

            for (Team team : teams) {
                String stateIcon = getStateIcon(team.getState());
                String time = "";

                if (team.getState() == GameState.RUNNING) {
                    long elapsed = plugin.getTimeManager().getElapsedTime();
                    time = " §7[" + TimeUtil.formatTimeShort(elapsed) + "]";
                } else if (team.getState() == GameState.COMPLETED) {
                    time = " §a[" + TimeUtil.formatTime(team.getCompletionTime()) + "]";
                }

                String worldStatus = team.hasWorldSet() ? "" : " §c(ワールド未作成)";
                sender.sendMessage(stateIcon + " §e" + team.getName() +
                    " §7(" + team.getMemberCount() + "人)" + time + worldStatus);
            }
        }
    }

    private void showTeamStatus(CommandSender sender, Team team) {
        sender.sendMessage("§6=== チーム「" + team.getName() + "」===");
        sender.sendMessage("§e状態: §f" + getStateDisplayName(team.getState()));
        sender.sendMessage("§eメンバー数: §f" + team.getMemberCount());

        if (team.hasWorldSet()) {
            sender.sendMessage("§eワールド: §a作成済み");
            sender.sendMessage("  §7オーバーワールド: " + team.getWorldSet().getOverworldName());
            sender.sendMessage("  §7ネザー: " + team.getWorldSet().getNetherName());
            sender.sendMessage("  §7エンド: " + team.getWorldSet().getEndName());
        } else {
            sender.sendMessage("§eワールド: §c未作成");
        }

        if (team.getState() == GameState.RUNNING) {
            long elapsed = plugin.getTimeManager().getElapsedTime();
            sender.sendMessage("§e経過時間: §f" + TimeUtil.formatTime(elapsed));
        } else if (team.getState() == GameState.COMPLETED) {
            sender.sendMessage("§eクリアタイム: §a" + TimeUtil.formatTime(team.getCompletionTime()));
        }
    }

    private String getStateDisplayName(GameState state) {
        return switch (state) {
            case WAITING -> "待機中";
            case RUNNING -> "プレイ中";
            case COMPLETED -> "完了";
        };
    }

    private String getStateIcon(GameState state) {
        return switch (state) {
            case WAITING -> "§7○";
            case RUNNING -> "§e▶";
            case COMPLETED -> "§a✓";
        };
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

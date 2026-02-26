package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.GameState;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import me.f0reach.timeattack.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /ta status [team] - ゲーム状態を確認
 */
public class StatusCommand extends SubCommand {

    public StatusCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("status")
                .requires(source -> source.getSender().hasPermission("timeattack.status"))
                .executes(context -> {
                    if (context.getSource().getSender() instanceof Player player) {
                        showGlobalStatus(player);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("team", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String partial = builder.getRemaining().toLowerCase();
                            for (Team team : plugin.getTeamManager().getAllTeams()) {
                                if (team.getName().toLowerCase().startsWith(partial)) {
                                    builder.suggest(team.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) {
                                return Command.SINGLE_SUCCESS;
                            }
                            String teamName = StringArgumentType.getString(context, "team");
                            Team team = plugin.getTeamManager().getTeam(teamName);
                            if (team == null) {
                                MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
                                return Command.SINGLE_SUCCESS;
                            }
                            showTeamStatus(player, team);
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }

    private void showGlobalStatus(Player player) {
        GameState globalState = plugin.getGameManager().getGameState();
        long seed = plugin.getConfigManager().getCurrentSeed();

        player.sendMessage("§6=== タイムアタック状態 ===");
        player.sendMessage("§eゲーム状態: §f" + getStateDisplayName(globalState));

        if (seed != 0) {
            player.sendMessage("§eシード: §f" + seed);
        } else {
            player.sendMessage("§eシード: §c未設定");
        }

        var teams = plugin.getTeamManager().getAllTeams();
        player.sendMessage("§eチーム数: §f" + teams.size());

        if (!teams.isEmpty()) {
            player.sendMessage("");
            player.sendMessage("§6--- チーム状況 ---");

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
                player.sendMessage(stateIcon + " §e" + team.getName() +
                        " §7(" + team.getMemberCount() + "人)" + time + worldStatus);
            }
        }
    }

    private void showTeamStatus(Player player, Team team) {
        player.sendMessage("§6=== チーム「" + team.getName() + "」===");
        player.sendMessage("§e状態: §f" + getStateDisplayName(team.getState()));
        player.sendMessage("§eメンバー数: §f" + team.getMemberCount());

        for (UUID memberId : team.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            String memberName = member != null ? member.getName() : memberId.toString().substring(0, 8) + "...";
            String online = member != null && member.isOnline() ? "§a●" : "§c○";
            player.sendMessage("  " + online + " " + memberName);
        }

        if (team.hasWorldSet()) {
            player.sendMessage("§eワールド: §a作成済み");
            player.sendMessage("  §7オーバーワールド: " + team.getWorldSet().getOverworldName());
            player.sendMessage("  §7ネザー: " + team.getWorldSet().getNetherName());
            player.sendMessage("  §7エンド: " + team.getWorldSet().getEndName());
        } else {
            player.sendMessage("§eワールド: §c未作成");
        }

        if (team.getState() == GameState.RUNNING) {
            long elapsed = plugin.getTimeManager().getElapsedTime();
            player.sendMessage("§e経過時間: §f" + TimeUtil.formatTime(elapsed));
        } else if (team.getState() == GameState.COMPLETED) {
            player.sendMessage("§eクリアタイム: §a" + TimeUtil.formatTime(team.getCompletionTime()));
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
}

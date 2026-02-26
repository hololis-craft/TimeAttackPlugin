package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * /ta teamrandom - バイパス権限を持たない全オンラインプレイヤーをランダムにチームへ振り分け（管理者専用）
 */
public class TeamRandomCommand extends SubCommand {

    public TeamRandomCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("teamrandom")
                .requires(source -> source.getSender().hasPermission("timeattack.team.admin"))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();

                    if (plugin.getTeamManager().getTeamCount() == 0) {
                        if (sender instanceof Player player) {
                            MessageUtil.sendError(player, "チームがありません。先にチームを作成してください");
                        } else {
                            sender.sendMessage("チームがありません。先にチームを作成してください");
                        }
                        return Command.SINGLE_SUCCESS;
                    }

                    Map<Player, Team> assignments = plugin.getTeamManager().randomAssignAllPlayers();

                    if (assignments.isEmpty()) {
                        if (sender instanceof Player player) {
                            MessageUtil.sendInfo(player, "振り分け対象のプレイヤーがいません");
                        } else {
                            sender.sendMessage("振り分け対象のプレイヤーがいません");
                        }
                        return Command.SINGLE_SUCCESS;
                    }

                    if (sender instanceof Player player) {
                        MessageUtil.sendSuccess(player, assignments.size() + "人のプレイヤーをチームに振り分けました");
                    } else {
                        sender.sendMessage(assignments.size() + "人のプレイヤーをチームに振り分けました");
                    }

                    for (Map.Entry<Player, Team> entry : assignments.entrySet()) {
                        MessageUtil.sendSuccess(entry.getKey(), "チーム「" + entry.getValue().getName() + "」に振り分けられました");
                    }

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

                    return Command.SINGLE_SUCCESS;
                });
    }
}

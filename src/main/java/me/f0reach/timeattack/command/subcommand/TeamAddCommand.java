package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ta teamadd <player> <team> - プレイヤーをチームに追加（管理者専用）
 */
public class TeamAddCommand extends SubCommand {

    public TeamAddCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("teamadd")
                .requires(source -> source.getSender().hasPermission("timeattack.team.admin"))
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String partial = builder.getRemaining().toLowerCase();
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getName().toLowerCase().startsWith(partial)) {
                                    builder.suggest(p.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("team", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    String partial = builder.getRemaining().toLowerCase();
                                    for (Team team : plugin.getTeamManager().getAllTeams()) {
                                        if (!plugin.getTeamManager().isTeamFull(team.getName()) &&
                                                team.getName().toLowerCase().startsWith(partial)) {
                                            builder.suggest(team.getName());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    String playerName = StringArgumentType.getString(context, "player");
                                    String teamName = StringArgumentType.getString(context, "team");

                                    Player targetPlayer = Bukkit.getPlayer(playerName);
                                    if (targetPlayer == null) {
                                        if (sender instanceof Player player) {
                                            MessageUtil.sendError(player, "プレイヤー「" + playerName + "」が見つかりません（オンラインである必要があります）");
                                        } else {
                                            sender.sendMessage("エラー: プレイヤー「" + playerName + "」が見つかりません");
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Team team = plugin.getTeamManager().getTeam(teamName);
                                    if (team == null) {
                                        if (sender instanceof Player player) {
                                            MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
                                        } else {
                                            sender.sendMessage("エラー: チーム「" + teamName + "」が存在しません");
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if (plugin.getTeamManager().isTeamFull(teamName)) {
                                        if (sender instanceof Player player) {
                                            MessageUtil.sendError(player, "チーム「" + teamName + "」は満員です");
                                        } else {
                                            sender.sendMessage("エラー: チーム「" + teamName + "」は満員です");
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Team currentTeam = plugin.getTeamManager().getPlayerTeam(targetPlayer.getUniqueId());
                                    if (currentTeam != null && currentTeam.getName().equals(teamName)) {
                                        if (sender instanceof Player player) {
                                            MessageUtil.sendWarning(player, "プレイヤー「" + playerName + "」は既にチーム「" + teamName + "」に所属しています");
                                        } else {
                                            sender.sendMessage("プレイヤー「" + playerName + "」は既にチーム「" + teamName + "」に所属しています");
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    boolean success = plugin.getTeamManager().addPlayer(targetPlayer.getUniqueId(), teamName);
                                    if (success) {
                                        if (sender instanceof Player player) {
                                            MessageUtil.sendSuccess(player, "プレイヤー「" + playerName + "」をチーム「" + teamName + "」に追加しました");
                                        } else {
                                            sender.sendMessage("プレイヤー「" + playerName + "」をチーム「" + teamName + "」に追加しました");
                                        }
                                        MessageUtil.sendInfo(targetPlayer, "チーム「" + teamName + "」に追加されました");
                                    } else {
                                        if (sender instanceof Player player) {
                                            MessageUtil.sendError(player, "チームへの追加に失敗しました");
                                        } else {
                                            sender.sendMessage("エラー: チームへの追加に失敗しました");
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                );
    }
}

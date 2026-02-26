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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ta delete <team> - チームとワールドを削除
 */
public class DeleteCommand extends SubCommand {

    public DeleteCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("delete")
                .requires(source -> source.getSender().hasPermission("timeattack.admin"))
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
                            CommandSender sender = context.getSource().getSender();

                            if (plugin.getGameManager().getGameState() == GameState.RUNNING) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "ゲーム進行中はチームを削除できません");
                                } else {
                                    sender.sendMessage("エラー: ゲーム進行中はチームを削除できません");
                                }
                                return Command.SINGLE_SUCCESS;
                            }

                            String teamName = StringArgumentType.getString(context, "team");
                            Team team = plugin.getTeamManager().getTeam(teamName);
                            if (team == null) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
                                } else {
                                    sender.sendMessage("エラー: チーム「" + teamName + "」が存在しません");
                                }
                                return Command.SINGLE_SUCCESS;
                            }

                            if (sender instanceof Player player) {
                                MessageUtil.sendInfo(player, "チーム「" + teamName + "」を削除中...");
                            } else {
                                sender.sendMessage("チーム「" + teamName + "」を削除中...");
                            }

                            boolean success = plugin.getTeamManager().deleteTeam(teamName);
                            if (success) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendSuccess(player, "チーム「" + teamName + "」を削除しました");
                                } else {
                                    sender.sendMessage("チーム「" + teamName + "」を削除しました");
                                }
                            } else {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "チームの削除に失敗しました");
                                } else {
                                    sender.sendMessage("エラー: チームの削除に失敗しました");
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}

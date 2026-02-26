package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.model.WorldSet;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ta create <team> - チームのワールドセットを作成
 */
public class CreateCommand extends SubCommand {

    public CreateCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("create")
                .requires(source -> source.getSender().hasPermission("timeattack.admin"))
                .then(Commands.argument("team", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String partial = builder.getRemaining().toLowerCase();
                            for (Team team : plugin.getTeamManager().getAllTeams()) {
                                if (!team.hasWorldSet() && team.getName().toLowerCase().startsWith(partial)) {
                                    builder.suggest(team.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String teamName = StringArgumentType.getString(context, "team");

                            if (!plugin.getConfigManager().hasSeed()) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "シードが設定されていません。先に /ta setup <seed> を実行してください");
                                } else {
                                    sender.sendMessage("エラー: シードが設定されていません");
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

                            if (team.hasWorldSet()) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "チーム「" + teamName + "」のワールドは既に作成されています");
                                } else {
                                    sender.sendMessage("エラー: チーム「" + teamName + "」のワールドは既に作成されています");
                                }
                                return Command.SINGLE_SUCCESS;
                            }

                            long seed = plugin.getConfigManager().getCurrentSeed();
                            if (sender instanceof Player player) {
                                MessageUtil.sendInfo(player, "ワールドを作成中... (シード: " + seed + ")");
                            } else {
                                sender.sendMessage("ワールドを作成中... (シード: " + seed + ")");
                            }

                            WorldSet worldSet = plugin.getWorldSetManager().createWorldSet(teamName, seed);
                            if (worldSet == null) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "ワールドの作成に失敗しました");
                                } else {
                                    sender.sendMessage("エラー: ワールドの作成に失敗しました");
                                }
                                return Command.SINGLE_SUCCESS;
                            }

                            plugin.getTeamManager().setTeamWorldSet(teamName, worldSet);
                            if (sender instanceof Player player) {
                                MessageUtil.sendSuccess(player, "チーム「" + teamName + "」のワールドを作成しました");
                                MessageUtil.sendInfo(player, "  オーバーワールド: " + worldSet.getOverworldName());
                                MessageUtil.sendInfo(player, "  ネザー: " + worldSet.getNetherName());
                                MessageUtil.sendInfo(player, "  エンド: " + worldSet.getEndName());
                            } else {
                                sender.sendMessage("チーム「" + teamName + "」のワールドを作成しました");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}

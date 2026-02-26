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
 * /ta teamremove <player> - プレイヤーをチームから削除（管理者専用）
 */
public class TeamRemoveCommand extends SubCommand {

    public TeamRemoveCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("teamremove")
                .requires(source -> source.getSender().hasPermission("timeattack.team.admin"))
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String partial = builder.getRemaining().toLowerCase();
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (plugin.getTeamManager().hasTeam(p.getUniqueId()) &&
                                        p.getName().toLowerCase().startsWith(partial)) {
                                    builder.suggest(p.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String playerName = StringArgumentType.getString(context, "player");

                            Player targetPlayer = Bukkit.getPlayer(playerName);
                            if (targetPlayer == null) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "プレイヤー「" + playerName + "」が見つかりません（オンラインである必要があります）");
                                } else {
                                    sender.sendMessage("エラー: プレイヤー「" + playerName + "」が見つかりません");
                                }
                                return Command.SINGLE_SUCCESS;
                            }

                            Team currentTeam = plugin.getTeamManager().getPlayerTeam(targetPlayer.getUniqueId());
                            if (currentTeam == null) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "プレイヤー「" + playerName + "」はチームに所属していません");
                                } else {
                                    sender.sendMessage("エラー: プレイヤー「" + playerName + "」はチームに所属していません");
                                }
                                return Command.SINGLE_SUCCESS;
                            }

                            String teamName = currentTeam.getName();
                            boolean success = plugin.getTeamManager().removePlayer(targetPlayer.getUniqueId());
                            if (success) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendSuccess(player, "プレイヤー「" + playerName + "」をチーム「" + teamName + "」から削除しました");
                                } else {
                                    sender.sendMessage("プレイヤー「" + playerName + "」をチーム「" + teamName + "」から削除しました");
                                }
                                MessageUtil.sendInfo(targetPlayer, "チーム「" + teamName + "」から削除されました");
                            } else {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "チームからの削除に失敗しました");
                                } else {
                                    sender.sendMessage("エラー: チームからの削除に失敗しました");
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}

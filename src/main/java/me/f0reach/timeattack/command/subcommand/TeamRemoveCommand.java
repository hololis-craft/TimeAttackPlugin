package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
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
                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            var sender = context.getSource().getSender();
                            var targetResolver = context.getArgument("player",
                                    PlayerSelectorArgumentResolver.class);
                            var targetPlayers = targetResolver.resolve(context.getSource());
                            if (targetPlayers.size() != 1) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "プレイヤーは1人だけ指定してください");
                                } else {
                                    sender.sendMessage("エラー: プレイヤーは1人だけ指定してください");
                                }
                                return Command.SINGLE_SUCCESS;
                            }
                            var targetPlayer = targetPlayers.getFirst();
                            var playerName = targetPlayer.getName();

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
                                    MessageUtil.sendSuccess(player,
                                            "プレイヤー「" + playerName + "」をチーム「" + teamName + "」から削除しました");
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
                        }));
    }
}

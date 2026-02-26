package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ta teamcreate <name> - 新しいチームを作成（管理者専用）
 */
public class TeamCreateCommand extends SubCommand {

    public TeamCreateCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("teamcreate")
                .requires(source -> source.getSender().hasPermission("timeattack.team.admin"))
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String teamName = StringArgumentType.getString(context, "name");

                            if (!teamName.matches("^[a-zA-Z0-9_-]+$")) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "チーム名は英数字、アンダースコア、ハイフンのみ使用できます");
                                } else {
                                    sender.sendMessage("エラー: チーム名は英数字、アンダースコア、ハイフンのみ使用できます");
                                }
                                return Command.SINGLE_SUCCESS;
                            }

                            Team team = plugin.getTeamManager().createTeam(teamName);
                            if (team == null) {
                                if (sender instanceof Player player) {
                                    MessageUtil.sendError(player, "チーム「" + teamName + "」は既に存在します");
                                } else {
                                    sender.sendMessage("エラー: チーム「" + teamName + "」は既に存在します");
                                }
                                return Command.SINGLE_SUCCESS;
                            }

                            if (sender instanceof Player player) {
                                MessageUtil.sendSuccess(player, "チーム「" + teamName + "」を作成しました");
                            } else {
                                sender.sendMessage("チーム「" + teamName + "」を作成しました");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}

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
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ta tp <team> [dimension] - 指定したチームのワールドにテレポート（管理者専用）
 */
public class TeleportCommand extends SubCommand {

    public TeleportCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("tp")
                .requires(source -> source.getSender().hasPermission("timeattack.team.admin"))
                .then(Commands.argument("team", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String partial = builder.getRemaining().toLowerCase();
                            for (Team team : plugin.getTeamManager().getAllTeams()) {
                                if (team.hasWorldSet() && team.getName().toLowerCase().startsWith(partial)) {
                                    builder.suggest(team.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> teleport(
                                context.getSource().getSender(),
                                StringArgumentType.getString(context, "team"),
                                "overworld"))
                        .then(Commands.argument("dimension", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    String partial = builder.getRemaining().toLowerCase();
                                    for (String dim : new String[]{"overworld", "nether", "end"}) {
                                        if (dim.startsWith(partial)) {
                                            builder.suggest(dim);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> teleport(
                                        context.getSource().getSender(),
                                        StringArgumentType.getString(context, "team"),
                                        StringArgumentType.getString(context, "dimension")))
                        )
                );
    }

    private int teleport(CommandSender sender, String teamName, String dimension) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます");
            return Command.SINGLE_SUCCESS;
        }

        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
            return Command.SINGLE_SUCCESS;
        }

        WorldSet worldSet = team.getWorldSet();
        if (worldSet == null) {
            MessageUtil.sendError(player, "チーム「" + teamName + "」にはワールドが設定されていません");
            return Command.SINGLE_SUCCESS;
        }

        World.Environment env = switch (dimension.toLowerCase()) {
            case "nether" -> World.Environment.NETHER;
            case "end" -> World.Environment.THE_END;
            case "overworld" -> World.Environment.NORMAL;
            default -> null;
        };

        if (env == null) {
            MessageUtil.sendError(player, "無効な次元です。overworld、nether、end のいずれかを指定してください");
            return Command.SINGLE_SUCCESS;
        }

        plugin.getWorldSetManager().teleportToWorldSetSpawn(worldSet, env, player);
        return Command.SINGLE_SUCCESS;
    }
}

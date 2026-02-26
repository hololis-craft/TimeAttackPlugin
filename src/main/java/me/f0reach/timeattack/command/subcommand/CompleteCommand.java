package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.WorldSet;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.entity.Player;

/**
 * /ta complete <worldId> - ゲーム完了を通知
 */
public class CompleteCommand extends SubCommand {

    public CompleteCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("complete")
                .requires(source -> source.getSender().hasPermission("timeattack.admin"))
                .then(Commands.argument("worldId", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String partial = builder.getRemaining().toLowerCase();
                            for (WorldSet worldSet : plugin.getWorldSetManager().getAllWorldSets().values()) {
                                if (worldSet.getOverworldName().toLowerCase().startsWith(partial)) {
                                    builder.suggest(worldSet.getOverworldName());
                                }
                                if (worldSet.getNetherName().toLowerCase().startsWith(partial)) {
                                    builder.suggest(worldSet.getNetherName());
                                }
                                if (worldSet.getEndName().toLowerCase().startsWith(partial)) {
                                    builder.suggest(worldSet.getEndName());
                                }
                            }
                            return builder.buildFuture();
                        }))
                .executes(context -> {
                    // ワールドIDからチームを特定
                    var worldId = StringArgumentType.getString(context, "worldId");
                    var worldSet = plugin.getWorldSetManager().getWorldSetByWorldName(worldId);
                    if (worldSet == null) {
                        if (context.getSource().getSender() instanceof Player player) {
                            MessageUtil.sendError(player, "ワールド「" + worldId + "」はタイムアタック用ワールドではありません");
                        }
                        return Command.SINGLE_SUCCESS;
                    }

                    // 完了処理
                    boolean success = plugin.getGameManager().completeGame(worldId);

                    if (success) {
                        if (context.getSource().getSender() instanceof Player player) {
                            MessageUtil.sendSuccess(player, "チーム「" + worldSet.getTeamName() + "」の完了を記録しました");
                        }
                    } else {
                        if (context.getSource().getSender() instanceof Player player) {
                            MessageUtil.sendError(player, "完了の記録に失敗しました（ゲームが開始されていないか、既に完了しています）");
                        }
                    }

                    return Command.SINGLE_SUCCESS;
                });
    }
}

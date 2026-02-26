package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ta start - ゲームを開始
 */
public class StartCommand extends SubCommand {

    public StartCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("start")
                .requires(source -> source.getSender().hasPermission("timeattack.admin"))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    String error = plugin.getGameManager().canStartGame();
                    if (error != null) {
                        if (sender instanceof Player player) {
                            MessageUtil.sendError(player, error);
                        } else {
                            sender.sendMessage("エラー: " + error);
                        }
                        return Command.SINGLE_SUCCESS;
                    }

                    boolean success = plugin.getGameManager().startGame();
                    if (success) {
                        if (sender instanceof Player player) {
                            MessageUtil.sendSuccess(player, "ゲームを開始しました");
                        } else {
                            sender.sendMessage("ゲームを開始しました");
                        }
                    } else {
                        if (sender instanceof Player player) {
                            MessageUtil.sendError(player, "ゲームの開始に失敗しました");
                        } else {
                            sender.sendMessage("エラー: ゲームの開始に失敗しました");
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }
}

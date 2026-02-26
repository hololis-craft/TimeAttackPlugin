package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.entity.Player;

/**
 * /ta reset - ゲームをリセット
 */
public class ResetCommand extends SubCommand {

    public ResetCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("reset")
                .requires(source -> source.getSender().hasPermission("timeattack.admin"))
                .executes(context -> {
                    plugin.getGameManager().resetGame();
                    if (context.getSource().getSender() instanceof Player player) {
                        MessageUtil.sendSuccess(player, "ゲームをリセットしました");
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }
}

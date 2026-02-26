package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.entity.Player;

/**
 * /ta reload - 設定をリロード
 */
public class ReloadCommand extends SubCommand {

    public ReloadCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("reload")
                .requires(source -> source.getSender().hasPermission("timeattack.admin"))
                .executes(context -> {
                    plugin.getConfigManager().reload();
                    MessageUtil.setPrefix(plugin.getConfigManager().getMessagePrefix());
                    if (context.getSource().getSender() instanceof Player player) {
                        MessageUtil.sendSuccess(player, "設定をリロードしました");
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }
}

package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * /ta setup <seed> - ゲームのシードを設定
 */
public class SetupCommand extends SubCommand {

    public SetupCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("setup")
                .requires(source -> source.getSender().hasPermission("timeattack.admin"))
                .then(Commands.literal("random").executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    long seed = new Random().nextLong();
                    setSeed(sender, seed);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.argument("seed", LongArgumentType.longArg()).executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    long seed = LongArgumentType.getLong(context, "seed");
                    setSeed(sender, seed);
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private void setSeed(CommandSender sender, long seed) {
        plugin.getConfigManager().setCurrentSeed(seed);
        if (sender instanceof Player player) {
            MessageUtil.sendSuccess(player, "シードを設定しました: " + seed);
        } else {
            sender.sendMessage("シードを設定しました: " + seed);
        }
    }
}

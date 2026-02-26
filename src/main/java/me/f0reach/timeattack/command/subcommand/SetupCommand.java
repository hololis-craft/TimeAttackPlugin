package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                .then(Commands.argument("seed", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            builder.suggest("random");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String seedStr = StringArgumentType.getString(context, "seed");
                            long seed;
                            if (seedStr.equalsIgnoreCase("random")) {
                                seed = new Random().nextLong();
                            } else {
                                try {
                                    seed = Long.parseLong(seedStr);
                                } catch (NumberFormatException e) {
                                    seed = seedStr.hashCode();
                                }
                            }
                            plugin.getConfigManager().setCurrentSeed(seed);
                            CommandSender sender = context.getSource().getSender();
                            if (sender instanceof Player player) {
                                MessageUtil.sendSuccess(player, "シードを設定しました: " + seed);
                            } else {
                                sender.sendMessage("シードを設定しました: " + seed);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}

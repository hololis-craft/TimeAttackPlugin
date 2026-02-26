package me.f0reach.timeattack.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.f0reach.timeattack.PluginMain;

/**
 * サブコマンドの基底クラス
 */
public abstract class SubCommand {
    protected final PluginMain plugin;

    public SubCommand(PluginMain plugin) {
        this.plugin = plugin;
    }

    /**
     * サブコマンド名を取得
     */
    public abstract LiteralArgumentBuilder<CommandSourceStack> createCommand();
}

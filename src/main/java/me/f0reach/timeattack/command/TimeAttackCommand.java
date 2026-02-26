package me.f0reach.timeattack.command;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.command.subcommand.*;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.*;

/**
 * メインコマンドハンドラ (/timeattack, /ta)
 */
public class TimeAttackCommand {
    private final PluginMain plugin;
    private final List<SubCommand> subCommands;

    public TimeAttackCommand(PluginMain plugin) {
        this.plugin = plugin;
        this.subCommands = new LinkedList<>();

        // サブコマンドを登録
        subCommands.add(new SetupCommand(plugin));
        subCommands.add(new CreateCommand(plugin));
        subCommands.add(new DeleteCommand(plugin));
        subCommands.add(new TeamCreateCommand(plugin));
        subCommands.add(new TeamAddCommand(plugin));
        subCommands.add(new TeamRemoveCommand(plugin));
        subCommands.add(new TeamRandomCommand(plugin));
        subCommands.add(new TeleportCommand(plugin));
        subCommands.add(new StartCommand(plugin));
        subCommands.add(new StatusCommand(plugin));
        subCommands.add(new CompleteCommand(plugin));
        subCommands.add(new ResetCommand(plugin));
        subCommands.add(new ReloadCommand(plugin));
    }

    public void registerCommands() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            var builder = Commands.literal("ta");
            for (SubCommand subCommand : subCommands) {
                builder.then(subCommand.createCommand());
            }
            commands.registrar().register(builder.build());
        });
    }
}

package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.WorldSet;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /ta complete <worldId> - ゲーム完了を通知
 */
public class CompleteCommand extends SubCommand {

    public CompleteCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "complete";
    }

    @Override
    public String getDescription() {
        return "チームのゲーム完了を記録します（ワールドIDで指定）";
    }

    @Override
    public String getUsage() {
        return "/ta complete <worldId>";
    }

    @Override
    public String getPermission() {
        return "timeattack.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "使用方法: " + getUsage());
            }
            return false;
        }

        String worldId = args[0];

        // ワールドIDからチームを特定
        WorldSet worldSet = plugin.getWorldSetManager().getWorldSetByWorldName(worldId);
        if (worldSet == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "ワールド「" + worldId + "」はタイムアタック用ワールドではありません");
            }
            return false;
        }

        // 完了処理
        boolean success = plugin.getGameManager().completeGame(worldId);

        if (success) {
            if (sender instanceof Player player) {
                MessageUtil.sendSuccess(player, "チーム「" + worldSet.getTeamName() + "」の完了を記録しました");
            }
        } else {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "完了の記録に失敗しました（ゲームが開始されていないか、既に完了しています）");
            }
        }

        return success;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            // 全ワールドセットのワールド名を候補に追加
            for (WorldSet worldSet : plugin.getWorldSetManager().getAllWorldSets().values()) {
                if (worldSet.getOverworldName().toLowerCase().startsWith(partial)) {
                    completions.add(worldSet.getOverworldName());
                }
                if (worldSet.getNetherName().toLowerCase().startsWith(partial)) {
                    completions.add(worldSet.getNetherName());
                }
                if (worldSet.getEndName().toLowerCase().startsWith(partial)) {
                    completions.add(worldSet.getEndName());
                }
            }

            return completions;
        }
        return List.of();
    }
}

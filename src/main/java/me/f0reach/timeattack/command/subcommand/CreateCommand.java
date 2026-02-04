package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.model.WorldSet;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /ta create <team> - チームのワールドセットを作成
 */
public class CreateCommand extends SubCommand {

    public CreateCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "チームのワールドセット（オーバーワールド/ネザー/エンド）を作成します";
    }

    @Override
    public String getUsage() {
        return "/ta create <team>";
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

        String teamName = args[0];

        // シードが設定されているか確認
        if (!plugin.getConfigManager().hasSeed()) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "シードが設定されていません。先に /ta setup <seed> を実行してください");
            }
            return false;
        }

        // チームが存在するか確認
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
            }
            return false;
        }

        // 既にワールドセットが存在するか確認
        if (team.hasWorldSet()) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム「" + teamName + "」のワールドは既に作成されています");
            }
            return false;
        }

        long seed = plugin.getConfigManager().getCurrentSeed();

        if (sender instanceof Player player) {
            MessageUtil.sendInfo(player, "ワールドを作成中... (シード: " + seed + ")");
        }

        // ワールドセットを作成
        WorldSet worldSet = plugin.getWorldSetManager().createWorldSet(teamName, seed);
        if (worldSet == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "ワールドの作成に失敗しました");
            }
            return false;
        }

        // チームにワールドセットを設定
        plugin.getTeamManager().setTeamWorldSet(teamName, worldSet);

        if (sender instanceof Player player) {
            MessageUtil.sendSuccess(player, "チーム「" + teamName + "」のワールドを作成しました");
            MessageUtil.sendInfo(player, "  オーバーワールド: " + worldSet.getOverworldName());
            MessageUtil.sendInfo(player, "  ネザー: " + worldSet.getNetherName());
            MessageUtil.sendInfo(player, "  エンド: " + worldSet.getEndName());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return plugin.getTeamManager().getAllTeams().stream()
                .filter(team -> !team.hasWorldSet())
                .map(Team::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}

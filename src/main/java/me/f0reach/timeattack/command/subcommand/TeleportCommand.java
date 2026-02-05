package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.model.WorldSet;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /ta tp <team> - 指定したチームのワールドにテレポート（管理者専用）
 */
public class TeleportCommand extends SubCommand {

    public TeleportCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "tp";
    }

    @Override
    public String getDescription() {
        return "指定したチームのワールドにテレポートします";
    }

    @Override
    public String getUsage() {
        return "/ta tp <team> <dimension>";
    }

    @Override
    public String getPermission() {
        return "timeattack.team.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます");
            return false;
        }

        if (args.length < 1) {
            MessageUtil.sendError(player, "使用方法: " + getUsage());
            return false;
        }

        String teamName = args[0];

        // チームの存在確認
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
            return false;
        }

        // ワールドセットの確認
        WorldSet worldSet = team.getWorldSet();
        if (worldSet == null) {
            MessageUtil.sendError(player, "チーム「" + teamName + "」にはワールドが設定されていません");
            return false;
        }

        // テレポート先の次元を確認（省略時はオーバーワールド）
        String dimension = "overworld";
        if (args.length >= 2) {
            dimension = args[1].toLowerCase();
        }

        World.Environment env;
        switch (dimension) {
            case "nether" -> env = World.Environment.NETHER;
            case "end" -> env = World.Environment.THE_END;
            case "overworld" -> env = World.Environment.NORMAL;
            default -> {
                MessageUtil.sendError(player, "無効な次元です。overworld、nether、end のいずれかを指定してください");
                return false;
            }
        }

        // 指定された次元のワールドにテレポート
        plugin.getWorldSetManager().teleportToWorldSetSpawn(worldSet, env, player);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // ワールドセットが設定されているチームを提案
            String partial = args[0].toLowerCase();
            return plugin.getTeamManager().getAllTeams().stream()
                    .filter(Team::hasWorldSet)
                    .map(Team::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return List.of("overworld", "nether", "end").stream()
                    .filter(dim -> dim.startsWith(partial))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}

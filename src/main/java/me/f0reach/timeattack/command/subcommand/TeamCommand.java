package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /ta team <create|join|leave|auto|list> - チーム管理コマンド
 */
public class TeamCommand extends SubCommand {

    public TeamCommand(PluginMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "team";
    }

    @Override
    public String getDescription() {
        return "チームを管理します";
    }

    @Override
    public String getUsage() {
        return "/ta team <create|join|leave|auto|list|info> [name]";
    }

    @Override
    public String getPermission() {
        return "timeattack.team";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "使用方法: " + getUsage());
            }
            return false;
        }

        String action = args[0].toLowerCase();

        return switch (action) {
            case "create" -> executeCreate(sender, args);
            case "join" -> executeJoin(sender, args);
            case "leave" -> executeLeave(sender, args);
            case "auto" -> executeAuto(sender, args);
            case "list" -> executeList(sender, args);
            case "info" -> executeInfo(sender, args);
            default -> {
                if (sender instanceof Player player) {
                    MessageUtil.sendError(player, "不明なアクション: " + action);
                }
                yield false;
            }
        };
    }

    private boolean executeCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "使用方法: /ta team create <name>");
            }
            return false;
        }

        String teamName = args[1];

        // チーム名のバリデーション
        if (!teamName.matches("^[a-zA-Z0-9_-]+$")) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム名は英数字、アンダースコア、ハイフンのみ使用できます");
            }
            return false;
        }

        Team team = plugin.getTeamManager().createTeam(teamName);
        if (team == null) {
            if (sender instanceof Player player) {
                MessageUtil.sendError(player, "チーム「" + teamName + "」は既に存在します");
            }
            return false;
        }

        if (sender instanceof Player player) {
            MessageUtil.sendSuccess(player, "チーム「" + teamName + "」を作成しました");
        }
        return true;
    }

    private boolean executeJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます");
            return false;
        }

        if (args.length < 2) {
            MessageUtil.sendError(player, "使用方法: /ta team join <name>");
            return false;
        }

        String teamName = args[1];
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
            return false;
        }

        if (plugin.getTeamManager().isTeamFull(teamName)) {
            MessageUtil.sendError(player, "チーム「" + teamName + "」は満員です");
            return false;
        }

        boolean success = plugin.getTeamManager().addPlayer(player.getUniqueId(), teamName);
        if (success) {
            MessageUtil.sendSuccess(player, "チーム「" + teamName + "」に参加しました");
        } else {
            MessageUtil.sendError(player, "チームへの参加に失敗しました");
        }
        return success;
    }

    private boolean executeLeave(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます");
            return false;
        }

        Team currentTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        if (currentTeam == null) {
            MessageUtil.sendError(player, "チームに所属していません");
            return false;
        }

        String teamName = currentTeam.getName();
        boolean success = plugin.getTeamManager().removePlayer(player.getUniqueId());
        if (success) {
            MessageUtil.sendSuccess(player, "チーム「" + teamName + "」から脱退しました");
        } else {
            MessageUtil.sendError(player, "チームからの脱退に失敗しました");
        }
        return success;
    }

    private boolean executeAuto(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます");
            return false;
        }

        // 既にチームに所属しているか確認
        Team currentTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        if (currentTeam != null) {
            MessageUtil.sendInfo(player, "既にチーム「" + currentTeam.getName() + "」に所属しています");
            return true;
        }

        Team team = plugin.getTeamManager().autoAssignPlayer(player.getUniqueId());
        if (team != null) {
            MessageUtil.sendSuccess(player, "チーム「" + team.getName() + "」に自動で振り分けられました");
            return true;
        } else {
            MessageUtil.sendError(player, "自動振り分けに失敗しました（利用可能なチームがありません）");
            return false;
        }
    }

    private boolean executeList(CommandSender sender, String[] args) {
        var teams = plugin.getTeamManager().getAllTeams();

        if (teams.isEmpty()) {
            if (sender instanceof Player player) {
                MessageUtil.sendInfo(player, "チームがありません");
            } else {
                sender.sendMessage("チームがありません");
            }
            return true;
        }

        sender.sendMessage("§6=== チーム一覧 ===");
        for (Team team : teams) {
            String worldStatus = team.hasWorldSet() ? "§a[ワールド有]" : "§c[ワールド無]";
            sender.sendMessage("§e" + team.getName() + " §7(" + team.getMemberCount() + "人) " + worldStatus);

            // メンバー一覧
            for (UUID memberId : team.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                String memberName = member != null ? member.getName() : memberId.toString().substring(0, 8) + "...";
                String online = member != null && member.isOnline() ? "§a●" : "§c○";
                sender.sendMessage("  " + online + " " + memberName);
            }
        }

        return true;
    }

    private boolean executeInfo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます");
            return false;
        }

        if (args.length < 2) {
            MessageUtil.sendError(player, "使用方法: /ta team join <name>");
            return false;
        }

        String teamName = args[1];
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            MessageUtil.sendError(player, "チーム「" + teamName + "」が存在しません");
            return false;
        }

        var status = plugin.getGameManager().getTeamStatus(team);

        MessageUtil.sendInfo(player, status);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Arrays.asList("create", "join", "leave", "auto", "list","info").stream()
                .filter(s -> s.startsWith(partial))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String action = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            if (action.equals("join")) {
                return plugin.getTeamManager().getAllTeams().stream()
                    .filter(team -> !plugin.getTeamManager().isTeamFull(team.getName()))
                    .map(Team::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
            }

            if (action.equals("info")) {
                return plugin.getTeamManager()
                        .getAllTeams()
                        .stream()
                        .map(Team::getName)
                        .toList();
            }
        }

        return List.of();
    }
}

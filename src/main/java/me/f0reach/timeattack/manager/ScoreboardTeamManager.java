package me.f0reach.timeattack.manager;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Bukkit Scoreboard Teamを使用してプレイヤーの色分けを管理するクラス
 * タブリストとネームタグの色を制御する
 */
public class ScoreboardTeamManager {
    private final PluginMain plugin;
    private final Scoreboard scoreboard;
    private int colorIndex = 0;

    // スコアボードチーム名のプレフィックス（他プラグインとの競合を避けるため）
    private static final String TEAM_PREFIX = "ta_";

    public ScoreboardTeamManager(PluginMain plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    /**
     * 全ての既存チームをスコアボードに同期する
     */
    public void initialize() {
        if (!plugin.getConfigManager().isTeamColorsEnabled()) {
            return;
        }

        for (Team team : plugin.getTeamManager().getAllTeams()) {
            syncTeam(team);
        }

        plugin.getLogger().info("ScoreboardTeamManager initialized");
    }

    /**
     * プラグインのチームをBukkitスコアボードチームに同期する
     */
    public void syncTeam(Team team) {
        if (!plugin.getConfigManager().isTeamColorsEnabled()) {
            return;
        }

        String teamName = team.getName();
        String scoreboardTeamName = TEAM_PREFIX + teamName;

        // Bukkitチームを取得または作成
        org.bukkit.scoreboard.Team bukkitTeam = scoreboard.getTeam(scoreboardTeamName);
        if (bukkitTeam == null) {
            bukkitTeam = scoreboard.registerNewTeam(scoreboardTeamName);
        }

        // 色が未設定の場合は自動割り当て
        String color = team.getColor();
        if (color == null) {
            color = assignNextColor(teamName);
            team.setColor(color);
            plugin.getConfigManager().saveTeam(team);
        }

        // Bukkitチームに色を適用
        NamedTextColor namedColor = parseColor(color);
        if (namedColor != null) {
            bukkitTeam.color(namedColor);
        }

        // 表示オプション設定
        bukkitTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);

        // チームメンバーを同期
        for (UUID memberId : team.getMembers()) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null) {
                addPlayerToScoreboardTeam(player, team);
            }
        }
    }

    /**
     * 次の利用可能な色を割り当てる
     */
    private String assignNextColor(String teamName) {
        List<String> availableColors = plugin.getConfigManager().getAvailableColors();
        if (availableColors.isEmpty()) {
            return "WHITE";
        }

        // 既に使用中の色を取得
        Set<String> usedColors = new HashSet<>();
        for (Team team : plugin.getTeamManager().getAllTeams()) {
            if (team.hasColor()) {
                usedColors.add(team.getColor());
            }
        }

        // 未使用の色を探す
        for (String color : availableColors) {
            if (!usedColors.contains(color)) {
                return color;
            }
        }

        // 全色使用済みの場合は順番に割り当て
        String color = availableColors.get(colorIndex % availableColors.size());
        colorIndex++;
        return color;
    }

    /**
     * プレイヤーをスコアボードチームに追加する
     */
    public void addPlayerToScoreboardTeam(Player player, Team team) {
        if (!plugin.getConfigManager().isTeamColorsEnabled()) {
            return;
        }

        String scoreboardTeamName = TEAM_PREFIX + team.getName();
        org.bukkit.scoreboard.Team bukkitTeam = scoreboard.getTeam(scoreboardTeamName);

        if (bukkitTeam != null) {
            // まず他のチームから削除
            removePlayerFromAllScoreboardTeams(player);
            bukkitTeam.addPlayer(player);
        }
    }

    /**
     * プレイヤーを全てのプラグインスコアボードチームから削除する
     */
    public void removePlayerFromAllScoreboardTeams(Player player) {
        for (org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                team.removePlayer(player);
            }
        }
    }

    /**
     * スコアボードチームを削除する
     */
    public void removeScoreboardTeam(String teamName) {
        String scoreboardTeamName = TEAM_PREFIX + teamName;
        org.bukkit.scoreboard.Team bukkitTeam = scoreboard.getTeam(scoreboardTeamName);
        if (bukkitTeam != null) {
            bukkitTeam.unregister();
        }
    }

    /**
     * 色名をNamedTextColorに変換する
     */
    public static NamedTextColor parseColor(String colorName) {
        if (colorName == null) return NamedTextColor.WHITE;

        return switch (colorName.toUpperCase()) {
            case "BLACK" -> NamedTextColor.BLACK;
            case "DARK_BLUE" -> NamedTextColor.DARK_BLUE;
            case "DARK_GREEN" -> NamedTextColor.DARK_GREEN;
            case "DARK_AQUA" -> NamedTextColor.DARK_AQUA;
            case "DARK_RED" -> NamedTextColor.DARK_RED;
            case "DARK_PURPLE" -> NamedTextColor.DARK_PURPLE;
            case "GOLD" -> NamedTextColor.GOLD;
            case "GRAY" -> NamedTextColor.GRAY;
            case "DARK_GRAY" -> NamedTextColor.DARK_GRAY;
            case "BLUE" -> NamedTextColor.BLUE;
            case "GREEN" -> NamedTextColor.GREEN;
            case "AQUA" -> NamedTextColor.AQUA;
            case "RED" -> NamedTextColor.RED;
            case "LIGHT_PURPLE" -> NamedTextColor.LIGHT_PURPLE;
            case "YELLOW" -> NamedTextColor.YELLOW;
            case "WHITE" -> NamedTextColor.WHITE;
            default -> NamedTextColor.WHITE;
        };
    }

    /**
     * チームの色を取得する
     */
    public NamedTextColor getTeamColor(Team team) {
        return parseColor(team.getColor());
    }

    /**
     * プラグイン終了時のクリーンアップ
     */
    public void cleanup() {
        for (org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                team.unregister();
            }
        }
    }
}

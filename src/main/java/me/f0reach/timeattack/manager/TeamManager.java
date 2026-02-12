package me.f0reach.timeattack.manager;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.GameState;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.model.WorldSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * チームの管理を行うクラス
 */
public class TeamManager {
    private final PluginMain plugin;
    private final Map<String, Team> teams; // teamName -> Team
    private final Map<UUID, String> playerTeams; // playerUUID -> teamName

    public TeamManager(PluginMain plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.playerTeams = new HashMap<>();
    }

    /**
     * 保存されたデータからチームを読み込む
     */
    public void loadTeams() {
        long seed = plugin.getConfigManager().getCurrentSeed();
        Map<String, Team> loaded = plugin.getConfigManager().loadTeams(seed);

        teams.clear();
        playerTeams.clear();

        for (Map.Entry<String, Team> entry : loaded.entrySet()) {
            Team team = entry.getValue();
            teams.put(team.getName(), team);

            // プレイヤーマッピングを再構築
            for (UUID memberId : team.getMembers()) {
                playerTeams.put(memberId, team.getName());
            }

            // ワールドセットをWorldSetManagerに登録
            if (team.getWorldSet() != null) {
                plugin.getWorldSetManager().registerWorldSet(team.getWorldSet());
            }
        }

        plugin.getLogger().info("Loaded " + teams.size() + " teams from data file");
    }

    /**
     * 新しいチームを作成
     * 
     * @return 作成されたチーム、既に存在する場合はnull
     */
    public Team createTeam(String name) {
        if (teams.containsKey(name)) {
            return null;
        }

        Team team = new Team(name);
        // ゲーム進行中の場合、チーム状態をRUNNINGに設定
        if (plugin.getGameManager().getGameState() == GameState.RUNNING) {
            team.setState(GameState.RUNNING);
        }
        teams.put(name, team);
        plugin.getConfigManager().saveTeam(team);

        // スコアボードチームに同期
        if (plugin.getScoreboardTeamManager() != null) {
            plugin.getScoreboardTeamManager().syncTeam(team);
        }

        plugin.getLogger().info("Created team: " + name);
        return team;
    }

    /**
     * チームを削除
     * 
     * @return 削除成功の場合true
     */
    public boolean deleteTeam(String name) {
        Team team = teams.get(name);
        if (team == null) {
            return false;
        }

        if (team.hasWorldSet()) {
            WorldSet worldSet = team.getWorldSet();

            // ワールドが削除できるかを確認
            if (!plugin.getWorldSetManager().canDeleteWorldSet(worldSet)) {
                plugin.getLogger().warning("Cannot delete team '" + name + "' because its worlds are in use.");
                return false;
            }

            // ワールドセットを削除
            if (plugin.getWorldSetManager().deleteWorldSet(worldSet)) {
                plugin.getLogger().info("Deleted world set for team: " + name);
            } else {
                plugin.getLogger().warning("Failed to delete world set for team: " + name);
                return false;
            }
        }

        // メンバーをスコアボードチームから削除
        if (plugin.getScoreboardTeamManager() != null) {
            for (UUID memberId : team.getMembers()) {
                Player player = Bukkit.getPlayer(memberId);
                if (player != null) {
                    plugin.getScoreboardTeamManager().removePlayerFromAllScoreboardTeams(player);
                }
            }

            plugin.getScoreboardTeamManager().removeScoreboardTeam(name);
        }

        // メンバーをチームから削除
        // Note: team.getMembers()とplayerTeamsは独立しているため、ここでのループは安全
        for (UUID memberId : team.getMembers()) {
            playerTeams.remove(memberId);
        }

        teams.remove(name);
        plugin.getConfigManager().deleteTeam(name);

        plugin.getLogger().info("Deleted team: " + name);
        return true;
    }

    /**
     * プレイヤーをチームに追加
     * 
     * @return 追加成功の場合true
     */
    public boolean addPlayer(UUID playerId, String teamName) {
        Team team = teams.get(teamName);
        if (team == null) {
            return false;
        }

        // 既に別のチームに所属している場合は除外
        String currentTeam = playerTeams.get(playerId);
        if (currentTeam != null) {
            if (currentTeam.equals(teamName)) {
                return true; // 既に同じチームに所属
            }
            removePlayer(playerId); // 以前のチームから削除
        }

        // チーム人数制限チェック
        int maxMembers = plugin.getConfigManager().getMaxTeamMembers();
        if (maxMembers > 0 && team.getMemberCount() >= maxMembers) {
            return false;
        }

        team.addMember(playerId);
        playerTeams.put(playerId, teamName);
        plugin.getConfigManager().saveTeam(team);

        // スコアボードチームに追加
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && plugin.getScoreboardTeamManager() != null) {
            plugin.getScoreboardTeamManager().addPlayerToScoreboardTeam(player, team);
        }

        return true;
    }

    /**
     * プレイヤーをチームから削除
     * 
     * @return 削除成功の場合true
     */
    public boolean removePlayer(UUID playerId) {
        String teamName = playerTeams.get(playerId);
        if (teamName == null) {
            return false;
        }

        Team team = teams.get(teamName);
        if (team != null) {
            team.removeMember(playerId);
            plugin.getConfigManager().saveTeam(team);
        }

        // スコアボードチームから削除
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && plugin.getScoreboardTeamManager() != null) {
            plugin.getScoreboardTeamManager().removePlayerFromAllScoreboardTeams(player);
        }

        playerTeams.remove(playerId);
        return true;
    }

    /**
     * チームを取得
     */
    public Team getTeam(String name) {
        return teams.get(name);
    }

    /**
     * プレイヤーのチームを取得
     */
    public Team getPlayerTeam(UUID playerId) {
        String teamName = playerTeams.get(playerId);
        return teamName != null ? teams.get(teamName) : null;
    }

    /**
     * プレイヤーのチーム名を取得
     */
    public String getPlayerTeamName(UUID playerId) {
        return playerTeams.get(playerId);
    }

    /**
     * プレイヤーがチームに所属しているか確認
     */
    public boolean hasTeam(UUID playerId) {
        return playerTeams.containsKey(playerId);
    }

    /**
     * 人数が最も少ないチームを取得（自動振り分け用）
     * 
     * @return 最も人数が少ないチーム、チームがない場合はnull
     */
    public Team getTeamWithFewestMembers() {
        if (teams.isEmpty()) {
            return null;
        }

        int maxMembers = plugin.getConfigManager().getMaxTeamMembers();
        Team fewestTeam = null;
        int fewestCount = Integer.MAX_VALUE;

        for (Team team : teams.values()) {
            int count = team.getMemberCount();
            // 人数制限がある場合、満員のチームはスキップ
            if (maxMembers > 0 && count >= maxMembers) {
                continue;
            }
            if (count < fewestCount) {
                fewestCount = count;
                fewestTeam = team;
            }
        }

        return fewestTeam;
    }

    /**
     * プレイヤーを自動的にチームに振り分け
     * 
     * @return 振り分けられたチーム、失敗時はnull
     */
    public Team autoAssignPlayer(UUID playerId) {
        // 既にチームに所属している場合はそのチームを返す
        Team currentTeam = getPlayerTeam(playerId);
        if (currentTeam != null) {
            return currentTeam;
        }

        // ゲーム状態チェック
        if (plugin.getConfigManager().isAutoAssignOnlyWhenWaiting()) {
            GameState globalState = plugin.getConfigManager().getGlobalGameState();
            if (globalState != GameState.WAITING) {
                return null;
            }
        }

        // 人数最小のチームに振り分け
        Team team = getTeamWithFewestMembers();
        if (team != null && addPlayer(playerId, team.getName())) {
            return team;
        }

        return null;
    }

    /**
     * 全チームを取得
     */
    public Collection<Team> getAllTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    /**
     * チーム数を取得
     */
    public int getTeamCount() {
        return teams.size();
    }

    /**
     * 指定チームが満員か確認
     */
    public boolean isTeamFull(String teamName) {
        Team team = teams.get(teamName);
        if (team == null) {
            return true;
        }

        int maxMembers = plugin.getConfigManager().getMaxTeamMembers();
        return maxMembers > 0 && team.getMemberCount() >= maxMembers;
    }

    /**
     * チームにワールドセットを設定
     */
    public boolean setTeamWorldSet(String teamName, WorldSet worldSet) {
        Team team = teams.get(teamName);
        if (team == null) {
            return false;
        }

        team.setWorldSet(worldSet);
        plugin.getConfigManager().saveTeam(team);
        return true;
    }

    /**
     * 全チームの状態をリセット
     */
    public void resetAllTeams() {
        for (Team team : teams.values()) {
            team.reset();
            plugin.getConfigManager().saveTeam(team);
        }
    }

    /**
     * 全データをクリア
     */
    public void clearAll() {
        teams.clear();
        playerTeams.clear();
    }

    /**
     * バイパス権限を持たない全オンラインプレイヤーをランダムにチームへ振り分け
     * 
     * @return 振り分けられたプレイヤーとチームのマップ
     */
    public Map<Player, Team> randomAssignAllPlayers() {
        Map<Player, Team> assignments = new LinkedHashMap<>();

        // バイパス権限を持たず、チーム未所属のオンラインプレイヤーを取得
        List<Player> eligiblePlayers = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.hasPermission("timeattack.autojoin.bypass"))
                .filter(p -> !hasTeam(p.getUniqueId()))
                .collect(Collectors.toList());

        if (eligiblePlayers.isEmpty() || teams.isEmpty()) {
            return assignments;
        }

        // ランダムにシャッフル
        Collections.shuffle(eligiblePlayers);

        // 各プレイヤーを人数最小のチームに振り分け
        for (Player player : eligiblePlayers) {
            Team team = getTeamWithFewestMembers();
            if (team != null && addPlayer(player.getUniqueId(), team.getName())) {
                assignments.put(player, team);
            }
        }

        return assignments;
    }
}

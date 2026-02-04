package me.f0reach.timeattack.manager;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.GameState;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.model.WorldSet;
import me.f0reach.timeattack.util.MessageUtil;
import me.f0reach.timeattack.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * タイムアタックのゲームフローを管理するクラス
 */
public class GameManager {
    private final PluginMain plugin;
    private GameState gameState;
    private BukkitTask countdownTask;
    private final List<CompletionRecord> completionRecords;

    public GameManager(PluginMain plugin) {
        this.plugin = plugin;
        this.gameState = GameState.WAITING;
        this.completionRecords = new ArrayList<>();
    }

    /**
     * 保存されたゲーム状態を読み込む
     */
    public void loadState() {
        this.gameState = plugin.getConfigManager().getGlobalGameState();
    }

    /**
     * 現在のゲーム状態を取得
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * ゲーム状態を設定
     */
    private void setGameState(GameState state) {
        this.gameState = state;
        plugin.getConfigManager().setGlobalGameState(state);
    }

    /**
     * ゲームを開始可能か確認
     */
    public String canStartGame() {
        if (gameState != GameState.WAITING) {
            return "ゲームは既に開始されています";
        }

        if (!plugin.getConfigManager().hasSeed()) {
            return "シードが設定されていません。/ta setup <seed> でシードを設定してください";
        }

        Collection<Team> teams = plugin.getTeamManager().getAllTeams();
        if (teams.isEmpty()) {
            return "チームが存在しません";
        }

        for (Team team : teams) {
            if (!team.hasWorldSet()) {
                return "チーム「" + team.getName() + "」のワールドが作成されていません。/ta create " + team.getName() + " で作成してください";
            }
            if (team.getMemberCount() == 0) {
                return "チーム「" + team.getName() + "」にメンバーがいません";
            }
        }

        return null; // 開始可能
    }

    /**
     * ゲームを開始（カウントダウン付き）
     *
     * @return 開始成功の場合true
     */
    public boolean startGame() {
        String error = canStartGame();
        if (error != null) {
            return false;
        }

        int countdown = plugin.getConfigManager().getStartCountdown();
        if (countdown > 0) {
            startCountdown(countdown);
        } else {
            executeGameStart();
        }

        return true;
    }

    /**
     * カウントダウンを開始
     */
    private void startCountdown(int seconds) {
        final int[] remaining = {seconds};

        MessageUtil.broadcast("ゲーム開始まであと " + seconds + " 秒！");

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            remaining[0]--;

            if (remaining[0] <= 0) {
                countdownTask.cancel();
                countdownTask = null;
                executeGameStart();
            } else if (remaining[0] <= 5 || remaining[0] % 10 == 0) {
                MessageUtil.broadcast("あと " + remaining[0] + " 秒...");

                if (remaining[0] <= 3) {
                    // 全プレイヤーにタイトル表示
                    for (Team team : plugin.getTeamManager().getAllTeams()) {
                        for (UUID memberId : team.getMembers()) {
                            Player player = Bukkit.getPlayer(memberId);
                            if (player != null) {
                                MessageUtil.showTitle(player, String.valueOf(remaining[0]), "準備してください！");
                            }
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    /**
     * ゲームを実際に開始
     */
    private void executeGameStart() {
        setGameState(GameState.RUNNING);
        completionRecords.clear();

        // 全チームのプレイヤーをテレポート
        for (Team team : plugin.getTeamManager().getAllTeams()) {
            team.setState(GameState.RUNNING);
            teleportTeamToSpawn(team);
            plugin.getConfigManager().saveTeam(team);
        }

        // タイマー開始（全チーム共通）
        plugin.getTimeManager().startTimer();

        // 開始アナウンス
        MessageUtil.broadcast("タイムアタック開始！");

        if (plugin.getConfigManager().isShowTitleOnStart()) {
            for (Team team : plugin.getTeamManager().getAllTeams()) {
                for (UUID memberId : team.getMembers()) {
                    Player player = Bukkit.getPlayer(memberId);
                    if (player != null) {
                        MessageUtil.showTitle(player, "GO!", "目指せ最速クリア！");
                    }
                }
            }
        }

        plugin.getLogger().info("Time Attack game started!");
    }

    /**
     * チームのメンバーをスポーン地点にテレポート
     */
    public void teleportTeamToSpawn(Team team) {
        WorldSet worldSet = team.getWorldSet();
        if (worldSet == null) {
            return;
        }

        for (UUID memberId : team.getMembers()) {
            Player player = Bukkit.getPlayer(memberId);
            if (player == null) {
                continue;
            }
            plugin.getWorldSetManager().teleportToWorldSetSpawn(worldSet, World.Environment.NORMAL, player);
        }
    }

    /**
     * ゲーム完了を処理（ワールドIDで呼び出し）
     *
     * @param worldId 完了したワールドの名前
     * @return 完了処理成功の場合true
     */
    public boolean completeGame(String worldId) {
        if (gameState != GameState.RUNNING) {
            return false;
        }

        // ワールドIDからチームを特定
        WorldSet worldSet = plugin.getWorldSetManager().getWorldSetByWorldName(worldId);
        if (worldSet == null) {
            plugin.getLogger().warning("Unknown world ID for completion: " + worldId);
            return false;
        }

        String teamName = worldSet.getTeamName();
        return completeTeam(teamName);
    }

    /**
     * チームのゲーム完了を処理
     */
    public boolean completeTeam(String teamName) {
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            return false;
        }

        if (team.getState() != GameState.RUNNING) {
            return false; // 既に完了している場合
        }

        // 完了時間を記録（共通タイマーから計算）
        long completionTime = plugin.getTimeManager().recordCompletion(teamName);

        // チーム状態を更新
        team.setState(GameState.COMPLETED);
        team.setCompletionTime(completionTime);
        plugin.getConfigManager().saveTeam(team);

        // 完了記録を追加
        int rank = completionRecords.size() + 1;
        completionRecords.add(new CompletionRecord(teamName, completionTime, rank));

        // 完了アナウンス
        announceCompletion(team, completionTime, rank);

        // 全チーム完了チェック
        checkAllTeamsCompleted();

        plugin.getLogger().info("Team " + teamName + " completed in " + TimeUtil.formatTime(completionTime));
        return true;
    }

    /**
     * 完了をアナウンス
     */
    private void announceCompletion(Team team, long time, int rank) {
        String rankStr = switch (rank) {
            case 1 -> "§6§l1位";
            case 2 -> "§72位";
            case 3 -> "§c3位";
            default -> rank + "位";
        };

        MessageUtil.announceCompletion(team.getName(), time);
        MessageUtil.broadcast(rankStr + " でゴール！");

        // チームメンバーにタイトル表示
        String timeStr = TimeUtil.formatTime(time);
        for (UUID memberId : team.getMembers()) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null) {
                MessageUtil.showTitle(player, "クリア！", timeStr);
            }
        }
    }

    /**
     * 全チームが完了したかチェック
     */
    private void checkAllTeamsCompleted() {
        for (Team team : plugin.getTeamManager().getAllTeams()) {
            if (team.getState() != GameState.COMPLETED) {
                return; // まだ完了していないチームがある
            }
        }

        // 全チーム完了
        endGame();
    }

    /**
     * ゲームを終了
     */
    public void endGame() {
        setGameState(GameState.COMPLETED);
        plugin.getTimeManager().stopUpdateTask();

        MessageUtil.broadcast("=== タイムアタック終了 ===");
        showLeaderboard();
    }

    /**
     * リーダーボードを表示
     */
    public void showLeaderboard() {
        if (completionRecords.isEmpty()) {
            MessageUtil.broadcast("完了したチームはありません");
            return;
        }

        MessageUtil.broadcast("=== 最終結果 ===");
        for (CompletionRecord record : completionRecords) {
            String rankStr = switch (record.rank) {
                case 1 -> "§6§l1位";
                case 2 -> "§72位";
                case 3 -> "§c3位";
                default -> record.rank + "位";
            };
            MessageUtil.broadcast(rankStr + " " + record.teamName + " - " + TimeUtil.formatTime(record.time));
        }
    }

    /**
     * ゲームをリセット
     */
    public void resetGame() {
        // カウントダウン中の場合はキャンセル
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        // タイマー停止とリセット
        plugin.getTimeManager().stopTimer();
        plugin.getTimeManager().resetTimer();

        // チーム状態をリセット
        plugin.getTeamManager().resetAllTeams();

        // 完了記録をクリア
        completionRecords.clear();

        // ゲーム状態をリセット
        setGameState(GameState.WAITING);

        MessageUtil.broadcast("ゲームがリセットされました");
        plugin.getLogger().info("Game reset completed");
    }

    /**
     * チームの現在のステータス情報を取得
     */
    public String getTeamStatus(Team team) {
        StringBuilder sb = new StringBuilder();
        sb.append("チーム: ").append(team.getName()).append("\n");
        sb.append("状態: ").append(getStateDisplayName(team.getState())).append("\n");
        sb.append("メンバー数: ").append(team.getMemberCount()).append("\n");

        if (team.hasWorldSet()) {
            sb.append("ワールド: 作成済み\n");
        } else {
            sb.append("ワールド: 未作成\n");
        }

        if (team.getState() == GameState.RUNNING) {
            long elapsed = plugin.getTimeManager().getElapsedTime();
            sb.append("経過時間: ").append(TimeUtil.formatTime(elapsed)).append("\n");
        } else if (team.getState() == GameState.COMPLETED) {
            sb.append("クリアタイム: ").append(TimeUtil.formatTime(team.getCompletionTime())).append("\n");
        }

        return sb.toString();
    }

    private String getStateDisplayName(GameState state) {
        return switch (state) {
            case WAITING -> "待機中";
            case RUNNING -> "プレイ中";
            case COMPLETED -> "完了";
        };
    }

    /**
     * 完了記録を保持するレコードクラス
     */
    private record CompletionRecord(String teamName, long time, int rank) {
    }
}

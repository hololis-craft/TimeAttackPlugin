package me.f0reach.timeattack.manager;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import me.f0reach.timeattack.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * ゲーム全体で共通のタイマーを管理するクラス
 * 開始時刻はConfigに保存され、サーバー再起動後も継続可能
 */
public class TimeManager {
    private final PluginMain plugin;
    private BukkitTask updateTask;

    public TimeManager(PluginMain plugin) {
        this.plugin = plugin;
    }

    /**
     * ゲームタイマーを開始（開始時刻をConfigに保存）
     */
    public void startTimer() {
        long startTime = System.currentTimeMillis();
        plugin.getConfigManager().setGameStartTime(startTime);
        plugin.getLogger().info("Game timer started at: " + startTime);
        startUpdateTask();
    }

    /**
     * ゲームタイマーを停止（開始時刻はクリアしない）
     */
    public void stopTimer() {
        stopUpdateTask();
        plugin.getLogger().info("Game timer stopped");
    }

    /**
     * 現在の経過時間を取得
     * @return 経過時間（ミリ秒）、タイマーが開始されていない場合は0
     */
    public long getElapsedTime() {
        long startTime = plugin.getConfigManager().getGameStartTime();
        if (startTime <= 0) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 指定時点での経過時間を取得（完了時刻から計算）
     * @param endTime 終了時刻（ミリ秒）
     * @return 経過時間（ミリ秒）
     */
    public long getElapsedTimeAt(long endTime) {
        long startTime = plugin.getConfigManager().getGameStartTime();
        if (startTime <= 0 || endTime <= 0) {
            return 0;
        }
        return endTime - startTime;
    }

    /**
     * チームの完了時間を記録し、経過時間を返す
     * @param teamName チーム名
     * @return 経過時間（ミリ秒）
     */
    public long recordCompletion(String teamName) {
        long endTime = System.currentTimeMillis();
        long elapsed = getElapsedTimeAt(endTime);
        plugin.getLogger().info("Team " + teamName + " completed at: " + endTime + " (elapsed: " + TimeUtil.formatTime(elapsed) + ")");
        return elapsed;
    }

    /**
     * タイマーが動作中か確認
     */
    public boolean isTimerRunning() {
        return plugin.getConfigManager().getGameStartTime() > 0;
    }

    /**
     * タイマーをリセット（開始時刻をクリア）
     */
    public void resetTimer() {
        stopUpdateTask();
        plugin.getConfigManager().setGameStartTime(0);
        plugin.getLogger().info("Game timer reset");
    }

    /**
     * アクションバー更新タスクを開始
     */
    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        if (!plugin.getConfigManager().isShowActionbar()) {
            return;
        }

        int interval = plugin.getConfigManager().getTimeUpdateInterval();
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateActionBars, interval, interval);
    }

    /**
     * アクションバー更新タスクを停止
     */
    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    /**
     * 全プレイヤーのアクションバーを更新
     */
    private void updateActionBars() {
        long elapsed = getElapsedTime();
        if (elapsed <= 0) {
            return;
        }

        String timeStr = TimeUtil.formatTimeShort(elapsed);
        String message = "⏱ " + timeStr;

        // ゲームに参加している全プレイヤーにアクションバーを表示
        for (Team team : plugin.getTeamManager().getAllTeams()) {
            for (UUID memberId : team.getMembers()) {
                Player player = Bukkit.getPlayer(memberId);
                if (player != null && player.isOnline()) {
                    MessageUtil.showActionBar(player, message);
                }
            }
        }
    }

    /**
     * サーバー再起動後にタイマータスクを再開
     * （ゲームが進行中の場合）
     */
    public void resumeIfRunning() {
        long startTime = plugin.getConfigManager().getGameStartTime();
        if (startTime > 0) {
            plugin.getLogger().info("Resuming game timer from: " + startTime + " (elapsed: " + TimeUtil.formatTime(getElapsedTime()) + ")");
            startUpdateTask();
        }
    }
}

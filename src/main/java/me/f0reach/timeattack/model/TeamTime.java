package me.f0reach.timeattack.model;

/**
 * チームのタイムアタック時間を管理するクラス
 */
public class TeamTime {
    private final String teamName;
    private long startTime;
    private long endTime;
    private boolean running;

    public TeamTime(String teamName) {
        this.teamName = teamName;
        this.startTime = -1;
        this.endTime = -1;
        this.running = false;
    }

    /**
     * タイマーを開始する
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.endTime = -1;
        this.running = true;
    }

    /**
     * タイマーを停止し、経過時間を返す
     * @return 経過時間（ミリ秒）、開始していない場合は-1
     */
    public long stop() {
        if (!running || startTime < 0) {
            return -1;
        }
        this.endTime = System.currentTimeMillis();
        this.running = false;
        return getElapsed();
    }

    /**
     * 現在の経過時間を取得
     * @return 経過時間（ミリ秒）、開始していない場合は0
     */
    public long getElapsed() {
        if (startTime < 0) {
            return 0;
        }
        if (running) {
            return System.currentTimeMillis() - startTime;
        }
        if (endTime > 0) {
            return endTime - startTime;
        }
        return 0;
    }

    /**
     * タイマーが動作中か確認
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * タイマーをリセット
     */
    public void reset() {
        this.startTime = -1;
        this.endTime = -1;
        this.running = false;
    }

    public String getTeamName() {
        return teamName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}

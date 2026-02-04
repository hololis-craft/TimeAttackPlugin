package me.f0reach.timeattack.api;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.model.WorldSet;

/**
 * 外部プラグイン向けAPI
 * タイムアタックの完了通知などに使用
 */
public final class TimeAttackAPI {

    private TimeAttackAPI() {
        // インスタンス化防止
    }

    /**
     * ゲームの完了を通知する
     * エンダードラゴン討伐検知プラグインなどから呼び出される
     *
     * @param worldId 完了したワールドの名前（オーバーワールド/ネザー/エンドのいずれか）
     * @return 完了処理が成功した場合true
     */
    public static boolean completeGame(String worldId) {
        PluginMain plugin = PluginMain.getInstance();
        if (plugin == null) {
            return false;
        }

        return plugin.getGameManager().completeGame(worldId);
    }

    /**
     * ワールド名からチーム名を取得する
     *
     * @param worldName ワールド名
     * @return チーム名、該当するチームがない場合はnull
     */
    public static String getTeamByWorld(String worldName) {
        PluginMain plugin = PluginMain.getInstance();
        if (plugin == null) {
            return null;
        }

        WorldSet worldSet = plugin.getWorldSetManager().getWorldSetByWorldName(worldName);
        return worldSet != null ? worldSet.getTeamName() : null;
    }

    /**
     * チームの完了時間を取得する
     *
     * @param teamName チーム名
     * @return 完了時間（ミリ秒）、未完了または該当チームがない場合は-1
     */
    public static long getCompletionTime(String teamName) {
        PluginMain plugin = PluginMain.getInstance();
        if (plugin == null) {
            return -1;
        }

        Team team = plugin.getTeamManager().getTeam(teamName);
        return team != null ? team.getCompletionTime() : -1;
    }

    /**
     * 指定されたワールドがタイムアタック用ワールドか確認する
     *
     * @param worldName ワールド名
     * @return タイムアタック用ワールドの場合true
     */
    public static boolean isTimeAttackWorld(String worldName) {
        PluginMain plugin = PluginMain.getInstance();
        if (plugin == null) {
            return false;
        }

        return plugin.getWorldSetManager().isTimeAttackWorld(worldName);
    }

    /**
     * ゲームの現在の経過時間を取得する（全チーム共通）
     *
     * @return 経過時間（ミリ秒）、タイマーが動作していない場合は0
     */
    public static long getElapsedTime() {
        PluginMain plugin = PluginMain.getInstance();
        if (plugin == null) {
            return 0;
        }

        return plugin.getTimeManager().getElapsedTime();
    }

    /**
     * ゲームの開始時刻を取得する
     *
     * @return 開始時刻（ミリ秒）、ゲームが開始されていない場合は0
     */
    public static long getGameStartTime() {
        PluginMain plugin = PluginMain.getInstance();
        if (plugin == null) {
            return 0;
        }

        return plugin.getConfigManager().getGameStartTime();
    }

    /**
     * タイムアタックプラグインがロードされているか確認
     *
     * @return プラグインがロードされている場合true
     */
    public static boolean isLoaded() {
        return PluginMain.getInstance() != null;
    }
}

package me.f0reach.timeattack.model;

/**
 * ゲームの状態を表すenum
 */
public enum GameState {
    /**
     * ゲーム開始待ち
     */
    WAITING,

    /**
     * ゲーム進行中
     */
    RUNNING,

    /**
     * ゲーム完了（チーム単位で使用）
     */
    COMPLETED
}

package me.f0reach.timeattack.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * タイムアタックに参加するチームを表すクラス
 */
public class Team {
    private final String name;
    private final Set<UUID> members;
    private WorldSet worldSet;
    private GameState state;
    private long completionTime;
    private String color;

    public Team(String name) {
        this.name = name;
        this.members = new HashSet<>();
        this.worldSet = null;
        this.state = GameState.WAITING;
        this.completionTime = -1;
        this.color = null;
    }

    public String getName() {
        return name;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public int getMemberCount() {
        return members.size();
    }

    /**
     * メンバーを追加
     * @return 追加に成功した場合true
     */
    public boolean addMember(UUID playerId) {
        return members.add(playerId);
    }

    /**
     * メンバーを削除
     * @return 削除に成功した場合true
     */
    public boolean removeMember(UUID playerId) {
        return members.remove(playerId);
    }

    /**
     * 指定したプレイヤーがメンバーか確認
     */
    public boolean hasMember(UUID playerId) {
        return members.contains(playerId);
    }

    public WorldSet getWorldSet() {
        return worldSet;
    }

    public void setWorldSet(WorldSet worldSet) {
        this.worldSet = worldSet;
    }

    /**
     * ワールドセットが設定されているか確認
     */
    public boolean hasWorldSet() {
        return worldSet != null;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    /**
     * チームがゲームを完了しているか確認
     */
    public boolean isCompleted() {
        return state == GameState.COMPLETED && completionTime >= 0;
    }

    /**
     * チームの状態をリセット
     */
    public void reset() {
        this.state = GameState.WAITING;
        this.completionTime = -1;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * 色が設定されているか確認
     */
    public boolean hasColor() {
        return color != null;
    }
}

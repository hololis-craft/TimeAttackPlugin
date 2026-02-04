package me.f0reach.timeattack.model;

import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * チームに紐づくワールドセット（オーバーワールド/ネザー/エンド）
 */
public class WorldSet {
    private final String teamName;
    private final String overworldName;
    private final String netherName;
    private final String endName;
    private final long seed;

    public WorldSet(String teamName, long seed) {
        this.teamName = teamName;
        this.seed = seed;
        // Multiverse-NetherPortals の自動リンク命名規則に従う
        this.overworldName = teamName;
        this.netherName = teamName + "_nether";
        this.endName = teamName + "_the_end";
    }

    public WorldSet(String teamName, String overworldName, String netherName, String endName, long seed) {
        this.teamName = teamName;
        this.overworldName = overworldName;
        this.netherName = netherName;
        this.endName = endName;
        this.seed = seed;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getOverworldName() {
        return overworldName;
    }

    public String getNetherName() {
        return netherName;
    }

    public String getEndName() {
        return endName;
    }

    public long getSeed() {
        return seed;
    }

    /**
     * オーバーワールドのBukkitワールドを取得
     */
    public World getOverworld() {
        return Bukkit.getWorld(overworldName);
    }

    /**
     * ネザーのBukkitワールドを取得
     */
    public World getNether() {
        return Bukkit.getWorld(netherName);
    }

    /**
     * エンドのBukkitワールドを取得
     */
    public World getEnd() {
        return Bukkit.getWorld(endName);
    }

    /**
     * 指定されたワールド名がこのセットに含まれるか確認
     */
    public boolean containsWorld(String worldName) {
        return overworldName.equals(worldName)
            || netherName.equals(worldName)
            || endName.equals(worldName);
    }

    /**
     * 指定されたワールドがこのセットに含まれるか確認
     */
    public boolean containsWorld(World world) {
        return world != null && containsWorld(world.getName());
    }

    /**
     * 環境タイプに対応するワールドを取得
     */
    public World getWorld(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> getOverworld();
            case NETHER -> getNether();
            case THE_END -> getEnd();
            default -> null;
        };
    }

    /**
     * 環境タイプに対応するワールド名を取得
     */
    public String getWorldName(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> overworldName;
            case NETHER -> netherName;
            case THE_END -> endName;
            default -> null;
        };
    }
}

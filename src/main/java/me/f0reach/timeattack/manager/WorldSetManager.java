package me.f0reach.timeattack.manager;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.WorldSet;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Multiverse Core APIを使用してワールドセットを管理するクラス
 */
public class WorldSetManager {
    private final PluginMain plugin;
    private final Map<String, WorldSet> worldSets; // teamName -> WorldSet
    private MultiverseCoreApi mvApi;

    public WorldSetManager(PluginMain plugin) {
        this.plugin = plugin;
        this.worldSets = new HashMap<>();
    }

    /**
     * Multiverse Coreとの連携を初期化
     *
     * @return 初期化成功の場合true
     */
    public boolean initialize() {
        try {
            mvApi = MultiverseCoreApi.get();
            if (mvApi == null) {
                plugin.getLogger().severe("Multiverse-Core API is not available!");
                return false;
            }
            plugin.getLogger().info("Successfully connected to Multiverse-Core API");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize Multiverse-Core integration", e);
            return false;
        }
    }

    /**
     * チーム用のワールドセットを作成
     *
     * @param teamName チーム名（ワールド名のプレフィックスになる）
     * @param seed     ワールドのシード値
     * @return 作成されたWorldSet、失敗時はnull
     */
    public WorldSet createWorldSet(String teamName, long seed) {
        if (mvApi == null) {
            plugin.getLogger().severe("Multiverse-Core is not initialized!");
            return null;
        }

        WorldManager worldManager = mvApi.getWorldManager();
        WorldSet worldSet = new WorldSet(teamName, seed);
        boolean generateStructures = plugin.getConfigManager().isGenerateStructures();

        try {
            // オーバーワールド作成
            plugin.getLogger().info("Creating overworld: " + worldSet.getOverworldName());
            var overworldResult = worldManager.createWorld(
                    CreateWorldOptions.worldName(worldSet.getOverworldName())
                            .environment(World.Environment.NORMAL)
                            .seed(seed)
                            .generateStructures(generateStructures)
            );

            if (overworldResult.isFailure()) {
                plugin.getLogger().severe("Failed to create overworld: " + overworldResult.getFailureReason());
                return null;
            }

            // ネザー作成
            plugin.getLogger().info("Creating nether: " + worldSet.getNetherName());
            var netherResult = worldManager.createWorld(
                    CreateWorldOptions.worldName(worldSet.getNetherName())
                            .environment(World.Environment.NETHER)
                            .seed(seed)
                            .generateStructures(generateStructures)
            );

            if (netherResult.isFailure()) {
                plugin.getLogger().severe("Failed to create nether: " + netherResult.getFailureReason());
                // クリーンアップ：作成済みのオーバーワールドを削除
                deleteWorld(worldSet.getOverworldName());
                return null;
            }

            // エンド作成
            plugin.getLogger().info("Creating end: " + worldSet.getEndName());
            var endResult = worldManager.createWorld(
                    CreateWorldOptions.worldName(worldSet.getEndName())
                            .environment(World.Environment.THE_END)
                            .seed(seed)
                            .generateStructures(generateStructures)
            );

            if (endResult.isFailure()) {
                plugin.getLogger().severe("Failed to create end: " + endResult.getFailureReason());
                // クリーンアップ
                deleteWorld(worldSet.getOverworldName());
                deleteWorld(worldSet.getNetherName());
                return null;
            }

            // 成功：ワールドセットを登録
            worldSets.put(teamName, worldSet);
            plugin.getLogger().info("Successfully created world set for team: " + teamName);

            return worldSet;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Exception while creating world set for " + teamName, e);
            return null;
        }
    }

    /**
     * ワールドセットのスポーン位置に安全にテレポート
     */
    public void teleportToWorldSetSpawn(WorldSet worldSet, World.Environment environment, Player player) {
        var targetWorld = worldSet.getWorld(environment);

        if (targetWorld == null) {
            plugin.getLogger().warning("Target world is null for environment: " + environment);
            return;
        }

        // 安全なスポーン位置を取得
        var spawnLocation = targetWorld.getSpawnLocation().clone();
        mvApi.getSafetyTeleporter().to(spawnLocation)
                .checkSafety(true)
                .teleport(player);
    }

    /**
     * ワールドセットを削除
     */
    public boolean deleteWorldSet(String teamName) {
        WorldSet worldSet = worldSets.get(teamName);
        if (worldSet == null) {
            return false;
        }

        boolean success = true;
        success &= deleteWorld(worldSet.getOverworldName());
        success &= deleteWorld(worldSet.getNetherName());
        success &= deleteWorld(worldSet.getEndName());

        if (success) {
            worldSets.remove(teamName);
        }

        return success;
    }

    /**
     * 単一のワールドを削除
     */
    private boolean deleteWorld(String worldName) {
        if (mvApi == null) {
            return false;
        }

        try {
            WorldManager worldManager = mvApi.getWorldManager();
            var worldOpt = worldManager.getLoadedWorld(worldName);

            if (worldOpt.isEmpty()) {
                plugin.getLogger().warning("World not found for deletion: " + worldName);
                return true; // ワールドが存在しない場合は成功とみなす
            }

            LoadedMultiverseWorld world = worldOpt.get();
            var deleteResult = worldManager.deleteWorld(DeleteWorldOptions.world(world));

            if (deleteResult.isFailure()) {
                plugin.getLogger().severe("Failed to delete world " + worldName + ": " + deleteResult.getFailureReason());
                return false;
            }

            plugin.getLogger().info("Successfully deleted world: " + worldName);
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Exception while deleting world: " + worldName, e);
            return false;
        }
    }

    /**
     * ワールド名からワールドセットを取得
     */
    public WorldSet getWorldSetByWorldName(String worldName) {
        for (WorldSet worldSet : worldSets.values()) {
            if (worldSet.containsWorld(worldName)) {
                return worldSet;
            }
        }
        return null;
    }

    /**
     * ワールドからチーム名を取得
     */
    public String getTeamNameByWorld(World world) {
        if (world == null) {
            return null;
        }
        WorldSet worldSet = getWorldSetByWorldName(world.getName());
        return worldSet != null ? worldSet.getTeamName() : null;
    }

    /**
     * チーム名からワールドセットを取得
     */
    public WorldSet getWorldSet(String teamName) {
        return worldSets.get(teamName);
    }

    /**
     * 指定されたワールドがタイムアタック用ワールドか確認
     */
    public boolean isTimeAttackWorld(String worldName) {
        return getWorldSetByWorldName(worldName) != null;
    }

    /**
     * ワールドセットを登録（リロード時などに使用）
     */
    public void registerWorldSet(WorldSet worldSet) {
        worldSets.put(worldSet.getTeamName(), worldSet);
    }

    /**
     * 全ワールドセットをクリア
     */
    public void clearAllWorldSets() {
        worldSets.clear();
    }

    /**
     * 登録されている全ワールドセットを取得
     */
    public Map<String, WorldSet> getAllWorldSets() {
        return new HashMap<>(worldSets);
    }
}

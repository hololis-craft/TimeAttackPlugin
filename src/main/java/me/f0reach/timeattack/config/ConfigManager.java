package me.f0reach.timeattack.config;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.GameState;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.model.WorldSet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * プラグインの設定を管理するクラス
 */
public class ConfigManager {
    private final PluginMain plugin;
    private FileConfiguration config;
    private File gameDataFile;
    private YamlConfiguration gameData;

    // 設定値のキャッシュ
    private boolean debug;
    private String messagePrefix;
    private boolean generateStructures;
    private int maxTeamMembers;
    private boolean autoAssignEnabled;
    private boolean autoAssignOnlyWhenWaiting;
    private int startCountdown;
    private boolean showTitleOnStart;
    private int timeUpdateInterval;
    private boolean showActionbar;

    public ConfigManager(PluginMain plugin) {
        this.plugin = plugin;
    }

    /**
     * 設定を読み込む
     */
    public void load() {
        // config.yml
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // 値をキャッシュ
        debug = config.getBoolean("general.debug", false);
        messagePrefix = config.getString("general.message-prefix", "[TimeAttack] ");
        generateStructures = config.getBoolean("worlds.generate-structures", true);
        maxTeamMembers = config.getInt("teams.max-members", 4);
        autoAssignEnabled = config.getBoolean("teams.auto-assign.enabled", true);
        autoAssignOnlyWhenWaiting = config.getBoolean("teams.auto-assign.only-when-waiting", true);
        startCountdown = config.getInt("game.start-countdown", 10);
        showTitleOnStart = config.getBoolean("game.show-title-on-start", true);
        timeUpdateInterval = config.getInt("time.update-interval", 20);
        showActionbar = config.getBoolean("time.show-actionbar", true);

        // game-data.yml
        gameDataFile = new File(plugin.getDataFolder(), "game-data.yml");
        if (!gameDataFile.exists()) {
            try {
                gameDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create game-data.yml: " + e.getMessage());
            }
        }
        gameData = YamlConfiguration.loadConfiguration(gameDataFile);
    }

    /**
     * 設定をリロード
     */
    public void reload() {
        load();
    }

    /**
     * ゲームデータを保存
     */
    public void saveGameData() {
        try {
            gameData.save(gameDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save game-data.yml: " + e.getMessage());
        }
    }

    // ========== ゲームデータの読み書き ==========

    /**
     * 現在のシードを取得
     */
    public long getCurrentSeed() {
        return gameData.getLong("current-seed", 0);
    }

    /**
     * シードを設定
     */
    public void setCurrentSeed(long seed) {
        gameData.set("current-seed", seed);
        saveGameData();
    }

    /**
     * シードが設定されているか確認
     */
    public boolean hasSeed() {
        return gameData.contains("current-seed") && getCurrentSeed() != 0;
    }

    /**
     * グローバルゲーム状態を取得
     */
    public GameState getGlobalGameState() {
        String state = gameData.getString("game-state", "WAITING");
        try {
            return GameState.valueOf(state);
        } catch (IllegalArgumentException e) {
            return GameState.WAITING;
        }
    }

    /**
     * グローバルゲーム状態を設定
     */
    public void setGlobalGameState(GameState state) {
        gameData.set("game-state", state.name());
        saveGameData();
    }

    /**
     * チームデータを保存
     */
    public void saveTeam(Team team) {
        String path = "teams." + team.getName();

        // メンバーをUUID文字列リストとして保存
        List<String> memberStrings = new ArrayList<>();
        for (UUID uuid : team.getMembers()) {
            memberStrings.add(uuid.toString());
        }
        gameData.set(path + ".members", memberStrings);

        // ワールドセット情報
        WorldSet worldSet = team.getWorldSet();
        if (worldSet != null) {
            gameData.set(path + ".world-set.overworld", worldSet.getOverworldName());
            gameData.set(path + ".world-set.nether", worldSet.getNetherName());
            gameData.set(path + ".world-set.end", worldSet.getEndName());
        }

        // 状態
        gameData.set(path + ".state", team.getState().name());
        gameData.set(path + ".completion-time", team.getCompletionTime());

        saveGameData();
    }

    /**
     * チームデータを読み込む
     */
    public Map<String, Team> loadTeams(long seed) {
        Map<String, Team> teams = new HashMap<>();
        ConfigurationSection teamsSection = gameData.getConfigurationSection("teams");

        if (teamsSection == null) {
            return teams;
        }

        for (String teamName : teamsSection.getKeys(false)) {
            String path = "teams." + teamName;
            Team team = new Team(teamName);

            // メンバー読み込み
            List<String> memberStrings = gameData.getStringList(path + ".members");
            for (String uuidStr : memberStrings) {
                try {
                    team.addMember(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {
                }
            }

            // ワールドセット
            if (gameData.contains(path + ".world-set.overworld")) {
                String overworld = gameData.getString(path + ".world-set.overworld");
                String nether = gameData.getString(path + ".world-set.nether");
                String end = gameData.getString(path + ".world-set.end");
                WorldSet worldSet = new WorldSet(teamName, overworld, nether, end, seed);
                team.setWorldSet(worldSet);
            }

            // 状態
            String stateStr = gameData.getString(path + ".state", "WAITING");
            try {
                team.setState(GameState.valueOf(stateStr));
            } catch (IllegalArgumentException e) {
                team.setState(GameState.WAITING);
            }

            team.setCompletionTime(gameData.getLong(path + ".completion-time", -1));

            teams.put(teamName, team);
        }

        return teams;
    }

    /**
     * チームデータを削除
     */
    public void deleteTeam(String teamName) {
        gameData.set("teams." + teamName, null);
        saveGameData();
    }

    /**
     * 全ゲームデータをクリア
     */
    public void clearAllGameData() {
        gameData.set("current-seed", null);
        gameData.set("game-state", "WAITING");
        gameData.set("game-start-time", null);
        gameData.set("teams", null);
        saveGameData();
    }

    /**
     * ゲーム開始時刻を取得
     * @return 開始時刻（ミリ秒）、未設定の場合は0
     */
    public long getGameStartTime() {
        return gameData.getLong("game-start-time", 0);
    }

    /**
     * ゲーム開始時刻を設定
     * @param startTime 開始時刻（ミリ秒）、0でクリア
     */
    public void setGameStartTime(long startTime) {
        if (startTime <= 0) {
            gameData.set("game-start-time", null);
        } else {
            gameData.set("game-start-time", startTime);
        }
        saveGameData();
    }

    // ========== Getters ==========

    public boolean isDebug() {
        return debug;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public boolean isGenerateStructures() {
        return generateStructures;
    }

    public int getMaxTeamMembers() {
        return maxTeamMembers;
    }

    public boolean isAutoAssignEnabled() {
        return autoAssignEnabled;
    }

    public boolean isAutoAssignOnlyWhenWaiting() {
        return autoAssignOnlyWhenWaiting;
    }

    public int getStartCountdown() {
        return startCountdown;
    }

    public boolean isShowTitleOnStart() {
        return showTitleOnStart;
    }

    public int getTimeUpdateInterval() {
        return timeUpdateInterval;
    }

    public boolean isShowActionbar() {
        return showActionbar;
    }
}

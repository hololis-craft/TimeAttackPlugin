package me.f0reach.timeattack;

import me.f0reach.timeattack.command.TimeAttackCommand;
import me.f0reach.timeattack.config.ConfigManager;
import me.f0reach.timeattack.listener.AdvancementListener;
import me.f0reach.timeattack.listener.ChatListener;
import me.f0reach.timeattack.listener.PlayerJoinListener;
import me.f0reach.timeattack.listener.PlayerQuitListener;
import me.f0reach.timeattack.manager.GameManager;
import me.f0reach.timeattack.manager.ScoreboardTeamManager;
import me.f0reach.timeattack.manager.TeamManager;
import me.f0reach.timeattack.manager.TimeManager;
import me.f0reach.timeattack.manager.WorldSetManager;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginMain extends JavaPlugin {

    private static PluginMain instance;

    private ConfigManager configManager;
    private WorldSetManager worldSetManager;
    private TeamManager teamManager;
    private GameManager gameManager;
    private TimeManager timeManager;
    private ScoreboardTeamManager scoreboardTeamManager;

    @Override
    public void onEnable() {
        instance = this;

        // 設定マネージャーを初期化
        configManager = new ConfigManager(this);
        configManager.load();

        // メッセージプレフィックスを設定
        MessageUtil.setPrefix(configManager.getMessagePrefix());

        // Multiverse-Coreの存在確認
        if (getServer().getPluginManager().getPlugin("Multiverse-Core") == null) {
            getLogger().severe("Multiverse-Core is required but not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // マネージャーを初期化
        worldSetManager = new WorldSetManager(this);
        if (!worldSetManager.initialize()) {
            getLogger().severe("Failed to initialize WorldSetManager!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        teamManager = new TeamManager(this);
        timeManager = new TimeManager(this);
        gameManager = new GameManager(this);
        scoreboardTeamManager = new ScoreboardTeamManager(this);

        // 保存されたデータを読み込む
        teamManager.loadTeams();
        gameManager.loadState();

        // スコアボードチームを初期化（チーム読み込み後）
        scoreboardTeamManager.initialize();

        // ゲームが進行中の場合、タイマータスクを再開
        timeManager.resumeIfRunning();

        // コマンドを登録
        registerCommands();

        // イベントリスナーを登録
        registerListeners();

        getLogger().info("TimeAttackPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // タイマーを停止
        if (timeManager != null) {
            timeManager.stopUpdateTask();
        }

        // スコアボードチームをクリーンアップ
        if (scoreboardTeamManager != null) {
            scoreboardTeamManager.cleanup();
        }

        getLogger().info("TimeAttackPlugin has been disabled!");
    }

    private void registerCommands() {
        PluginCommand command = getCommand("timeattack");
        if (command != null) {
            TimeAttackCommand executor = new TimeAttackCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new AdvancementListener(this), this);
    }

    // ========== Singleton & Getters ==========

    public static PluginMain getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WorldSetManager getWorldSetManager() {
        return worldSetManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public ScoreboardTeamManager getScoreboardTeamManager() {
        return scoreboardTeamManager;
    }
}

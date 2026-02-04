package me.f0reach.timeattack.listener;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.GameState;
import me.f0reach.timeattack.model.Team;
import me.f0reach.timeattack.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * プレイヤー参加時の処理
 */
public class PlayerJoinListener implements Listener {
    private final PluginMain plugin;

    public PlayerJoinListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 既にチームに所属しているか確認
        Team currentTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        if (currentTeam != null) {
            // 既存のチームに所属している場合
            MessageUtil.sendInfo(player, "おかえりなさい！チーム「" + currentTeam.getName() + "」に所属しています");

            // ゲームが進行中の場合、チームのワールドにテレポート
            if (plugin.getGameManager().getGameState() == GameState.RUNNING &&
                currentTeam.getState() == GameState.RUNNING &&
                currentTeam.hasWorldSet()) {
                // 少し遅延してテレポート（ログイン処理完了後）
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && !currentTeam.getWorldSet().containsWorld(player.getWorld())) {
                        plugin.getGameManager().teleportTeamToSpawn(currentTeam);
                        MessageUtil.sendInfo(player, "ゲーム進行中のためチームワールドにテレポートしました");
                    }
                }, 20L);
            }
            return;
        }

        // 自動振り分けが有効か確認
        if (!plugin.getConfigManager().isAutoAssignEnabled()) {
            return;
        }

        // 自動振り分けバイパス権限を持っているか確認
        if (player.hasPermission("timeattack.autojoin.bypass")) {
            return;
        }

        // ゲーム状態に応じて自動振り分けを行うか判断
        if (plugin.getConfigManager().isAutoAssignOnlyWhenWaiting()) {
            GameState globalState = plugin.getGameManager().getGameState();
            if (globalState != GameState.WAITING) {
                return;
            }
        }

        // チームが存在しない場合は振り分けない
        if (plugin.getTeamManager().getTeamCount() == 0) {
            return;
        }

        // 自動振り分け
        Team assignedTeam = plugin.getTeamManager().autoAssignPlayer(player.getUniqueId());
        if (assignedTeam != null) {
            MessageUtil.sendSuccess(player, "チーム「" + assignedTeam.getName() + "」に自動で振り分けられました");
        }
    }
}

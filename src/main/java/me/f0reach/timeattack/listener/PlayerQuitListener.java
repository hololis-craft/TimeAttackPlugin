package me.f0reach.timeattack.listener;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * プレイヤー退出時の処理
 */
public class PlayerQuitListener implements Listener {
    private final PluginMain plugin;

    public PlayerQuitListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // チームに所属しているか確認
        Team team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        if (team == null) {
            return;
        }

        // ログ出力（チームからは削除しない - 再接続時に復帰できるように）
        plugin.getLogger().info("Player " + player.getName() + " from team " + team.getName() + " has disconnected");

        // 注意：プレイヤーがチームから離脱するわけではない
        // 再接続時に同じチームに復帰できるようにする
    }
}

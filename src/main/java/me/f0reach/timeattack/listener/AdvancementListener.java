package me.f0reach.timeattack.listener;

import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.model.GameState;
import me.f0reach.timeattack.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.List;

/**
 * 実績達成時のゲーム完了判定を処理するリスナー
 */
public class AdvancementListener implements Listener {
    private final PluginMain plugin;

    public AdvancementListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        // ゲームが進行中でなければ無視
        if (plugin.getGameManager().getGameState() != GameState.RUNNING) {
            return;
        }

        // 達成条件の実績リストを取得
        List<String> completionAdvancements = plugin.getConfigManager().getCompletionAdvancements();
        if (completionAdvancements.isEmpty()) {
            return;
        }

        // 達成した実績のキーが条件リストに含まれるか確認
        String advancementKey = event.getAdvancement().getKey().toString();
        if (!completionAdvancements.contains(advancementKey)) {
            return;
        }

        // プレイヤーの所属チームを取得
        Player player = event.getPlayer();
        Team team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        if (team == null) {
            return;
        }

        // チームが既に完了していれば無視
        if (team.getState() != GameState.RUNNING) {
            return;
        }

        // ゲーム完了処理
        plugin.getGameManager().completeTeam(team.getName());
    }
}

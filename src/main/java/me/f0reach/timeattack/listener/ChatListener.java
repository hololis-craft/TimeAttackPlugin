package me.f0reach.timeattack.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.f0reach.timeattack.PluginMain;
import me.f0reach.timeattack.manager.ScoreboardTeamManager;
import me.f0reach.timeattack.model.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * チャットにチームカラーを適用するリスナー
 */
public class ChatListener implements Listener {
    private final PluginMain plugin;

    public ChatListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        // チームカラー機能が無効の場合はスキップ
        if (!plugin.getConfigManager().isTeamColorsEnabled() ||
            !plugin.getConfigManager().isTeamChatEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        Team team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());

        // チームに所属していない場合はスキップ
        if (team == null) {
            return;
        }

        NamedTextColor teamColor = ScoreboardTeamManager.parseColor(team.getColor());

        // 元のメッセージをプレーンテキストとして取得
        String originalMessage = PlainTextComponentSerializer.plainText()
                .serialize(event.message());

        // フォーマットを取得
        String format = plugin.getConfigManager().getTeamChatFormat();

        // フォーマットされたメッセージを構築
        Component formattedMessage = buildChatMessage(
                format,
                team.getName(),
                player.getName(),
                originalMessage,
                teamColor
        );

        // レンダラーを使用してチャット出力をカスタマイズ
        event.renderer((source, sourceDisplayName, message, viewer) -> formattedMessage);
    }

    /**
     * フォーマット文字列からチャットメッセージComponentを構築する
     */
    private Component buildChatMessage(String format, String teamName,
                                       String playerName, String message,
                                       NamedTextColor color) {
        Component result = Component.empty();

        int i = 0;
        while (i < format.length()) {
            if (format.charAt(i) == '%') {
                // プレースホルダーの終了位置を探す
                int end = format.indexOf('%', i + 1);
                if (end > i) {
                    String placeholder = format.substring(i + 1, end);
                    switch (placeholder) {
                        case "team" -> result = result.append(
                                Component.text(teamName).color(color));
                        case "player" -> result = result.append(
                                Component.text(playerName).color(color));
                        case "message" -> result = result.append(
                                Component.text(message).color(NamedTextColor.WHITE));
                        default -> result = result.append(
                                Component.text("%" + placeholder + "%"));
                    }
                    i = end + 1;
                    continue;
                }
            }

            // プレースホルダーでない通常文字を収集
            StringBuilder sb = new StringBuilder();
            while (i < format.length() && format.charAt(i) != '%') {
                sb.append(format.charAt(i));
                i++;
            }
            if (sb.length() > 0) {
                result = result.append(Component.text(sb.toString()).color(NamedTextColor.GRAY));
            }
        }

        return result;
    }
}

package me.f0reach.timeattack.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;

/**
 * メッセージフォーマットユーティリティ
 */
public final class MessageUtil {
    private static String prefix = "[TimeAttack] ";

    private MessageUtil() {
    }

    public static void setPrefix(String newPrefix) {
        prefix = newPrefix;
    }

    /**
     * プレフィックス付きの情報メッセージを送信
     */
    public static void sendInfo(Player player, String message) {
        player.sendMessage(Component.text(prefix)
            .color(NamedTextColor.GOLD)
            .append(Component.text(message).color(NamedTextColor.WHITE)));
    }

    /**
     * プレフィックス付きの成功メッセージを送信
     */
    public static void sendSuccess(Player player, String message) {
        player.sendMessage(Component.text(prefix)
            .color(NamedTextColor.GOLD)
            .append(Component.text(message).color(NamedTextColor.GREEN)));
    }

    /**
     * プレフィックス付きのエラーメッセージを送信
     */
    public static void sendError(Player player, String message) {
        player.sendMessage(Component.text(prefix)
            .color(NamedTextColor.GOLD)
            .append(Component.text(message).color(NamedTextColor.RED)));
    }

    /**
     * プレフィックス付きの警告メッセージを送信
     */
    public static void sendWarning(Player player, String message) {
        player.sendMessage(Component.text(prefix)
            .color(NamedTextColor.GOLD)
            .append(Component.text(message).color(NamedTextColor.YELLOW)));
    }

    /**
     * 全プレイヤーにブロードキャスト
     */
    public static void broadcast(String message) {
        Bukkit.broadcast(Component.text(prefix)
            .color(NamedTextColor.GOLD)
            .append(Component.text(message).color(NamedTextColor.WHITE)));
    }

    /**
     * 指定したプレイヤーにブロードキャスト
     */
    public static void broadcast(Collection<? extends Player> players, String message) {
        Component msg = Component.text(prefix)
            .color(NamedTextColor.GOLD)
            .append(Component.text(message).color(NamedTextColor.WHITE));
        for (Player player : players) {
            player.sendMessage(msg);
        }
    }

    /**
     * タイトルを表示
     */
    public static void showTitle(Player player, String title, String subtitle) {
        Title.Times times = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofSeconds(3),
            Duration.ofMillis(500)
        );
        player.showTitle(Title.title(
            Component.text(title).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
            Component.text(subtitle).color(NamedTextColor.WHITE),
            times
        ));
    }

    /**
     * 指定したプレイヤー全員にタイトルを表示
     */
    public static void showTitle(Collection<? extends Player> players, String title, String subtitle) {
        for (Player player : players) {
            showTitle(player, title, subtitle);
        }
    }

    /**
     * アクションバーにメッセージを表示
     */
    public static void showActionBar(Player player, String message) {
        player.sendActionBar(Component.text(message).color(NamedTextColor.GREEN));
    }

    /**
     * アクションバーにメッセージを表示（複数プレイヤー）
     */
    public static void showActionBar(Collection<? extends Player> players, String message) {
        Component msg = Component.text(message).color(NamedTextColor.GREEN);
        for (Player player : players) {
            player.sendActionBar(msg);
        }
    }

    /**
     * ゲーム完了時のアナウンス
     */
    public static void announceCompletion(String teamName, long timeMillis) {
        String timeFormatted = TimeUtil.formatTime(timeMillis);
        Component message = Component.text(prefix)
            .color(NamedTextColor.GOLD)
            .append(Component.text("チーム ").color(NamedTextColor.WHITE))
            .append(Component.text(teamName).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
            .append(Component.text(" がクリア！ タイム: ").color(NamedTextColor.WHITE))
            .append(Component.text(timeFormatted).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));

        Bukkit.broadcast(message);
    }
}

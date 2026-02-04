package me.f0reach.timeattack.util;

/**
 * 時間フォーマットユーティリティ
 */
public final class TimeUtil {
    private TimeUtil() {
    }

    /**
     * ミリ秒を HH:mm:ss.SSS 形式にフォーマット
     */
    public static String formatTime(long millis) {
        if (millis < 0) {
            return "--:--:--.---";
        }

        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;
        long ms = millis % 1000;

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);
    }

    /**
     * ミリ秒を mm:ss 形式にフォーマット（短縮版）
     */
    public static String formatTimeShort(long millis) {
        if (millis < 0) {
            return "--:--";
        }

        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes >= 60) {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * ミリ秒を人間が読みやすい形式にフォーマット
     * 例: "1時間23分45秒"
     */
    public static String formatTimeReadable(long millis) {
        if (millis < 0) {
            return "計測中...";
        }

        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;
        long ms = millis % 1000;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("時間");
        }
        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append("分");
        }
        sb.append(seconds).append(".").append(String.format("%03d", ms)).append("秒");

        return sb.toString();
    }
}

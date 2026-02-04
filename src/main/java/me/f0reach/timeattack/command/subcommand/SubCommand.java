package me.f0reach.timeattack.command.subcommand;

import me.f0reach.timeattack.PluginMain;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * サブコマンドの基底クラス
 */
public abstract class SubCommand {
    protected final PluginMain plugin;

    public SubCommand(PluginMain plugin) {
        this.plugin = plugin;
    }

    /**
     * サブコマンド名を取得
     */
    public abstract String getName();

    /**
     * サブコマンドの説明を取得
     */
    public abstract String getDescription();

    /**
     * 使用方法を取得
     */
    public abstract String getUsage();

    /**
     * 必要な権限を取得
     */
    public abstract String getPermission();

    /**
     * コマンドを実行
     * @param sender コマンド送信者
     * @param args 引数（サブコマンド名は含まない）
     * @return 実行成功の場合true
     */
    public abstract boolean execute(CommandSender sender, String[] args);

    /**
     * タブ補完候補を取得
     * @param sender コマンド送信者
     * @param args 引数（サブコマンド名は含まない）
     * @return 補完候補のリスト
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * 権限チェック
     */
    public boolean hasPermission(CommandSender sender) {
        String permission = getPermission();
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }
}

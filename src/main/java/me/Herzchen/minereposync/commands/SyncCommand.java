package me.Herzchen.minereposync.commands;

import me.Herzchen.minereposync.MineRepoSync;
import me.Herzchen.minereposync.core.SyncEngine;
import me.Herzchen.minereposync.utils.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class SyncCommand implements CommandExecutor {

    private final MineRepoSync plugin;
    private final SyncEngine syncEngine;
    private final Logger logger;

    public SyncCommand(MineRepoSync plugin, SyncEngine syncEngine) {
        this.plugin = plugin;
        this.syncEngine = syncEngine;
        this.logger = new Logger(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "update".equalsIgnoreCase(args[0])) {
            logger.logCommand("update", sender.getName(), "started", Collections.emptyList(), null);

            try {
                List<String> changedPlugins = syncEngine.sync(sender);
                logger.logCommand("update", sender.getName(), "success", changedPlugins, null);
                sender.sendMessage("§aĐồng bộ thành công! Đã cập nhật " + changedPlugins.size() + " plugin");
                return true;
            } catch (Exception e) {
                logger.logCommand("update", sender.getName(), "failed", Collections.emptyList(), e.getMessage());
                sender.sendMessage("§cLỗi khi đồng bộ: " + e.getMessage());
                return true;
            }
        } else if ("reload".equalsIgnoreCase(args[0])) {
            plugin.getPluginConfig().load();
            sender.sendMessage("§aĐã reload config!");
            return true;
        }
        return false;
    }
}
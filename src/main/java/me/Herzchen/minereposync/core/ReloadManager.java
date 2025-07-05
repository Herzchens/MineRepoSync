package me.Herzchen.minereposync.core;

import me.Herzchen.minereposync.MineRepoSync;
import me.Herzchen.minereposync.config.PluginConfig;
import me.Herzchen.minereposync.model.ReloadMapping;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class ReloadManager {

    private final MineRepoSync plugin;
    private final PluginConfig config;

    public ReloadManager(MineRepoSync plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void executeReloads(Set<String> changedPlugins, CommandSender sender) {
        for (String pluginName : changedPlugins) {
            ReloadMapping mapping = config.getReloadMappings().get(pluginName);
            if (mapping != null) {
                for (String cmd : mapping.getCommands()) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(sender, cmd);
                    });
                }
            }
        }
    }
}
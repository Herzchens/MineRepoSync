package me.Herzchen.minereposync;

import me.Herzchen.minereposync.commands.SyncCommand;
import me.Herzchen.minereposync.config.PluginConfig;
import me.Herzchen.minereposync.core.SyncEngine;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MineRepoSync extends JavaPlugin {

    private PluginConfig pluginConfig;
    private SyncEngine syncEngine;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("log.yml", false);

        pluginConfig = new PluginConfig(this);
        pluginConfig.load();

        syncEngine = new SyncEngine(this, pluginConfig);

        Objects.requireNonNull(getCommand("reposync")).setExecutor(new SyncCommand(this, syncEngine));

        getLogger().info("MineRepoSync đã được kích hoạt!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MineRepoSync đã bị vô hiệu hóa!");
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public SyncEngine getSyncEngine() {
        return syncEngine;
    }
}
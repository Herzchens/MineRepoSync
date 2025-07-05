package me.Herzchen.minereposync.config;

import me.Herzchen.minereposync.MineRepoSync;
import me.Herzchen.minereposync.model.ReloadMapping;
import me.Herzchen.minereposync.model.RepoConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginConfig {

    private final MineRepoSync plugin;
    private RepoConfig repoConfig;
    private Map<String, ReloadMapping> reloadMappings = new HashMap<>();

    public PluginConfig(MineRepoSync plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        repoConfig = ConfigMapper.mapRepoConfig(config.getConfigurationSection("repository"));

        ConfigurationSection reloadSection = config.getConfigurationSection("reload_mapping");
        if (reloadSection != null) {
            for (String key : reloadSection.getKeys(false)) {
                List<String> commands = reloadSection.getStringList(key);
                reloadMappings.put(key, new ReloadMapping(key, commands));
            }
        }
    }

    public RepoConfig getRepoConfig() {
        return repoConfig;
    }

    public Map<String, ReloadMapping> getReloadMappings() {
        return reloadMappings;
    }
}
package me.Herzchen.minereposync.utils;

import me.Herzchen.minereposync.MineRepoSync;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class Logger {

    private final MineRepoSync plugin;
    private File logFile;
    private YamlConfiguration logConfig;

    public Logger(MineRepoSync plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "log.yml");
        reload();
    }

    public void reload() {
        if (!logFile.exists()) {
            plugin.saveResource("log.yml", false);
        }
        logConfig = YamlConfiguration.loadConfiguration(logFile);
    }

    public void logCommand(String command, String executor, String status, List<String> changedPlugins, String error) {
        List<Map<String, Object>> logs = new ArrayList<>();
        if (logConfig.contains("logs")) {
            logs = (List<Map<String, Object>>) logConfig.getList("logs");
        }

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("timestamp", Instant.now().toString());
        entry.put("command", command);
        entry.put("executor", executor);
        entry.put("status", status);
        entry.put("changed_plugins", changedPlugins);
        entry.put("error", error != null ? error : "");

        logs.add(entry);
        logConfig.set("logs", logs);

        try {
            logConfig.save(logFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save log: " + e.getMessage());
        }
    }
}
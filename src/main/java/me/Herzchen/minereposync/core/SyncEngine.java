package me.Herzchen.minereposync.core;

import me.Herzchen.minereposync.MineRepoSync;
import me.Herzchen.minereposync.config.PluginConfig;
import me.Herzchen.minereposync.model.RepoConfig;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.*;

public class SyncEngine {

    private final MineRepoSync plugin;
    private final PluginConfig config;
    private final GitService gitService;
    private final FileProcessor fileProcessor;
    private final ReloadManager reloadManager;

    public SyncEngine(MineRepoSync plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        RepoConfig repoConfig = config.getRepoConfig();
        this.gitService = new GitService(plugin, repoConfig);
        this.fileProcessor = new FileProcessor(plugin, config);
        this.reloadManager = new ReloadManager(plugin, config);
    }

    public List<String> sync(CommandSender sender) {
        try {
            Set<String> changedPaths = gitService.pullRepository();

            Map<String, Set<String>> processedFiles = fileProcessor.processChanges(changedPaths);
            List<String> changedPlugins = new ArrayList<>(processedFiles.keySet());

            reloadManager.executeReloads(processedFiles.keySet(), sender);

            return changedPlugins;
        } catch (Exception e) {
            throw new RuntimeException("Đồng bộ thất bại: " + e.getMessage(), e);
        }
    }
}
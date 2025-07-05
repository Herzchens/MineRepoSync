package me.Herzchen.minereposync.core;

import me.Herzchen.minereposync.MineRepoSync;
import me.Herzchen.minereposync.config.PluginConfig;
import me.Herzchen.minereposync.model.RepoConfig;
import me.Herzchen.minereposync.utils.FileUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileProcessor {

    private final MineRepoSync plugin;
    private final PluginConfig config;

    public FileProcessor(MineRepoSync plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public Map<String, Set<String>> processChanges(Set<String> changedPaths) {
        Map<String, Set<String>> pluginChanges = new HashMap<>();
        RepoConfig repoConfig = config.getRepoConfig();

        for (String repoPath : repoConfig.getPaths()) {
            File repoDir = new File(plugin.getDataFolder(), "repo_cache");
            File sourceDir = new File(repoDir, repoPath);
            File targetDir = new File("plugins");

            if (sourceDir.exists() && sourceDir.isDirectory()) {
                try {
                    processDirectory(sourceDir, targetDir, pluginChanges);
                } catch (IOException e) {
                    plugin.getLogger().severe("Lỗi khi xử lý thư mục: " + repoPath);
                    e.printStackTrace();
                }
            }
        }

        return pluginChanges;
    }

    private void processDirectory(File source, File target, Map<String, Set<String>> pluginChanges) throws IOException {
        for (File file : source.listFiles()) {
            Path targetPath = Paths.get(target.getPath(), file.getName());

            if (file.isDirectory()) {
                processDirectory(file, new File(target, file.getName()), pluginChanges);
            } else {
                String pluginName = target.getName();
                pluginChanges.computeIfAbsent(pluginName, k -> new HashSet<>()).add(file.getName());

                FileUtils.createBackup(targetPath.toFile());

                Files.copy(file.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
package me.Herzchen.minereposync.core;

import me.Herzchen.minereposync.MineRepoSync;
import me.Herzchen.minereposync.config.PluginConfig;
import me.Herzchen.minereposync.model.RepoConfig;
import me.Herzchen.minereposync.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FileProcessor {

    private final MineRepoSync plugin;
    private final PluginConfig config;
    private final Map<File, File> rollbackFiles = new HashMap<>();
    private boolean hasErrors = false;

    public FileProcessor(MineRepoSync plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public Map<String, Set<String>> processChanges(Set<String> changedPaths) {
        Map<String, Set<String>> pluginChanges = new HashMap<>();
        RepoConfig repoConfig = config.getRepoConfig();
        rollbackFiles.clear();
        hasErrors = false;

        try {
            for (String repoPath : repoConfig.getPaths()) {
                File repoDir = new File(plugin.getDataFolder(), "repo_cache");
                File sourceDir = new File(repoDir, repoPath);
                File targetDir = new File("plugins", repoPath);

                if (sourceDir.exists() && sourceDir.isDirectory()) {
                    processDirectory(sourceDir, targetDir, pluginChanges);
                } else {
                    plugin.getLogger().warning("Source directory not found: " + sourceDir.getPath());
                }
            }

            if (hasErrors) {
                rollbackChanges();
                return Collections.emptyMap();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Lỗi nghiêm trọng khi xử lý file: " + e.getMessage());
            rollbackChanges();
            return Collections.emptyMap();
        }

        return pluginChanges;
    }

    private void processDirectory(File source, File target, Map<String, Set<String>> pluginChanges) {
        for (File file : source.listFiles()) {
            if (file == null) continue;

            Path targetPath = Paths.get(target.getPath(), file.getName());

            if (file.isDirectory()) {
                processDirectory(file, new File(target, file.getName()), pluginChanges);
            } else {
                try {
                    processFile(file, target, targetPath, pluginChanges);
                } catch (IOException e) {
                    plugin.getLogger().severe("Lỗi xử lý file: " + file.getName() + " - " + e.getMessage());
                    hasErrors = true;
                }
            }
        }
    }

    private void processFile(File sourceFile, File targetDir, Path targetPath,
                             Map<String, Set<String>> pluginChanges) throws IOException {
        String pluginName = targetDir.getName();
        pluginChanges.computeIfAbsent(pluginName, k -> new HashSet<>()).add(sourceFile.getName());

        File targetFile = targetPath.toFile();

        if (targetFile.exists() && isConflict(sourceFile, targetFile)) {
            handleConflict(sourceFile, targetFile);
        }

        if (targetFile.exists()) {
            File backup = createTempBackup(targetFile);
            rollbackFiles.put(targetFile, backup);
        }

        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isConflict(File sourceFile, File targetFile) {
        try {
            return !FileUtils.calculateHash(sourceFile).equals(FileUtils.calculateHash(targetFile));
        } catch (IOException e) {
            plugin.getLogger().warning("Không thể kiểm tra hash, sử dụng timestamp: " + e.getMessage());
            return sourceFile.lastModified() != targetFile.lastModified();
        }
    }

    private void handleConflict(File sourceFile, File targetFile) {
        String conflictStrategy = config.getRepoConfig().getConflictHandling();
        plugin.getLogger().warning("Phát hiện xung đột: " + targetFile.getName());

        try {
            switch (conflictStrategy) {
                case "backup":
                    FileUtils.createBackup(targetFile);
                    break;
                case "ignore":
                    break;
                default:
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Lỗi khi xử lý xung đột: " + e.getMessage());
        }
    }

    private File createTempBackup(File file) throws IOException {
        File backup = File.createTempFile("backup-", ".tmp");
        Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return backup;
    }

    private void rollbackChanges() {
        plugin.getLogger().warning("Bắt đầu rollback các thay đổi...");
        int success = 0, failed = 0;

        for (Map.Entry<File, File> entry : rollbackFiles.entrySet()) {
            File original = entry.getKey();
            File backup = entry.getValue();

            try {
                Files.copy(backup.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
                success++;
            } catch (IOException e) {
                plugin.getLogger().severe("Rollback thất bại cho: " + original.getName());
                failed++;
            } finally {
                if (!backup.delete()) {
                    plugin.getLogger().warning("Không thể xóa file backup tạm: " + backup.getName());
                }
            }
        }

        plugin.getLogger().warning(String.format(
                "Rollback hoàn tất: %d thành công, %d thất bại",
                success, failed
        ));
    }
}
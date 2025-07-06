package me.Herzchen.minereposync.model;

import java.util.List;
import java.util.Map;

public class RepoConfig {
    private String url;
    private String accessToken;
    private List<String> paths;
    private Map<String, List<String>> reloadMapping;
    private RepoOptions options;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public List<String> getPaths() { return paths; }
    public void setPaths(List<String> paths) { this.paths = paths; }

    public Map<String, List<String>> getReloadMapping() { return reloadMapping; }
    public void setReloadMapping(Map<String, List<String>> reloadMapping) { this.reloadMapping = reloadMapping; }

    public RepoOptions getOptions() { return options; }
    public void setOptions(RepoOptions options) { this.options = options; }

    public String getBackupDir() {
        return (options != null && options.getBackupDir() != null)
                ? options.getBackupDir()
                : "backups/MineRepoSync";
    }

    public String getConflictHandling() {
        return (options != null && options.getConflictHandling() != null)
                ? options.getConflictHandling()
                : "backup";
    }

    public boolean isDebugEnabled() {
        return options != null && options.isDebug();
    }

    public static class RepoOptions {
        private int autoSyncInterval;
        private String conflictHandling;
        private String backupDir;
        private boolean debug;

        public int getAutoSyncInterval() { return autoSyncInterval; }
        public void setAutoSyncInterval(int autoSyncInterval) { this.autoSyncInterval = autoSyncInterval; }

        public String getConflictHandling() { return conflictHandling; }
        public void setConflictHandling(String conflictHandling) { this.conflictHandling = conflictHandling; }

        public String getBackupDir() { return backupDir; }
        public void setBackupDir(String backupDir) { this.backupDir = backupDir; }

        public boolean isDebug() { return debug; }
        public void setDebug(boolean debug) { this.debug = debug; }
    }
}
package me.Herzchen.minereposync.core;

import me.Herzchen.minereposync.MineRepoSync;
import me.Herzchen.minereposync.model.RepoConfig;
import me.Herzchen.minereposync.utils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.PackInvalidException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GitService {

    private final MineRepoSync plugin;
    private final RepoConfig repoConfig;
    private final File repoDir;
    private final File backupDir;

    public GitService(MineRepoSync plugin, RepoConfig repoConfig) {
        this.plugin = plugin;
        this.repoConfig = repoConfig;
        this.repoDir = new File(plugin.getDataFolder(), "repo_cache");
        this.backupDir = new File(plugin.getDataFolder(), repoConfig.getBackupDir());
    }

    public Set<String> pullRepository() throws GitSyncException {
        try {
            if (!repoDir.exists()) {
                return cloneRepository();
            } else {
                return pullChanges();
            }
        } catch (GitAPIException | IOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CorruptObjectException || cause instanceof PackInvalidException) {
                try {
                    recoverCorruptedRepository();
                    return cloneRepository();
                } catch (Exception ex) {
                    throw new GitSyncException("Failed to recover corrupted repository", ex);
                }
            }
            throw new GitSyncException("Git operation failed: " + e.getMessage(), e);
        }
    }

    private Set<String> cloneRepository() throws GitAPIException, IOException {
        logDebug("Cloning repository: " + repoConfig.getUrl());
        Git.cloneRepository()
                .setURI(repoConfig.getUrl())
                .setDirectory(repoDir)
                .setCredentialsProvider(createCredentialsProvider())
                .setTimeout(30)
                .setDepth(1)
                .call()
                .close();

        int fileCount = countFilesInRepo();
        plugin.getLogger().warning("Full repository cloned! Total files: " + fileCount);
        return Collections.emptySet();
    }

    private Set<String> pullChanges() throws IOException, GitAPIException, GitSyncException {
        logDebug("Pulling changes from repository: " + repoConfig.getUrl());
        try (Repository repository = buildRepository();
             Git git = new Git(repository)) {

            ObjectId oldHead = repository.resolve("HEAD");
            if (oldHead == null) {
                logDebug("No HEAD found, performing initial clone");
                return cloneRepository();
            }

            PullResult pullResult = git.pull()
                    .setCredentialsProvider(createCredentialsProvider())
                    .setTimeout(30)
                    .call();

            if (!pullResult.isSuccessful()) {
                handlePullFailure(pullResult);
            }

            ObjectId newHead = repository.resolve("HEAD");

            if (Objects.equals(oldHead, newHead)) {
                logDebug("No changes detected in repository: " + repoConfig.getUrl());
                return Collections.emptySet();
            }

            Set<String> changedFiles = getChangedFiles(repository, git, oldHead, newHead);
            alertFileChanges(changedFiles.size());
            return changedFiles;
        }
    }

    private void handlePullFailure(PullResult pullResult) throws GitAPIException, GitSyncException {
        org.eclipse.jgit.api.MergeResult mergeResult = pullResult.getMergeResult();
        if (mergeResult != null && !mergeResult.getConflicts().isEmpty()) {
            handleMergeConflicts(mergeResult);
        } else {
            throw new TransportException("Pull failed: " + pullResult.toString());
        }
    }

    private void handleMergeConflicts(org.eclipse.jgit.api.MergeResult mergeResult) throws GitSyncException {
        String conflictStrategy = repoConfig.getConflictHandling();
        logDebug("Handling merge conflicts with strategy: " + conflictStrategy);
        switch (conflictStrategy) {
            case "backup":
                createConflictBackups(mergeResult);
                break;
            case "overwrite":
                break;
            case "ignore":
                throw new GitSyncException("Merge conflicts encountered and strategy is set to ignore");
            default:
                throw new GitSyncException("Unsupported conflict handling strategy: " + conflictStrategy);
        }
    }

    private void createConflictBackups(org.eclipse.jgit.api.MergeResult mergeResult) {
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            plugin.getLogger().warning("Failed to create backup directory: " + backupDir.getAbsolutePath());
            return;
        }

        Set<String> conflictedFiles = new HashSet<>();
        conflictedFiles.addAll(mergeResult.getCheckoutConflicts());
        conflictedFiles.addAll(mergeResult.getFailingPaths().keySet());

        for (String file : conflictedFiles) {
            try {
                File conflictFile = new File(repoDir, file);
                if (conflictFile.exists()) {
                    String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    File backupFile = new File(backupDir, conflictFile.getName() + "." + timestamp + ".conflict.bak");
                    FileUtils.copyFile(conflictFile, backupFile);
                    logDebug("Backup created for conflicted file: " + file);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to backup conflicted file: " + file);
            }
        }
    }

    private void recoverCorruptedRepository() throws IOException {
        logDebug("Attempting to recover corrupted repository...");
        if (repoDir.exists()) {
            deleteDirectory(repoDir);
        }
        if (!repoDir.mkdirs()) {
            throw new IOException("Failed to recreate repository directory");
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        plugin.getLogger().warning("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        if (!directory.delete()) {
            plugin.getLogger().warning("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

    private Repository buildRepository() throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(new File(repoDir, ".git"))
                .readEnvironment()
                .findGitDir()
                .build();
    }

    private Set<String> getChangedFiles(
            Repository repository,
            Git git,
            ObjectId oldHead,
            ObjectId newHead
    ) throws IOException, GitAPIException {

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit oldCommit = walk.parseCommit(oldHead);
            RevCommit newCommit = walk.parseCommit(newHead);

            List<DiffEntry> diffs = git.diff()
                    .setOldTree(getTreeParser(repository, oldCommit))
                    .setNewTree(getTreeParser(repository, newCommit))
                    .setShowNameAndStatusOnly(true)
                    .call();

            return diffs.stream()
                    .map(DiffEntry::getNewPath)
                    .filter(path -> !path.equals("/dev/null") && !path.isEmpty())
                    .collect(Collectors.toSet());
        }
    }

    private CanonicalTreeParser getTreeParser(Repository repo, RevCommit commit) throws IOException {
        try (ObjectReader reader = repo.newObjectReader()) {
            return new CanonicalTreeParser(null, reader, commit.getTree());
        }
    }

    private void alertFileChanges(int changedCount) {
        if (changedCount > 0) {
            String msg = String.format(
                    "Detected %d changed file%s! [Repository: %s]",
                    changedCount,
                    changedCount > 1 ? "s" : "",
                    repoConfig.getUrl()
            );
            plugin.getLogger().warning(msg);
        }
    }

    private int countFilesInRepo() {
        try {
            return FileUtils.listFilesRecursive(repoDir).size();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to count files: " + e.getMessage());
            return 0;
        }
    }

    private UsernamePasswordCredentialsProvider createCredentialsProvider() {
        String token = repoConfig.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Git access token is missing");
        }
        return new UsernamePasswordCredentialsProvider("token", token);
    }

    private void logDebug(String message) {
        if (repoConfig.isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
}
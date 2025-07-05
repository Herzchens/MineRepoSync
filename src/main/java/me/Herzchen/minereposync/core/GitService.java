package me.Herzchen.minereposync.core;

import me.Herzchen.minereposync.MineRepoSync;
import me.Herzchen.minereposync.model.RepoConfig;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class GitService {

    private final MineRepoSync plugin;
    private final RepoConfig repoConfig;
    private final File repoDir;

    public GitService(MineRepoSync plugin, RepoConfig repoConfig) {
        this.plugin = plugin;
        this.repoConfig = repoConfig;
        this.repoDir = new File(plugin.getDataFolder(), "repo_cache");
    }

    public Set<String> pullRepository() throws GitAPIException, IOException {
        if (!repoDir.exists()) {
            return cloneRepository();
        } else {
            return pullChanges();
        }
    }

    private Set<String> cloneRepository() throws GitAPIException {
        Git.cloneRepository()
                .setURI(repoConfig.getUrl())
                .setDirectory(repoDir)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", repoConfig.getAccessToken()))
                .call();

        return Collections.emptySet();
    }

    private Set<String> pullChanges() throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File(repoDir, ".git"))
                .readEnvironment()
                .findGitDir()
                .build();

        Git git = new Git(repository);
        PullResult pullResult = git.pull()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", repoConfig.getAccessToken()))
                .call();

        // TODO: Trả về danh sách file thay đổi thực tế
        return Collections.emptySet();
    }
}
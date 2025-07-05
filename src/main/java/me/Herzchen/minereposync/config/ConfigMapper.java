package me.Herzchen.minereposync.config;

import me.Herzchen.minereposync.model.ReloadMapping;
import me.Herzchen.minereposync.model.RepoConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ConfigMapper {

    public static RepoConfig mapRepoConfig(ConfigurationSection section) {
        if (section == null) return null;

        RepoConfig repoConfig = new RepoConfig();
        repoConfig.setUrl(section.getString("url"));
        repoConfig.setAccessToken(section.getString("access_token"));
        repoConfig.setPaths(section.getStringList("paths"));
        return repoConfig;
    }
}
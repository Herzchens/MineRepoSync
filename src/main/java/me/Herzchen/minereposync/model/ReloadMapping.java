package me.Herzchen.minereposync.model;

import java.util.List;

public class ReloadMapping {

    private String pluginName;
    private List<String> commands;

    public ReloadMapping(String pluginName, List<String> commands) {
        this.pluginName = pluginName;
        this.commands = commands;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
}
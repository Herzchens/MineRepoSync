package me.Herzchen.minereposync;

import me.Herzchen.minereposync.commands.SyncCommand;
import me.Herzchen.minereposync.commands.TabComplete;
import me.Herzchen.minereposync.config.PluginConfig;
import me.Herzchen.minereposync.core.SyncEngine;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MineRepoSync extends JavaPlugin {

    private PluginConfig pluginConfig;
    private SyncEngine syncEngine;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("log.yml", false);
        getCommand("reposync").setTabCompleter(new TabComplete());
        pluginConfig = new PluginConfig(this);
        pluginConfig.load();

        syncEngine = new SyncEngine(this, pluginConfig);

        Objects.requireNonNull(getCommand("reposync")).setExecutor(new SyncCommand(this, syncEngine));

        this.getServer().getConsoleSender().sendMessage("§a╔═══════════════════════════════════════════════════════╗");
        this.getServer().getConsoleSender().sendMessage("§a║                                                       ║");
        this.getServer().getConsoleSender().sendMessage("§a║   ███╗   ███╗██████╗ ███████╗███████╗                 ║");
        this.getServer().getConsoleSender().sendMessage("§a║   ████╗ ████║██╔══██╗██╔════╝██╔════╝                 ║");
        this.getServer().getConsoleSender().sendMessage("§a║   ██╔████╔██║██████╔╝█████╗  ███████╗                 ║");
        this.getServer().getConsoleSender().sendMessage("§a║   ██║╚██╔╝██║██╔══██╗██╔══╝  ╚════██║                 ║");
        this.getServer().getConsoleSender().sendMessage("§a║   ██║ ╚═╝ ██║██║  ██║███████╗███████║                 ║");
        this.getServer().getConsoleSender().sendMessage("§a║   ╚═╝     ╚═╝╚═╝  ╚═╝╚══════╝╚══════╝                 ║");
        this.getServer().getConsoleSender().sendMessage("§a║                                                       ║");
        this.getServer().getConsoleSender().sendMessage("§a║   §6Plugin đã được kích hoạt thành công!                §a║");
        this.getServer().getConsoleSender().sendMessage("§a║   §bVersion: " + getDescription().getVersion() + "                                      §a║");
        this.getServer().getConsoleSender().sendMessage("§a║   §dLiên hệ: discord itztli_herzchen                    §a║");
        this.getServer().getConsoleSender().sendMessage("§a║                                                       ║");
        this.getServer().getConsoleSender().sendMessage("§a╚═══════════════════════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        this.getServer().getConsoleSender().sendMessage("§c╔═══════════════════════════════════════════════════════╗");
        this.getServer().getConsoleSender().sendMessage("§c║                                                       ║");
        this.getServer().getConsoleSender().sendMessage("§c║   ███╗   ███╗██████╗ ███████╗███████╗                 ║");
        this.getServer().getConsoleSender().sendMessage("§c║   ████╗ ████║██╔══██╗██╔════╝██╔════╝                 ║");
        this.getServer().getConsoleSender().sendMessage("§c║   ██╔████╔██║██████╔╝█████╗  ███████╗                 ║");
        this.getServer().getConsoleSender().sendMessage("§c║   ██║╚██╔╝██║██╔══██╗██╔══╝  ╚════██║                 ║");
        this.getServer().getConsoleSender().sendMessage("§c║   ██║ ╚═╝ ██║██║  ██║███████╗███████║                 ║");
        this.getServer().getConsoleSender().sendMessage("§c║   ╚═╝     ╚═╝╚═╝  ╚═╝╚══════╝╚══════╝                 ║");
        this.getServer().getConsoleSender().sendMessage("§c║                                                       ║");
        this.getServer().getConsoleSender().sendMessage("§c║   §ePlugin đã được vô hiệu hóa an toàn!                 §c║");
        this.getServer().getConsoleSender().sendMessage("§c║   §bCảm ơn đã sử dụng MRS!                                                              §c║");
        this.getServer().getConsoleSender().sendMessage("§c║   §dLiên hệ: discord itztli_herzchen                    §c║");
        this.getServer().getConsoleSender().sendMessage("§c║                                                       ║");
        this.getServer().getConsoleSender().sendMessage("§c╚═══════════════════════════════════════════════════════╝");
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public SyncEngine getSyncEngine() {
        return syncEngine;
    }
}
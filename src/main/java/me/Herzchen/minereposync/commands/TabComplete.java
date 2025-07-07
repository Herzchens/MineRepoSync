package me.Herzchen.minereposync.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabComplete implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("update", "reload");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Kiểm tra cả tên lệnh chính và alias
        if (!command.getName().equalsIgnoreCase("reposync") && !alias.equalsIgnoreCase("rsync")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        // Gợi ý cho argument đầu tiên
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(partial)) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        return new ArrayList<>();
    }
}
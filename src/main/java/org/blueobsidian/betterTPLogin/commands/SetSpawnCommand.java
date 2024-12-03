package org.blueobsidian.betterTPLogin.commands;

import org.blueobsidian.betterTPLogin.managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    private static final String PREFIX = ChatColor.BLUE + "[BetterTPLogin] " + ChatColor.RESET;
    private final ConfigManager configManager;

    public SetSpawnCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!sender.hasPermission("bettertplogin.setspawn")) {
            sendMessage(sender, ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        try {
            configManager.setSpawnLocation(location);
            sendMessage(player, ChatColor.GREEN + "Spawn location set to: " +
                    ChatColor.YELLOW + formatLocation(location));
        } catch (Exception e) {
            sendMessage(player, ChatColor.RED + "Failed to set spawn location. Please try again.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + message);
    }

    private String formatLocation(Location loc) {
        return String.format("X: %.1f, Y: %.1f, Z: %.1f",
                loc.getX(), loc.getY(), loc.getZ());
    }
}
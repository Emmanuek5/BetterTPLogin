package org.blueobsidian.betterTPLogin.commands;

import org.blueobsidian.betterTPLogin.managers.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class RegisterCommand implements CommandExecutor {
    private static final String PREFIX = ChatColor.BLUE + "[BetterTPLogin] " + ChatColor.RESET;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private final PlayerManager playerManager;

    public RegisterCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            sendMessage(player, ChatColor.YELLOW + "Usage: /register <password>");
            return true;
        }

        String password = args[0];
        if (password.length() < MIN_PASSWORD_LENGTH) {
            sendMessage(player, ChatColor.RED + "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            return true;
        }

        if (playerManager.getPlayerData(player.getUniqueId()) != null) {
            sendMessage(player, ChatColor.RED + "You are already registered. Use /login <password> to log in.");
            return true;
        }

        try {
            String hashedPassword = hashPassword(password);
            Location spawnLocation = player.getLocation();
            String ipAddress = player.getAddress().getAddress().getHostAddress();
            playerManager.addPlayer(player.getUniqueId(), spawnLocation, hashedPassword, ipAddress);
            sendMessage(player, ChatColor.GREEN + "Registration successful! Use /login <password> to log in.");
        } catch (Exception e) {
            sendMessage(player, ChatColor.RED + "Registration failed. Please try again.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + message);
    }
}
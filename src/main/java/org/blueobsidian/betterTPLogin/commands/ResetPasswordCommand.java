package org.blueobsidian.betterTPLogin.commands;

import org.blueobsidian.betterTPLogin.managers.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class ResetPasswordCommand implements CommandExecutor {
    private static final String PREFIX = ChatColor.BLUE + "[BetterTPLogin] " + ChatColor.RESET;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private final PlayerManager playerManager;

    public ResetPasswordCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!playerManager.isLoggedIn(player.getUniqueId())) {
            sendMessage(player, ChatColor.RED + "You must be logged in to reset your password.");
            return true;
        }

        if (args.length != 2) {
            sendMessage(player, ChatColor.YELLOW + "Usage: /resetpassword <currentPassword> <newPassword>");
            return true;
        }

        String currentPassword = args[0];
        String newPassword = args[1];

        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            sendMessage(player, ChatColor.RED + "New password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            return true;
        }

        try {
            String currentHash = hashPassword(currentPassword);
            if (!playerManager.getPlayerData(player.getUniqueId()).getPasswordHash().equals(currentHash)) {
                sendMessage(player, ChatColor.RED + "Current password is incorrect.");
                return true;
            }

            String newHash = hashPassword(newPassword);
            playerManager.getPlayerData(player.getUniqueId()).setPasswordHash(newHash);
            playerManager.savePlayerData();
            sendMessage(player, ChatColor.GREEN + "Password successfully reset!");
        } catch (NoSuchAlgorithmException e) {
            sendMessage(player, ChatColor.RED + "Failed to reset password. Please try again.");
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
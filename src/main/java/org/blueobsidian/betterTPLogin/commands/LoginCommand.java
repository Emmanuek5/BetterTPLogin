package org.blueobsidian.betterTPLogin.commands;

import org.blueobsidian.betterTPLogin.managers.PlayerManager;
import org.blueobsidian.betterTPLogin.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class LoginCommand implements CommandExecutor {
    private static final String PREFIX = ChatColor.BLUE + "[BetterTPLogin] " + ChatColor.RESET;
    private static final int MAX_ATTEMPTS = 3;
    private final PlayerManager playerManager;

    public LoginCommand(PlayerManager playerManager) {
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
            sendMessage(player, ChatColor.YELLOW + "Usage: /login <password>");
            return true;
        }

        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null) {
            sendMessage(player, ChatColor.RED + "Please register first using /register <password>");
            return true;
        }

        try {
            String hashedPassword = hashPassword(args[0]);
            if (!data.getPasswordHash().equals(hashedPassword)) {
                handleFailedLogin(player);
                return true;
            }

            handleSuccessfulLogin(player, data.getLastKnownLocation());
        } catch (Exception e) {
            sendMessage(player, ChatColor.RED + "Login failed. Please try again.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void handleSuccessfulLogin(Player player, Location lastLocation) {
        sendMessage(player, ChatColor.GREEN + "Login successful!");
        playerManager.setLoggedIn(player.getUniqueId(), true);
        if (lastLocation != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(lastLocation);
                }
            }.runTaskLater(playerManager.getPlugin(), 1L);
        }
    }

    private void handleFailedLogin(Player player) {
        // TODO: Implement attempt counter and timeout
        sendMessage(player, ChatColor.RED + "Incorrect password. Please try again.");
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
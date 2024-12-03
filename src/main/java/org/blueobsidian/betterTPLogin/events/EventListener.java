package org.blueobsidian.betterTPLogin.events;

import org.blueobsidian.betterTPLogin.BetterTPLogin;
import org.blueobsidian.betterTPLogin.managers.PlayerManager;
import org.blueobsidian.betterTPLogin.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class EventListener implements Listener {
    private static final String PREFIX = ChatColor.BLUE + "[BetterTPLogin] " + ChatColor.RESET;
    private static final int SPAWN_RADIUS = 4;
    private static final int LOGIN_TIMEOUT = 60; // in seconds
    private static final int REMINDER_INTERVAL = 5; // in seconds
    private final Random random = new Random();
    private final BetterTPLogin plugin;
    private final PlayerManager playerManager;
    private final Map<UUID, BukkitRunnable> loginTasks = new HashMap<>();

    public EventListener(BetterTPLogin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (playerManager.getPlayerData(playerId) == null) {
            handleNewPlayer(player);
        } else {
            handleReturnPlayer(player);
        }

        // Start login timer and reminders
        startLoginTimer(player);
    }

    private void startLoginTimer(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable loginTask = new BukkitRunnable() {
            int timeLeft = LOGIN_TIMEOUT;

            @Override
            public void run() {
                if (playerManager.isLoggedIn(playerId)) {
                    clearScreenMessage(player);
                    cancel();
                    loginTasks.remove(playerId);
                    return;
                }

                if (timeLeft <= 0) {
                    player.kickPlayer(ChatColor.RED + "You were kicked for not logging in or registering within 60 seconds.");
                    clearScreenMessage(player);
                    cancel();
                    loginTasks.remove(playerId);
                    return;
                }

                // Display login/register messages
                boolean needsRegistration = playerManager.getPlayerData(playerId) == null;
                displayLoginScreen(player, needsRegistration, timeLeft);

                timeLeft--;
            }
        };

        loginTasks.put(playerId, loginTask);
        loginTask.runTaskTimer(plugin, 0, 20); // 20 ticks = 1 second
    }


    private void displayLoginScreen(Player player, boolean needsRegistration, int countdown) {
        String title = needsRegistration
                ? ChatColor.GOLD + "Please Register"
                : ChatColor.GOLD + "Please Login";

        String subtitle = needsRegistration
                ? ChatColor.YELLOW + "Use /register <password>"
                : ChatColor.YELLOW + "Use /login <password>";

        String actionBarMessage = ChatColor.RED + "Time left: " + countdown + " seconds";

        // Display title and subtitle
        player.sendTitle(title, subtitle, 10, 20, 10);

        // Display countdown on the action bar
        player.spigot().sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarMessage));
    }

    private void clearScreenMessage(Player player) {
        // Clear any lingering messages by sending an empty title and action bar
        player.sendTitle("", "", 0, 0, 0);
        player.spigot().sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(""));
    }


    private void handleNewPlayer(Player player) {
        Location spawnLoc = getRandomSpawnLocation();
        player.teleport(spawnLoc);
        displayLoginScreen(player, true, LOGIN_TIMEOUT);
        playerManager.setLoggedIn(player.getUniqueId(), false);
    }

    private void handleReturnPlayer(Player player) {
        Location spawnLoc = getRandomSpawnLocation();
        player.teleport(spawnLoc);
        displayLoginScreen(player, false, LOGIN_TIMEOUT);
        playerManager.setLoggedIn(player.getUniqueId(), false);
    }
    private void handleAutoLogin(Player player, PlayerData data) {
        player.teleport(data.getLastKnownLocation());
        playerManager.setLoggedIn(player.getUniqueId(), true);
        sendMessage(player, ChatColor.GREEN + "You've been automatically logged in!");
    }



    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();

        if (playerManager.isLoggedIn(playerId)) {
            playerManager.setLoggedIn(playerId, false);
            playerManager.updateLastLocation(playerId, player.getLocation());
            playerManager.updateIpAddress(playerId, player.getAddress().getAddress().getHostAddress());
        }
    }

    // Rest of the code remains the same...
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!playerManager.isLoggedIn(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
                event.setCancelled(true);
                sendMessage(event.getPlayer(), ChatColor.RED + "You must log in to move.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!playerManager.isLoggedIn(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            sendMessage(event.getPlayer(), ChatColor.RED + "You must log in first.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!playerManager.isLoggedIn(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            sendMessage(event.getPlayer(), ChatColor.RED + "You must log in first.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!playerManager.isLoggedIn(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            sendMessage(event.getPlayer(), ChatColor.RED + "You must log in first.");
        }
    }

    private Location getRandomSpawnLocation() {
        Location spawn = plugin.getConfigManager().getSpawnLocation();
        double angle = random.nextDouble() * 2 * Math.PI;
        double x = spawn.getX() + SPAWN_RADIUS * Math.cos(angle);
        double z = spawn.getZ() + SPAWN_RADIUS * Math.sin(angle);
        return new Location(spawn.getWorld(), x, spawn.getY(), z, spawn.getYaw(), spawn.getPitch());
    }

    private void displayAuthMessage(Player player, boolean needsRegistration) {
        String[] messages;
        if (needsRegistration) {
            messages = new String[]{
                    "",
                    ChatColor.GOLD + "★═══════════════════════════════════════════════★",
                    ChatColor.YELLOW + "           Welcome to the server!",
                    ChatColor.YELLOW + "     Please register using: " + ChatColor.WHITE + "/register <password>",
                    ChatColor.GOLD + "★═══════════════════════════════════════════════★",
                    ""
            };
        } else {
            messages = new String[]{
                    "",
                    ChatColor.GOLD + "★═══════════════════════════════════════════════★",
                    ChatColor.YELLOW + "           Welcome back!",
                    ChatColor.YELLOW + "     Please login using: " + ChatColor.WHITE + "/login <password>",
                    ChatColor.GOLD + "★═══════════════════════════════════════════════★",
                    ""
            };
        }
        for (String message : messages) {
            player.sendMessage(message);
        }
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(PREFIX + message);
    }

    public Location getLastKnownLocation(UUID playerId) {
        return playerManager.getPlayerData(playerId).getLastKnownLocation();
    }
}
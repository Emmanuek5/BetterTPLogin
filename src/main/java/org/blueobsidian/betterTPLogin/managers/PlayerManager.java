package org.blueobsidian.betterTPLogin.managers;

import org.blueobsidian.betterTPLogin.BetterTPLogin;
import org.blueobsidian.betterTPLogin.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerManager {
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final File dataFile;
    private final YamlConfiguration dataConfig;
    private final Set<UUID> loggedInPlayers = new HashSet<>();

    public PlayerManager(File pluginFolder) {
        this.dataFile = new File(pluginFolder, "playerdata.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadPlayerData();
    }

    public void addPlayer(UUID uuid, Location location, String passwordHash, String ipAddress) {
        playerDataMap.put(uuid, new PlayerData(location, passwordHash, ipAddress));
        savePlayerData();
    }

    public boolean isValidIpAddress(UUID uuid, String ipAddress) {
        PlayerData data = getPlayerData(uuid);
        return data != null && data.getLastIpAddress().equals(ipAddress);
    }

    public void updateIpAddress(UUID uuid, String ipAddress) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.setLastIpAddress(ipAddress);
            savePlayerData();
        }
    }

    public void savePlayerData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();
            String base = uuid.toString() + ".";
            dataConfig.set(base + "location.world", data.getLastKnownLocation().getWorld().getName());
            dataConfig.set(base + "location.x", data.getLastKnownLocation().getX());
            dataConfig.set(base + "location.y", data.getLastKnownLocation().getY());
            dataConfig.set(base + "location.z", data.getLastKnownLocation().getZ());
            dataConfig.set(base + "location.yaw", data.getLastKnownLocation().getYaw());
            dataConfig.set(base + "location.pitch", data.getLastKnownLocation().getPitch());
            dataConfig.set(base + "passwordHash", data.getPasswordHash());
            dataConfig.set(base + "ipAddress", data.getLastIpAddress());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }

    private void loadPlayerData() {
        for (String key : dataConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String base = uuid + ".";
            String world = dataConfig.getString(base + "location.world");
            Location location = new Location(
                    Bukkit.getWorld(world),
                    dataConfig.getDouble(base + "location.x"),
                    dataConfig.getDouble(base + "location.y"),
                    dataConfig.getDouble(base + "location.z"),
                    (float) dataConfig.getDouble(base + "location.yaw"),
                    (float) dataConfig.getDouble(base + "location.pitch")
            );
            String passwordHash = dataConfig.getString(base + "passwordHash");
            String ipAddress = dataConfig.getString(base + "ipAddress", "");
            playerDataMap.put(uuid, new PlayerData(location, passwordHash, ipAddress));
        }
    }

    // Existing methods remain the same
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public boolean isLoggedIn(UUID uuid) {
        return loggedInPlayers.contains(uuid);
    }

    public void setLoggedIn(UUID uuid, boolean loggedIn) {
        if (loggedIn) {
            loggedInPlayers.add(uuid);
        } else {
            loggedInPlayers.remove(uuid);
        }
    }

    public @NotNull Plugin getPlugin() {
        return BetterTPLogin.getPlugin();
    }

    public boolean isRegistered(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    public void updateLastLocation(UUID playerId, @NotNull Location location) {
        PlayerData playerData = playerDataMap.get(playerId);
        if (playerData != null) {
            playerData.setLastKnownLocation(location);
        }
    }
}
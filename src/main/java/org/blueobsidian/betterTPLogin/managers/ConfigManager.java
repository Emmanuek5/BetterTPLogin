package org.blueobsidian.betterTPLogin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final File configFile;
    private final FileConfiguration config;

    public ConfigManager(File pluginFolder) {
        this.configFile = new File(pluginFolder, "config.yml");
        if (!configFile.exists()) {
            pluginFolder.mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Could not create config.yml!");
            }
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
    }

    private void setDefaults() {
        if (!config.contains("spawn-location")) {
            config.set("spawn-location.world", "world");
            config.set("spawn-location.x", 0.0);
            config.set("spawn-location.y", 64.0);
            config.set("spawn-location.z", 0.0);
            config.set("spawn-location.yaw", 0.0);
            config.set("spawn-location.pitch", 0.0);
            saveConfig();
        }
    }

    public Location getSpawnLocation() {
        String worldName = config.getString("spawn-location.world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().severe("World " + worldName + " not found! Using default spawn location.");
            return new Location(Bukkit.getWorlds().get(0), 0.0, 64.0, 0.0);
        }
        double x = config.getDouble("spawn-location.x");
        double y = config.getDouble("spawn-location.y");
        double z = config.getDouble("spawn-location.z");
        float yaw = (float) config.getDouble("spawn-location.yaw");
        float pitch = (float) config.getDouble("spawn-location.pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setSpawnLocation(Location location) {
        config.set("spawn-location.world", location.getWorld().getName());
        config.set("spawn-location.x", location.getX());
        config.set("spawn-location.y", location.getY());
        config.set("spawn-location.z", location.getZ());
        config.set("spawn-location.yaw", location.getYaw());
        config.set("spawn-location.pitch", location.getPitch());
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save config.yml!");
        }
    }
}

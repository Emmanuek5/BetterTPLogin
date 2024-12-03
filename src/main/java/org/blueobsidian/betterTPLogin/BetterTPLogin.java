package org.blueobsidian.betterTPLogin;

import org.blueobsidian.betterTPLogin.commands.*;
import org.blueobsidian.betterTPLogin.events.EventListener;
import org.blueobsidian.betterTPLogin.managers.*;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;

public class BetterTPLogin extends JavaPlugin {
    private PlayerManager playerManager;
    private ConfigManager configManager;
    private static final String PREFIX = ChatColor.BLUE + "[BetterTPLogin] " + ChatColor.RESET;

    public static @NotNull Plugin getPlugin() {
        return BetterTPLogin.getPlugin(BetterTPLogin.class);
    }

    @Override
    public void onEnable() {
        initializePlugin();
        registerManagers();
        registerEvents();
        registerCommands();
        getLogger().info(PREFIX + "Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (playerManager != null) {
            playerManager.savePlayerData();
        }
        getLogger().info(PREFIX + "Plugin disabled successfully!");
    }

    private void initializePlugin() {
        saveDefaultConfig();
        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists() && !pluginFolder.mkdirs()) {
            getLogger().log(Level.SEVERE, "Failed to create plugin directory!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerManagers() {
        playerManager = new PlayerManager(getDataFolder());
        configManager = new ConfigManager(getDataFolder());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    private void registerCommands() {
        CommandRegistration[] commands = {
                new CommandRegistration("setspawn", new SetSpawnCommand(configManager)),
                new CommandRegistration("login", new LoginCommand(playerManager)),
                new CommandRegistration("register", new RegisterCommand(playerManager)),
                new CommandRegistration("reset-password", new ResetPasswordCommand(playerManager))
        };

        for (CommandRegistration cmd : commands) {
            PluginCommand command = getCommand(cmd.name);
            if (command != null) {
                command.setExecutor(cmd.executor);
            } else {
                getLogger().warning(PREFIX + "Failed to register command: " + cmd.name);
            }
        }
    }

    private static class CommandRegistration {
        final String name;
        final org.bukkit.command.CommandExecutor executor;

        CommandRegistration(String name, org.bukkit.command.CommandExecutor executor) {
            this.name = name;
            this.executor = executor;
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
package org.blueobsidian.betterTPLogin.models;

import org.bukkit.Location;

import java.io.Serializable;

public class PlayerData implements Serializable {
    private Location lastKnownLocation;
    private String passwordHash;
    private String lastIpAddress;

    public PlayerData(Location lastKnownLocation, String passwordHash) {
        this.lastKnownLocation = lastKnownLocation;
        this.passwordHash = passwordHash;
    }

    public PlayerData(Location lastKnownLocation, String passwordHash, String ipAddress) {
        this.lastKnownLocation = lastKnownLocation;
        this.passwordHash = passwordHash;
        this.lastIpAddress = ipAddress;
    }

    public String getLastIpAddress() {
        return lastIpAddress;
    }
public void setLastIpAddress(String lastIpAddress) {
    this.lastIpAddress = lastIpAddress;
}
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(Location lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}

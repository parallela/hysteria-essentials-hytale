package com.nhulston.essentials.models;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
public class PlayerData {
    private Map<String, Home> homes;
    private Map<String, Long> kitCooldowns;   
    private Long lastRepairTime;
    private Long lastRtpTime;
    public PlayerData() {
        this.homes = new HashMap<>();
        this.kitCooldowns = new HashMap<>();
    }
    private void ensureInitialized() {
        if (homes == null) {
            homes = new HashMap<>();
        }
        if (kitCooldowns == null) {
            kitCooldowns = new HashMap<>();
        }
    }
    public Map<String, Home> getHomes() {
        ensureInitialized();
        return homes;
    }
    public Home getHome(String name) {
        ensureInitialized();
        return homes.get(name.toLowerCase());
    }
    public void setHome(String name, Home home) {
        ensureInitialized();
        homes.put(name.toLowerCase(), home);
    }
    public void deleteHome(String name) {
        ensureInitialized();
        homes.remove(name.toLowerCase());
    }
    public int getHomeCount() {
        ensureInitialized();
        return homes.size();
    }
    @Nullable
    public Long getKitCooldown(@Nonnull String kitId) {
        ensureInitialized();
        return kitCooldowns.get(kitId.toLowerCase());
    }
    public void setKitCooldown(@Nonnull String kitId, long timestamp) {
        ensureInitialized();
        kitCooldowns.put(kitId.toLowerCase(), timestamp);
    }
    @Nullable
    public Long getLastRepairTime() {
        return lastRepairTime;
    }
    public void setLastRepairTime(long timestamp) {
        this.lastRepairTime = timestamp;
    }
    @Nullable
    public Long getLastRtpTime() {
        return lastRtpTime;
    }
    public void setLastRtpTime(long timestamp) {
        this.lastRtpTime = timestamp;
    }
}

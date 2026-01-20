package com.nhulston.essentials.managers;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.nhulston.essentials.models.Spawn;
import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.StorageManager;
import javax.annotation.Nonnull;
import java.util.UUID;
public class SpawnProtectionManager {
    private static final String BYPASS_PERMISSION = "essentials.spawn.bypass";
    private final ConfigManager configManager;
    private final StorageManager storageManager;
    public SpawnProtectionManager(@Nonnull ConfigManager configManager, @Nonnull StorageManager storageManager) {
        this.configManager = configManager;
        this.storageManager = storageManager;
    }
    public boolean isEnabled() {
        return configManager.isSpawnProtectionEnabled();
    }
    public boolean isInvulnerableEnabled() {
        return configManager.isSpawnProtectionInvulnerable();
    }
    public boolean isInProtectedArea(@Nonnull Vector3i blockPos) {
        Spawn spawn = storageManager.getSpawn();
        if (spawn == null) {
            return false;
        }
        int radius = configManager.getSpawnProtectionRadius();
        double dx = Math.abs(blockPos.getX() - spawn.getX());
        double dz = Math.abs(blockPos.getZ() - spawn.getZ());
        if (dx > radius || dz > radius) {
            return false;
        }
        return isInYRange(blockPos.getY());
    }
    public boolean isInProtectedArea(@Nonnull Vector3d entityPos) {
        Spawn spawn = storageManager.getSpawn();
        if (spawn == null) {
            return false;
        }
        int radius = configManager.getSpawnProtectionRadius();
        double dx = Math.abs(entityPos.getX() - spawn.getX());
        double dz = Math.abs(entityPos.getZ() - spawn.getZ());
        if (dx > radius || dz > radius) {
            return false;
        }
        return isInYRange((int) entityPos.getY());
    }
    private boolean isInYRange(int y) {
        int minY = configManager.getSpawnProtectionMinY();
        int maxY = configManager.getSpawnProtectionMaxY();
        if (minY == -1 && maxY == -1) {
            return true;
        }
        if (minY != -1 && y < minY) {
            return false;
        }
        if (maxY != -1 && y > maxY) {
            return false;
        }
        return true;
    }
    public boolean canBypass(@Nonnull UUID playerUuid) {
        return PermissionsModule.get().hasPermission(playerUuid, BYPASS_PERMISSION);
    }
}

package com.nhulston.essentials.managers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nhulston.essentials.models.PersonalBenchProtection;
import com.nhulston.essentials.models.PersonalBenchProtection.ProtectionFlag;
import com.nhulston.essentials.util.Log;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
public class PersonalBenchManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path benchesFile;
    private final Map<String, PersonalBenchProtection> protectedBenches = new ConcurrentHashMap<>();
    private final Map<UUID, PendingProtection> pendingProtections = new ConcurrentHashMap<>();
    public PersonalBenchManager(@Nonnull Path dataFolder) {
        this.benchesFile = dataFolder.resolve("personal_benches.json");
        loadBenches();
    }
    public static boolean isBench(@Nonnull String blockId) {
        String lower = blockId.toLowerCase();
        if (lower.contains("bench") && !lower.contains("furniture")) {
            return true;
        }
        return lower.contains("arcanist") ||
               lower.contains("workbench") ||
               lower.contains("smithing") ||
               lower.contains("crafting");
    }
    public void recordBenchPlacement(@Nonnull UUID playerUuid, @Nonnull String playerName,
                                      @Nonnull String worldName, int x, int y, int z,
                                      @Nonnull String benchType) {
        PendingProtection pending = new PendingProtection(playerUuid, playerName, worldName, x, y, z, benchType);
        pendingProtections.put(playerUuid, pending);
    }
    @Nullable
    public PendingProtection getPendingProtection(@Nonnull UUID playerUuid) {
        return pendingProtections.get(playerUuid);
    }
    @Nonnull
    public PersonalBenchProtection protectBench(@Nonnull PendingProtection pending,
                                                @Nonnull Set<ProtectionFlag> flags) {
        String key = getKey(pending.worldName, pending.x, pending.y, pending.z);
        PersonalBenchProtection protection = new PersonalBenchProtection(
                pending.worldName, pending.x, pending.y, pending.z,
                pending.playerUuid, pending.playerName, pending.benchType,
                System.currentTimeMillis(), flags
        );
        protectedBenches.put(key, protection);
        pendingProtections.remove(pending.playerUuid);
        saveBenches();
        return protection;
    }
    @Nullable
    public PersonalBenchProtection getProtection(@Nonnull String worldName, int x, int y, int z) {
        return protectedBenches.get(getKey(worldName, x, y, z));
    }
    public boolean canUse(@Nonnull UUID playerUuid, @Nonnull String worldName, int x, int y, int z) {
        PersonalBenchProtection protection = getProtection(worldName, x, y, z);
        if (protection == null) {
            return true;  
        }
        if (protection.getOwnerUuid().equals(playerUuid)) {
            return true;
        }
        return !protection.hasFlag(ProtectionFlag.USE);
    }
    public boolean canDestroy(@Nonnull UUID playerUuid, @Nonnull String worldName, int x, int y, int z) {
        PersonalBenchProtection protection = getProtection(worldName, x, y, z);
        if (protection == null) {
            return true;  
        }
        if (protection.getOwnerUuid().equals(playerUuid)) {
            return true;
        }
        return !protection.hasFlag(ProtectionFlag.DESTROY);
    }
    public boolean removeProtection(@Nonnull String worldName, int x, int y, int z) {
        String key = getKey(worldName, x, y, z);
        boolean removed = protectedBenches.remove(key) != null;
        if (removed) {
            saveBenches();
        }
        return removed;
    }
    @Nonnull
    public List<PersonalBenchProtection> getPlayerBenches(@Nonnull UUID playerUuid) {
        return protectedBenches.values().stream()
                .filter(p -> p.getOwnerUuid().equals(playerUuid))
                .collect(Collectors.toList());
    }
    public boolean updateFlags(@Nonnull UUID playerUuid, @Nonnull String worldName,
                               int x, int y, int z, @Nonnull Set<ProtectionFlag> flags) {
        PersonalBenchProtection protection = getProtection(worldName, x, y, z);
        if (protection == null || !protection.getOwnerUuid().equals(playerUuid)) {
            return false;
        }
        protectedBenches.remove(getKey(worldName, x, y, z));
        PersonalBenchProtection updated = new PersonalBenchProtection(
                worldName, x, y, z,
                protection.getOwnerUuid(), protection.getOwnerName(),
                protection.getBenchType(), protection.getPlacedTime(),
                flags
        );
        protectedBenches.put(getKey(worldName, x, y, z), updated);
        saveBenches();
        return true;
    }
    private String getKey(@Nonnull String worldName, int x, int y, int z) {
        return worldName + ":" + x + ":" + y + ":" + z;
    }
    private void loadBenches() {
        if (!Files.exists(benchesFile)) {
            return;
        }
        try {
            String json = Files.readString(benchesFile);
            List<PersonalBenchProtection> benches = GSON.fromJson(json,
                    new TypeToken<List<PersonalBenchProtection>>(){}.getType());
            if (benches != null) {
                protectedBenches.clear();
                for (PersonalBenchProtection bench : benches) {
                    String key = getKey(bench.getWorldName(), bench.getX(), bench.getY(), bench.getZ());
                    protectedBenches.put(key, bench);
                }
                Log.info("Loaded " + benches.size() + " protected benches.");
            }
        } catch (IOException e) {
            Log.error("Failed to load protected benches: " + e.getMessage());
        }
    }
    private void saveBenches() {
        try {
            List<PersonalBenchProtection> benches = new ArrayList<>(protectedBenches.values());
            String json = GSON.toJson(benches);
            Files.writeString(benchesFile, json);
        } catch (IOException e) {
            Log.error("Failed to save protected benches: " + e.getMessage());
        }
    }
    public static class PendingProtection {
        public final UUID playerUuid;
        public final String playerName;
        public final String worldName;
        public final int x;
        public final int y;
        public final int z;
        public final String benchType;
        public final long timestamp;
        public PendingProtection(@Nonnull UUID playerUuid, @Nonnull String playerName,
                                 @Nonnull String worldName, int x, int y, int z,
                                 @Nonnull String benchType) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.benchType = benchType;
            this.timestamp = System.currentTimeMillis();
        }
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 300000;
        }
    }
}

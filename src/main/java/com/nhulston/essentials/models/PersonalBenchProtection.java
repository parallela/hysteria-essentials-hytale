package com.nhulston.essentials.models;
import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
public class PersonalBenchProtection {
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private final UUID ownerUuid;
    private final String ownerName;
    private final String benchType;
    private final long placedTime;
    private final Set<ProtectionFlag> flags;
    public PersonalBenchProtection(@Nonnull String worldName, int x, int y, int z,
                                    @Nonnull UUID ownerUuid, @Nonnull String ownerName,
                                    @Nonnull String benchType, long placedTime,
                                    @Nonnull Set<ProtectionFlag> flags) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.benchType = benchType;
        this.placedTime = placedTime;
        this.flags = EnumSet.copyOf(flags);
    }
    @Nonnull
    public String getWorldName() {
        return worldName;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getZ() {
        return z;
    }
    @Nonnull
    public UUID getOwnerUuid() {
        return ownerUuid;
    }
    @Nonnull
    public String getOwnerName() {
        return ownerName;
    }
    @Nonnull
    public String getBenchType() {
        return benchType;
    }
    public long getPlacedTime() {
        return placedTime;
    }
    @Nonnull
    public Set<ProtectionFlag> getFlags() {
        return EnumSet.copyOf(flags);
    }
    public boolean hasFlag(@Nonnull ProtectionFlag flag) {
        return flags.contains(flag);
    }
    public void addFlag(@Nonnull ProtectionFlag flag) {
        flags.add(flag);
    }
    public void removeFlag(@Nonnull ProtectionFlag flag) {
        flags.remove(flag);
    }
    public enum ProtectionFlag {
        USE,      
        DESTROY   
    }
}

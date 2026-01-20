package com.nhulston.essentials.util;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.models.Spawn;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
public final class TeleportUtil {
    private static final int MAX_SAFE_SEARCH = 128;
    private static final int PLAYER_HEIGHT = 2;
    private static final float YAW_NORTH = 0f;
    private static final float YAW_EAST = (float) Math.toRadians(-90);    
    private static final float YAW_SOUTH = (float) Math.PI;               
    private static final float YAW_WEST = (float) Math.toRadians(90);    
    private TeleportUtil() {}
    public static float roundToCardinalYaw(float yawRadians) {
        float yawDegrees = (float) Math.toDegrees(yawRadians);
        yawDegrees = yawDegrees % 360;
        if (yawDegrees > 180) yawDegrees -= 360;
        if (yawDegrees < -180) yawDegrees += 360;
        if (yawDegrees >= -45 && yawDegrees < 45) {
            return YAW_NORTH;  
        } else if (yawDegrees >= 45 && yawDegrees < 135) {
            return YAW_WEST;  
        } else if (yawDegrees >= 135 || yawDegrees < -135) {
            return YAW_SOUTH;  
        } else {
            return YAW_EAST;  
        }
    }
    private static Vector3f cardinalRotation(@Nonnull Vector3f currentRotation) {
        float cardinalYaw = roundToCardinalYaw(currentRotation.y);
        return new Vector3f(0, cardinalYaw, 0);
    }
    @Nullable
    public static String teleport(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                  @Nonnull String worldName, double x, double y, double z,
                                  float yaw, float pitch) {
        World targetWorld = Universe.get().getWorld(worldName);
        if (targetWorld == null) {
            return "World '" + worldName + "' is not loaded.";
        }
        Vector3d position = new Vector3d(x, y, z);
        Vector3f rotation = new Vector3f(0, roundToCardinalYaw(yaw), 0);
        Teleport teleport = new Teleport(targetWorld, position, rotation);
        store.putComponent(ref, Teleport.getComponentType(), teleport);
        return null;
    }
    @Nullable
    public static String teleportSafe(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                      @Nonnull String worldName, double x, double y, double z,
                                      float yaw, float pitch) {
        World targetWorld = Universe.get().getWorld(worldName);
        if (targetWorld == null) {
            return "World '" + worldName + "' is not loaded.";
        }
        double safeY = findSafeY(targetWorld, x, y, z);
        Vector3d position = new Vector3d(x, safeY, z);
        Vector3f rotation = new Vector3f(0, roundToCardinalYaw(yaw), 0);
        Teleport teleport = new Teleport(targetWorld, position, rotation);
        store.putComponent(ref, Teleport.getComponentType(), teleport);
        return null;
    }
    public static void teleportToPlayer(@Nonnull PlayerRef player, @Nonnull PlayerRef target) {
        Ref<EntityStore> playerRef = player.getReference();
        Ref<EntityStore> targetRef = target.getReference();
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }
        if (targetRef == null || !targetRef.isValid()) {
            return;
        }
        Store<EntityStore> playerStore = playerRef.getStore();
        Store<EntityStore> targetStore = targetRef.getStore();
        TransformComponent targetTransform = targetStore.getComponent(targetRef, TransformComponent.getComponentType());
        if (targetTransform == null) {
            return;
        }
        Vector3d targetPos = targetTransform.getPosition();
        EntityStore targetEntityStore = targetStore.getExternalData();
        World targetWorld = targetEntityStore.getWorld();
        Vector3f rotation = cardinalRotation(targetTransform.getRotation());
        Teleport teleport = new Teleport(targetWorld, targetPos, rotation);
        playerStore.putComponent(playerRef, Teleport.getComponentType(), teleport);
    }
    public static void teleportToSpawn(@Nonnull PlayerRef player, @Nonnull Spawn spawn) {
        Ref<EntityStore> playerRef = player.getReference();
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }
        World targetWorld = Universe.get().getWorld(spawn.getWorld());
        if (targetWorld == null) {
            return;
        }
        double safeY = findSafeY(targetWorld, spawn.getX(), spawn.getY(), spawn.getZ());
        Store<EntityStore> store = playerRef.getStore();
        Vector3d position = new Vector3d(spawn.getX(), safeY, spawn.getZ());
        Vector3f rotation = new Vector3f(0, roundToCardinalYaw(spawn.getYaw()), 0);
        Teleport teleport = new Teleport(targetWorld, position, rotation);
        store.putComponent(playerRef, Teleport.getComponentType(), teleport);
    }
    public static void teleportToSpawnBuffered(@Nonnull Ref<EntityStore> ref,
                                               @Nonnull CommandBuffer<EntityStore> buffer,
                                               @Nonnull Spawn spawn) {
        World targetWorld = Universe.get().getWorld(spawn.getWorld());
        if (targetWorld == null) {
            return;
        }
        Vector3d position = new Vector3d(spawn.getX(), spawn.getY(), spawn.getZ());
        Vector3f rotation = new Vector3f(0, roundToCardinalYaw(spawn.getYaw()), 0);
        Teleport teleport = new Teleport(targetWorld, position, rotation);
        buffer.putComponent(ref, Teleport.getComponentType(), teleport);
    }
    @Nullable
    public static String teleportToPlayerByUuid(@Nonnull Store<EntityStore> store,
                                                @Nonnull Ref<EntityStore> playerRef,
                                                @Nonnull java.util.UUID targetUuid) {
        PlayerRef target = Universe.get().getPlayer(targetUuid);
        if (target == null) {
            return "Target player is no longer online.";
        }
        Ref<EntityStore> targetRef = target.getReference();
        if (targetRef == null || !targetRef.isValid()) {
            return "Target player is not available.";
        }
        Store<EntityStore> targetStore = targetRef.getStore();
        TransformComponent targetTransform = targetStore.getComponent(targetRef, TransformComponent.getComponentType());
        if (targetTransform == null) {
            return "Could not get target position.";
        }
        Vector3d targetPos = targetTransform.getPosition();
        EntityStore targetEntityStore = targetStore.getExternalData();
        World targetWorld = targetEntityStore.getWorld();
        Vector3f rotation = cardinalRotation(targetTransform.getRotation());
        Teleport teleport = new Teleport(targetWorld, targetPos, rotation);
        store.putComponent(playerRef, Teleport.getComponentType(), teleport);
        return null;
    }
    private static double findSafeY(@Nonnull World world, double x, double y, double z) {
        int blockX = (int) Math.floor(x);
        int blockY = (int) Math.floor(y);
        int blockZ = (int) Math.floor(z);
        long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);
        WorldChunk chunk = world.getChunk(chunkIndex);
        if (chunk == null) {
            return y;
        }
        for (int offsetY = 0; offsetY < MAX_SAFE_SEARCH; offsetY++) {
            int checkY = blockY + offsetY;
            if (hasSpaceForPlayer(chunk, blockX, checkY, blockZ)) {
                return checkY;
            }
        }
        return y;
    }
    private static boolean hasSpaceForPlayer(@Nonnull WorldChunk chunk, int x, int y, int z) {
        for (int i = 0; i < PLAYER_HEIGHT; i++) {
            if (isSolidBlock(chunk, x, y + i, z)) {
                return false;
            }
        }
        return true;
    }
    private static boolean isSolidBlock(@Nonnull WorldChunk chunk, int x, int y, int z) {
        BlockType blockType = chunk.getBlockType(x, y, z);
        if (blockType == null) {
            return false;  
        }
        BlockMaterial material = blockType.getMaterial();
        return material == BlockMaterial.Solid;
    }
    @SuppressWarnings("removal")
    private static boolean hasFluid(@Nonnull WorldChunk chunk, int x, int y, int z) {
        return chunk.getFluidId(x, y, z) > 0;
    }
    @Nullable
    public static Double findSafeRtpY(@Nonnull World world, double x, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);
        WorldChunk chunk = world.getChunk(chunkIndex);
        if (chunk == null) {
            return null;  
        }
        int startY = 200;
        int minY = 0;
        for (int checkY = startY; checkY >= minY; checkY--) {
            if (hasFluid(chunk, blockX, checkY, blockZ)) {
                return null;  
            }
            if (isSolidBlock(chunk, blockX, checkY, blockZ)) {
                int spawnY = checkY + 1;
                if (hasFluid(chunk, blockX, spawnY, blockZ) || 
                    hasFluid(chunk, blockX, spawnY + 1, blockZ)) {
                    return null;  
                }
                if (isSolidBlock(chunk, blockX, spawnY + 1, blockZ)) {
                    continue;
                }
                return (double) spawnY;
            }
        }
        return null;  
    }
    @Nonnull
    public static CompletableFuture<Double> findSafeRtpYAsync(@Nonnull World world, double x, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);
        return world.getChunkAsync(chunkIndex).thenApply(chunk -> {
            if (chunk == null) {
                return null;  
            }
            return findSafeRtpYFromChunk(chunk, blockX, blockZ);
        });
    }
    @Nullable
    private static Double findSafeRtpYFromChunk(@Nonnull WorldChunk chunk, int blockX, int blockZ) {
        int startY = 200;
        int minY = 0;
        for (int checkY = startY; checkY >= minY; checkY--) {
            if (hasFluid(chunk, blockX, checkY, blockZ)) {
                return null;  
            }
            if (isSolidBlock(chunk, blockX, checkY, blockZ)) {
                int spawnY = checkY + 1;
                if (hasFluid(chunk, blockX, spawnY, blockZ) || 
                    hasFluid(chunk, blockX, spawnY + 1, blockZ)) {
                    return null;  
                }
                if (isSolidBlock(chunk, blockX, spawnY + 1, blockZ)) {
                    continue;
                }
                return (double) spawnY;
            }
        }
        return null;  
    }
}

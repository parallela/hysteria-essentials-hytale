package com.nhulston.essentials.events;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.TeleportManager;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nonnull;
public class TeleportMovementEvent {
    private final TeleportManager teleportManager;
    public TeleportMovementEvent(@Nonnull TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }
    public void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem(new TeleportMovementCheckSystem(teleportManager));
    }
    private static class TeleportMovementCheckSystem extends EntityTickingSystem<EntityStore> {
        private final TeleportManager teleportManager;
        TeleportMovementCheckSystem(@Nonnull TeleportManager teleportManager) {
            this.teleportManager = teleportManager;
        }
        @Override
        public Query<EntityStore> getQuery() {
            return Query.any();
        }
        @Override
        public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk,
                         @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> buffer) {
            PlayerRef playerRef = chunk.getComponent(index, PlayerRef.getComponentType());
            if (playerRef == null) {
                return;
            }
            if (!teleportManager.hasPendingTeleport(playerRef.getUuid())) {
                return;
            }
            Ref<EntityStore> currentRef = chunk.getReferenceTo(index);
            Vector3d currentPosition = playerRef.getTransform().getPosition();
            teleportManager.tick(playerRef.getUuid(), currentRef, currentPosition, deltaTime, buffer);
        }
    }
}

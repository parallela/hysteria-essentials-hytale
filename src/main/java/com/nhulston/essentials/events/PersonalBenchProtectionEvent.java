package com.nhulston.essentials.events;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.PersonalBenchManager;
import com.nhulston.essentials.models.PersonalBenchProtection;
import com.nhulston.essentials.util.Log;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
public class PersonalBenchProtectionEvent {
    private final PersonalBenchManager benchManager;
    public PersonalBenchProtectionEvent(@Nonnull PersonalBenchManager benchManager) {
        this.benchManager = benchManager;
    }
    public void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem(new BenchPlacementSystem(benchManager));
        registry.registerSystem(new BenchBreakSystem(benchManager));
        registry.registerSystem(new BenchUseSystem(benchManager));
        Log.info("Personal bench protection registered.");
    }
    private static class BenchPlacementSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        private final PersonalBenchManager benchManager;
        public BenchPlacementSystem(@Nonnull PersonalBenchManager benchManager) {
            super(PlaceBlockEvent.class);
            this.benchManager = benchManager;
        }
        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                          @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                          @Nonnull PlaceBlockEvent event) {
            if (event.isCancelled()) {
                return;
            }
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            Player player = store.getComponent(ref, Player.getComponentType());
            if (playerRef == null || player == null) {
                return;
            }
            ItemStack itemInHand = event.getItemInHand();
            if (itemInHand == null || itemInHand.isEmpty()) {
                return;
            }
            String itemId = itemInHand.getItemId();
            if (!PersonalBenchManager.isBench(itemId)) {
                return;
            }
            Vector3i pos = event.getTargetBlock();
            World world = player.getWorld();
            if (world == null) {
                return;
            }
            benchManager.recordBenchPlacement(
                    playerRef.getUuid(),
                    playerRef.getUsername(),
                    world.getName(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    itemId
            );
            playerRef.sendMessage(Message.join(
                    Message.raw("To protect your bench from others, use ").color("#FFAA00"),
                    Message.raw("/personal protect").color("#55FF55")
            ));
        }
        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
        @Nonnull
        @Override
        public Set<Dependency<EntityStore>> getDependencies() {
            return Collections.singleton(RootDependency.first());
        }
    }
    private static class BenchBreakSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {
        private final PersonalBenchManager benchManager;
        public BenchBreakSystem(@Nonnull PersonalBenchManager benchManager) {
            super(DamageBlockEvent.class);
            this.benchManager = benchManager;
        }
        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                          @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                          @Nonnull DamageBlockEvent event) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            Player player = store.getComponent(ref, Player.getComponentType());
            if (playerRef == null || player == null) {
                return;
            }
            Vector3i pos = event.getTargetBlock();
            World world = player.getWorld();
            if (world == null) {
                return;
            }
            PersonalBenchProtection protection = benchManager.getProtection(
                    world.getName(), pos.getX(), pos.getY(), pos.getZ()
            );
            if (protection != null && !benchManager.canDestroy(playerRef.getUuid(),
                    world.getName(), pos.getX(), pos.getY(), pos.getZ())) {
                event.setCancelled(true);
                playerRef.sendMessage(Message.join(
                        Message.raw("This bench is protected by ").color("#FF5555"),
                        Message.raw(protection.getOwnerName()).color("#FFAA00")
                ));
            } else if (protection != null && protection.getOwnerUuid().equals(playerRef.getUuid())) {
                benchManager.removeProtection(world.getName(), pos.getX(), pos.getY(), pos.getZ());
            }
        }
        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
        @Nonnull
        @Override
        public Set<Dependency<EntityStore>> getDependencies() {
            return Collections.singleton(RootDependency.first());
        }
    }
    private static class BenchUseSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
        private final PersonalBenchManager benchManager;
        public BenchUseSystem(@Nonnull PersonalBenchManager benchManager) {
            super(UseBlockEvent.Pre.class);
            this.benchManager = benchManager;
        }
        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                          @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                          @Nonnull UseBlockEvent.Pre event) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            Player player = store.getComponent(ref, Player.getComponentType());
            if (playerRef == null || player == null) {
                return;
            }
            String blockId = event.getBlockType().getId();
            if (!PersonalBenchManager.isBench(blockId)) {
                return;
            }
            Vector3i pos = event.getTargetBlock();
            World world = player.getWorld();
            if (world == null) {
                return;
            }
            PersonalBenchProtection protection = benchManager.getProtection(
                    world.getName(), pos.getX(), pos.getY(), pos.getZ()
            );
            if (protection != null && !benchManager.canUse(playerRef.getUuid(),
                    world.getName(), pos.getX(), pos.getY(), pos.getZ())) {
                event.setCancelled(true);
                playerRef.sendMessage(Message.join(
                        Message.raw("This bench is protected by ").color("#FF5555"),
                        Message.raw(protection.getOwnerName()).color("#FFAA00")
                ));
            }
        }
        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
        @Nonnull
        @Override
        public Set<Dependency<EntityStore>> getDependencies() {
            return Collections.singleton(RootDependency.first());
        }
    }
}

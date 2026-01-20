package com.nhulston.essentials.events;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.SpawnProtectionManager;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nonnull;
public class SpawnProtectionEvent {
    private static final String PROTECTED_MESSAGE = "This area is protected.";
    private static final String PROTECTED_COLOR = "#FF5555";
    private static final String PVP_MESSAGE = "PvP is disabled in spawn.";
    private final SpawnProtectionManager spawnProtectionManager;
    public SpawnProtectionEvent(@Nonnull SpawnProtectionManager spawnProtectionManager) {
        this.spawnProtectionManager = spawnProtectionManager;
    }
    private static void sendProtectedMessage(PlayerRef playerRef) {
        if (playerRef != null) {
            playerRef.sendMessage(Message.raw(PROTECTED_MESSAGE).color(PROTECTED_COLOR));
        }
    }
    private static void sendPvpMessage(PlayerRef playerRef) {
        if (playerRef != null) {
            playerRef.sendMessage(Message.raw(PVP_MESSAGE).color(PROTECTED_COLOR));
        }
    }
    public void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem(new BreakBlockProtectionSystem(spawnProtectionManager));
        registry.registerSystem(new PlaceBlockProtectionSystem(spawnProtectionManager));
        registry.registerSystem(new DamageBlockProtectionSystem(spawnProtectionManager));
        registry.registerSystem(new SpawnDamageFilterSystem(spawnProtectionManager));
    }
    private static class BreakBlockProtectionSystem 
            extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        private final SpawnProtectionManager manager;
        BreakBlockProtectionSystem(SpawnProtectionManager manager) {
            super(BreakBlockEvent.class);
            this.manager = manager;
        }
        @Override
        public Query<EntityStore> getQuery() {
            return Query.any();
        }
        @Override
        public void handle(int index, @NotNull ArchetypeChunk<EntityStore> chunk,
                           @NotNull Store<EntityStore> store,
                           @NotNull CommandBuffer<EntityStore> buffer,
                           BreakBlockEvent event) {
            if (!manager.isEnabled() || event.isCancelled()) {
                return;
            }
            if (!manager.isInProtectedArea(event.getTargetBlock())) {
                return;
            }
            PlayerRef playerRef = chunk.getComponent(index, PlayerRef.getComponentType());
            if (playerRef != null && manager.canBypass(playerRef.getUuid())) {
                return;
            }
            event.setCancelled(true);
            sendProtectedMessage(playerRef);
        }
    }
    private static class PlaceBlockProtectionSystem 
            extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        private final SpawnProtectionManager manager;
        PlaceBlockProtectionSystem(SpawnProtectionManager manager) {
            super(PlaceBlockEvent.class);
            this.manager = manager;
        }
        @Override
        public Query<EntityStore> getQuery() {
            return Query.any();
        }
        @Override
        public void handle(int index, @NotNull ArchetypeChunk<EntityStore> chunk,
                           @NotNull Store<EntityStore> store,
                           @NotNull CommandBuffer<EntityStore> buffer,
                           PlaceBlockEvent event) {
            if (!manager.isEnabled() || event.isCancelled()) {
                return;
            }
            if (!manager.isInProtectedArea(event.getTargetBlock())) {
                return;
            }
            PlayerRef playerRef = chunk.getComponent(index, PlayerRef.getComponentType());
            if (playerRef != null && manager.canBypass(playerRef.getUuid())) {
                return;
            }
            event.setCancelled(true);
            sendProtectedMessage(playerRef);
        }
    }
    private static class DamageBlockProtectionSystem 
            extends EntityEventSystem<EntityStore, DamageBlockEvent> {
        private final SpawnProtectionManager manager;
        DamageBlockProtectionSystem(SpawnProtectionManager manager) {
            super(DamageBlockEvent.class);
            this.manager = manager;
        }
        @Override
        public Query<EntityStore> getQuery() {
            return Query.any();
        }
        @Override
        public void handle(int index, @NotNull ArchetypeChunk<EntityStore> chunk,
                           @NotNull Store<EntityStore> store,
                           @NotNull CommandBuffer<EntityStore> buffer,
                           DamageBlockEvent event) {
            if (!manager.isEnabled() || event.isCancelled()) {
                return;
            }
            if (!manager.isInProtectedArea(event.getTargetBlock())) {
                return;
            }
            PlayerRef playerRef = chunk.getComponent(index, PlayerRef.getComponentType());
            if (playerRef != null && manager.canBypass(playerRef.getUuid())) {
                return;
            }
            event.setCancelled(true);
        }
    }
    private static class SpawnDamageFilterSystem extends DamageEventSystem {
        private final SpawnProtectionManager manager;
        SpawnDamageFilterSystem(SpawnProtectionManager manager) {
            super();
            this.manager = manager;
        }
        @Override
        public SystemGroup<EntityStore> getGroup() {
            return DamageModule.get().getFilterDamageGroup();
        }
        @Override
        public Query<EntityStore> getQuery() {
            return Query.any();
        }
        @Override
        public void handle(int index, @NotNull ArchetypeChunk<EntityStore> chunk,
                           @NotNull Store<EntityStore> store,
                           @NotNull CommandBuffer<EntityStore> buffer,
                           Damage event) {
            if (!manager.isEnabled() || !manager.isInvulnerableEnabled() || event.isCancelled()) {
                return;
            }
            PlayerRef victimRef = chunk.getComponent(index, PlayerRef.getComponentType());
            if (victimRef == null) {
                return;
            }
            if (!manager.isInProtectedArea(victimRef.getTransform().getPosition())) {
                return;
            }
            Damage.Source source = event.getSource();
            if (!(source instanceof Damage.EntitySource entitySource)) {
                return;
            }
            Ref<EntityStore> attackerRef = entitySource.getRef();
            if (!attackerRef.isValid()) {
                return;
            }
            PlayerRef attackerPlayerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
            if (attackerPlayerRef == null) {
                return;  
            }
            event.setCancelled(true);
            event.setAmount(0);
            sendPvpMessage(attackerPlayerRef);
        }
    }
}

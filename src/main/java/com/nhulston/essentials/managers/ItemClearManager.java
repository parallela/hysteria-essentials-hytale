package com.nhulston.essentials.managers;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.Log;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages automatic clearing of dropped items from worlds at configurable intervals.
 * Uses DespawnComponent to identify dropped items that should be cleared.
 * Items without DespawnComponent (admin-spawned via entity tool) are preserved.
 */
public class ItemClearManager {
    private final ConfigManager configManager;
    private final Set<World> trackedWorlds = ConcurrentHashMap.newKeySet();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong secondsRemaining = new AtomicLong(0);

    public ItemClearManager(@Nonnull ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Starts the item clear scheduler.
     */
    public void start() {
        if (!configManager.isItemClearEnabled()) {
            Log.info("Item clear system is disabled in config.");
            return;
        }

        if (running.compareAndSet(false, true)) {
            long interval = configManager.getItemClearInterval();
            secondsRemaining.set(interval);

            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ItemClearScheduler");
                t.setDaemon(true);
                return t;
            });

            scheduledFuture = scheduler.scheduleAtFixedRate(this::tick, 1L, 1L, TimeUnit.SECONDS);

            Log.info("Item clear system started with " + formatDuration(interval) + " interval.");
        }
    }

    /**
     * Stops the item clear scheduler.
     */
    public void shutdown() {
        running.set(false);

        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }

        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Adds a world to be tracked for item clearing.
     */
    public void trackWorld(@Nonnull World world) {
        if (world != null && world.isAlive()) {
            trackedWorlds.add(world);
        }
    }

    /**
     * Removes dead worlds from tracking.
     */
    private void cleanupWorlds() {
        trackedWorlds.removeIf(w -> w == null || !w.isAlive());
    }

    /**
     * Tick method called every second.
     */
    private void tick() {
        if (!running.get() || !configManager.isItemClearEnabled()) {
            return;
        }

        long remaining = secondsRemaining.decrementAndGet();

        if (remaining <= 0) {
            // Time to clear items
            broadcast(ColorUtil.colorize(configManager.getItemClearClearingMessage()));
            clearAllItems();
            secondsRemaining.set(configManager.getItemClearInterval());
        } else {
            // Check if we should send a warning
            for (String warningTime : configManager.getItemClearWarnings()) {
                long warningSeconds = parseDuration(warningTime);
                if (remaining == warningSeconds) {
                    String message = configManager.getItemClearWarningMessage()
                            .replace("{time}", warningTime);
                    broadcast(ColorUtil.colorize(message));
                    break;
                }
            }
        }
    }

    /**
     * Clears all dropped items from all tracked worlds.
     */
    private void clearAllItems() {
        cleanupWorlds();

        for (World world : trackedWorlds) {
            if (world != null && world.isAlive()) {
                world.execute(() -> {
                    int cleared = clearItemsInWorld(world);
                    if (cleared > 0) {
                        String message = configManager.getItemClearClearedMessage()
                                .replace("{count}", String.valueOf(cleared));
                        world.sendMessage(ColorUtil.colorize(message));
                    }
                });
            }
        }
    }

    /**
     * Clears dropped items in a specific world.
     * Only clears items that have DespawnComponent (naturally dropped/timed items).
     * Items without DespawnComponent (admin-spawned via entity tool) are preserved.
     * @return Number of items cleared
     */
    private int clearItemsInWorld(@Nonnull World world) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        ComponentType<EntityStore, ItemComponent> itemType = ItemComponent.getComponentType();
        ComponentType<EntityStore, DespawnComponent> despawnType = DespawnComponent.getComponentType();

        // Query for items that have DespawnComponent (naturally dropped items)
        // This excludes admin-spawned items from entity tool which don't have DespawnComponent
        Query<EntityStore> query = Query.and(itemType, despawnType);

        int[] count = {0};
        store.forEachChunk(query, (chunk, buffer) -> {
            int size = chunk.size();
            for (int i = 0; i < size; i++) {
                Ref<EntityStore> ref = chunk.getReferenceTo(i);
                buffer.tryRemoveEntity(ref, RemoveReason.REMOVE);
                count[0]++;
            }
        });

        return count[0];
    }

    /**
     * Broadcasts a message to all tracked worlds.
     */
    private void broadcast(@Nonnull Message message) {
        cleanupWorlds();
        Universe.get().sendMessage(message);
    }

    /**
     * Parses a duration string (e.g., "5min", "30sec") into seconds.
     */
    private long parseDuration(@Nonnull String duration) {
        String input = duration.toLowerCase().trim();

        if (input.endsWith("d")) {
            return Long.parseLong(input.substring(0, input.length() - 1)) * 86400L;
        } else if (input.endsWith("h")) {
            return Long.parseLong(input.substring(0, input.length() - 1)) * 3600L;
        } else if (input.endsWith("min")) {
            return Long.parseLong(input.substring(0, input.length() - 3)) * 60L;
        } else if (input.endsWith("m")) {
            return Long.parseLong(input.substring(0, input.length() - 1)) * 60L;
        } else if (input.endsWith("sec")) {
            return Long.parseLong(input.substring(0, input.length() - 3));
        } else if (input.endsWith("s")) {
            return Long.parseLong(input.substring(0, input.length() - 1));
        }

        // Fallback: try to parse as plain seconds
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return 300L; // Default to 5 minutes
        }
    }

    /**
     * Formats seconds into a readable duration string.
     */
    @Nonnull
    private String formatDuration(long totalSeconds) {
        if (totalSeconds % 86400L == 0L) {
            return (totalSeconds / 86400L) + "d";
        } else if (totalSeconds % 3600L == 0L) {
            return (totalSeconds / 3600L) + "h";
        } else if (totalSeconds % 60L == 0L) {
            return (totalSeconds / 60L) + "min";
        } else {
            return totalSeconds + "sec";
        }
    }
}


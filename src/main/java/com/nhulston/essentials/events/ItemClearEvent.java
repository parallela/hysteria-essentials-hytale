package com.nhulston.essentials.events;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.nhulston.essentials.managers.ItemClearManager;
import com.nhulston.essentials.util.Log;

import javax.annotation.Nonnull;

/**
 * Tracks worlds that have players so the ItemClearManager knows which worlds to clear.
 */
public class ItemClearEvent {
    private final ItemClearManager itemClearManager;

    public ItemClearEvent(@Nonnull ItemClearManager itemClearManager) {
        this.itemClearManager = itemClearManager;
    }

    public void register(@Nonnull EventRegistry eventRegistry) {
        // Track world when player connects
        eventRegistry.registerGlobal(PlayerConnectEvent.class, event -> {
            World world = event.getWorld();
            if (world != null) {
                itemClearManager.trackWorld(world);
            }
        });

        // Track world when player is added to a world
        eventRegistry.registerGlobal(AddPlayerToWorldEvent.class, event -> {
            World world = event.getWorld();
            if (world != null) {
                itemClearManager.trackWorld(world);
            }
        });

        Log.info("Item clear world tracking enabled.");
    }
}


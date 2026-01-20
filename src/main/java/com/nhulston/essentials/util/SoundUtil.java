package com.nhulston.essentials.util;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;
public final class SoundUtil {
    private SoundUtil() {}
    public static void playSound(@Nonnull PlayerRef playerRef, @Nonnull String soundName) {
        int soundIndex = SoundEvent.getAssetMap().getIndex(soundName);
        if (soundIndex == 0) {
            Log.warning("Sound not found: " + soundName);
            return;
        }
        com.hypixel.hytale.server.core.universe.world.SoundUtil.playSoundEvent2dToPlayer(
                playerRef, soundIndex, SoundCategory.SFX);
    }
}

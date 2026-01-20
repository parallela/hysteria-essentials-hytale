package com.nhulston.essentials.util;
import javax.annotation.Nonnull;
public final class CooldownUtil {
    private CooldownUtil() {}
    @Nonnull
    public static String formatCooldown(long seconds) {
        if (seconds <= 0) {
            return "Ready";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}

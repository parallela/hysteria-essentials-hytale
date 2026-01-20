package com.nhulston.essentials.managers;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.nhulston.essentials.util.Log;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
public class TpaManager {
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, TpaRequest>> pendingRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long EXPIRATION_SECONDS = 20;
    public boolean createRequest(@Nonnull PlayerRef requester, @Nonnull PlayerRef target) {
        UUID targetUuid = target.getUuid();
        UUID requesterUuid = requester.getUuid();
        ConcurrentHashMap<UUID, TpaRequest> targetRequests = pendingRequests.computeIfAbsent(
            targetUuid, _ -> new ConcurrentHashMap<>()
        );
        if (targetRequests.containsKey(requesterUuid)) {
            return false;
        }
        TpaRequest request = new TpaRequest(requesterUuid, requester.getUsername(), target.getUsername());
        targetRequests.put(requesterUuid, request);
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            expireRequest(targetUuid, requesterUuid);
        }, EXPIRATION_SECONDS, TimeUnit.SECONDS);
        request.setExpirationFuture(future);
        Log.info("TPA request created: " + requester.getUsername() + " -> " + target.getUsername());
        return true;
    }
    @Nullable
    public TpaRequest acceptRequest(@Nonnull PlayerRef target, @Nonnull String requesterName) {
        UUID targetUuid = target.getUuid();
        ConcurrentHashMap<UUID, TpaRequest> targetRequests = pendingRequests.get(targetUuid);
        if (targetRequests == null || targetRequests.isEmpty()) {
            return null;
        }
        TpaRequest foundRequest = null;
        UUID foundRequesterUuid = null;
        for (Map.Entry<UUID, TpaRequest> entry : targetRequests.entrySet()) {
            if (entry.getValue().getRequesterName().equalsIgnoreCase(requesterName)) {
                foundRequest = entry.getValue();
                foundRequesterUuid = entry.getKey();
                break;
            }
        }
        if (foundRequest == null) {
            return null;
        }
        targetRequests.remove(foundRequesterUuid);
        foundRequest.cancel();
        if (targetRequests.isEmpty()) {
            pendingRequests.remove(targetUuid);
        }
        Log.info("TPA request accepted: " + foundRequest.getRequesterName() + " -> " + target.getUsername());
        return foundRequest;
    }
    private void expireRequest(UUID targetUuid, UUID requesterUuid) {
        ConcurrentHashMap<UUID, TpaRequest> targetRequests = pendingRequests.get(targetUuid);
        if (targetRequests == null) {
            return;
        }
        TpaRequest request = targetRequests.remove(requesterUuid);
        if (request != null) {
            if (targetRequests.isEmpty()) {
                pendingRequests.remove(targetUuid);
            }
            PlayerRef requester = Universe.get().getPlayer(requesterUuid);
            if (requester != null) {
                Msg.fail(requester, "Your teleport request to " + request.getTargetName() + " has expired.");
            }
        }
    }
    public void onPlayerQuit(@Nonnull UUID playerUuid) {
        ConcurrentHashMap<UUID, TpaRequest> targetRequests = pendingRequests.remove(playerUuid);
        if (targetRequests != null) {
            for (TpaRequest request : targetRequests.values()) {
                request.cancel();
            }
        }
        for (ConcurrentHashMap<UUID, TpaRequest> requests : pendingRequests.values()) {
            TpaRequest request = requests.remove(playerUuid);
            if (request != null) {
                request.cancel();
            }
        }
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    public void shutdown() {
        scheduler.shutdownNow();
        pendingRequests.clear();
    }
    public static class TpaRequest {
        private final UUID requesterUuid;
        private final String requesterName;
        private final String targetName;
        private ScheduledFuture<?> expirationFuture;
        public TpaRequest(UUID requesterUuid, String requesterName, String targetName) {
            this.requesterUuid = requesterUuid;
            this.requesterName = requesterName;
            this.targetName = targetName;
        }
        public UUID getRequesterUuid() {
            return requesterUuid;
        }
        public String getRequesterName() {
            return requesterName;
        }
        public String getTargetName() {
            return targetName;
        }
        void setExpirationFuture(ScheduledFuture<?> future) {
            this.expirationFuture = future;
        }
        void cancel() {
            if (expirationFuture != null) {
                expirationFuture.cancel(false);
            }
        }
    }
}

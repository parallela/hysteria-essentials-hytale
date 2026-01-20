package com.nhulston.essentials.util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
public class VersionChecker {
    private static final String CFWIDGET_API_URL = "https://api.cfwidget.com/hytale/mods/essentials-core";
    private static final int TIMEOUT_MS = 10000;
    private final String currentVersion;
    private volatile String latestVersion = null;
    private volatile boolean updateAvailable = false;
    public VersionChecker(@Nonnull String currentVersion) {
        this.currentVersion = currentVersion;
    }
    public void checkForUpdatesAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                String latest = fetchLatestVersion();
                if (latest != null) {
                    latestVersion = latest;
                    updateAvailable = isNewerVersion(latest, currentVersion);
                    if (updateAvailable) {
                        Log.info("New version available: " + latest + " (current: " + currentVersion + ")");
                    } else {
                        Log.info("Plugin is up to date (v" + currentVersion + ")");
                    }
                }
            } catch (Exception e) {
                Log.warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }
    @Nullable
    private String fetchLatestVersion() {
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(CFWIDGET_API_URL);
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "Essentials-Plugin-VersionChecker/1.0");
            connection.setRequestProperty("Accept", "application/json");
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Log.warning("CFWidget API returned status " + responseCode);
                return null;
            }
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            return parseVersionFromJson(response.toString());
        } catch (Exception e) {
            Log.warning("Error fetching version: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    @Nullable
    private String parseVersionFromJson(@Nonnull String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("download")) {
                JsonObject download = root.getAsJsonObject("download");
                String name = download.get("name").getAsString();
                return extractVersion(name);
            }
            if (root.has("files")) {
                JsonArray files = root.getAsJsonArray("files");
                if (!files.isEmpty()) {
                    JsonObject firstFile = files.get(0).getAsJsonObject();
                    String name = firstFile.get("name").getAsString();
                    return extractVersion(name);
                }
            }
            Log.warning("Could not find version in CFWidget response");
            return null;
        } catch (Exception e) {
            Log.warning("Error parsing CFWidget JSON: " + e.getMessage());
            return null;
        }
    }
    @Nullable
    private String extractVersion(@Nonnull String filename) {
        if (filename.startsWith("Essentials-") && filename.endsWith(".jar")) {
            return filename.substring(11, filename.length() - 4);
        }
        return null;
    }
    private boolean isNewerVersion(@Nonnull String version1, @Nonnull String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int v1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int v2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            if (v1 > v2) return true;
            if (v1 < v2) return false;
        }
        return false;
    }
    private int parseVersionPart(@Nonnull String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
    @Nullable
    public String getLatestVersion() {
        return latestVersion;
    }
    @Nonnull
    public String getCurrentVersion() {
        return currentVersion;
    }
}

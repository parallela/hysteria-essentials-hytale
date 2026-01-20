package com.nhulston.essentials.util;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ConfigManager {
    private static final String DEFAULT_CHAT_FORMAT = "&7%player%&f: %message%";
    private static final int DEFAULT_SPAWN_PROTECTION_RADIUS = 16;
    private static final int DEFAULT_TELEPORT_DELAY = 3;
    private static final int DEFAULT_RTP_COOLDOWN = 300;
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\[([a-zA-Z0-9_.-]+)]\\s*$");
    private final Path configPath;
    private final HashMap<String, Integer> homeLimits = new HashMap<>();
    private boolean chatEnabled = true;
    private String chatFallbackFormat = DEFAULT_CHAT_FORMAT;
    private final LinkedHashMap<String, String> chatFormats = new LinkedHashMap<>();
    private boolean disableBuilding = false;
    private boolean firstJoinSpawnEnabled = true;
    private boolean everyJoinSpawnEnabled = false;
    private boolean deathSpawnEnabled = true;
    private boolean welcomeBroadcastEnabled = true;
    private String welcomeBroadcastMessage = "&e%player% &6has joined the server for the first time!";
    private int teleportDelay = DEFAULT_TELEPORT_DELAY;
    private boolean spawnProtectionEnabled = true;
    private int spawnProtectionRadius = DEFAULT_SPAWN_PROTECTION_RADIUS;
    private int spawnProtectionMinY = -1;
    private int spawnProtectionMaxY = -1;
    private boolean spawnProtectionInvulnerable = true;
    private boolean spawnProtectionShowTitles = true;
    private String spawnProtectionEnterTitle = "Entering Spawn";
    private String spawnProtectionEnterSubtitle = "This is a protected area";
    private String spawnProtectionExitTitle = "Leaving Spawn";
    private String spawnProtectionExitSubtitle = "You can now build";
    private int rtpCooldown = DEFAULT_RTP_COOLDOWN;
    private String rtpDefaultWorld = "default";
    private final HashMap<String, Integer> rtpWorlds = new HashMap<>();
    private boolean motdEnabled = true;
    private String motdMessage = "&6Welcome to the server, &e%player%&6!";
    private boolean sleepEnabled = true;
    private int sleepPercentage = 20;
    private String shoutPrefix = "&0[&7Broadcast&0] &f";
    private int repairCooldown = 43200;
    private boolean joinLeaveMessagesEnabled = true;
    private String joinMessage = "&e%player% &ajoined the server";
    private String leaveMessage = "&e%player% &cleft the server";

    // Anti-spam settings
    private boolean antiSpamEnabled = true;
    private int antiSpamDelay = 1000; // 1 second in milliseconds
    private boolean antiSpamBlockDuplicates = true;

    public ConfigManager(@Nonnull Path dataFolder) {
        this.configPath = dataFolder.resolve("config.toml");
        load();
    }
    private void load() {
        if (!Files.exists(configPath)) {
            createDefault();
        } else {
            migrateConfig();
        }
        try {
            byte[] bytes = Files.readAllBytes(configPath);
            String configContent;
            if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                configContent = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
            } else {
                configContent = new String(bytes, StandardCharsets.UTF_8);
            }
            TomlParseResult config = Toml.parse(configContent);
            if (config.hasErrors()) {
                config.errors().forEach(error -> Log.error("Config error: " + error.toString()));
                Log.warning("Using default config values due to errors.");
                return;
            }
            homeLimits.clear();
            TomlTable homeLimitsTable = config.getTable("homes.limits");
            if (homeLimitsTable != null) {
                for (String tier : homeLimitsTable.keySet()) {
                    Long limit = homeLimitsTable.getLong(tier);
                    if (limit != null) {
                        homeLimits.put(tier.toLowerCase(), limit.intValue());
                    }
                }
            }
            chatEnabled = config.getBoolean("chat.enabled", () -> true);
            chatFallbackFormat = config.getString("chat.fallback-format", () -> DEFAULT_CHAT_FORMAT);
            chatFormats.clear();
            TomlTable formatsTable = config.getTable("chat.formats");
            if (formatsTable != null) {
                for (String group : formatsTable.keySet()) {
                    String format = formatsTable.getString(group);
                    if (format != null) {
                        chatFormats.put(group.toLowerCase(), format);
                    }
                }
            }
            disableBuilding = config.getBoolean("build.disable-building", () -> false);
            firstJoinSpawnEnabled = config.getBoolean("spawn.first-join", () -> true);
            everyJoinSpawnEnabled = config.getBoolean("spawn.every-join", () -> false);
            deathSpawnEnabled = config.getBoolean("spawn.death-spawn", () -> true);
            welcomeBroadcastEnabled = config.getBoolean("welcome-broadcast.enabled", () -> true);
            welcomeBroadcastMessage = config.getString("welcome-broadcast.message", 
                    () -> "&e%player% &6has joined the server for the first time!");
            teleportDelay = getIntSafe(config, "teleport.delay", DEFAULT_TELEPORT_DELAY);
            spawnProtectionEnabled = config.getBoolean("spawn-protection.enabled", () -> true);
            spawnProtectionRadius = getIntSafe(config, "spawn-protection.radius", DEFAULT_SPAWN_PROTECTION_RADIUS);
            spawnProtectionMinY = getIntSafe(config, "spawn-protection.min-y", -1);
            spawnProtectionMaxY = getIntSafe(config, "spawn-protection.max-y", -1);
            spawnProtectionInvulnerable = config.getBoolean("spawn-protection.invulnerable", () -> true);
            spawnProtectionShowTitles = config.getBoolean("spawn-protection.show-titles", () -> true);
            spawnProtectionEnterTitle = config.getString("spawn-protection.enter-title", () -> "Entering Spawn");
            spawnProtectionEnterSubtitle = config.getString("spawn-protection.enter-subtitle", () -> "This is a protected area");
            spawnProtectionExitTitle = config.getString("spawn-protection.exit-title", () -> "Leaving Spawn");
            spawnProtectionExitSubtitle = config.getString("spawn-protection.exit-subtitle", () -> "You can now build");
            rtpCooldown = getIntSafe(config, "rtp.cooldown", DEFAULT_RTP_COOLDOWN);
            rtpWorlds.clear();
            TomlTable rtpWorldsTable = config.getTable("rtp.worlds");
            if (rtpWorldsTable != null) {
                for (String worldName : rtpWorldsTable.keySet()) {
                    Long radius = rtpWorldsTable.getLong(worldName);
                    if (radius != null) {
                        rtpWorlds.put(worldName, radius.intValue());
                    }
                }
            }
            String defaultWorld = config.getString("rtp.default-world");
            rtpDefaultWorld = defaultWorld != null ? defaultWorld : "default";
            motdEnabled = config.getBoolean("motd.enabled", () -> true);
            motdMessage = config.getString("motd.message", () -> "&6Welcome to the server, &e%player%&6!");
            sleepEnabled = config.getBoolean("sleep.enabled", () -> true);
            sleepPercentage = getIntSafe(config, "sleep.percentage", 20);
            shoutPrefix = config.getString("shout.prefix", () -> "&0[&7Broadcast&0] &f");
            repairCooldown = getIntSafe(config, "repair.cooldown", 43200);
            joinLeaveMessagesEnabled = config.getBoolean("join-leave-messages.enabled", () -> true);
            joinMessage = config.getString("join-leave-messages.join-message", () -> "&e%player% &ajoined the server");
            leaveMessage = config.getString("join-leave-messages.leave-message", () -> "&e%player% &cleft the server");

            // Anti-spam settings
            antiSpamEnabled = config.getBoolean("anti-spam.enabled", () -> true);
            antiSpamDelay = getIntSafe(config, "anti-spam.delay", 1000);
            antiSpamBlockDuplicates = config.getBoolean("anti-spam.block-duplicates", () -> true);

            Log.info("Config loaded!");
        } catch (Exception e) {
            Log.error("Failed to load config: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if (e.getCause() != null) {
                Log.error("Caused by: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            Log.warning("Using default config values.");
        }
    }
    public void reload() {
        Log.info("Reloading config...");
        load();
    }
    private void migrateConfig() {
        String defaultConfig = loadDefaultConfigFromResources();
        if (defaultConfig == null) {
            Log.warning("Could not load default config from resources for migration.");
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(configPath);
            String userConfig;
            if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                userConfig = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
            } else {
                userConfig = new String(bytes, StandardCharsets.UTF_8);
            }
            Set<String> userSections = findTopLevelSections(userConfig);
            Map<String, String> defaultSections = extractSections(defaultConfig);
            List<String> missingSections = new ArrayList<>();
            for (String section : defaultSections.keySet()) {
                if (!userSections.contains(section)) {
                    missingSections.add(section);
                }
            }
            if (missingSections.isEmpty()) {
                return;
            }
            StringBuilder newConfig = new StringBuilder(userConfig);
            if (!userConfig.endsWith("\n")) {
                newConfig.append("\n");
            }
            for (String section : missingSections) {
                newConfig.append("\n");
                newConfig.append(defaultSections.get(section));
                Log.info("Added missing config section: [" + section + "]");
            }
            Files.writeString(configPath, newConfig.toString(), StandardCharsets.UTF_8);
            Log.info("Config migrated with " + missingSections.size() + " new section(s).");
        } catch (Exception e) {
            Log.warning("Config migration skipped: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }
    @Nullable
    private String loadDefaultConfigFromResources() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.toml")) {
            if (is == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        } catch (IOException e) {
            Log.warning("Failed to load default config from resources: " + e.getMessage());
            return null;
        }
    }
    @Nonnull
    private Set<String> findTopLevelSections(@Nonnull String config) {
        LinkedHashMap<String, Boolean> map = new LinkedHashMap<>();
        for (String line : config.split("\n")) {
            Matcher matcher = SECTION_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                map.put(matcher.group(1), true);
            }
        }
        return map.keySet();
    }
    @Nonnull
    private Map<String, String> extractSections(@Nonnull String config) {
        Map<String, String> sections = new LinkedHashMap<>();
        String[] lines = config.split("\n");
        String currentSection = null;
        StringBuilder currentContent = new StringBuilder();
        List<String> pendingComments = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = SECTION_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                if (currentSection != null) {
                    sections.put(currentSection, currentContent.toString());
                }
                currentSection = matcher.group(1);
                currentContent = new StringBuilder();
                for (String comment : pendingComments) {
                    currentContent.append(comment).append("\n");
                }
                pendingComments.clear();
                currentContent.append(line).append("\n");
            } else if (currentSection != null) {
                currentContent.append(line).append("\n");
            } else {
                if (line.trim().startsWith("#") || line.trim().isEmpty()) {
                    pendingComments.add(line);
                }
            }
        }
        if (currentSection != null) {
            sections.put(currentSection, currentContent.toString());
        }
        return sections;
    }
    private int getIntSafe(@Nonnull TomlParseResult config, @Nonnull String key, int defaultValue) {
        try {
            Long value = config.getLong(key);
            return value != null ? Math.toIntExact(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    private void createDefault() {
        try {
            Files.createDirectories(configPath.getParent());
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.toml")) {
                if (is != null) {
                    Files.copy(is, configPath);
                    Log.info("Created default config.");
                    return;
                }
            }
            Log.error("Could not find config.toml in resources.");
        } catch (IOException e) {
            Log.error("Failed to create default config: " + e.getMessage());
        }
    }
    @Nonnull
    public Map<String, Integer> getHomeLimits() {
        return homeLimits;
    }
    public boolean isChatEnabled() {
        return chatEnabled;
    }
    @Nonnull
    public String getChatFallbackFormat() {
        return chatFallbackFormat;
    }
    @Nonnull
    public Map<String, String> getChatFormats() {
        return chatFormats;
    }
    public boolean isBuildingDisabled() {
        return disableBuilding;
    }
    public boolean isFirstJoinSpawnEnabled() {
        return firstJoinSpawnEnabled;
    }
    public boolean isEveryJoinSpawnEnabled() {
        return everyJoinSpawnEnabled;
    }
    public boolean isDeathSpawnEnabled() {
        return deathSpawnEnabled;
    }
    public boolean isWelcomeBroadcastEnabled() {
        return welcomeBroadcastEnabled;
    }
    @Nonnull
    public String getWelcomeBroadcastMessage() {
        return welcomeBroadcastMessage;
    }
    public int getTeleportDelay() {
        return teleportDelay;
    }
    public boolean isSpawnProtectionEnabled() {
        return spawnProtectionEnabled;
    }
    public int getSpawnProtectionRadius() {
        return spawnProtectionRadius;
    }
    public boolean isSpawnProtectionInvulnerable() {
        return spawnProtectionInvulnerable;
    }
    public int getSpawnProtectionMinY() {
        return spawnProtectionMinY;
    }
    public int getSpawnProtectionMaxY() {
        return spawnProtectionMaxY;
    }
    public boolean isSpawnProtectionShowTitles() {
        return spawnProtectionShowTitles;
    }
    @Nonnull
    public String getSpawnProtectionEnterTitle() {
        return spawnProtectionEnterTitle;
    }
    @Nonnull
    public String getSpawnProtectionEnterSubtitle() {
        return spawnProtectionEnterSubtitle;
    }
    @Nonnull
    public String getSpawnProtectionExitTitle() {
        return spawnProtectionExitTitle;
    }
    @Nonnull
    public String getSpawnProtectionExitSubtitle() {
        return spawnProtectionExitSubtitle;
    }
    public int getRtpCooldown() {
        return rtpCooldown;
    }
    @Nonnull
    public String getRtpDefaultWorld() {
        return rtpDefaultWorld;
    }
    @Nullable
    public Integer getRtpRadius(@Nonnull String worldName) {
        return rtpWorlds.get(worldName);
    }
    public boolean isMotdEnabled() {
        return motdEnabled;
    }
    @Nonnull
    public String getMotdMessage() {
        return motdMessage;
    }
    public boolean isSleepEnabled() {
        return sleepEnabled;
    }
    public int getSleepPercentage() {
        return sleepPercentage;
    }
    @Nonnull
    public String getShoutPrefix() {
        return shoutPrefix;
    }
    public int getRepairCooldown() {
        return repairCooldown;
    }
    public boolean isJoinLeaveMessagesEnabled() {
        return joinLeaveMessagesEnabled;
    }
    @Nonnull
    public String getJoinMessage() {
        return joinMessage;
    }
    @Nonnull
    public String getLeaveMessage() {
        return leaveMessage;
    }

    // Anti-spam getters
    public boolean isAntiSpamEnabled() {
        return antiSpamEnabled;
    }

    public void setAntiSpamEnabled(boolean enabled) {
        this.antiSpamEnabled = enabled;
    }

    public int getAntiSpamDelay() {
        return antiSpamDelay;
    }

    public boolean isAntiSpamBlockDuplicates() {
        return antiSpamBlockDuplicates;
    }
}

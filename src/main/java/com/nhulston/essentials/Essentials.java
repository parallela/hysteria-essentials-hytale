package com.nhulston.essentials;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.nhulston.essentials.commands.alert.AlertCommand;
import com.nhulston.essentials.commands.back.BackCommand;
import com.nhulston.essentials.commands.essentials.EssentialsCommand;
import com.nhulston.essentials.commands.freecam.FreecamCommand;
import com.nhulston.essentials.commands.god.GodCommand;
import com.nhulston.essentials.commands.heal.HealCommand;
import com.nhulston.essentials.commands.home.DelHomeCommand;
import com.nhulston.essentials.commands.home.HomeCommand;
import com.nhulston.essentials.commands.home.SetHomeCommand;
import com.nhulston.essentials.commands.kit.KitCommand;
import com.nhulston.essentials.commands.list.ListCommand;
import com.nhulston.essentials.commands.msg.MsgCommand;
import com.nhulston.essentials.commands.msg.ReplyCommand;
import com.nhulston.essentials.commands.personal.PersonalProtectCommand;
import com.nhulston.essentials.commands.repair.RepairCommand;
import com.nhulston.essentials.commands.rtp.RtpCommand;
import com.nhulston.essentials.commands.shout.ShoutCommand;
import com.nhulston.essentials.commands.top.TopCommand;
import com.nhulston.essentials.commands.tphere.TphereCommand;
import com.nhulston.essentials.commands.spawn.SetSpawnCommand;
import com.nhulston.essentials.commands.spawn.SpawnCommand;
import com.nhulston.essentials.commands.tpa.TpaCommand;
import com.nhulston.essentials.commands.tpa.TpacceptCommand;
import com.nhulston.essentials.commands.warp.DelWarpCommand;
import com.nhulston.essentials.commands.warp.SetWarpCommand;
import com.nhulston.essentials.commands.warp.WarpCommand;
import com.nhulston.essentials.events.BuildProtectionEvent;
import com.nhulston.essentials.events.ChatEvent;
import com.nhulston.essentials.events.DeathLocationEvent;
import com.nhulston.essentials.events.JoinLeaveMessageEvent;
import com.nhulston.essentials.events.MotdEvent;
import com.nhulston.essentials.events.PersonalBenchProtectionEvent;
import com.nhulston.essentials.events.PlayerQuitEvent;
import com.nhulston.essentials.events.SpawnProtectionEvent;
import com.nhulston.essentials.events.SpawnRegionTitleEvent;
import com.nhulston.essentials.events.SpawnTeleportEvent;
import com.nhulston.essentials.events.TeleportMovementEvent;
import com.nhulston.essentials.events.SleepPercentageEvent;
import com.nhulston.essentials.events.UpdateNotifyEvent;
import com.nhulston.essentials.managers.BackManager;
import com.nhulston.essentials.managers.ChatManager;
import com.nhulston.essentials.managers.HomeManager;
import com.nhulston.essentials.managers.KitManager;
import com.nhulston.essentials.managers.PersonalBenchManager;
import com.nhulston.essentials.managers.SpawnManager;
import com.nhulston.essentials.managers.SpawnProtectionManager;
import com.nhulston.essentials.managers.TeleportManager;
import com.nhulston.essentials.managers.TpaManager;
import com.nhulston.essentials.managers.WarpManager;
import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.StorageManager;
import com.nhulston.essentials.util.Log;
import com.nhulston.essentials.util.VersionChecker;
import javax.annotation.Nonnull;
public class Essentials extends JavaPlugin {
    public static final String VERSION = "1.5.1";
    private static Essentials instance;
    private ConfigManager configManager;
    private StorageManager storageManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private ChatManager chatManager;
    private SpawnProtectionManager spawnProtectionManager;
    private TpaManager tpaManager;
    private TeleportManager teleportManager;
    private KitManager kitManager;
    private BackManager backManager;
    private PersonalBenchManager personalBenchManager;
    private VersionChecker versionChecker;
    public Essentials(@Nonnull JavaPluginInit init) {
        super(init);
    }
    @Override
    protected void setup() {
        instance = this;
        Log.init(getLogger());
        Log.info("Essentials is starting...");
        configManager = new ConfigManager(getDataDirectory());
        storageManager = new StorageManager(getDataDirectory());
        homeManager = new HomeManager(storageManager, configManager);
        warpManager = new WarpManager(storageManager);
        spawnManager = new SpawnManager(storageManager);
        chatManager = new ChatManager(configManager);
        spawnProtectionManager = new SpawnProtectionManager(configManager, storageManager);
        tpaManager = new TpaManager();
        teleportManager = new TeleportManager(configManager);
        kitManager = new KitManager(getDataDirectory(), storageManager);
        backManager = new BackManager();
        personalBenchManager = new PersonalBenchManager(getDataDirectory());
        versionChecker = new VersionChecker(VERSION);
    }
    @Override
    protected void start() {
        registerCommands();
        registerEvents();
        versionChecker.checkForUpdatesAsync();
        Log.info("Essentials v" + VERSION + " started successfully!");
    }
    @Override
    protected void shutdown() {
        Log.info("Essentials is shutting down...");
        if (storageManager != null) {
            storageManager.shutdown();
        }
        if (tpaManager != null) {
            tpaManager.shutdown();
        }
        if (teleportManager != null) {
            teleportManager.shutdown();
        }
        Log.info("Essentials shut down.");
    }
    private void registerCommands() {
        getCommandRegistry().registerCommand(new SetHomeCommand(homeManager));
        getCommandRegistry().registerCommand(new HomeCommand(homeManager, teleportManager));
        getCommandRegistry().registerCommand(new DelHomeCommand(homeManager));
        getCommandRegistry().registerCommand(new SetWarpCommand(warpManager));
        getCommandRegistry().registerCommand(new WarpCommand(warpManager, teleportManager));
        getCommandRegistry().registerCommand(new DelWarpCommand(warpManager));
        getCommandRegistry().registerCommand(new SetSpawnCommand(spawnManager));
        getCommandRegistry().registerCommand(new SpawnCommand(spawnManager, teleportManager));
        getCommandRegistry().registerCommand(new TpaCommand(tpaManager));
        getCommandRegistry().registerCommand(new TpacceptCommand(tpaManager, teleportManager));
        getCommandRegistry().registerCommand(new KitCommand(kitManager));
        getCommandRegistry().registerCommand(new BackCommand(backManager, teleportManager));
        getCommandRegistry().registerCommand(new RtpCommand(configManager, storageManager, teleportManager));
        getCommandRegistry().registerCommand(new ListCommand());
        getCommandRegistry().registerCommand(new HealCommand());
        getCommandRegistry().registerCommand(new FreecamCommand());
        getCommandRegistry().registerCommand(new GodCommand());
        getCommandRegistry().registerCommand(new MsgCommand());
        getCommandRegistry().registerCommand(new ReplyCommand());
        getCommandRegistry().registerCommand(new TphereCommand());
        getCommandRegistry().registerCommand(new TopCommand());
        getCommandRegistry().registerCommand(new EssentialsCommand());
        getCommandRegistry().registerCommand(new ShoutCommand(configManager));
        getCommandRegistry().registerCommand(new RepairCommand(configManager, storageManager));
        getCommandRegistry().registerCommand(new PersonalProtectCommand(personalBenchManager));
        getCommandRegistry().registerCommand(new AlertCommand());
    }
    private void registerEvents() {
        new JoinLeaveMessageEvent(configManager).register(getEventRegistry());
        new ChatEvent(chatManager).register(getEventRegistry());
        new BuildProtectionEvent(configManager).register(getEntityStoreRegistry());
        new SpawnProtectionEvent(spawnProtectionManager).register(getEntityStoreRegistry());
        new SpawnRegionTitleEvent(spawnProtectionManager, configManager).register(getEntityStoreRegistry());
        new TeleportMovementEvent(teleportManager).register(getEntityStoreRegistry());
        SpawnTeleportEvent spawnTeleportEvent = new SpawnTeleportEvent(spawnManager, configManager, storageManager);
        spawnTeleportEvent.registerEvents(getEventRegistry());
        spawnTeleportEvent.registerSystems(getEntityStoreRegistry());
        new DeathLocationEvent(backManager).register(getEntityStoreRegistry());
        new MotdEvent(configManager).register(getEventRegistry());
        new UpdateNotifyEvent(versionChecker).register(getEventRegistry());
        new SleepPercentageEvent(configManager).register(getEntityStoreRegistry());
        new PersonalBenchProtectionEvent(personalBenchManager).register(getEntityStoreRegistry());
        new PlayerQuitEvent(storageManager, tpaManager, teleportManager, backManager).register(getEventRegistry());
        getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, event -> {
            spawnManager.syncWorldSpawnProvider();
        });
    }
    @Nonnull
    public static Essentials getInstance() {
        return instance;
    }
    public void reloadConfigs() {
        configManager.reload();
        kitManager.reload();
        Log.info("All configurations reloaded.");
    }
}

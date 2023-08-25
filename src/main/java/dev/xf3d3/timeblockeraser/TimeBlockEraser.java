package dev.xf3d3.timeblockeraser;

import co.aikar.commands.PaperCommandManager;
import dev.xf3d3.timeblockeraser.commands.MainCommand;
import dev.xf3d3.timeblockeraser.config.Settings;
import dev.xf3d3.timeblockeraser.listener.BlockPlace;
import dev.xf3d3.timeblockeraser.listener.EntityPlace;
import dev.xf3d3.timeblockeraser.utils.TaskRunner;
import dev.xf3d3.timeblockeraser.utils.Utils;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.util.ThrowingConsumer;
import net.william278.desertwell.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class TimeBlockEraser extends JavaPlugin implements TaskRunner {
    private static final int METRICS_ID = 19258;
    private static TimeBlockEraser instance;
    private ConcurrentHashMap<Integer, ScheduledTask> tasks;
    private MorePaperLib paperLib;
    private UpdateChecker updateChecker;
    private PaperCommandManager manager;
    private Settings settings;

    @Override
    public void onLoad() {
        // Set the instance
        instance = this;
    }

    @Override
    public void onEnable() {
        this.tasks = new ConcurrentHashMap<>();
        this.paperLib = new MorePaperLib(this);
        this.manager = new PaperCommandManager(this);
        // this.updateChecker = new UpdateCheck(this);

        // Register commands
        initialize("commands", (plugin) -> registerCommands());

        // Register events
        initialize("events", (plugin) -> registerEvents());

        // Load settings and locales
        initialize("plugin config & locale files", (plugin) -> {
            if (!loadConfigs()) {
                throw new IllegalStateException("Failed to load config files. Please check the console for errors");
            }
        });

        // Hook into bStats
        initialize("metrics", (plugin) -> this.registerMetrics(METRICS_ID));

        // Check for updates
        //updateChecker.checkForUpdates();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        sendConsole("-------------------------------------------");
        sendConsole("&6TimeBlockEraser: &3Plugin by: &b&lxF3d3");

        // Cancel plugin tasks
        getScheduler().cancelGlobalTasks();

        // Final plugin shutdown message
        sendConsole("&6TimeBlockEraser: &3Plugin Version: &d&l" + getDescription().getVersion());
        sendConsole("&6TimeBlockEraser: &3Has been shutdown successfully");
        sendConsole("&6TimeBlockEraser: &3Goodbye!");
        sendConsole("-------------------------------------------");
    }

    private void initialize(@NotNull String name, @NotNull ThrowingConsumer<TimeBlockEraser> runner) {
        log(Level.INFO, "Initializing " + name + "...");
        try {
            runner.accept(this);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize " + name, e);
        }
        log(Level.INFO, "Successfully initialized " + name);
    }

    public void setSettings(@NotNull Settings settings) {
        this.settings = settings;
    }

    /**
     * Reloads the {@link Settings} from its config file
     *
     * @return {@code true} if the reload was successful, {@code false} otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean loadConfigs() {
        try {
            // Load settings
            setSettings(Annotaml.create(new File(getDataFolder(), "config.yml"), Settings.class).get());

            return true;
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log(Level.SEVERE, "Failed to reload UltimateTeams config or messages file", e);
        }
        return false;
    }

    @NotNull
    public Settings getSettings() {
        return settings;
    }

    private void registerCommands() {
        // Register the plugin commands
        this.manager.registerCommand(new MainCommand(this));
    }

    public void registerEvents() {
        // Register the plugin events
        this.getServer().getPluginManager().registerEvents(new BlockPlace(this), this);
        //this.getServer().getPluginManager().registerEvents(new EntityPlace(this), this);
    }

    public void registerMetrics(int metricsId) {
        try {
            final Metrics metrics = new Metrics(this, metricsId);

        } catch (Throwable e) {
            log(Level.WARNING, "Failed to register bStats metrics (" + e.getMessage() + ")");
        }
    }

    public void log(@NotNull Level level, @NotNull String message, @Nullable Throwable... throwable) {
        if (throwable != null && throwable.length > 0) {
            getLogger().log(level, message, throwable[0]);
            return;
        }
        getLogger().log(level, message);
    }

    public void sendConsole(String text) {
        Bukkit.getConsoleSender().sendMessage(Utils.Color(text));
    }

    public static TimeBlockEraser getPlugin() {
        return instance;
    }

    @Override
    @NotNull
    public GracefulScheduling getScheduler() {
        return paperLib.scheduling();
    }

    @Override
    @NotNull
    public ConcurrentHashMap<Integer, ScheduledTask> getTasks() {
        return tasks;
    }
}

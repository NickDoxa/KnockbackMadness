package net.oasisgames.km;

import net.oasisgames.km.commands.CommandController;
import net.oasisgames.km.commands.CommandTabs;
import net.oasisgames.km.controller.GameController;
import net.oasisgames.km.events.Attacks;
import net.oasisgames.km.events.Borders;
import net.oasisgames.km.events.GeneralEvents;
import net.oasisgames.km.events.JoinLeave;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.dataflow.qual.Pure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main class for the Knockback Madness Plugin
 * @author NickDoxa
 */
public final class KnockbackMadness extends JavaPlugin {

    private static FileConfiguration config;
    private static final List<ConfigReload> configFiles = new ArrayList<>();
    private static GeneralEvents generalEvents;

    /**
     * On Enable method where everything is initiated. Runs at the start of the server
     */
    @Override
    public void onEnable() {
        consoleLog("Enabled successfully!");
        saveDefaultConfig();
        loadPlugin();
    }

    /**
     * On Disable method where everything ends. Runs as the server stops
     */
    @Override
    public void onDisable() {
        consoleLog("Disabled successfully!");
    }

    private void loadPlugin() {
        config = getConfig();
        GameController controller = new GameController();
        enableListener(new JoinLeave(controller));
        enableListener(new Attacks(controller));
        enableListener(new Borders(controller));
        generalEvents = new GeneralEvents(controller);
        enableListener(generalEvents);
        Objects.requireNonNull(Bukkit.getPluginCommand("knockback"))
                .setExecutor(new CommandController(controller));
        Objects.requireNonNull(Bukkit.getPluginCommand("knockback"))
                .setTabCompleter(new CommandTabs());
    }

    /**
     * Log a message to the console
     * @param message the message to send to console
     */
    public static void consoleLog(String message) {
        Bukkit.getLogger().info("[KNOCKBACK MADNESS] " + message);
    }

    /**
     * Log a message in game
     * @param message the message to send to all players in the server
     */
    public static void gameLog(String message) {
        Bukkit.getServer().broadcastMessage(color("&7[&dKnockback Madness&7] &f" + message));
    }

    /**
     * Gets a minecraft color coded string from an original message
     * @param message the message to color code
     * @return Color coded message
     */
    @Pure
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Gets the default config file
     * @return Config File
     */
    @Pure
    public static FileConfiguration getConfigFile() {
        return config;
    }

    /**
     * Saves the edited config file
     */
    public static void saveConfigFile() {
        getPlugin(KnockbackMadness.class).saveConfig();
    }

    /**
     * Enables a listener class for use
     * @param listener the listener class to enable
     */
    private void enableListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public static void addConfigFile(ConfigReload configReload) {
        configFiles.add(configReload);
    }

    public static void reloadConfigFiles() {
        configFiles.forEach(ConfigReload::loadConfigData);
    }

    public static GeneralEvents getGeneralEvents() {
        return generalEvents;
    }

}

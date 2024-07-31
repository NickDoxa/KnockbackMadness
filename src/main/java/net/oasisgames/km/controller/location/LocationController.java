package net.oasisgames.km.controller.location;

import net.oasisgames.km.ConfigReload;
import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.GameController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * The location controller for the knockback madness minigame
 */
public class LocationController implements ConfigReload {

    private final List<Location> locations = new ArrayList<>();
    private static final Map<UUID, Location> startPoints = new HashMap<>();
    private final GameController controller;
    private Location spawnLocation;

    public LocationController(GameController controller) {
        this.controller = controller;
        loadConfigData();
        KnockbackMadness.addConfigFile(this);
    }

    @Override
    public void loadConfigData() {
        setLocations();
    }

    /**
     * Gets the locations listed in the config.yml and saves them to the
     * current list of potential spawn points
     */
    private void setLocations() {
        locations.clear();
        FileConfiguration config = KnockbackMadness.getConfigFile();
        if (config.getConfigurationSection("locations") == null) return;
        if (config.getConfigurationSection("locations.spawnpoints") == null) return;
        for (int i = 1; i <= Objects.requireNonNull(
                        config.getConfigurationSection("locations.spawnpoints"))
                .getKeys(false).size(); i++) {
            Location loc = new Location(
                    Bukkit.getWorld(UUID.fromString(Objects.requireNonNull(
                                    config.getString("locations.spawnpoints." + i + ".world")))),
                    config.getDouble("locations.spawnpoints." + i + ".x"),
                    config.getDouble("locations.spawnpoints." + i + ".y"),
                    config.getDouble("locations.spawnpoints." + i + ".z")
            );
            locations.add(loc);
            KnockbackMadness.consoleLog("Location added to spawn list: " + loc);
        }
        if (config.getConfigurationSection("locations.spawn") == null) return;
        spawnLocation = new Location(
                Bukkit.getWorld(UUID.fromString(Objects.requireNonNull(
                        config.getString("locations.spawn.world")))),
                config.getDouble("locations.spawn.x"),
                config.getDouble("locations.spawn.y"),
                config.getDouble("locations.spawn.z"));
    }

    /**
     * Saves the given location in the config.yml for future references
     * @param location the location to save
     */
    public void saveLocation(Location location) {
        if (location.getWorld() == null) return;
        locations.add(location);
        FileConfiguration config = KnockbackMadness.getConfigFile();
        UUID world = location.getWorld().getUID();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        int id = 0;
        if (config.getConfigurationSection("locations.spawnpoints") == null)
            id = 1;
        else {
            for (int i = 0; i < Objects.requireNonNull(
                    config.getConfigurationSection("locations.spawnpoints"))
                    .getKeys(false).size(); i++) {
                id++;
            }
            id += 1;
        }
        config.set("locations.spawnpoints." + id + ".world", world.toString());
        config.set("locations.spawnpoints." + id + ".x", x);
        config.set("locations.spawnpoints." + id + ".y", y);
        config.set("locations.spawnpoints." + id + ".z", z);
        KnockbackMadness.saveConfigFile();
    }

    public void saveSpawnLocation(Location location) {
        spawnLocation = location;
        FileConfiguration config = KnockbackMadness.getConfigFile();
        if (location.getWorld() == null) {
            KnockbackMadness.gameLog("&cError saving spawn location, contact admin!");
            return;
        }
        config.set("locations.spawn.world", location.getWorld().getUID().toString());
        config.set("locations.spawn.x", location.getX());
        config.set("locations.spawn.y", location.getY());
        config.set("locations.spawn.z", location.getZ());
        KnockbackMadness.saveConfigFile();
    }

    public Location getSpawnLocation(Player player) {
        if (spawnLocation.getWorld() == null) spawnLocation.setWorld(player.getWorld());
        return spawnLocation;
    }

    /**
     * Gets a random location from the list of spawn points
     * @return random location
     */
    public Location getRandomSpawnPoint(Player player) {
        try {
            Random random = new Random();
            Location loc = locations.get(random.nextInt(locations.size()));
            if (loc.getWorld() == null) loc.setWorld(player.getWorld());
            return loc;
        } catch (IllegalArgumentException | NullPointerException e) {
            KnockbackMadness.gameLog("&cNo locations set! Ending Game!");
            controller.endGame();
            return spawnLocation;
        }
    }

    /**
     * Sends a player to the position they were at before the game started
     * @param player the player to send back
     */
    public void returnPlayerToStartPoint(Player player) {
        UUID playerID = player.getUniqueId();
        if (!startPoints.containsKey(playerID)) return;
        player.teleport(startPoints.get(playerID));
        player.sendMessage(KnockbackMadness.color("&7&oYou have been returned to where " +
                "you were before the game started!"));
    }

    /**
     * Sets the players return point for when the game ends or when they are out
     * @param player the player to save location from
     */
    public void setPlayerStartPoint(Player player) {
        UUID playerID = player.getUniqueId();
        startPoints.put(playerID, player.getLocation());
        KnockbackMadness.consoleLog("Saved " + player.getName() + " start location to " + startPoints.get(playerID));
    }

    /**
     * Clears all saved player location points
     */
    public void clearPoints() {
        startPoints.clear();
    }
}

package net.oasisgames.km.controller;

import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.location.LocationController;
import net.oasisgames.km.controller.player.KnockPlayer;
import net.oasisgames.km.scoreboard.GameBoard;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Main game controller class for the knockback madness minigame
 */
public class GameController {

    private boolean gameActive;
    private final List<KnockPlayer> knockPlayers;
    private final LocationController locationController;
    private final GameBoard scoreboard;

    public GameController() {
        knockPlayers = new ArrayList<>();
        locationController = new LocationController(this);
        scoreboard = new GameBoard(this);
        gameActive = false;
    }

    /**
     * Checks if the game is active
     * @return true if the game is active, false if not
     */
    public boolean isGameActive() {
        return gameActive;
    }

    /**
     * Starts the minigame and setups the starting parameters for each player in the queue
     */
    public void startGame() {
        if (gameActive) return;
        gameActive = true;
        knockPlayers.forEach(kp -> {
            locationController.setPlayerStartPoint(kp.getPlayer());
            kp.getPlayer().setHealth(20.0);
            kp.getPlayer().teleport(locationController.getRandomSpawnPoint(kp.getPlayer()));
            if (!gameActive) {
                locationController.returnPlayerToStartPoint(kp.getPlayer());
                return;
            }
            kp.getPlayer().setGameMode(GameMode.SURVIVAL);
            setupKnockbackInventory(kp.getPlayer());
        });
        scoreboard.createBoard();
        KnockbackMadness.gameLog("&aThe game has begun!");
    }

    /**
     * Ends the current game if one is active!
     */
    public void endGame() {
        if (!gameActive) return;
        gameActive = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(locationController.getSpawnLocation(player));
        }
        String winner = knockPlayers.stream().findFirst().orElseThrow().getPlayer().getName();
        KnockbackMadness.gameLog("&cThe game has ended! ");
        KnockbackMadness.gameLog("&a" + winner + " wins!");
        knockPlayers.clear();
        locationController.clearPoints();
        scoreboard.destroyBoard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(KnockbackMadness.color("&9You have been re-queued!"));
            addPlayer(new KnockPlayer(player, this));
        }
    }

    /**
     * Adds a player to the game queue
     * @param player KnockPlayer to add
     */
    public void addPlayer(KnockPlayer player) {
        if (isPlayerInGame(player.getPlayer().getName())) return;
        knockPlayers.add(player);
        knockPlayers.forEach(knockPlayer -> {
           if (knockPlayer.getPlayer().getUniqueId() != player.getPlayer().getUniqueId()) {
               knockPlayer.getPlayer().sendMessage(KnockbackMadness.color(
                       "&a" + player.getPlayer().getName() + " joined the game queue!"));
               knockPlayer.getPlayer().playSound(knockPlayer.getPlayer().getLocation(),
                       Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
           } else {
               knockPlayer.getPlayer().sendMessage(KnockbackMadness.color("&aYou joined the game queue!"));
               knockPlayer.getPlayer().playSound(knockPlayer.getPlayer().getLocation(),
                       Sound.BLOCK_NOTE_BLOCK_GUITAR, 1, 1);
           }
        });
        KnockbackMadness.consoleLog(player.getPlayer().getName() + " joined the game queue!");
    }

    /**
     * Removes a player from the queue list and returns them to their starting point.
     * Optionally ends the game as well if there are less than 2 players remaining.
     * @param player KnockPlayer to remove
     */
    private void removePlayer(KnockPlayer player) {
        if (!isPlayerInGame(player.getPlayer().getName())) return;
        knockPlayers.remove(player);
        scoreboard.removePlayerFromBoard(player.getPlayer());
        locationController.returnPlayerToStartPoint(player.getPlayer());
        player.getPlayer().setGameMode(GameMode.SPECTATOR);
        if (knockPlayers.size() < 2) endGame();
    }

    /**
     * Removes a player from the queue list and returns them to their starting point.
     * Optionally ends the game as well if there are less than 2 players remaining.
     * @param playerName name of the KnockPlayer to remove
     */
    public void removePlayer(String playerName) {
        if (!isPlayerInGame(playerName))
            return;
        KnockPlayer player = getInGameKnockPlayer(playerName);
        player.getPlayer().sendMessage(KnockbackMadness.color("&cYou left the game queue!"));
        knockPlayers.stream()
                .filter(kp -> !kp.getPlayer().getName().equalsIgnoreCase(playerName))
                .forEach(kp -> kp.getPlayer().sendMessage(
                        KnockbackMadness.color("&c" + playerName + " left the game queue!")));
        removePlayer(player);
    }

    /**
     * Checks if a player is currently in the game queue
     * @param playerName name of the KnockPlayer to check
     * @return true if the player is in game queue, false if not
     */
    public boolean isPlayerInGame(String playerName) {
        return knockPlayers.stream().anyMatch(player -> player.getPlayer().getName().equalsIgnoreCase(playerName));
    }

    /**
     * Gets a KnockPlayer from the list of active players
     * @param playerName The name of the KnockPlayer to get
     * @return KnockPlayer object
     */
    public KnockPlayer getInGameKnockPlayer(String playerName) {
        return knockPlayers.stream()
                .filter(kp -> kp.getPlayer().getName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Sets up the default game mode inventory
     * @param player Player to set inventory for
     */
    private void setupKnockbackInventory(Player player) {
        ItemStack bow = new ItemStack(Material.BOW, 1);
        ItemMeta bowMeta = bow.getItemMeta();
        if (bowMeta == null) return;
        bowMeta.addEnchant(Enchantment.INFINITY, 1, false);
        bowMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
        bowMeta.addEnchant(Enchantment.PUNCH, 5, true);
        bowMeta.setDisplayName(KnockbackMadness.color("&d&lKnockback Bow"));
        bowMeta.setUnbreakable(true);
        bow.setItemMeta(bowMeta);
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        player.getInventory().clear();
        player.getInventory().addItem(bow);
        player.getInventory().addItem(arrow);
    }

    /**
     * Gets the location controller for the game
     * @return location controller
     */
    public LocationController getLocationController() {
        return locationController;
    }

    /**
     * Gets how many players are in the game queue
     * @return queue size
     */
    public int getTotalPlayerCount() {
        return knockPlayers.size();
    }

    /**
     * Gets all Knock Players
     * @return List of all KnockPlayers
     */
    public List<KnockPlayer> getAllKnockPlayers() {
        return knockPlayers;
    }

}

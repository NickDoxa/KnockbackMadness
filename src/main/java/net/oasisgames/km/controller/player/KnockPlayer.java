package net.oasisgames.km.controller.player;

import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.GameController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * KnockPlayer class that contains the attached player along with game related methods and data
 */
public class KnockPlayer {

    private final Player player;
    private int lives;
    private final GameController controller;

    public KnockPlayer(Player player, GameController controller) {
        this.player = player;
        this.controller = controller;
        lives = 3;
    }

    /**
     * Gets the attached Bukkit player
     * @return Bukkit player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the players remaining life count
     * @return remaining life count
     */
    public int getLivesRemaining() {
        return lives;
    }

    /**
     * Takes 1 life from the players life count and optionally
     * kills them if they have no lives remaining
     */
    public void loseLife() {
        lives--;
        player.sendMessage(KnockbackMadness.color("&eLives: " + lives));
        player.setInvulnerable(true);
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.scheduleSyncDelayedTask(KnockbackMadness.getPlugin(KnockbackMadness.class), () -> {
            player.setInvulnerable(false);
        }, 2 * 20);
        if (lives < 1) kill();
    }

    /**
     * Kills the player and removes them from the game
     */
    public void kill() {
        if (player.getName().equalsIgnoreCase("konky"))
            getPlayer().sendMessage(KnockbackMadness.color("&cL BOZO! Get fucked idiot, you're dead!"));
        else
            getPlayer().sendMessage(KnockbackMadness.color("&cYou died! better luck next time!"));
        controller.removePlayer(player.getName());
    }

}

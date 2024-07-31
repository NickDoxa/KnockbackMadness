package net.oasisgames.km.events;

import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.GameController;
import net.oasisgames.km.controller.player.KnockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Event handler class for joining and leaving players
 */
public class JoinLeave implements Listener {

    private final GameController controller;

    public JoinLeave(GameController controller) {
        this.controller = controller;
    }

    /**
     * Handles when players join the game and adds them
     * automatically unless the game is in progress
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (controller.isGameActive()) return;
        Player player = event.getPlayer();
        controller.addPlayer(new KnockPlayer(event.getPlayer(), controller));
        Bukkit.getScheduler().scheduleSyncDelayedTask(KnockbackMadness.getPlugin(KnockbackMadness.class), () -> {
            player.teleport(controller.getLocationController().getSpawnLocation(event.getPlayer()));
        }, 20);
        player.setGameMode(GameMode.ADVENTURE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 10000000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10000000, 1));
    }

    /**
     * Handles when players quit the game and removes them
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        controller.removePlayer(event.getPlayer().getName());
    }

}

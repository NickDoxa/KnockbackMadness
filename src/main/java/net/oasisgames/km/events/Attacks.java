package net.oasisgames.km.events;

import net.oasisgames.km.ConfigReload;
import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.GameController;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * The attacks listener for the events pertaining to players damaging each other
 */
public class Attacks implements Listener, ConfigReload {

    Map<Player, Long> cooldowns = new HashMap<>();

    private final GameController controller;
    private int attackCooldown;
    private double attackPower;
    private double bowAttackPower;

    public Attacks(GameController controller) {
        this.controller = controller;
        loadConfigData();
        KnockbackMadness.addConfigFile(this);
    }

    @Override
    public void loadConfigData() {
        FileConfiguration config = KnockbackMadness.getConfigFile();
        bowAttackPower = config.getDouble("bow-attack-power");
        attackPower = config.getDouble("attack-power");
        attackCooldown = config.getInt("attack-cooldown");
    }

    /**
     * Event handler for the player damaging another player event. Distributes knockback velocity
     * if the requirements are met and then installs a cooldown as defined in config.yml
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler
    public void onHitPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof Player hitPlayer) {
            if (isOnCooldown(attacker)) return;
            if (attacker.getInventory().getItemInMainHand().getType() != Material.BOW) return;
            setPlayerCooldown(attacker, attackCooldown);
            Vector direction = attacker.getEyeLocation().getDirection();
            double power = event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE ?
                    attackPower : bowAttackPower;
            hitPlayer.setVelocity(direction.multiply(power));
            if (hitPlayer.getHealth() - event.getDamage() >= 1) return;
            attacker.setHealth(20);

        }
    }

    /**
     * EventHandler for projectile arrows hitting another knock player
     * @param event ProjectileHitEvent
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow && arrow.getShooter() instanceof Player shooter
        && event.getHitEntity() instanceof Player hitPlayer) {
            if (controller.isPlayerInGame(hitPlayer.getName()) && controller.isPlayerInGame(shooter.getName())) {
                hitPlayer.setVelocity(shooter.getEyeLocation().getDirection().multiply(bowAttackPower));
            }
        }
    }

    /**
     * Checks if the given player is on an attack cooldown
     * @param player Player to check
     * @return true if the player is on cooldown false if not
     */
    private boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player)) return false;
        return cooldowns.get(player) > System.currentTimeMillis();
    }

    /**
     * Puts a players attack on cooldown for the given amount of seconds
     * @param player Player to cooldown
     * @param seconds The amount of seconds for the cooldown to last
     */
    private void setPlayerCooldown(Player player, long seconds) {
        cooldowns.put(player, System.currentTimeMillis() + (seconds * 1000));
    }
}

package net.oasisgames.km.events;

import net.oasisgames.km.ConfigReload;
import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.GameController;
import net.oasisgames.km.controller.player.KnockPlayer;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * General event handling for knockback game
 */
public class GeneralEvents implements Listener, ConfigReload {

    private final GameController controller;
    private double jumpHeight;
    private double superJumpHeight;
    Map<UUID, Boolean> cooldowns = new HashMap<>();

    public GeneralEvents(GameController controller) {
        this.controller = controller;
        loadConfigData();
        KnockbackMadness.addConfigFile(this);
    }

    @Override
    public void loadConfigData() {
        jumpHeight = KnockbackMadness.getConfigFile().getDouble("jump-height");
        superJumpHeight = KnockbackMadness.getConfigFile().getDouble("super-jump-height");
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!(controller.isPlayerInGame(event.getPlayer().getName()) && controller.isGameActive())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event) {
        if (!(controller.isPlayerInGame(event.getPlayer().getName()) && controller.isGameActive())) return;
        Player player = event.getPlayer();
        if (isPlayerOnCooldown(player)) return;
        setPlayerCooldownOn(player);
        player.setVelocity(new Vector(0, jumpHeight, 0));
        player.playSound(player.getLocation(),
                Sound.ENTITY_FROG_LONG_JUMP, 1, 1);
        player.spawnParticle(Particle.SPIT, player.getLocation(), 10);
    }

    @EventHandler
    public void onTouchGround(PlayerMoveEvent event) {
        if (!(controller.isPlayerInGame(event.getPlayer().getName()) && controller.isGameActive())) return;
        Player player = event.getPlayer();
        if (!isPlayerOnCooldown(player)) return;
        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) return;
        setPlayerCooldownOff(player);
    }

    @EventHandler
    public void onJump(PlayerMoveEvent event) {
        if (!(controller.isPlayerInGame(event.getPlayer().getName()) && controller.isGameActive())) return;
        Player player = event.getPlayer();
        if (event.getTo() == null) return;
        if (event.getTo().getY() <= event.getFrom().getY()) return;
        if (!isJumperBlock(event.getFrom().getBlock().getRelative(BlockFace.DOWN))) return;
        player.setVelocity(new Vector(0, superJumpHeight, 0));
        player.playSound(player.getLocation(),
                Sound.ENTITY_BREEZE_JUMP, 1, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(controller.isPlayerInGame(player.getName()) && controller.isGameActive())) return;
        if (player.getHealth() - event.getDamage() >= 1) return;
        event.setCancelled(true);
        KnockPlayer p = controller.getInGameKnockPlayer(player.getName());
        p.loseLife();
        if (p.getLivesRemaining() >= 1)
            player.teleport(controller.getLocationController().getRandomSpawnPoint(player));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 1000000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1000000, 1));
    }

    private boolean isJumperBlock(Block block) {
        for (String mat : KnockbackMadness.getConfigFile().getStringList("accepted-jump-blocks")) {
            if (block.getType().toString().equalsIgnoreCase(mat)) return true;
        }
        return false;
    }

    private boolean isPlayerOnCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return false;
        return cooldowns.get(player.getUniqueId());
    }

    private void setPlayerCooldownOn(Player player) {
        cooldowns.put(player.getUniqueId(), true);
    }

    public void setPlayerCooldownOff(Player player) {
        cooldowns.put(player.getUniqueId(), false);
    }
}

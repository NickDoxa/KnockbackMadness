package net.oasisgames.km.events;

import net.oasisgames.km.ConfigReload;
import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.GameController;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Borders listener for getting the player movement event and checking their position
 */
public class Borders implements Listener, ConfigReload {

    private final GameController controller;
    private double y_level;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public Borders(GameController controller) {
        this.controller = controller;
        loadConfigData();
        KnockbackMadness.addConfigFile(this);
    }

    @Override
    public void loadConfigData() {
        FileConfiguration config = KnockbackMadness.getConfigFile();
        y_level = config.getDouble("y-level-cutoff");
    }

    /**
     * Event handler for checking if the player is outside the maps bounds as defined in the config.yml
     * @param event PlayerMoveEvent
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!controller.isGameActive() || !controller.isPlayerInGame(event.getPlayer().getName())) return;
        Player player = event.getPlayer();
        if (player.getLocation().getY() >= y_level) return;
        if (cooldowns.containsKey(player.getUniqueId())) {
            if (cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) return;
        }
        controller.getInGameKnockPlayer(player.getName()).loseLife();
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 1000);
        player.teleport(controller.getLocationController().getRandomSpawnPoint(player));
    }
}

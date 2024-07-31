package net.oasisgames.km.commands;

import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.GameController;
import net.oasisgames.km.controller.player.KnockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import java.util.List;

public class CommandController implements CommandExecutor {

    private final GameController gameController;
    private final FileConfiguration config;

    public CommandController(GameController gameController) {
        this.gameController = gameController;
        config = KnockbackMadness.getConfigFile();
    }

    /**
     * Command method for handling the knockback command
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return bukkit passed boolean
     */
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command,
                             String label, @Nonnull String[] args) {
        if (!label.equalsIgnoreCase("knockback")) return false;
        if (sender instanceof Player player) {
            if (args.length != 1) {
                sendHelpMessage(player);
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "join":
                    if (gameController.isGameActive()) {
                        return false;
                    }
                    if (gameController.isPlayerInGame(player.getName())) {
                        player.sendMessage(KnockbackMadness.color("&cYou are already in the game queue!"));
                        return false;
                    }
                    gameController.addPlayer(new KnockPlayer(player, gameController));
                    break;
                case "leave":
                    if (!gameController.isPlayerInGame(player.getName())) {
                        player.sendMessage(KnockbackMadness.color("&cYou are not in the game queue!"));
                        return false;
                    }
                    if (gameController.isGameActive()) {
                        gameController.getInGameKnockPlayer(player.getName()).kill();
                        return false;
                    }
                    gameController.removePlayer(player.getName());
                    break;
                case "start":
                    if (gameController.isGameActive()) {
                        player.sendMessage(KnockbackMadness.color("&cGame is already started!"));
                        return false;
                    }
                    if (gameController.getTotalPlayerCount() < 2) {
                        player.sendMessage(KnockbackMadness.color("&cCannot start game " +
                                "with less than 2 players!"));
                        return false;
                    }
                    player.sendMessage(KnockbackMadness.color("&aStarting game..."));
                    sendCountdownMessage();
                    break;
                case "end":
                    if (!gameController.isGameActive()) {
                        player.sendMessage(KnockbackMadness.color("&cGame is not active!"));
                        return false;
                    }
                    player.sendMessage(KnockbackMadness.color("&aSuccessfully ended game!"));
                    gameController.endGame();
                    break;
                case "addpoint":
                    gameController.getLocationController().saveLocation(player.getLocation());
                    player.sendMessage(KnockbackMadness.color("&aSaved location to config!"));
                    break;
                case "setspawn":
                    gameController.getLocationController().saveSpawnLocation(player.getLocation());
                    player.sendMessage(KnockbackMadness.color("&aSaved spawn location to config!"));
                    break;
                case "reload":
                    KnockbackMadness.reloadConfigFiles();
                    player.sendMessage(KnockbackMadness.color("&aReloaded the game config!"));
                    break;
                case "config":
                    player.sendMessage(KnockbackMadness.color("&bConfig Layout:"));
                    player.sendMessage(KnockbackMadness.color("&e" + "attack cooldown: &7" +
                            config.getInt("attack-cooldown")));
                    player.sendMessage(KnockbackMadness.color("&e" + "attack power: &7" +
                            config.getDouble("attack-power")));
                    player.sendMessage(KnockbackMadness.color("&e" + "bow attack power: &7" +
                            config.getDouble("bow-attack-power")));
                    player.sendMessage(KnockbackMadness.color("&e" + "jump height: &7" +
                            config.getDouble("jump-height")));
                    player.sendMessage(KnockbackMadness.color("&e" + "super jump height: &7" +
                            config.getDouble("super-jump-height")));
                    player.sendMessage(KnockbackMadness.color("&e" + "Y level cutoff: &7" +
                            config.getDouble("y-level-cutoff")));
                    player.sendMessage(KnockbackMadness.color("&e" + "accepted jump blocks:"));
                    List<String> blocks = config.getStringList("accepted-jump-blocks");
                    for (String block : blocks) {
                        player.sendMessage(KnockbackMadness.color("&7" + block));
                    }
                    break;
                default:
                    sendHelpMessage(player);
                    break;
            }
        }
        return false;
    }

    /**
     * Sends a help message to the player about the command
     * @param player player to send message to
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage(KnockbackMadness.color("&aIncorrect command usage! Try:"));
        player.sendMessage(KnockbackMadness.color("&b/knockback join - joins the game queue"));
        player.sendMessage(KnockbackMadness.color("&b/knockback leave - leaves the game queue"));
        if (player.hasPermission("knockback.admin") || player.isOp()) {
            player.sendMessage(KnockbackMadness.color("&eAdmin only commands:"));
            player.sendMessage(KnockbackMadness.color("&b/knockback start - starts the game if there are enough" +
                    "players"));
            player.sendMessage(KnockbackMadness.color("&b/knockback end - forcefully ends the game if " +
                    "its in progress"));
            player.sendMessage(KnockbackMadness.color("&b/knockback addpoint - adds a spawnpoint to the save" +
                    "file"));
            player.sendMessage(KnockbackMadness.color("&b/knockback reload - reloads the entire plugin"));
            player.sendMessage(KnockbackMadness.color("&b/knockback config - see the current config layout"));
        }
    }

    /**
     * Sends the countdown message to all players in the server and then starts the game
     */
    private void sendCountdownMessage() {
        KnockbackMadness.gameLog("&dGame begins in:");
        BukkitScheduler scheduler = Bukkit.getScheduler();
        for (int i = 1; i < 4; i++) {
            final int second = i;
            scheduler.scheduleSyncDelayedTask(KnockbackMadness.getPlugin(KnockbackMadness.class), () ->
                    {
                        KnockbackMadness.gameLog("&d" + (4-second) + "...");
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(),
                                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        }
                    }, i * 20);
        }
        scheduler.scheduleSyncDelayedTask(KnockbackMadness.getPlugin(KnockbackMadness.class),
                gameController::startGame, 4 * 20);
        scheduler.scheduleSyncDelayedTask(KnockbackMadness.getPlugin(KnockbackMadness.class), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(),
                        Sound.ITEM_GOAT_HORN_SOUND_0, 1, 1);
            }
        }, 4 * 20);
    }

}

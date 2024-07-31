package net.oasisgames.km.scoreboard;

import net.oasisgames.km.KnockbackMadness;
import net.oasisgames.km.controller.GameController;
import net.oasisgames.km.controller.player.KnockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Objects;

public class GameBoard {

    private final GameController controller;
    private Scoreboard activeBoard;
    private final ScoreboardManager manager;
    private int updateTaskID;

    public GameBoard(GameController controller) {
        this.controller = controller;
        manager = Bukkit.getScoreboardManager();
        if (manager == null) throw new RuntimeException();
    }

    public void createBoard() {
        if (manager == null) return;
        activeBoard = manager.getNewScoreboard();
        Team team = activeBoard.registerNewTeam("knockback-players");
        team.setAllowFriendlyFire(true);
        Objective objective = activeBoard.registerNewObjective(
                "lives", Criteria.DUMMY, KnockbackMadness.color("&b&nLives Remaining:"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(activeBoard);
        }
        beginUpdateTask();
    }

    private void beginUpdateTask() {
        updateTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(KnockbackMadness.getPlugin(KnockbackMadness.class), () -> {
            Objective objective = activeBoard.getObjective("lives");
            if (objective == null) return;
            for (KnockPlayer player : controller.getAllKnockPlayers()) {
                Score playerLives = objective.getScore(player.getPlayer().getName());
                playerLives.setScore(player.getLivesRemaining());
            }
        }, 0, 5);
    }

    public void destroyBoard() {
        Bukkit.getScheduler().cancelTask(updateTaskID);
        activeBoard = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(manager.getNewScoreboard());
        }
    }

    public void removePlayerFromBoard(Player player) {
        if (activeBoard.getObjective("lives") == null) return;
        Objects.requireNonNull(activeBoard.getObjective("lives")).getScore(player.getName()).setScore(0);
    }

}

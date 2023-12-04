package dev.deann.Managers;

import dev.deann.Enum.GameState;
import dev.deann.Enum.GameType;
import dev.deann.GameHelpers;
import dev.deann.MinigameServer;
import dev.deann.Runnables.CountdownRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameManager implements AbstractGameManager {
    protected final ConfigurationSection config;
    private final String TEMPLATE_FOLDER;

    protected final MinigameServer plugin;
    protected final Logger serverLogger;
    protected GameState gameState;
    protected final World activeWorld;
    private ArrayList<Player> activePlayers;
    private ArrayList<Player> spectators;
    protected ArrayList<Player> playersInGameServer;
    protected BukkitTask countdownTask;

    protected GameType gameType;
    public GameManager(MinigameServer plugin, GameType gameType) {
        this.plugin = plugin;
        this.gameType = gameType;
        serverLogger = plugin.getLogger();
        config = plugin.getGameConfig(gameType.getName());
        // MOVE TO BASE
        TEMPLATE_FOLDER = config.getString("template");
        activeWorld = GameHelpers.resetMap(gameType.getName() + "-" + System.currentTimeMillis(), TEMPLATE_FOLDER);
    }

    // @param allPlayers should not be used directly as it may change
    // i.e. when the plugin queue is passed to start the game
    public boolean startGame(ArrayList<Player> allPlayers) {
        gameState = GameState.SETUP;
        activePlayers = new ArrayList<>(allPlayers);
        playersInGameServer = new ArrayList<>(allPlayers);
        spectators = new ArrayList<>();

        return true;
    }

    public void endGame(boolean force) {
        int countDownTimer = (force) ? 3 : 10;
        gameState = GameState.FINISHED;
        if (force) {
            activeWorld.sendMessage(Component.text("This game has been stopped", NamedTextColor.RED));
            serverLogger.log(Level.INFO, "Game at " + activeWorld.getName() + " has been force shutdown");
        }
        else if (activePlayers.isEmpty()) {
            serverLogger.info("Game at " + activeWorld.getName() + " shutting down with 0 players");
            countDownTimer = 1;
        }
        else {
            Player winner = activePlayers.get(0);
            serverLogger.log(Level.INFO, "Game at " + activeWorld.getName() + " ending");
            // Have to delay messages slightly longer than respawn, so the dying player still sees them
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> activeWorld.sendMessage(
                    Component.text(winner.getName() + " has won the game!", NamedTextColor.GREEN)), 3);
        }
        if (gameState == GameState.COUNTDOWN) {
            countdownTask.cancel();
        }
        new CountdownRunnable( countDownTimer, "Returning to lobby in ",
                "Returning to lobby!", activeWorld).runTaskTimer(plugin, 3, 20);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player p : playersInGameServer) {
                p.setGameMode(GameMode.ADVENTURE);
                p.getInventory().clear();
                plugin.bringToLobby(p);
            }
            GameHelpers.removeWorld(activeWorld, serverLogger);
            serverLogger.log(Level.INFO, "Players returned to lobby");
        }, 20 * countDownTimer);

        plugin.removeGameManager(this);
    }

    protected void runCountDown(BukkitRunnable runAfter) {
        String countdownMessage = gameType.getTitleName() + " starting in ",
                finishedMessage = GameType.SKYWARS.getTitleName() + " started!";
        int countdownLength = 5;
        countdownTask = new CountdownRunnable(countdownLength, countdownMessage, finishedMessage, activeWorld)
                .runTaskTimer(plugin, 0, 20);
        gameState = GameState.COUNTDOWN;

        runAfter.runTaskLater(plugin, 20 * countdownLength);
    }

    public GameState getGameState() {
        return gameState;
    }
    public World getActiveWorld() {
        return activeWorld;
    }

    public void removeActivePlayer(Player player) {
        activePlayers.remove(player);
        if (activePlayers.size() <= 1) {
            endGame(false);
        }
    }

    public void addSpectator(Player player) {
        spectators.add(player);
    }

    public void removePlayerFromGameServer(Player player) {
        playersInGameServer.remove(player);
        removeActivePlayer(player);
        spectators.remove(player);

        activeWorld.sendMessage(Component.text(player.getName() + " has left the game!", NamedTextColor.AQUA));
    }
}

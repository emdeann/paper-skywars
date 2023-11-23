package dev.deann;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameEventListener implements Listener {


    private final int MAX_PLAYERS;

    private final GameManager gameManager;

    private final Logger serverLogger;

    public GameEventListener(ArrayList<Player> playerList, GameManager gameManager, int maxPlayers, Logger logger) {
        this.gameManager = gameManager;
        this.MAX_PLAYERS = maxPlayers;
        this.serverLogger = logger;
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        Player dead = event.getPlayer();
        Location deathLoc = dead.getLocation();
        // Skip respawn screen
        Bukkit.getScheduler().scheduleSyncDelayedTask(Skywars.getInstance(), () -> dead.spigot().respawn(), 2);
        dead.setGameMode(GameMode.SPECTATOR);
        // Prevent game win message from being sent before death message
        Bukkit.getScheduler().scheduleSyncDelayedTask(Skywars.getInstance(),
                () -> gameManager.removeActivePlayer(dead), 2);
        gameManager.addSpectator(dead);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        Player joined = event.getPlayer();
        joined.setGameMode(GameMode.SPECTATOR);
        joined.sendMessage(Component.text("A game is in progress, you will be added to the next one!",
                    NamedTextColor.DARK_PURPLE));
        gameManager.addSpectator(joined);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (gameManager.getPlayersInGame() >= MAX_PLAYERS) {
            event.kickMessage(Component.text("This server is full!", NamedTextColor.RED));
            event.setResult(PlayerLoginEvent.Result.KICK_FULL);
            serverLogger.log(Level.INFO, "%s (%s) was kicked due to the server being full"
                    .formatted(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString()));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (gameManager.getGameState() == GameState.COUNTDOWN && gameManager.getActivePlayers().contains(event.getPlayer())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
        }
    }

    @EventHandler public void onPlayerInteract(PlayerInteractEvent event) {
        if (gameManager.getGameState() == GameState.COUNTDOWN && event.getAction() == Action.RIGHT_CLICK_BLOCK &&
        event.getClickedBlock().getType() == Material.CHEST) {
            event.setCancelled(true);
        }
    }
}

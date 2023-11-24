package dev.deann.EventListeners;

import dev.deann.GameManager;
import dev.deann.Enum.GameState;
import dev.deann.Skywars;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.logging.Logger;

public class GameEventListener implements Listener {



    private final GameManager gameManager;
    private final Skywars plugin;

    public GameEventListener(GameManager gameManager) {
        this.gameManager = gameManager;
        this.plugin = gameManager.getPlugin();
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        Player dead = event.getPlayer();
        Location deathLoc = dead.getLocation();
        int spawnY = dead.getWorld().getSpawnLocation().getBlockY();
        if (deathLoc.getBlockY() < spawnY) {
            deathLoc.setY(spawnY);
        }
        // Don't respawn in blocks
        while (!gameManager.getActiveWorld().getBlockAt(deathLoc).isEmpty()) {
            deathLoc.setY(deathLoc.getBlockY() + 1);
        }
        // Skip respawn screen
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> dead.spigot().respawn(), 2);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> dead.teleport(deathLoc), 5);
        dead.setGameMode(GameMode.SPECTATOR);
        // Prevent game win message from being sent before death message
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                () -> gameManager.removeActivePlayer(dead), 2);
        gameManager.addSpectator(dead);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (playerInCountdown(event.getPlayer())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (gameManager.getGameState() == GameState.COUNTDOWN && event.getAction() == Action.RIGHT_CLICK_BLOCK &&
        event.getClickedBlock().getType() == Material.CHEST) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        // Instant kill with void damage
        if (entity instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            ((Player) entity).setHealth(0);
        }
    }

    private boolean playerInCountdown(Player p) {
        return gameManager.getGameState() == GameState.COUNTDOWN && gameManager.getActivePlayers().contains(p);
    }
}

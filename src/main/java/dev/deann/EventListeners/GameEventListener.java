package dev.deann.EventListeners;

import dev.deann.GameManager;
import dev.deann.Enum.GameState;
import dev.deann.MinigameServer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventListener implements Listener {
    private final MinigameServer plugin;

    public GameEventListener(MinigameServer plugin) {
        this.plugin = plugin;
    }

    // Handlers in this class must use either playerInGame or playerInCountdown
    // To avoid affecting players in lobby

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        Player dead = event.getPlayer();
        event.deathMessage(null);
        if (!playerInGame(dead)) return;

        Location deathLoc = dead.getLocation();
        GameManager gameManager = getPlayerGame(dead);
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
    public void blockBreakEvent(BlockBreakEvent event) {
        if (playerInCountdown(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (playerInCountdown(event.getPlayer()) && event.getAction() == Action.RIGHT_CLICK_BLOCK &&
        event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        // Instant kill with void damage
        if (entity instanceof Player && playerInGame((Player) entity) && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            ((Player) entity).setHealth(0);
            event.setCancelled(true);
        }
    }

    @EventHandler public void onPlayerLeave(PlayerQuitEvent event) {
        if (!playerInGame(event.getPlayer())) return;

        event.quitMessage(null);
        Player p = event.getPlayer();
        GameManager manager = getPlayerGame(p);
        manager.removePlayerFromServerList(p);
    }

    private boolean playerInCountdown(Player p) {
        return playerInGame(p) && getPlayerGame(p).getGameState() == GameState.COUNTDOWN;
    }

    private boolean playerInGame(Player p) {
        return getPlayerGame(p) != null;
    }

    private GameManager getPlayerGame(Player p) {
        return plugin.getWorldToGame().get(p.getWorld());
    }

}

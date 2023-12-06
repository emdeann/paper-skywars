package dev.deann.EventListeners;

import dev.deann.MinigameServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class LobbyEventListener implements Listener {

    private final World lobbyWorld;

    public LobbyEventListener(MinigameServer plugin) {
        this.lobbyWorld = plugin.getLobbyWorld();
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        // No food loss in lobbies
        event.setCancelled(true);
        event.getEntity().setFoodLevel(20);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        Player joined = event.getPlayer();
        joined.getInventory().clear();
        joined.setGameMode(GameMode.ADVENTURE);
        joined.teleport(lobbyWorld.getSpawnLocation());
        joined.sendMessage(Component.text("Welcome to the lobby!",
                NamedTextColor.DARK_PURPLE));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getWorld().equals(lobbyWorld)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                event.getEntity().teleport(lobbyWorld.getSpawnLocation());
            }
            event.setCancelled(true);
        }
    }

}

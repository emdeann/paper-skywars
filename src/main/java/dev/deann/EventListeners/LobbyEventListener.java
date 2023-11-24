package dev.deann.EventListeners;

import dev.deann.Skywars;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class LobbyEventListener implements Listener {

    private final Skywars plugin;
    private final World lobbyWorld;

    public LobbyEventListener(Skywars plugin) {
        this.plugin = plugin;
        this.lobbyWorld = plugin.getLobbyWorld();
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
        if (event.getEntity().getWorld().equals(plugin.getLobbyWorld())) {
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                event.getEntity().teleport(plugin.getLobbyWorld().getSpawnLocation());
            }
            event.setCancelled(true);
        }
    }

}

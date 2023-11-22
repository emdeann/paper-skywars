package dev.deann;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.ArrayList;

public class GameEventListener implements Listener {

    private final ArrayList<Player> playerList;
    private final ArrayList<Player> spectatorList;

    private final int MAX_PLAYERS;

    private final GameManager gameManager;
    public GameEventListener(ArrayList<Player> playerList, GameManager gameManager, int maxPlayers) {
        this.playerList = new ArrayList<>(playerList);
        spectatorList = new ArrayList<>();
        this.gameManager = gameManager;
        this.MAX_PLAYERS = maxPlayers;
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        Player dead = event.getPlayer();
        // Skip respawn screen
        dead.spigot().respawn();
        dead.setGameMode(GameMode.SPECTATOR);
        playerList.remove(dead);
        spectatorList.add(dead);

        if (playerList.size() == 1) {
            playerList.addAll(spectatorList);
            gameManager.endGame(playerList.get(0));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        Player joined = event.getPlayer();
        joined.setGameMode(GameMode.SPECTATOR);
        joined.sendMessage(Component.text("A game is in progress, you will be added to the next one!",
                    NamedTextColor.DARK_PURPLE));
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (playerList.size() + spectatorList.size() >= MAX_PLAYERS) {
            event.kickMessage(Component.text("This server is full!", NamedTextColor.RED));
            event.setResult(PlayerLoginEvent.Result.KICK_FULL);
        }
    }
}

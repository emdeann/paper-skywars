package dev.deann;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;

public class DeathEvent implements Listener {

    private ArrayList<Player> playerList;
    private ArrayList<Player> spectatorList;

    private GameManager gameManager;
    public DeathEvent(ArrayList<Player> playerList, GameManager gameManager) {
        this.playerList = new ArrayList<>(playerList);
        spectatorList = new ArrayList<>();
        this.gameManager = gameManager;
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
            gameManager.endGame(playerList.get(0), playerList);
        }

    }
}

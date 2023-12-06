package dev.deann.Managers;

import dev.deann.Enum.GameState;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

// The GameManager handles the main setup and execution of the game
// Certain events are handled by the GameEventListener, which
// calls back to the manager
public interface AbstractGameManager {
    boolean startGame(ArrayList<Player> allPlayers);

    void endGame(boolean force);

    GameState getGameState();

    World getActiveWorld();

    void removeActivePlayer(Player player);

    void removePlayerFromGameServer(Player player);


}
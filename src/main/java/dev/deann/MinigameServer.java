package dev.deann;

import dev.deann.Commands.QueueCommand;
import dev.deann.Commands.StartCommand;
import dev.deann.Commands.StopCommand;
import dev.deann.EventListeners.GameEventListener;
import dev.deann.EventListeners.LobbyEventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;


public final class MinigameServer extends JavaPlugin implements Listener {

    private Map<World, GameManager> worldToGame;
    private ArrayList<Player> queue;
    private String lobbyName;
    private String templateName;
    private int maxPlayersPerGame;
    private int maxGames;
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        maxGames = config.getInt("MaxGames", 5);
        lobbyName = config.getString("Lobby", "skywars");
        templateName = config.getString("Template", "skywars_template");
        maxPlayersPerGame = config.getStringList("Spawns").size();
        worldToGame = new HashMap<>();
        queue = new ArrayList<>();
        if (maxPlayersPerGame == 0) maxPlayersPerGame = 4;
        Objects.requireNonNull(Bukkit.getPluginCommand("start")).setExecutor(new StartCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("stopGame")).setExecutor(new StopCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("queue")).setExecutor(new QueueCommand(this));
        Bukkit.getPluginManager().registerEvents(new LobbyEventListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameEventListener(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        File worldContainer = this.getServer().getWorldContainer();
        for (File f : worldContainer.listFiles()) {
            if (f.getName().startsWith("skywars-")) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    this.getLogger().log(Level.WARNING, "Failed to delete an old temporary map file");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Game not active; players spawning in lobby
        Player p = event.getPlayer();
        p.setGameMode(GameMode.ADVENTURE);
        p.sendMessage(Component.text("There is no active game right now, one should start soon!",
                NamedTextColor.DARK_PURPLE));
    }

    public GameManager addGameManager() {
        GameManager newManager = new GameManager(this);
        worldToGame.put(newManager.getActiveWorld(), newManager);
        return newManager;
    }

    public void removeGameManager(GameManager manager) {
        worldToGame.remove(manager.getActiveWorld());
    }

    public Map<World, GameManager> getWorldToGame() {
        return worldToGame;
    }

    public int getMaxGames() {
        return maxGames;
    }

    public int getActiveGames() {
        return worldToGame.size();
    }

    public World getLobbyWorld() {
        if (Bukkit.getWorld(lobbyName) == null) {
            new WorldCreator(lobbyName).createWorld();
        }
        return Bukkit.getWorld(lobbyName);
    }

    public String getTemplateName() {
        return templateName;
    }
    public int getMaxPlayersPerGame() {
        return maxPlayersPerGame;
    }

    public void addToQueue(Player p) {
        queue.add(p);
        if (queue.size() == maxPlayersPerGame) {
            for (Player player : queue) {
                player.sendMessage(Component.text("Queue has filled, sending you to a game!", NamedTextColor.GREEN));
            }
            addGameManager().start(queue);
        }
    }

    // GameManager will call this on every player on game start
    // To ensure nobody can both be in a game and in the queue
    public void removeFromQueue(Player p) {
        queue.remove(p);
    }

    public boolean playerInQueue(Player p) {
        return queue.contains(p);
    }
}

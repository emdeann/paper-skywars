package dev.deann;

import dev.deann.Commands.HubCommand;
import dev.deann.Commands.QueueCommand;
import dev.deann.Commands.StartCommand;
import dev.deann.Commands.StopCommand;
import dev.deann.Enum.GameType;
import dev.deann.EventListeners.GameEventListener;
import dev.deann.EventListeners.LobbyEventListener;
import dev.deann.Managers.ChestsSpawnsGameManager;
import dev.deann.Managers.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;


public final class MinigameServer extends JavaPlugin implements Listener {

    private Map<World, GameManager> worldToGame;
    private Map<GameType, Class<? extends GameManager>> gameTypeToClass;
    private Map<String, GameType> nameToGameType;
    private Map<GameType, ArrayList<Player>> queue;
    private String lobbyName;
    private int maxPlayersPerGame;
    private int maxGames;
    private ConfigurationSection templates;
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        maxGames = config.getInt("MaxGames", 5);
        lobbyName = config.getString("Lobby", "skywars");
        maxPlayersPerGame = config.getStringList("Spawns").size();
        worldToGame = new HashMap<>();
        gameTypeToClass = Map.of(GameType.SKYWARS, ChestsSpawnsGameManager.class,
                GameType.SURVIVAL_GAMES, ChestsSpawnsGameManager.class);
        nameToGameType = Map.of("skywars", GameType.SKYWARS, "sg", GameType.SURVIVAL_GAMES);
        queue = new HashMap<>();
        if (maxPlayersPerGame == 0) maxPlayersPerGame = 4;
        Objects.requireNonNull(Bukkit.getPluginCommand("start")).setExecutor(new StartCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("stopGame")).setExecutor(new StopCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("queue")).setExecutor(new QueueCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("hub")).setExecutor(new HubCommand(this));
        Bukkit.getPluginManager().registerEvents(new LobbyEventListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameEventListener(this), this);

        templates = getConfig().getConfigurationSection("templates");
        if (templates == null) {
            this.getLogger().severe("Template files not configured. Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

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

    public GameManager addGameManager(String type) {
        if (worldToGame.size() >= maxGames) {
            this.getLogger().info("Attempted to start an overflow game");
            return null;
        }

        GameType gameType = nameToGameType.get(type.toLowerCase());
        Class<? extends GameManager> gameClass = gameTypeToClass.get(gameType);
        if (gameClass == null) {
            return null;
        }
        GameManager newManager;
        try {
            newManager = (GameManager) gameClass.getConstructors()[0].newInstance(this, type.toLowerCase());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            this.getLogger().severe("Couldn't create new game of type " + type);
            return null;
        }
        worldToGame.put(newManager.getActiveWorld(), newManager);
        return newManager;
    }

    public void removeGameManager(GameManager manager) {
        worldToGame.remove(manager.getActiveWorld());
    }

    public Map<World, GameManager> getWorldToGame() {
        return worldToGame;
    }

    public GameManager getPlayerGame(Player pLayer) {
        return worldToGame.get(pLayer.getWorld());
    }

    public boolean playerInGame(Player player) {
        return worldToGame.containsKey(player.getWorld());
    }

    public void bringToLobby(Player player) {
        player.teleport(getLobbyWorld().getSpawnLocation());
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

    public String getTemplateName(String gameName) {
        return templates.getString(gameName.toLowerCase());
    }
    public int getMaxPlayersPerGame() {
        return maxPlayersPerGame;
    }

    public boolean addToQueue(Player p, String type) {
        GameType gameType = nameToGameType.get(type.toLowerCase());
        if (gameType == null) return false;

        queue.computeIfAbsent(gameType, k -> new ArrayList<>());
        ArrayList<Player> thisQueue = queue.get(gameType);
        thisQueue.add(p);

        if (queue.size() == maxPlayersPerGame) {
            for (Player player : thisQueue) {
                player.sendMessage(Component.text("Queue has filled, sending you to a game!", NamedTextColor.GREEN));
            }
            if (worldToGame.size() < maxGames) {
                Objects.requireNonNull(addGameManager(type)).startGame(thisQueue);
            } else {
                for (Player player : thisQueue) {
                    player.sendMessage(Component.text("The maximum number of games has been reached, waiting to send you to the next available game"
                            , NamedTextColor.GREEN));
                }
            }
        }
        return true;
    }

    // GameManager will call this on every player on game start
    // To ensure nobody can both be in a game and in the queue
    public void removeFromQueue(Player p, String type) {
        ArrayList<Player> thisQueue = queue.get(nameToGameType.get(type.toLowerCase()));
        if (thisQueue != null) {
            queue.get(nameToGameType.get(type.toLowerCase())).remove(p);
        } else {
            getLogger().severe("Attempted to remove " + p.getName() + " from the " + type + " queue, but it didn't exist");
        }
    }

    public boolean playerInQueue(Player p) {
        for (ArrayList<Player> queued : queue.values()) {
            if (queued.contains(p)) return true;
        }
        return false;
    }
}

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
import org.bukkit.*;
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
    private Map<GameType, ArrayList<Player>> queue;
    private String lobbyName;
    private int maxPlayersPerGame;
    private int maxGames;
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        maxGames = config.getInt("max_games", 5);
        lobbyName = config.getString("lobby", "skywars");
        // Temp
        // todo: use max players for individual games instead of globally
        maxPlayersPerGame = 4;
        worldToGame = new HashMap<>();
        gameTypeToClass = Map.of(GameType.SKYWARS, ChestsSpawnsGameManager.class,
                GameType.SURVIVAL_GAMES, ChestsSpawnsGameManager.class);
        queue = new HashMap<>();
        if (maxPlayersPerGame == 0) maxPlayersPerGame = 4;
        Objects.requireNonNull(Bukkit.getPluginCommand("start")).setExecutor(new StartCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("stopGame")).setExecutor(new StopCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("queue")).setExecutor(new QueueCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("hub")).setExecutor(new HubCommand(this));
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
        bringToLobby(p);
    }

    public GameManager addGameManager(GameType gameType) {
        if (worldToGame.size() >= maxGames) {
            this.getLogger().info("Attempted to start an overflow game");
            return null;
        }
        Class<? extends GameManager> gameClass = gameTypeToClass.get(gameType);
        GameManager newManager;
        try {
            newManager = (GameManager) gameClass.getConstructors()[0].newInstance(this, gameType);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            this.getLogger().severe("Couldn't create new game of type " + gameType.getTitleName());
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
            new WorldCreator(lobbyName)
                .generateStructures(false)
                .environment(World.Environment.CUSTOM)
                .createWorld();
        }
        World w = Bukkit.getWorld(lobbyName);
        if (w == null) {
            return null;
        }
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        return w;
    }

    public int getMaxPlayersPerGame() {
        return maxPlayersPerGame;
    }

    public ConfigurationSection getGameConfig(String gameName) {
        return Objects.requireNonNull(getConfig().getConfigurationSection("games"))
                .getConfigurationSection(gameName.toLowerCase());
    }

    public void addToQueue(Player p, GameType gameType) {

        queue.computeIfAbsent(gameType, k -> new ArrayList<>());
        ArrayList<Player> thisQueue = queue.get(gameType);
        thisQueue.add(p);

        if (queue.size() == maxPlayersPerGame) {
            for (Player player : thisQueue) {
                player.sendMessage(Component.text("Queue has filled, sending you to a game!", NamedTextColor.GREEN));
            }
            if (worldToGame.size() < maxGames) {
                Objects.requireNonNull(addGameManager(gameType)).startGame(thisQueue);
            } else {
                for (Player player : thisQueue) {
                    player.sendMessage(Component.text("The maximum number of games has been reached, waiting to send you to the next available game"
                            , NamedTextColor.GREEN));
                }
            }
        }
    }

    // GameManager will call this on every player on game start
    // To ensure nobody can both be in a game and in the queue
    public void removeFromQueue(Player p, GameType gameType) {
        ArrayList<Player> thisQueue = queue.get(gameType);
        if (thisQueue != null) {
            thisQueue.remove(p);
        } else {
            getLogger().severe("Attempted to remove " + p.getName() + " from the " + gameType.getTitleName() + " queue, but it didn't exist");
        }
    }

    public boolean playerInQueue(Player p) {
        for (ArrayList<Player> queued : queue.values()) {
            if (queued.contains(p)) return true;
        }
        return false;
    }
}

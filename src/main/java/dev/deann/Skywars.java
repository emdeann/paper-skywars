package dev.deann;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;


public final class Skywars extends JavaPlugin implements Listener {

    private ArrayList<GameManager> gameManagers;

    private int DEFAULT_MAX_GAMES = 5;
    private int maxGames;
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        maxGames = this.getConfig().getInt("MaxGames", DEFAULT_MAX_GAMES);
        gameManagers = new ArrayList<>();
        Bukkit.getPluginCommand("start").setExecutor(new StartExecutor(this));

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
        gameManagers.add(newManager);
        return newManager;
    }

    public void removeGameManager(GameManager manager) {
        gameManagers.remove(manager);
    }

    public void addEventListener(Listener e) {
        Bukkit.getPluginManager().registerEvents(e, this);
    }

    public void removeGameListener(Listener l) {
        HandlerList.unregisterAll(l);
    }

    public int getMaxGames() {
        return maxGames;
    }

    public int getActiveGames() {
        return gameManagers.size();
    }

}

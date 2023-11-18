package dev.deann;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;


public final class Skywars extends JavaPlugin {

    private static Skywars instance;

    private static GameManager gameManager;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        Bukkit.getPluginCommand("start").setExecutor(new StartExecutor());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Skywars getInstance() {
        return instance;
    }

    public static void setGameManager(GameManager manager) {
        gameManager = manager;
    }
    public static GameManager getGameManager() {
        return gameManager;
    }

    public static void addEventListener(Listener e) {
        Bukkit.getPluginManager().registerEvents(e, instance);
    }

    public static void removeDeathListener(Listener l) {
        PlayerDeathEvent.getHandlerList().unregister(l);
    }
}

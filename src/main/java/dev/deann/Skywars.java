package dev.deann;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;


public final class Skywars extends JavaPlugin {

    private static Skywars instance;

    private static GameManager gameManager;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        this.saveDefaultConfig();
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

    public static void removeGameListener(Listener l) {
        PlayerDeathEvent.getHandlerList().unregister(l);
    }

}

package dev.deann;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Skywars extends JavaPlugin {

    private static Skywars instance;
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
}

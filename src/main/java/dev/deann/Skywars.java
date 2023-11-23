package dev.deann;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.logging.Level;


public final class Skywars extends JavaPlugin implements Listener {
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Game not active; players spawning in lobby
        if (gameManager == null) {
            Player p = event.getPlayer();
            p.setGameMode(GameMode.ADVENTURE);
            p.sendMessage(Component.text("There is no active game right now, one should start soon!",
                    NamedTextColor.DARK_PURPLE));
        }
    }

    public static Skywars getInstance() {
        return instance;
    }

    public static void setGameManager(GameManager manager) {
        gameManager = manager;
    }

    public static void addEventListener(Listener e) {
        Bukkit.getPluginManager().registerEvents(e, instance);
    }

    public static void removeGameListener(Listener l) {
        PlayerDeathEvent.getHandlerList().unregister(l);
    }

}

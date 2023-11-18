package dev.deann;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {
    private static boolean started = false;

    public static void start() {
        started = true;
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        if (!started) return;


    }
}

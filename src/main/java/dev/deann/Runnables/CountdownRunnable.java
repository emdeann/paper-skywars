package dev.deann.Runnables;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CountdownRunnable extends BukkitRunnable {

    private final Plugin plugin;
    private final String countdownMessage;
    private final String finishedMessage;
    private int timer;

    public CountdownRunnable(Plugin plugin, int timerStart, String countdownMessage, String finishedMessage) {
        this.plugin = plugin;
        this.countdownMessage = countdownMessage;
        this.finishedMessage = finishedMessage;
        timer = timerStart;
    }

    @Override
    public void run() {
        if (timer > 0) {
            this.plugin.getServer().sendMessage(Component.text(countdownMessage + timer + "...", NamedTextColor.DARK_PURPLE));
            timer--;
        } else {
            this.plugin.getServer().sendMessage(Component.text(finishedMessage, NamedTextColor.DARK_PURPLE));
            this.cancel();
        }
    }


}

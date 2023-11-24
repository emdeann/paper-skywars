package dev.deann.Runnables;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class CountdownRunnable extends BukkitRunnable {

    private final String countdownMessage;
    private final String finishedMessage;
    private int timer;
    private final World world;

    public CountdownRunnable(int timerStart, String countdownMessage, String finishedMessage, World world) {
        this.countdownMessage = countdownMessage;
        this.finishedMessage = finishedMessage;
        this.world = world;
        timer = timerStart;
    }

    @Override
    public void run() {
        if (timer > 0) {
            world.sendMessage(Component.text(countdownMessage + timer + "...", NamedTextColor.DARK_PURPLE));
            timer--;
        } else {
            world.sendMessage(Component.text(finishedMessage, NamedTextColor.DARK_PURPLE));
            this.cancel();
        }
    }


}

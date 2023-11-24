package dev.deann.Commands;

import dev.deann.GameManager;
import dev.deann.Skywars;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StopCommand implements CommandExecutor {

    private Skywars plugin;

    public StopCommand(Skywars plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<GameManager> activeGames = plugin.getGameManagers();
        World senderWorld = ((Player) sender).getWorld();
        for (GameManager game : activeGames) {
            if (game.getActiveWorld().equals(senderWorld)) {
                game.endGame(true);
            }
        }
        return true;
    }
}

package dev.deann.Commands;

import dev.deann.GameManager;
import dev.deann.Skywars;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StopCommand implements CommandExecutor {

    private final Skywars plugin;

    public StopCommand(Skywars plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getServer().getConsoleSender().sendMessage("Stop command must be sent by a player!");
            return true;
        }

        World senderWorld = player.getWorld();
        GameManager game = plugin.getWorldToGame().get(senderWorld);
        if (game != null) {
            game.endGame(true);
            return true;
        }
        sender.sendMessage(Component.text("You aren't in an active game!", NamedTextColor.RED));
        return true;
    }
}

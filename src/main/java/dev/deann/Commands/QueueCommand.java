package dev.deann.Commands;

import dev.deann.MinigameServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QueueCommand implements CommandExecutor {

    MinigameServer plugin;

    public QueueCommand(MinigameServer plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getServer().getConsoleSender().sendMessage("Only players can queue!");
            return true;
        }

        if (plugin.playerInQueue(player)) {
            player.sendMessage(Component.text("You're already in the queue!", NamedTextColor.RED));
            return true;
        }

        if (plugin.getWorldToGame().containsKey(player.getWorld())) {
            player.sendMessage(Component.text("You're already in a game!", NamedTextColor.RED));
            return true;
        }

        player.sendMessage(Component.text("You've been added to the queue!", NamedTextColor.GREEN));
        plugin.addToQueue(player);
        return true;
    }
}

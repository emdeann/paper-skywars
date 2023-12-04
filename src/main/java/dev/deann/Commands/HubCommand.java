package dev.deann.Commands;

import dev.deann.MinigameServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HubCommand implements CommandExecutor {

    private final MinigameServer plugin;

    public HubCommand(MinigameServer plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getServer().getConsoleSender().sendMessage("Only players may use this command!");
            return true;
        }

        if (plugin.playerInGame(player)) {
            plugin.getPlayerGame(player).removePlayerFromGameServer(player);
            player.sendMessage(Component.text("Returning you to the lobby...", NamedTextColor.AQUA));
            player.setGameMode(GameMode.ADVENTURE);
            plugin.bringToLobby(player);
        } else {
            player.sendMessage(Component.text("You're already in the lobby!", NamedTextColor.RED));
        }

        return true;
    }
}

package dev.deann.Commands;
import dev.deann.GameManager;
import dev.deann.MinigameServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class StartCommand implements CommandExecutor {

    private final MinigameServer plugin;
    public StartCommand(MinigameServer plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getServer().getConsoleSender().sendMessage("Start command must be sent by a player!");
            return true;
        }
        if (plugin.getActiveGames() >= plugin.getMaxGames()) {
            sender.sendMessage(Component.text("Max games already reached!", NamedTextColor.RED));
            return true;
        }
        if (player.getWorld() != plugin.getLobbyWorld()) {
            sender.sendMessage(Component.text("Game may only be started from lobby!", NamedTextColor.RED));
            return true;
        }

        ArrayList<Player> sendToGame = new ArrayList<>(List.of(player));

        for (int i = 0; i < plugin.getMaxPlayersPerGame() - 1 && i < args.length; i++) {
            Player p = Bukkit.getPlayer(args[i]);
            if (p != null) {
                sendToGame.add(p);
            }
        }
        GameManager manager = plugin.addGameManager();
        return manager.start(sendToGame);
    }
}

package dev.deann.Commands;
import dev.deann.Managers.GameManager;
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
        if (plugin.playerInGame(player)) {
            sender.sendMessage(Component.text("Game may only be started from lobby!", NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(Component.text("A game type must be specified!", NamedTextColor.RED));
            return true;
        }

        String gameType = args[0];
        GameManager manager = plugin.addGameManager(gameType);
        if (manager == null) {
            sender.sendMessage(Component.text("Invalid game type!", NamedTextColor.RED));
            return true;
        }

        ArrayList<Player> sendToGame = new ArrayList<>(List.of(player));
        for (int i = 1; i < plugin.getMaxPlayersPerGame() - 1 && i < args.length; i++) {
            Player p = Bukkit.getPlayer(args[i]);
            if (p != null) {
                sendToGame.add(p);
            }
        }
        return manager.startGame(sendToGame);
    }
}

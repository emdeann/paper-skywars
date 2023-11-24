package dev.deann;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class StartExecutor implements CommandExecutor {

    private Skywars plugin;
    public StartExecutor(Skywars plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            Bukkit.getServer().getConsoleSender().sendMessage("Start command must be sent by a player!");
            return false;
        }
        if (plugin.getActiveGames() >= plugin.getMaxGames()) {
            sender.sendMessage(Component.text("Max games already reached!", NamedTextColor.RED));
        }
        GameManager manager = plugin.addGameManager();
        return manager.start((Player) sender);
    }
}

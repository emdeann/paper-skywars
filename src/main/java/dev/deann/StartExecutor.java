package dev.deann;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


public class StartExecutor implements CommandExecutor {

    private Skywars plugin;
    public StartExecutor(Skywars plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        GameManager manager = plugin.addGameManager();

        return manager.start(sender);
    }
}

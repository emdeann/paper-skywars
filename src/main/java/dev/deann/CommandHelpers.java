package dev.deann;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CommandHelpers {
    public static void sendMessage(Component c, CommandSender sender) {
        if (sender instanceof Player) {
            ((Player) sender).sendMessage(c);
        } else {
            Skywars.getInstance().getLogger().log(Level.ALL, c.toString());
        }
    }
}

package dev.deann;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Map.entry;

public class StartExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Logger serverLogger = Skywars.getInstance().getLogger();
        Yaml yaml = new Yaml();
        InputStream iStream;
        try {
            iStream = new BufferedInputStream(new FileInputStream("C:/Users/dtm44/Desktop/config.yml"));
        } catch (IOException e) {
            CommandHelpers.sendMessage(Component.text("An internal file error occurred"), sender);
            return false;
        }
        Map<String, ArrayList<String>> config = yaml.load(iStream);
        ArrayList<int[]> spawnLocations = parseLocations(config.get("Spawns"));
        ArrayList<int[]> chestLocations = parseLocations(config.get("Chests"));
        ArrayList<Player> players = new ArrayList(sender.getServer().getOnlinePlayers());
        if (players.size() > spawnLocations.size()) {
            CommandHelpers.sendMessage(Component.text("Too many players!", NamedTextColor.RED), sender);
            return false;
        }
        if (!(sender instanceof Player)) {
            CommandHelpers.sendMessage(Component.text("Start command must be sent by player", NamedTextColor.RED), sender);
            return false;
        }
        World world = ((Player) sender).getLocation().getWorld();
        for (int i = 0; i < sender.getServer().getOnlinePlayers().size(); i++) {
            int[] curLoc = spawnLocations.get(i);
            players.get(i).teleport(new Location(world, curLoc[0], curLoc[1], curLoc[2]));
        }
        serverLogger.log(Level.INFO, "Players sent to spawns");
        for (int[] curLoc : chestLocations) {
            Location chestLoc = new Location(world, curLoc[0], curLoc[1], curLoc[2]);
            chestLoc.getBlock().setType(Material.CHEST);
            Chest chest = (Chest) chestLoc.getBlock().getState();
            chest.update();
            chest.getBlockInventory().setItem(12, new ItemStack(Material.GOAT_HORN, 23));
        }
        serverLogger.log(Level.INFO, "Chests Set");

        return true;
    }

    private ArrayList<int[]> parseLocations(ArrayList<String> locations) {
        ArrayList<int[]> arr = new ArrayList<>();
        for (String s : locations) {
            String[] coordStrs = s.split(" ");
            int[] cur = new int[3];
            for (int i = 0; i < coordStrs.length; i++) {
                cur[i] = Integer.valueOf(coordStrs[i]);
            }
            arr.add(cur);
        }
        return arr;
    }
}

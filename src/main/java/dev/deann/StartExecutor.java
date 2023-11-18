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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Map.entry;

public class StartExecutor implements CommandExecutor {

    // Chest filling
    private final double STARTER_PROBABILITY = 0.035;
    private final double PROBABILITY_MOD = 0.005;

    private final Material[] availableSwords = {Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD};
    private final Material[] availableAxes = {Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE};
    private final Material[] availableTools = {Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE,
            Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE};
    private final Material[] availableHelmets = {Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET};
    private final Material[] availableChestplates = {Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE};
    private final Material[] availableLeggings = {Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS};
    private final Material[] availableBoots = {Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS};
    private final Material[] availableBlocks = {Material.CRIMSON_PLANKS, Material.STONE, Material.RED_SANDSTONE};

    private final Material[][] availableItems = {availableSwords, availableAxes, availableTools, availableHelmets,
                                        availableChestplates, availableLeggings, availableBoots, availableBlocks};
    // Parallel to availableItems
    private final int[] MAX_ITEMS = {2, 1, 1, 1, 1, 1, 1, 4};
    private int[] setItems = {0, 0, 0, 0, 0, 0, 0, 0};

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
            Inventory chestInventory = chest.getBlockInventory();
            chestInventory.clear();
            resetSetItems(setItems);
            for (int curInvSlot = 0; curInvSlot < 27; curInvSlot++) {
                boolean itemSet = false;
                for (int curItemSet = 0; curItemSet < availableItems.length && !itemSet; curItemSet++) {
                    Material[] curItemsSet = availableItems[curItemSet];
                    for (int curItem = 0; curItem < curItemsSet.length && (setItems[curItemSet] < MAX_ITEMS[curItemSet]); curItem++) {
                        double probability = STARTER_PROBABILITY - curItem * PROBABILITY_MOD;
                        if (Arrays.equals(curItemsSet, availableBlocks)) {
                            // Boosted odds for blocks
                            probability += 2 * STARTER_PROBABILITY;
                        }
                        if (Math.random() < probability) {
                            ItemStack item = new ItemStack(curItemsSet[curItem]);
                            if (item.getMaxStackSize() > 1) {
                                item.setAmount((int) (Math.random() * item.getMaxStackSize()));
                            }
                            chestInventory.setItem(curInvSlot, item);
                            setItems[curItemSet]++;
                            itemSet = true;
                            break;
                        }
                    }
                }
            }
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

    private void resetSetItems(int[] setItems) {
        for (int i = 0; i < setItems.length; i++) {
            setItems[i] = 0;
        }
    }
}

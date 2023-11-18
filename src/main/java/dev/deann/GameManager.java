package dev.deann;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("ALL")
public class GameManager {

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
    private final int[] setItems = {0, 0, 0, 0, 0, 0, 0, 0};

    private Logger serverLogger;

    private DeathEvent deathListener;

    public GameManager() {
        serverLogger = Skywars.getInstance().getLogger();
    }
    public boolean start(CommandSender sender) {
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
        ArrayList<Player> players = new ArrayList<>(sender.getServer().getOnlinePlayers());
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
            Player p = players.get(i);
            p.setFoodLevel(20);
            p.setHealth(20);
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(new Location(world, curLoc[0], curLoc[1], curLoc[2]));
        }
        serverLogger.log(Level.INFO, "Players sent to spawns");
        setChests(world, chestLocations);
        serverLogger.log(Level.INFO, "Chests Set");
        deathListener = new DeathEvent(players, this);
        Skywars.addEventListener(deathListener);
        serverLogger.log(Level.INFO, "Death events being watched");
        return true;
    }

    public void endGame(Player winner) {
        Skywars.removeDeathListener(deathListener);
        winner.getServer().sendMessage(Component.text(winner.getName() + " has won the game!", NamedTextColor.GREEN));
    }

    // Helpers
    private ArrayList<int[]> parseLocations(ArrayList<String> locations) {
        ArrayList<int[]> arr = new ArrayList<>();
        for (String s : locations) {
            String[] coordStrs = s.split(" ");
            int[] cur = new int[3];
            for (int i = 0; i < coordStrs.length; i++) {
                cur[i] = Integer.parseInt(coordStrs[i]);
            }
            arr.add(cur);
        }
        return arr;
    }

    private void setChests(World world, ArrayList<int[]> chestLocations) {
        for (int[] curLoc : chestLocations) {
            Location chestLoc = new Location(world, curLoc[0], curLoc[1], curLoc[2]);
            chestLoc.getBlock().setType(Material.CHEST);
            Chest chest = (Chest) chestLoc.getBlock().getState();
            chest.update();
            Inventory chestInventory = chest.getBlockInventory();
            chestInventory.clear();
            Arrays.fill(setItems, 0);
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
    }


}

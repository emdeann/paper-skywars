package dev.deann.Managers;

import dev.deann.Enum.GameState;
import dev.deann.Enum.GameType;
import dev.deann.GameHelpers;
import dev.deann.MinigameServer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class ChestsSpawnsGameManager extends GameManager {

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

    public ChestsSpawnsGameManager(MinigameServer plugin, GameType gameType) {
        super(plugin, gameType);
    }

    @Override
    public boolean startGame(ArrayList<Player> allPlayers) {
        super.startGame(allPlayers);
        ArrayList<int[]> spawnLocations = GameHelpers.parseLocations(config.getStringList("spawns"));
        // BASE
        ArrayList<int[]> chestLocations = GameHelpers.parseLocations(config.getStringList("chests"));
        Material cageMaterial = Material.GLASS;
        for (int i = 0; i < playersInGameServer.size(); i++) {
            int[] curLoc = spawnLocations.get(i);
            Player p = playersInGameServer.get(i);
            plugin.removeFromQueue(p, gameType);
            p.setFoodLevel(20);
            p.setHealth(20);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            serverLogger.log(Level.INFO, "Players sent to cages");
            Location toTeleport = new Location(activeWorld, curLoc[0], curLoc[1], curLoc[2]);
            p.teleport(new Location(activeWorld, toTeleport.getBlockX() + 0.5, toTeleport.getBlockY(),
                    toTeleport.getBlockZ() + 0.5));
            setCageBlocks(p, cageMaterial, Material.AIR);
        }
        super.runCountDown(new BukkitRunnable() {
            @Override
            public void run() {
                gameState = GameState.ACTIVE;
                for (Player p : playersInGameServer) {
                    setCageBlocks(p, Material.AIR, cageMaterial);
                }
            }
        });

        setChests(activeWorld, chestLocations);
        serverLogger.info("Chests Set");
        return true;
    }

    @Override
    public void endGame(boolean force) {
        super.endGame(force);
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
                        double PROBABILITY_MOD = 0.005;
                        double STARTER_PROBABILITY = 0.035;
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

    private ArrayList<Block> getCageBlocks(Player player, Material replaceable) {
        ArrayList<Block> cageList = new ArrayList<>();
        Block[] offLimits = {player.getLocation().getBlock(), player.getLocation().add(0, 1, 0).getBlock()};
        for (int i = 0; i < 2; i++) {
            Block curBlock = offLimits[i];
            BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
            for (BlockFace face : faces) {
                Block newBlock = curBlock.getRelative(face);
                if (newBlock.getType() == replaceable && !newBlock.equals(offLimits[i ^ 1])) {
                    cageList.add(newBlock);
                }
            }
        }
        return cageList;
    }


    private void setCageBlocks(Player player, Material setTo, Material replace) {
        for (Block b : getCageBlocks(player, replace)) {
            b.setType(setTo);
        }
    }
}
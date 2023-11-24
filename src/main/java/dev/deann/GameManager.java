package dev.deann;

import dev.deann.Enum.GameState;
import dev.deann.Runnables.CountdownRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


// GameManager handles the main setup and execution of the game
// Certain events are handled by the GameEventListener, which
// calls back to the manager
public class GameManager {

    private final FileConfiguration config;
    private final String TEMPLATE_FOLDER;

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

    private final Skywars plugin;
    private final Logger serverLogger;
    private GameState gameState;
    private final World activeWorld;
    private ArrayList<Player> activePlayers;
    private ArrayList<Player> spectators;
    private ArrayList<Player> playersInGameServer;
    private BukkitTask countdownTask;
    public GameManager(Skywars plugin) {
        this.plugin = plugin;
        serverLogger = plugin.getLogger();
        config = plugin.getConfig();
        TEMPLATE_FOLDER = plugin.getTemplateName();
        activeWorld = resetMap("skywars-" + System.currentTimeMillis());
    }

    // @param allPlayers should not be used directly as it may change
    // i.e. when the plugin queue is passed to start the game
    public boolean start(ArrayList<Player> allPlayers) {
        gameState = GameState.SETUP;
        activePlayers = new ArrayList<>(allPlayers);
        playersInGameServer = new ArrayList<>(allPlayers);
        spectators = new ArrayList<>();
        ArrayList<int[]> spawnLocations = parseLocations(config.getStringList("Spawns"));
        ArrayList<int[]> chestLocations = parseLocations(config.getStringList("Chests"));
        Material cageMaterial = Material.GLASS;
        for (int i = 0; i < playersInGameServer.size(); i++) {
            int[] curLoc = spawnLocations.get(i);
            Player p = playersInGameServer.get(i);
            plugin.removeFromQueue(p);
            p.setFoodLevel(20);
            p.setHealth(20);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            Location toTeleport = new Location(activeWorld, curLoc[0], curLoc[1], curLoc[2]);
            p.teleport(new Location(activeWorld, toTeleport.getBlockX() + 0.5, toTeleport.getBlockY(),
                    toTeleport.getBlockZ() + 0.5));
            setCageBlocks(p, cageMaterial, Material.AIR);
        }
        serverLogger.log(Level.INFO, "Players sent to spawns");
        setChests(activeWorld, chestLocations);
        serverLogger.log(Level.INFO, "Chests Set");
        serverLogger.log(Level.INFO, "Death events being watched");

        String countdownMessage = "Skywars starting in ", finishedMessage = "Skywars started!";
        int countdownLength = 5;
        countdownTask = new CountdownRunnable(countdownLength, countdownMessage, finishedMessage, activeWorld)
                .runTaskTimer(plugin, 0, 20);
        gameState = GameState.COUNTDOWN;
        new BukkitRunnable() {
            @Override
            public void run() {
                gameState = GameState.ACTIVE;
                for (Player p : playersInGameServer) {
                    setCageBlocks(p, Material.AIR, cageMaterial);
                }
            }
        }.runTaskLater(plugin, 20 * countdownLength);
        return true;
    }

    public void endGame(boolean force) {
        int countDownTimer = (force) ? 3 : 10;
        gameState = GameState.FINISHED;
        if (force) {
            activeWorld.sendMessage(Component.text("This game has been stopped", NamedTextColor.RED));
            serverLogger.log(Level.INFO, "Game at " + activeWorld.getName() + " has been force shutdown");
        }
        else {
            Player winner = activePlayers.get(0);
            serverLogger.log(Level.INFO, "Game at " + activeWorld.getName() + " ending");
            // Have to delay messages slightly longer than respawn, so the dying player still sees them
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> activeWorld.sendMessage(
                    Component.text(winner.getName() + " has won the game!", NamedTextColor.GREEN)), 3);
        }
        if (gameState == GameState.COUNTDOWN) {
            countdownTask.cancel();
        }
        new CountdownRunnable( countDownTimer, "Returning to lobby in ",
                "Returning to lobby!", activeWorld).runTaskTimer(plugin, 3, 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : playersInGameServer) {
                    p.setGameMode(GameMode.ADVENTURE);
                    p.getInventory().clear();
                    p.teleport(plugin.getLobbyWorld().getSpawnLocation());
                }
                removeWorld(activeWorld);
                serverLogger.log(Level.INFO, "Players returned to lobby");
            }
        }.runTaskLater(plugin, 20 * countDownTimer);

        plugin.removeGameManager(this);
    }

    public GameState getGameState() {
        return gameState;
    }
    public World getActiveWorld() {
        return activeWorld;
    }

    public void removeActivePlayer(Player player) {
        activePlayers.remove(player);
        if (activePlayers.size() == 1) {
            endGame(false);
        }
    }

    public void addSpectator(Player player) {
        spectators.add(player);
    }

    // Helpers
    private ArrayList<int[]> parseLocations(List<String> locations) {
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

    private void removeWorld(World world) {
        try {
            Bukkit.unloadWorld(world, false);
            FileUtils.deleteDirectory(world.getWorldFolder());
        } catch (IOException e) {
            serverLogger.log(Level.WARNING, "Error deleting old world file");
        }
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


    private World resetMap(String newWorldName) {
       copyFileStructure(new File(Bukkit.getWorldContainer(), TEMPLATE_FOLDER),
               new File(Bukkit.getWorldContainer(), newWorldName));
       new WorldCreator(newWorldName).createWorld();
       World newWorld = Bukkit.getWorld(newWorldName);
       if (newWorld != null) {
       newWorld.setAutoSave(false);
       }
       return newWorld;
    }

    private void copyFileStructure(File source, File target){
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock"));
            if(!ignore.contains(source.getName())) {
                if(source.isDirectory()) {
                    if(!target.exists())
                        if (!target.mkdirs())
                            throw new IOException("Couldn't create world directory!");
                    String[] files = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFileStructure(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

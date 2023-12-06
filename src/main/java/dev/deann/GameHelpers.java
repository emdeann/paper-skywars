package dev.deann;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameHelpers {

    public static ArrayList<int[]> parseLocations(List<String> locations) {
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

    public static void removeWorld(World world, Logger serverLogger) {
        try {
            Bukkit.unloadWorld(world, false);
            FileUtils.deleteDirectory(world.getWorldFolder());
        } catch (IOException e) {
            serverLogger.log(Level.WARNING, "Error deleting old world file");
        }
    }

    public static World resetMap(String newWorldName, String template) {
        copyFileStructure(new File(Bukkit.getWorldContainer(), template),
                new File(Bukkit.getWorldContainer(), newWorldName));
        new WorldCreator(newWorldName)
                .generateStructures(false)
                .environment(World.Environment.CUSTOM)
                .createWorld();
        World newWorld = Bukkit.getWorld(newWorldName);
        if (newWorld != null) {
            newWorld.setAutoSave(false);
            newWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            newWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            newWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }
        return newWorld;
    }

    private static void copyFileStructure(File source, File target){
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

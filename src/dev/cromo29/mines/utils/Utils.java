package dev.cromo29.mines.utils;

import dev.cromo29.durkcore.util.Cuboid;
import dev.cromo29.mines.object.Mine;
import dev.cromo29.mines.object.MineBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static long getCurrentBlocksFrom(Mine mine) {
        long blocks = 0;

        for (Block block : getBlocksBetweenLocations(mine.getStart(), mine.getEnd())) {
            for (MineBlock mineBlock : mine.getMineBlocks()) {
                if (block.getType() == mineBlock.getMaterial() && block.getData() == mineBlock.getData()) {
                    blocks++;
                    break;
                }
            }
        }

        return blocks;
    }

    public static boolean containsLocationBetwen(final Location toCheck, final Location a, final Location b) {
        if (toCheck == null || a == null || b == null) return false;

        return new Cuboid(a, b).contains(toCheck);
    }

    public static List<Block> getBlocksBetweenLocations(final Location a, final Location b) {
        if (a == null || b == null) return new ArrayList<>();

        return new Cuboid(a, b).getBlocks();
    }

    public static List<Player> getPlayersBetweenLocations(final Location a, final Location b) {
        if (a == null || b == null) return new ArrayList<>();

        final List<Player> players = new ArrayList<>();

        if (a.getWorld().getEntities().isEmpty()) return new ArrayList<>();

        for (Player player : a.getWorld().getEntitiesByClass(Player.class)) {

            if (containsLocationBetwen(player.getLocation().getBlock().getLocation(), a, b)) players.add(player);
        }

        return players;
    }
}

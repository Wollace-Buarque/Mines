package dev.cromo29.mines.utils;

import dev.cromo29.durkcore.Util.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static boolean containsLocationBetwen(final Location toCheck, final Location a, final Location b) {
        return new Cuboid(a, b).contains(toCheck);
    }

    public static List<Block> getBlocksBetweenLocations(final Location a, final Location b) {
        return new Cuboid(a, b).getBlocks();
    }

    public static List<Player> getPlayersBetweenLocations(final Location a, final Location b) {
        final List<Player> players = new ArrayList<>();

        if (a.getWorld().getEntities().isEmpty()) return new ArrayList<>();

        for (Player player : a.getWorld().getEntitiesByClass(Player.class)) {

            if (containsLocationBetwen(player.getLocation().getBlock().getLocation(), a, b)) players.add(player);
        }

        return players;
    }
}

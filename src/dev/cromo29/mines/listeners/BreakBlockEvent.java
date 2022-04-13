package dev.cromo29.mines.listeners;

import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.object.Mine;
import dev.cromo29.mines.service.MineServiceImpl;
import dev.cromo29.mines.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakBlockEvent implements Listener {

    private final MinePlugin plugin;

    public BreakBlockEvent(MinePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void breakMineBlock(BlockBreakEvent event) {

        if (event.isCancelled()) return;

        MineServiceImpl mineService = plugin.getMineService();

        if (mineService.getMines().isEmpty()) return;

        Block block = event.getBlock();

        Mine mine = mineService.getMine(block.getLocation());

        if (mine == null) return;

        mine.setCurrentBlocks(Utils.getBlocksBetweenLocations(mine.getStart(), mine.getEnd())
                .stream()
                .filter(blockFilter -> blockFilter != null && blockFilter.getType() != Material.AIR)
                .count());

        if (mine.getCurrentPercentage() <= mine.getResetPercentage()) mineService.resetMine(mine);

    }
}

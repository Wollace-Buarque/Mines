package dev.cromo29.mines.listeners;

import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.objects.Mine;
import dev.cromo29.mines.service.IMineService;
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

        IMineService mineService = plugin.getMineService();

        if (mineService.getMines().isEmpty()) return;

        Block block = event.getBlock();

        Mine mine = mineService.getMine(block.getLocation());

        if (mine == null) return;

//        int currentBlocks = 0; // Removido STREAM por problemas de desempenhos no Java 8
//        for (Block forBlock : Utils.getBlocksBetweenLocations(mine.getStart(), mine.getEnd())) {
//            if (forBlock != null && forBlock.getType() != Material.AIR) currentBlocks++;
//        }

        mine.setCurrentBlocks(mine.getCurrentBlocks() - 1);

        if (mine.getCurrentPercentage() <= mine.getResetPercentage()) mineService.resetMine(mine);
    }
}

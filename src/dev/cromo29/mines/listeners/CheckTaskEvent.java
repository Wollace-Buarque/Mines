package dev.cromo29.mines.listeners;

import dev.cromo29.durkcore.updater.UpdateType;
import dev.cromo29.durkcore.updater.event.UpdaterEvent;
import dev.cromo29.mines.objects.Mine;
import dev.cromo29.mines.service.IMineService;
import dev.cromo29.mines.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CheckTaskEvent implements Listener {

    private final IMineService mineService;

    public CheckTaskEvent(IMineService mineService) {
        this.mineService = mineService;
    }

    @EventHandler
    public void updaterEvent(UpdaterEvent event) {

        if (event.getType() != UpdateType.MIN_08) return;

        for (Mine mine : mineService.getMines()) {

            mine.setCurrentBlocks(Utils.getCurrentBlocksFrom(mine));

            if (mine.getCurrentPercentage() <= mine.getResetPercentage()) mineService.resetMine(mine);
        }

    }
}

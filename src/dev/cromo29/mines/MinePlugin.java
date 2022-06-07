package dev.cromo29.mines;

import dev.cromo29.durkcore.api.DurkPlugin;
import dev.cromo29.mines.command.MineCommand;
import dev.cromo29.mines.listeners.BreakBlockEvent;
import dev.cromo29.mines.listeners.CheckTaskEvent;
import dev.cromo29.mines.listeners.InteractEvent;
import dev.cromo29.mines.listeners.QuitEvent;
import dev.cromo29.mines.managers.MessageManager;
import dev.cromo29.mines.managers.MineManager;
import dev.cromo29.mines.service.IMineService;

public class MinePlugin extends DurkPlugin {

    private MineManager mineManager;

    @Override
    public void onStart() {

        this.mineManager = new MineManager();

        this.registerCommand(new MineCommand(this));
        this.setListeners(new BreakBlockEvent(this), new InteractEvent(this),
                new QuitEvent(this), new CheckTaskEvent(getMineService()));

        getMineService().loadAll();

        mineManager.hologramTask();
    }

    @Override
    public void onStop() {

        getMineService().getMines().forEach(mine -> {
            if (mine.getHologram() == null) return;

            mine.getHologram().clear();
            mine.getHologram().getHologramLines().forEach(hologramLine -> hologramLine.getArmorStand().remove());
        });

    }

    public static MinePlugin get() {
        return getPlugin(MinePlugin.class);
    }


    public MineManager getMineManager() {
        return mineManager;
    }

    public MessageManager getMessageManager() {
        return mineManager.getMessageManager();
    }


    public IMineService getMineService() {
        return mineManager.getMineService();
    }
}

package dev.cromo29.mines;

import dev.cromo29.durkcore.API.DurkPlugin;
import dev.cromo29.mines.command.MineCommand;
import dev.cromo29.mines.listeners.BreakBlockEvent;
import dev.cromo29.mines.listeners.InteractEvent;
import dev.cromo29.mines.listeners.QuitEvent;
import dev.cromo29.mines.manager.MessageManager;
import dev.cromo29.mines.manager.MineManager;
import dev.cromo29.mines.service.IMineService;

public class MinePlugin extends DurkPlugin {

    private MineManager mineManager;

    @Override
    public void onStart() {

        this.mineManager = new MineManager();

        this.registerCommand(new MineCommand(this));
        this.setListeners(new BreakBlockEvent(this), new InteractEvent(this), new QuitEvent(this));

        getMineService().loadAll();

        mineManager.hologramTask();
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

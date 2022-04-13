package dev.cromo29.mines.listeners;

import dev.cromo29.mines.MinePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitEvent implements Listener {

    private final MinePlugin plugin;

    public QuitEvent(MinePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getMineManager().getSetupMap().remove(event.getPlayer().getName().toLowerCase());
    }
}

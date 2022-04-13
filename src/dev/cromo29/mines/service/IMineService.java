package dev.cromo29.mines.service;

import dev.cromo29.mines.object.Mine;
import org.bukkit.Location;

import java.util.List;

public interface IMineService {

    /**
     * Set the Mine in map.
     * @param mine object
     */
    void setMine(Mine mine);

    /**
     * Remove the Mine from map.
     * @param mine object
     */
    void deleteMine(Mine mine);

    /**
     * Get all mines from server.
     */
    List<Mine> getMines();

    /**
     * Get the Mine by name from map.
     * @param name from Mine.
     */
    Mine getMine(String name);

    /**
     * Get the Mine from location.
     * @param location to check
     */
    Mine getMine(Location location);

    /**
     * Check if exists an Mine with name.
     * @param name to check if exists an Mine.
     */
    boolean hasMine(String name);

    /**
     * Check if exists an mine at location.
     * @param location to check if exists an Mine.
     */
    boolean hasMine(Location location);

    /**
     * Save the mine to file.
     * @param mine object
     * @param async Save in aSync mode.
     */
    void saveMine(Mine mine, boolean async);

    /**
     * Remove the mine from file.
     * @param mine object
     * @param async Remove in aSync mode.
     */
    void removeMine(Mine mine, boolean async);

    /**
     * Reset a Mine.
     * @param mine to be reseted.
     */
    void resetMine(Mine mine);

    /**
     * Clear all blocks from the mine.
     * @param mine to be cleaned.
     */
    void clearMine(Mine mine);

    /**
     * Load all mines from storage.
     */
    void loadAll();
}

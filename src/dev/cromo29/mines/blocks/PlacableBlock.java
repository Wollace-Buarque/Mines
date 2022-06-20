package dev.cromo29.mines.blocks;

import dev.cromo29.mines.utils.Paste;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.UUID;

public class PlacableBlock implements Workdload {

    private final UUID worldID;
    private final int blockX, blockY, blockZ;
    private final Material type;
    private final byte data;

    public PlacableBlock(UUID worldID, int blockX, int blockY, int blockZ, Material type, byte data) {
        this.worldID = worldID;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.type = type;
        this.data = data;
    }

    @Override
    public void compute() {
        //  Bukkit.getWorld(worldID).getBlockAt(blockX, blockY, blockZ).setTypeIdAndData(type.getId(), data, true);
        Paste.rapidSetBlock(Paste.getWorldHandle(Bukkit.getWorld(worldID)), Paste.getBlockData(type.getId(), data), blockX, blockY, blockZ);
    }
}

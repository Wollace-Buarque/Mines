package dev.cromo29.mines.utils;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

/**
 * Utility class designed for block placing a various speeds
 * depending on the stability that is needed.
 * <p>
 * * Where I got these ideas:
 *
 * @See https://www.spigotmc.org/threads/methods-for-changing-massive-amount-of-blocks-up-to-14m-blocks-s.395868/
 */
public class Paste {
    public static int test = 10;

    /**
     * Method of placing blocks that is roughly 3650% the speed of
     * the APIs setBlock method.
     * Packet needs to be sent after and chunk needs to be reloaded to show
     * the client the changes.
     *
     * @param world     the target world.
     * @param x         location via x axis.
     * @param y         location via y axis.
     * @param z         location via z axis.
     * @param blockData the block data to be set.
     */
    public static void rapidSetBlock(net.minecraft.server.v1_8_R3.World world, IBlockData blockData, int x, int y, int z) {
        final Chunk chunk = getChunkAt(world, x, z);
        final BlockPosition blockPosition = getBlockPosition(x, y, z);
        chunk.a(blockPosition, blockData);
        updateChange(world, blockPosition);
    }

    /**
     * Notifies a block update so that player can see.
     *
     * @param world         the world.
     * @param blockPosition the block.
     */
    private static void updateChange(net.minecraft.server.v1_8_R3.World world, BlockPosition blockPosition) {
        //world.c(EnumSkyBlock.BLOCK, blockPosition); //Fixes light but laggy.
        world.notify(blockPosition);
    }

    /**
     * Gets the chunk at a block's location.
     *
     * @param nmsWorld the handle of the target Bukkit world.
     * @param blockX   the x location of the block (NOT chunk coordinates).
     * @param blockZ   the z location of the block (NOT chunk coordinates).
     * @return the nms chunk.
     */
    public static Chunk getChunkAt(net.minecraft.server.v1_8_R3.World nmsWorld, int blockX, int blockZ) {
        return nmsWorld.getChunkAt(toChunkCoordinate(blockX), toChunkCoordinate(blockZ));
    }

    /**
     * Returns the chunk coordinate value of a block coordinate.
     *
     * @param blockCoordinate the block coordinate.
     * @return the chunk coordinate in respect to that block.
     */
    public static int toChunkCoordinate(final int blockCoordinate) {
        return blockCoordinate >> 4;
    }

    /**
     * Short hand way to get the block data.
     *
     * @param blockId the block id.
     * @param data    the block data.
     * @return an IBlockData object relative to the passed parameters.
     */
    public static IBlockData getBlockData(int blockId, byte data) {
        return net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockId + (data << 12));
    }

    /**
     * Short hand way to get the handle of the Bukkit world.
     *
     * @param world the target Bukkit world.
     * @return an NMS world.
     */
    public static net.minecraft.server.v1_8_R3.World getWorldHandle(World world) {
        return ((CraftWorld) world).getHandle();
    }

    /**
     * Gets a BlockPosition object at the specific block coordinates.
     *
     * @param x the x position.
     * @param y the y position.
     * @param z the z position.
     * @return a new BlockPosition object reference with the target coords.
     */
    public static BlockPosition getBlockPosition(int x, int y, int z) {
        return new BlockPosition(x, y, z);
    }

}

package com.chiorichan.bukkit.plugin;

import java.util.Set;

import org.bukkit.World;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public interface Region extends Iterable<BlockVector>, Cloneable {
    /**
     * Get the lower point of a region.
     *
     * @return min. point
     */
    public Vector getMinimumPoint();

    /**
     * Get the upper point of a region.
     *
     * @return max. point
     */
    public Vector getMaximumPoint();

    /**
     * Get the number of blocks in the region.
     *
     * @return number of blocks
     */
    public int getArea();

    /**
     * Get X-size.
     *
     * @return width
     */
    public int getWidth();

    /**
     * Get Y-size.
     *
     * @return height
     */
    public int getHeight();

    /**
     * Get Z-size.
     *
     * @return length
     */
    public int getLength();

    /**
     * Returns true based on whether the region contains the point,
     *
     * @param pt
     * @return
     */
    public boolean contains(Vector pt);

    /**
     * Get a list of chunks.
     *
     * @return
     */
    public Set<Vector2D> getChunks();

    /**
     * Return a list of 16*16*16 chunks in a region
     *
     * @return The chunk cubes this region overlaps with
     */
    public Set<Vector> getChunkCubes();

    public World getWorld();
    public void setWorld(World world);
}

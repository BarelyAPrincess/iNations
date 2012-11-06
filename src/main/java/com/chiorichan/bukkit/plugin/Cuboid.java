package com.chiorichan.bukkit.plugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class Cuboid implements Region {
	protected World world;
	private Vector pos1;
	private Vector pos2;
	
	public Cuboid (Vector pos1, Vector pos2)
	{
		this(null, pos1, pos2);
	}
	
	public Cuboid (World w, Vector pos1, Vector pos2)
	{
		this.world = w;
		this.pos1 = pos1;
		this.pos2 = pos2;
	}
	
	public Vector getMinimumPoint() {
		return new Vector(Math.min(pos1.getX(), pos2.getX()),
	        		Math.min(pos1.getY(), pos2.getY()),
	        		Math.min(pos1.getZ(), pos2.getZ()));
	}

	 public Vector getMaximumPoint() {
		 return new Vector(Math.max(pos1.getX(), pos2.getX()),
				 Math.max(pos1.getY(), pos2.getY()),
				 Math.max(pos1.getZ(), pos2.getZ()));
	 }
	 
	 public int getArea() {
		 Vector min = getMinimumPoint();
		 Vector max = getMaximumPoint();

		 return (int)((max.getX() - min.getX() + 1) *
				 (max.getY() - min.getY() + 1) *
				 (max.getZ() - min.getZ() + 1));
	 }
	 
	 public int getWidth() {
		 Vector min = getMinimumPoint();
		 Vector max = getMaximumPoint();

		 return (int) (max.getX() - min.getX() + 1);
	 }
	 
	 public int getHeight() {
		 Vector min = getMinimumPoint();
		 Vector max = getMaximumPoint();

		 return (int) (max.getY() - min.getY() + 1);
	 }
	 
	 public int getLength() {
		 Vector min = getMinimumPoint();
		 Vector max = getMaximumPoint();
		 
		 return (int) (max.getZ() - min.getZ() + 1);
	 }
	 
	 public void expand(Vector... changes) {
        for (Vector change : changes) {
            if (change.getX() > 0) {
                if (Math.max(pos1.getX(), pos2.getX()) == pos1.getX()) {
                    pos1 = pos1.add(new Vector(change.getX(), 0, 0));
                } else {
                    pos2 = pos2.add(new Vector(change.getX(), 0, 0));
                }
            } else {
                if (Math.min(pos1.getX(), pos2.getX()) == pos1.getX()) {
                    pos1 = pos1.add(new Vector(change.getX(), 0, 0));
                } else {
                    pos2 = pos2.add(new Vector(change.getX(), 0, 0));
                }
            }

            if (change.getY() > 0) {
                if (Math.max(pos1.getY(), pos2.getY()) == pos1.getY()) {
                    pos1 = pos1.add(new Vector(0, change.getY(), 0));
                } else {
                    pos2 = pos2.add(new Vector(0, change.getY(), 0));
                }
            } else {
                if (Math.min(pos1.getY(), pos2.getY()) == pos1.getY()) {
                    pos1 = pos1.add(new Vector(0, change.getY(), 0));
                } else {
                    pos2 = pos2.add(new Vector(0, change.getY(), 0));
                }
            }

            if (change.getZ() > 0) {
                if (Math.max(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                    pos1 = pos1.add(new Vector(0, 0, change.getZ()));
                } else {
                    pos2 = pos2.add(new Vector(0, 0, change.getZ()));
                }
            } else {
                if (Math.min(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                    pos1 = pos1.add(new Vector(0, 0, change.getZ()));
                } else {
                    pos2 = pos2.add(new Vector(0, 0, change.getZ()));
                }
            }
        }

        recalculate();
    }

    public void contract(Vector... changes) {
        for (Vector change : changes) {
            if (change.getX() < 0) {
                if (Math.max(pos1.getX(), pos2.getX()) == pos1.getX()) {
                    pos1 = pos1.add(new Vector(change.getX(), 0, 0));
                } else {
                    pos2 = pos2.add(new Vector(change.getX(), 0, 0));
                }
            } else {
                if (Math.min(pos1.getX(), pos2.getX()) == pos1.getX()) {
                    pos1 = pos1.add(new Vector(change.getX(), 0, 0));
                } else {
                    pos2 = pos2.add(new Vector(change.getX(), 0, 0));
                }
            }

            if (change.getY() < 0) {
                if (Math.max(pos1.getY(), pos2.getY()) == pos1.getY()) {
                    pos1 = pos1.add(new Vector(0, change.getY(), 0));
                } else {
                    pos2 = pos2.add(new Vector(0, change.getY(), 0));
                }
            } else {
                if (Math.min(pos1.getY(), pos2.getY()) == pos1.getY()) {
                    pos1 = pos1.add(new Vector(0, change.getY(), 0));
                } else {
                    pos2 = pos2.add(new Vector(0, change.getY(), 0));
                }
            }

            if (change.getZ() < 0) {
                if (Math.max(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                    pos1 = pos1.add(new Vector(0, 0, change.getZ()));
                } else {
                    pos2 = pos2.add(new Vector(0, 0, change.getZ()));
                }
            } else {
                if (Math.min(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                    pos1 = pos1.add(new Vector(0, 0, change.getZ()));
                } else {
                    pos2 = pos2.add(new Vector(0, 0, change.getZ()));
                }
            }
        }

        recalculate();
    }

    private void recalculate() {
    	int max = (world == null) ? 255 : world.getMaxHeight();
    	
    	pos1 = new Vector(pos1.getBlockX(), Math.max(0, Math.min(max, pos1.getBlockY())), pos1.getBlockZ());
    	pos2 = new Vector(pos2.getBlockX(), Math.max(0, Math.min(max, pos2.getBlockY())), pos2.getBlockZ());
    }

    public void shift(Vector change) throws ChiorichanException {
        pos1 = pos1.add(change);
        pos2 = pos2.add(change);

        recalculate();
    }
	 
	 public Vector getPos1() {
		 return pos1;
	 }

	 public void setPos1(Vector pos1) {
		 this.pos1 = pos1;
	 }

	 public Vector getPos2() {
		 return pos2;
	 }

	 public void setPos2(Vector pos2) {
		 this.pos2 = pos2;
	 }
	 
	 public Set<Vector2D> getChunks() {
		 Set<Vector2D> chunks = new HashSet<Vector2D>();

		 Vector min = getMinimumPoint();
		 Vector max = getMaximumPoint();

		 for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
			 for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
				 chunks.add(new Vector2D(x >> ChunkStore.CHUNK_SHIFTS,
				 z >> ChunkStore.CHUNK_SHIFTS));
			 }
		 }

		 return chunks;
	 }

	 public Set<Vector> getChunkCubes() {
		 Set<Vector> chunks = new HashSet<Vector>();

		 Vector min = getMinimumPoint();
		 Vector max = getMaximumPoint();

		 for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
			 for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
				 for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
					 chunks.add(new BlockVector(x >> ChunkStore.CHUNK_SHIFTS,
					 y >> ChunkStore.CHUNK_SHIFTS, z >> ChunkStore.CHUNK_SHIFTS));
				 }
			 }
		 }

		 return chunks;
	 }
	 
	 public boolean contains(Vector pt)
	 {
		 double x = pt.getX();
		 double y = pt.getY();
		 double z = pt.getZ();

		 Vector min = getMinimumPoint();
		 Vector max = getMaximumPoint();

		 return x >= min.getBlockX() && x <= max.getBlockX()
				 && y >= min.getBlockY() && y <= max.getBlockY()
				 && z >= min.getBlockZ() && z <= max.getBlockZ();
	 }
	 
	 public boolean intersects (Cuboid cube)
	 {
		 Iterator<BlockVector> blocks = cube.iterator();
		 
		 while (blocks.hasNext())
		 {
			 BlockVector block = blocks.next();
			 if ( this.contains(block) )
			 {
				 return true;
			 }
		 }
		 
		 return false;
	 }
	 
	 public Iterator<BlockVector> iterator() {
		 return new Iterator<BlockVector>() {
			 private Vector min = getMinimumPoint();
			 private Vector max = getMaximumPoint();
			 private int nextX = min.getBlockX();
			 private int nextY = min.getBlockY();
			 private int nextZ = min.getBlockZ();

			 public boolean hasNext() {
				 return (nextX != Integer.MIN_VALUE);
			 }

			 public BlockVector next() {
				 if (!hasNext()) throw new java.util.NoSuchElementException();
				 BlockVector answer = new BlockVector(nextX, nextY, nextZ);
				 if (++nextX > max.getBlockX()) {
					 nextX = min.getBlockX();
					 if (++nextY > max.getBlockY()) {
						 nextY = min.getBlockY();
						 if (++nextZ > max.getBlockZ()) {
							 nextX = Integer.MIN_VALUE;
						 }
					 }
				 }
				 return answer;
			 }

			 public void remove() {
				 throw new UnsupportedOperationException();
			 }
		 };
	 }
	 
	 public Iterable<Vector2D> asFlatRegion() {
		 return new Iterable<Vector2D>() {
			 public Iterator<Vector2D> iterator() {
				 return new Iterator<Vector2D>() {
					 private Vector min = getMinimumPoint();
					 private Vector max = getMaximumPoint();
					 private int nextX = min.getBlockX();
					 private int nextZ = min.getBlockZ();

					 public boolean hasNext() {
						 return (nextX != Integer.MIN_VALUE);
					 }

					 public Vector2D next() {
						 if (!hasNext()) throw new java.util.NoSuchElementException();
						 Vector2D answer = new Vector2D(nextX, nextZ);
						 if (++nextX > max.getBlockX()) {
							 nextX = min.getBlockX();
							 if (++nextZ > max.getBlockZ()) {
								 nextX = Integer.MIN_VALUE;
							 }
						 }
						 return answer;
					 }

					 public void remove() {
						 throw new UnsupportedOperationException();
					 }
				 };
			 }
		 };
	 }
	 
	 public String toString() {
		 return getMinimumPoint() + " - " + getMaximumPoint();
	 }
	 
	 public World getWorld()
	 {
		 return world;
	 }
	
	 public void setWorld(World w)
	 {
		 this.world = w;
	 }

	public boolean contains(com.sk89q.worldedit.Vector pt) {
		// TODO Auto-generated method stub
		return false;
	}
}

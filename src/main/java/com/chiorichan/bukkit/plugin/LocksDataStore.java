package com.chiorichan.bukkit.plugin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;

import com.chiorichan.bukkit.plugin.iNations.INations;
import com.sk89q.worldedit.Vector;

public class LocksDataStore {

	@SuppressWarnings("unused")
	private INations plugin;
	public Map<String, Lock> locks = new HashMap<String, Lock>();
	
	public LocksDataStore(INations p) {
		plugin = p;
	}
	
	public void add (Vector v, String k)
	{
		locks.put(k, new Lock(v, k));
	}
	
	public void remove (Lock l)
	{
		if (l != null)
			remove(l.keyName);
	}
	
	public void remove (String l)
	{
		if (l != null && !l.isEmpty())
			locks.remove(l);
	}
	
	public Lock getLock (Vector v, World w)
	{
		Vector v1 = new Vector(v.getBlockX() - 1, v.getBlockY() - 1, v.getBlockZ() - 1);
		Vector v2 = new Vector(v.getBlockX() + 1, v.getBlockY() + 1, v.getBlockZ() + 1);
		
		for (Lock lock : locks.values())
    	{
    		if ( lock.vector.containedWithin(v1, v2) )
    		{
    			if ( lock.vector.equals(v) ) // Original Locked Block
    			{
    				return lock;
    			}
    			else // Inherited Block
    			{
    				if (w.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()) == 
    					w.getBlockAt(lock.vector.getBlockX(), lock.vector.getBlockY(), lock.vector.getBlockZ()) )
    				{
    					return lock;
    				}
    				else
    				{
    					return lock; // TEMP
    				}
    			}
    		}
    	}
		
		return null;
	}
}

package com.chiorichan.bukkit.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.chiorichan.bukkit.plugin.iNations.INations;
import com.sk89q.worldedit.Vector;

public class DataStore
{
	@SuppressWarnings("unused")
	private INations plugin;
	
	public Map<String, Area> areas = new HashMap<String, Area>();
	
	public DataStore(INations plugin)
	{
		this.plugin = plugin;
	}
	
	public void add (Area a)
	{
		areas.put(a.ID, a);
	}
	
	public void remove (Area a)
	{
		if (a != null)
			remove(a.ID);
	}
	
	public void remove (String a)
	{
		if (a != null && !a.isEmpty())
			areas.remove(a);
	}
	
    public Area getPlotFromID (String ID)
    {
    	if ( areas.containsKey(ID) )
    	{
    		return areas.get(ID);
    	}

    	return null;
    }
    
	@SuppressWarnings("unused")
	private org.bukkit.util.Vector VectorSwap(Vector vector)
    {
    	return new org.bukkit.util.Vector(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    
    private Vector VectorSwap(org.bukkit.util.Vector vector)
    {
    	return new Vector(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    
	public Area getPlotFromVector(org.bukkit.util.Vector vector) {
    	return this.getRegionFromVector(VectorSwap(vector));
	}
    
    public Area getRegionFromVector (org.bukkit.util.Vector vector)
    {
    	for (Area cube : areas.values())
    	{
    		if ( cube.contains( VectorSwap( vector ) ) )
    			return cube;
    	}
    	
    	return null;
    }
    
    public Area getRegionFromVector (Vector vector)
    {
    	for (Area cube : areas.values())
    	{
    		if ( cube.contains(vector) )
    			return cube;
    	}
    	
    	return null;
    }
    
    public Collection<Area> values ()
    {
    	return areas.values();
    }

	public void saveRegions()
	{
		/*
		HashMap<String, Object> conf = new HashMap<String, Object>();
		
		for (Area cube : areas.values())
		{
			
		}
		*/
	}
}

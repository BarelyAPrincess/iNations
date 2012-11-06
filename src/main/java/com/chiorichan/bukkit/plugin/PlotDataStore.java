package com.chiorichan.bukkit.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.chiorichan.bukkit.plugin.iNations.INations;
import com.sk89q.worldedit.Vector;

public class PlotDataStore {
	private INations plugin;
	
	public Map<String, Plot> plots = new HashMap<String, Plot>();
	
	public PlotDataStore(INations plugin) {
		this.plugin = plugin;
	}
	
	public void add (Plot p)
	{
		plots.put(p.plotID, p);
	}
	
	public void remove (Plot p)
	{
		if (p != null)
			remove(p.plotID);
	}
	
	public void remove (String p)
	{
		if (p != null && !p.isEmpty())
			plots.remove(p);
	}
	
    public Plot getPlotFromID (String plotID)
    {
    	if ( plots.containsKey(plotID) )
    	{
    		return plots.get(plotID);
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
    
	public Plot getPlotFromVector(org.bukkit.util.Vector vector) {
    	return this.getPlotFromVector(VectorSwap(vector));
	}
    
    public Plot getPlotFromVector (Vector vector)
    {
    	for (Plot cube : plots.values())
    	{
    		if ( cube.contains(vector) )
    			return cube;
    	}
    	
    	return null;
    }
    
    public Collection<Plot> values ()
    {
    	return plots.values();
    }

	public void savePlots()
	{
		HashMap<String, Object> conf = new HashMap<String, Object>();
		
		for (Plot cube : plots.values())
    	{
			conf = cube.serialize();
			
			for ( Entry<String, Object> obj : conf.entrySet() )
			{
				plugin.getConfig().set(obj.getKey(), obj.getValue());
			}
    	}
	}
}

package com.chiorichan.bukkit.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.chiorichan.bukkit.plugin.iNations.INations;

public class PlotDataStore {
	@SuppressWarnings("unused")
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
    
    public Plot getPlotFromVector (Vector vector)
    {
    	for (Plot cube : plots.values())
    	{
    		if ( cube.contains(vector) )
    			return cube;
    	}
    	
    	return null;
    }
    
    public Boolean isAuthorized (Player p, Community plot, String perm)
    {
    	// TODO: FINISH
    	return false;
    }
    
    public Boolean isAuthorized (Player p, Plot plot, String perm)
    { 	
    	// TODO: FINISH
    	return false;
    }
    
    public Collection<Plot> values ()
    {
    	return plots.values();
    }
}

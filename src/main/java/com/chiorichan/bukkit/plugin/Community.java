package com.chiorichan.bukkit.plugin;

import org.bukkit.World;

import com.sk89q.worldedit.Vector;

public class Community extends Cuboid {
	
	
	public Community (Vector pos1, Vector pos2)
	{
		this(null, pos1, pos2);
	}
	
	public Community (World w, Vector pos1, Vector pos2)
	{
		super(w, pos1, pos2);
	}
	
	public void addPlot (Plot plot)
	{
		
	}
}

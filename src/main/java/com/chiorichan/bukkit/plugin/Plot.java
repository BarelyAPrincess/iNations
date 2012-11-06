package com.chiorichan.bukkit.plugin;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;

public class Plot extends Cuboid {
	private Player player;
	public String plotID;
	public String owner;
	public String members;
	
	public Plot (Vector pos1, Vector pos2, Player p)
	{
		this(null, pos1, pos2, p);
	}
	
	public Plot (World w, Vector pos1, Vector pos2, Player p)
	{
		super(w, pos1, pos2);
		
		player = p;
		owner = (p == null) ? null : p.getName();
	}
	
	public void setPlayer (Player p)
	{
		player = p;
		owner = (p == null) ? null : p.getName();
	}
	
	public Player getPlayer ()
	{
		return player;
	}
}

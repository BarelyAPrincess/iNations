package com.chiorichan.bukkit.plugin;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;

public class Lock {
	public String keyName;
	public Vector vector;
	
	public Lock (Vector v, String k)
	{
		keyName = k;
		vector = v;
	}
	
	public boolean playerHasKey (Player p)
	{
		if (p.hasPermission("inations.keys." + keyName))
		{
			return true;
		}
		
		return false;
	}
}

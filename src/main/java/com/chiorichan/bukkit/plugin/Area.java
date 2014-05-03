package com.chiorichan.bukkit.plugin;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;

public class Area extends Cuboid {
	public String ID;
	public World world;
	public HashMap<String, Object> properties;
	
	public Area (Vector pos1, Vector pos2)
	{
		this(null, pos1, pos2);
	}
	
	public Area (World w, Vector pos1, Vector pos2)
	{
		super(w, pos1, pos2);
		
		ID = pos1.getBlockX() + "_" + pos1.getBlockY() + "_" + pos1.getBlockZ() + "_" + pos2.getBlockX() + "_" + pos2.getBlockY() + "_" + pos2.getBlockZ();
		world = w;
	}
	
	public Player presentPlayer ()
	{
		return null;
	}
	
	public int presentPlayerCount ()
	{
		if ( world == null )
			return 0;
		
		int cnt = 0;
		
		for ( Player player : world.getPlayers() )
		{
			 if ( contains( ChangeCordType( player.getLocation() ) ) )
			 {
				 cnt++;
			 }
		}
		
		return cnt;
	}

	public Object gets (String key, Object def)
	{
		if (properties.containsKey(key))
		{
			Object obj = properties.get(key);
			if (obj == null)
			{
				return def;
			}
			else
			{
				return obj;
			}
		}
		else
		{
			return def;
		}
	}
	
	public void sets (String key, Object value)
	{
		properties.put(key, value);
	}

	public HashMap<String, Object> serialize()
	{
		HashMap<String, Object> conf = new HashMap<String, Object>();
		
		String parent = "areas." + ID + ".";

		conf.put(parent + "world", world.getName());
		conf.put(parent + "ID", ID);
		conf.put(parent + "x1", getPos1().getBlockX());
		conf.put(parent + "y1", getPos1().getBlockY());
		conf.put(parent + "z1", getPos1().getBlockZ());
		conf.put(parent + "x2", getPos2().getBlockX());
		conf.put(parent + "y2", getPos2().getBlockY());
		conf.put(parent + "z2", getPos2().getBlockZ());
		
		return conf;
	}
	
	public Vector ChangeCordType (Location loc)
    {
    	return new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    public Location ChangeCordType (Vector vec)
    {
    	return new Location(null, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }
}

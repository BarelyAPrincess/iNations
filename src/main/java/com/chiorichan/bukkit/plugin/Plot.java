package com.chiorichan.bukkit.plugin;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;

public class Plot extends Cuboid {
	private Player player;
	public String plotID;
	public String owner;
	public List<String> members_;
	public List<Player> members;
	public World world;
	public HashMap<String, Object> properties;
	
	public Plot (Vector pos1, Vector pos2, Player p)
	{
		this(null, pos1, pos2, p);
	}
	
	public Plot (World w, Vector pos1, Vector pos2, Player p)
	{
		super(w, pos1, pos2);
		
		plotID = pos1.getBlockX() + "_" + pos1.getBlockY() + "_" + pos1.getBlockZ() + "_" + pos2.getBlockX() + "_" + pos2.getBlockY() + "_" + pos2.getBlockZ();
		world = w;
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
	
	public void addMember (Player player)
	{
		// TODO: Add persistent save.
		members.add(player);
		members_.add(player.getName());
	}
	
	public boolean isOwner (Player player)
	{
		if (this.player != null && player == this.player)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean canBuild (Player player)
	{
		if ( isMember( player ) )
			return true;
		
		if (isMember(player))
		{
			return (Boolean) gets("canMembersBuild", false);
		}
		else
		{
			return (Boolean) gets("nonMembersBuild", false);
		}
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
	
	public boolean isMember (Player player)
	{
		if (isOwner(player))
			return true;
		
		return members.contains(player);
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
		
		String parent = "plots." + plotID + ".";

		conf.put(parent + "world", world.getName());
		conf.put(parent + "player", owner);
		conf.put(parent + "plotID", plotID);
		conf.put(parent + "x1", getPos1().getBlockX());
		conf.put(parent + "y1", getPos1().getBlockY());
		conf.put(parent + "z1", getPos1().getBlockZ());
		conf.put(parent + "x2", getPos2().getBlockX());
		conf.put(parent + "y2", getPos2().getBlockY());
		conf.put(parent + "z2", getPos2().getBlockZ());
		
		//public List<String> members_;
		//public HashMap<String, Object> properties;
		
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

package com.chiorichan.bukkit.plugin.iNations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.chiorichan.bukkit.plugin.CommunityDataStore;
import com.chiorichan.bukkit.plugin.Cuboid;
import com.chiorichan.bukkit.plugin.Plot;
import com.chiorichan.bukkit.plugin.PlotDataStore;

public class INations extends JavaPlugin implements Listener {
	
	public Boolean pluginEnabled = false;
	public SQLite db;
	
	public Map<String, Cuboid> regions = new HashMap<String, Cuboid>();
	
	public PlotDataStore iPlots = new PlotDataStore(this);
	public CommunityDataStore iCommunity = new CommunityDataStore(this);
	
    public void onDisable() {
    	db.close();
    	saveConfig();
    	System.out.println(this.toString() + " has been unloaded.");
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        getConfig().options().copyDefaults(true);
        
        getCommand("iplayer").setExecutor(new INationsCommandExecutor(this));
        getCommand("iadmin").setExecutor(new INationsCommandExecutor(this));
        
        this.db = new SQLite(this.getLogger(), "[iNations]", "iNations", "./plugins/iNations");
        
        db.open();
        
        if (db.checkConnection())
        {
        	if (!db.checkTable("plots"))
            {
            	db.createTable("CREATE TABLE `plots` (" +
            			"`plotID` varchar(255) NOT NULL, " +
            			"`owner` varchar(255) NOT NULL, " +
            			"`members` text NOT NULL, " +
            			"`world` varchar(255) NOT NULL, " +
            			"`type` varchar(255) NOT NULL, " +
            			"`address` varchar(255) NOT NULL, " +
            			"`displayName` varchar(255) NOT NULL, " +
            			"`parentID` varchar(255) NOT NULL, " +            			
            			"`x1` int(4) NOT NULL, " +
            			"`y1` int(4) NOT NULL, " +
            			"`z1` int(4) NOT NULL, " +
            			"`x2` int(4) NOT NULL, " +
            			"`y2` int(4) NOT NULL, " +
            			"`z2` int(4) NOT NULL); ");
            	
            	System.out.println("Created missing table `plots`.");
            }
        	else
            {
            	ResultSet rows = db.query("SELECT * FROM `plots`;");
            	int cnt = 0;
            	
            	try {
					while (rows.next())
					{
						Vector loc1 = new Vector(rows.getInt("x1"), rows.getInt("y1"), rows.getInt("z1"));
						Vector loc2 = new Vector(rows.getInt("x2"), rows.getInt("y2"), rows.getInt("z2"));
						Player p = this.getServer().getPlayer(rows.getString("owner"));
						World w = this.getServer().getWorld(rows.getString("world"));
						
						if (w == null)
							w = this.getServer().getWorld("world");
						
						Plot plot = new Plot(w, loc1, loc2, p);
						plot.plotID = rows.getString("plotID");
						plot.owner = rows.getString("owner");
						
						iPlots.add(plot);
						
						cnt++;
					}
					
					rows.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
            	
            	System.out.println("Loaded " + cnt + " plot(s) from table `plots`.");
            }
            
            // TODO: Add plot and area integrity checks.
            
            System.out.println(this.toString() + " has been loaded.");
            
            pluginEnabled = true;
        }
        else
        {
        	System.out.println("Failed to initalize SQLite Database. Check Logs.");
        	pluginEnabled = false;
        }
    }
    
    public Vector ChangeCordType (Location loc)
    {
    	return new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    public Location ChangeCordType (Vector vec)
    {
    	return new Location(null, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }
    
    @EventHandler
    public void onCreatureSpawnEvent (CreatureSpawnEvent event)
    {
    	// Define Bad Mobs
    	List<EntityType> BadMobs = new ArrayList<EntityType>();
    	
    	BadMobs.add(EntityType.BLAZE);
    	BadMobs.add(EntityType.CAVE_SPIDER);
    	BadMobs.add(EntityType.CREEPER);
    	BadMobs.add(EntityType.ENDER_DRAGON);
    	BadMobs.add(EntityType.ENDERMAN);
    	BadMobs.add(EntityType.GHAST);
    	BadMobs.add(EntityType.GIANT);
    	BadMobs.add(EntityType.MAGMA_CUBE);
    	BadMobs.add(EntityType.PIG_ZOMBIE);
    	BadMobs.add(EntityType.SKELETON);
    	BadMobs.add(EntityType.SLIME);
    	BadMobs.add(EntityType.SPIDER);
    	BadMobs.add(EntityType.SQUID);
    	BadMobs.add(EntityType.ZOMBIE);
    	
    	// Define Animal Mobs
    	
    	List<EntityType> AniMobs = new ArrayList<EntityType>();
    	
    	AniMobs.add(EntityType.CHICKEN);
    	AniMobs.add(EntityType.COW);
    	AniMobs.add(EntityType.MUSHROOM_COW);
    	AniMobs.add(EntityType.PIG);
    	AniMobs.add(EntityType.SHEEP);
    	
    	// Get Default for Defined
    	
    	Boolean Allowed = getConfig().getBoolean("global.allowothermobs");
    	
    	Plot plot = iPlots.getPlotFromVector(event.getLocation().toVector());
    	
    	// Use WorldGuard to protect the world until it is added.
    	// Plot protection is the implemented entity protection at the moment.
    	if ( plot != null )
    	{
    		if (getConfig().getBoolean("global.alloweggspawns") && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG)
    		{
   				Allowed = true;
    		}
    		
    		if (getConfig().getBoolean("global.allowanimals") && AniMobs.contains(event.getEntityType()))
    		{
    			Allowed = true;
    		}
    		
    		if (getConfig().getBoolean("global.allowbadmobs") && BadMobs.contains(event.getEntityType()))
    		{
    			Allowed = true;
    		}
    		
    		if (!Allowed)
    		{
    			event.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler
    public void onBlockBreakEvent (BlockBreakEvent event)
    {
    	Vector loc = ChangeCordType(event.getBlock().getLocation());
    	
    	for (Plot cube : iPlots.values())
    	{
    		if (cube.contains(loc))
    		{
    			// TODO: Check if block breaking is allowed.
    			
    			if (cube.getPlayer() == null)
    			{
    				Player p = getServer().getPlayerExact(cube.owner);
    				if (p != null)
    					cube.setPlayer(p);
    			}
    			
    			if ( (cube.getPlayer() == null || cube.getPlayer() != event.getPlayer()) && getConfig().getBoolean("global.protectbuilds") )
    			{
    				event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break blocks on this plot.");
        			event.setCancelled(true);    				
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onBlockPlaceEvent (BlockPlaceEvent event)
    {
		Vector loc = ChangeCordType(event.getBlockPlaced().getLocation());
			
		for (Plot cube : iPlots.values())
    	{
    		if (cube.contains(loc))
    		{
    			// TODO: Check if block placement is allowed.
    			
    			if (cube.getPlayer() == null)
    			{
    				Player p = getServer().getPlayerExact(cube.owner);
    				if (p != null)
    					cube.setPlayer(p);
    			}
    			
    			if ( (cube.getPlayer() == null || cube.getPlayer() != event.getPlayer()) && getConfig().getBoolean("global.protectbuilds") )
    			{
    				event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to build on this plot.");
    				event.setBuild(false);    				
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event)
    {
    	if ( event.getPlayer().getItemInHand().getTypeId() == 271 )
    	{
    		if ( event.getClickedBlock() != null )
    		{
	    		Location loc = event.getClickedBlock().getLocation();
	    		Vector vector = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	    		
	    		if ( event.getAction() == Action.LEFT_CLICK_BLOCK )
	    		{
	    			setMetadata(event.getPlayer(), "position1", vector);
	    			event.getPlayer().sendMessage(ChatColor.AQUA + "[iNations]" + ChatColor.WHITE + " You have set position #1! (X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ")" );
	    			event.setCancelled(true);
	    		}
	    		else if ( event.getAction() == Action.RIGHT_CLICK_BLOCK )
	    		{
	    			setMetadata(event.getPlayer(), "position2", vector);
	    			event.getPlayer().sendMessage(ChatColor.AQUA + "[iNations]" + ChatColor.WHITE + " You have set position #2! (X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ")" );
	    			event.setCancelled(true);
	    		}
    		}
    	}
    }
    
    @EventHandler
    public void onPlayerJoin (PlayerJoinEvent event)
    {
    	setMetadata(event.getPlayer(), "position1", null);
		setMetadata(event.getPlayer(), "position2", null);
		
		for (Plot plot : iPlots.values())
		{
			if (plot.owner.equals(event.getPlayer().getName()))
			{
				plot.setPlayer(event.getPlayer());
			}
		}
        
        if (getConfig().getBoolean("players." + event.getPlayer().getName() + ".hasHome", false))
        {
        	event.getPlayer().sendMessage("Welcome, " + event.getPlayer().getDisplayName() + "!");
        	event.getPlayer().sendMessage("You don't currently seem to be a citizen of any nations.");
        	
        	getConfig().set("players." + event.getPlayer().getName() + ".hasHome", false);
        	getConfig().set("players." + event.getPlayer().getName() + ".homeID", "");
        }
        else
        {
        	String myHome = getConfig().getString("players." + event.getPlayer().getPlayerListName() + ".homeID", "");
        	
        	if (myHome.isEmpty())
        	{
        		getConfig().set("players." + event.getPlayer().getPlayerListName() + ".hasHome", false);
        	}
        	else
        	{
        		event.getPlayer().sendMessage("Welcome Back, " + event.getPlayer().getPlayerListName() + "!");
        		event.getPlayer().sendMessage("We are sure your home nation of **** is glad to see you have returned.");
        		event.getPlayer().sendMessage("You may use /gohome to return to your nation.");
        	}
        }
        
        String msg = getConfig().getString("global.motd");
        if (!msg.isEmpty())
        {
        	event.getPlayer().sendMessage(msg);
        }
    }
    
    public void setMetadata(Player player, String key, Object value)
    {
    	player.setMetadata(key, new FixedMetadataValue(this, value));
    }
    	
    public Object getMetadata(Player player, String key)
    {
    	List<MetadataValue> values = player.getMetadata(key);  
    	for(MetadataValue value : values)
    	{
    		if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName()))
    		{
    			return value.value();
    		}
    	}
    	
		return null;
    }
}
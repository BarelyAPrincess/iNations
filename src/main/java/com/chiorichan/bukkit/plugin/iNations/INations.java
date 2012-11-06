package com.chiorichan.bukkit.plugin.iNations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lib.PatPeter.SQLibrary.SQLite;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.chiorichan.bukkit.plugin.CommunityDataStore;
import com.chiorichan.bukkit.plugin.Cuboid;
import com.chiorichan.bukkit.plugin.LocksDataStore;
import com.chiorichan.bukkit.plugin.Plot;
import com.chiorichan.bukkit.plugin.PlotDataStore;
import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class INations extends JavaPlugin implements Listener {
	
	public static StateFlag TEST_FLAG = new StateFlag("test-chamber", true);
	
	public Boolean pluginEnabled = false;
	public SQLite db;
	
	public static Permission perm = null;
	public Boolean broadEnabled = true;
	public int syncId = -1;
	
	public Map<String, Cuboid> regions = new HashMap<String, Cuboid>();
	
	public PlotDataStore iPlots = new PlotDataStore(this);
	public CommunityDataStore iCommunity = new CommunityDataStore(this);
	public LocksDataStore iLocks = new LocksDataStore(this);
	
	public String lockWithKey;
	public int lastId = -1;
	
    public void onDisable() {
    	//db.close();
    	saveConfig();
    	
    	iPlots.savePlots();
    	
    	System.out.println(this.toString() + " has been unloaded.");
    }
    
    public void broadcastMessage ()
    {
    	//Player[] players = Bukkit.getOnlinePlayers();
    	Random rand = new Random();
    	
    	ConfigurationSection conf = getConfig().getConfigurationSection("messages");
    	Map<String, Object> msgs = conf.getValues(true);
    	
    	String broadcast = "";
    	
    	if (msgs.size() > 0)
    	{
    		int choice = -1;
    		int x = 0;
    		
    		while ( lastId == choice )
    		{
    			choice = rand.nextInt(msgs.size());
    		}
    		
    		lastId = choice;
    		
    		for (Object msg : msgs.values())
    		{
    			if (x == choice)
    			{
    				broadcast = msg.toString();
    				break;
    			}
    			
    			x++;
    		}
    		
    		if ( broadcast.equals("") )
    			return;
    		
    		/*
        	for (Player p : players)
        	{
        		p.sendMessage( ChatColor.RED + "[Announcement] " + ChatColor.RESET + broadcast.toString() );
        	}
        	*/
        	
        	getServer().broadcastMessage( ChatColor.RED + "[Announcement] " + ChatColor.RESET + ChatColor.translateAlternateColorCodes("&".charAt(0), broadcast.toString() ) );
        	//getServer().getConsoleSender().sendMessage( ChatColor.RED + "[Announcement] " + ChatColor.RESET + broadcast.toString() );
    	}
    }
    
    public void onEnable() {
    	getServer().getPluginManager().registerEvents(this, this);
        
        getConfig().options().copyDefaults(true);
        
        INationsCommandExecutor ExeClass = new INationsCommandExecutor(this);
        
        getCommand("iplayer").setExecutor(ExeClass);
        getCommand("iadmin").setExecutor(ExeClass);
        getCommand("inv").setExecutor(ExeClass);
        getCommand("clear").setExecutor(ExeClass);
        getCommand("t").setExecutor(ExeClass);
        getCommand("goto").setExecutor(ExeClass);
    	
    	Long ticks = getConfig().getLong("globals.msgFreq", 5000L);
    	
    	syncId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
    		public void run() {
    			if (broadEnabled)
    				broadcastMessage();
			}
    		
    	}, 200L, ticks);
    	
    	if ( syncId == -1 )
    	{
    		System.out.println("We have failed to initalize a synced task.");
    	}
        
    	RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
    	
        if (permissionProvider == null)
        {
        	System.out.println("Vault plugin was not detected. Please install it or else we can not properly format chat.");
        }
        else
        {
            perm = permissionProvider.getProvider();
        }
        
        System.out.println(this.toString() + " has been loaded.");
        pluginEnabled = true;
        
        ConfigurationSection conf = getConfig().getConfigurationSection("plots");
		
		for ( String key : conf.getKeys(true) )
		{
			if ( !key.contains(".") )
			{
				World wor = getServer().getWorld( conf.getString(key + ".world") );
				Player play = getServer().getPlayerExact( conf.getString(key + ".player") );
				
				Vector pos1 = new Vector();
				Vector pos2 = new Vector();
				
				pos1.setX(conf.getLong(key + ".x1"));
				pos1.setY(conf.getLong(key + ".y1"));
				pos1.setZ(conf.getLong(key + ".z1"));
				
				pos2.setX(conf.getLong(key + ".x2"));
				pos2.setY(conf.getLong(key + ".y2"));
				pos2.setZ(conf.getLong(key + ".z2"));
				
				Plot p = new Plot( wor, pos1, pos2, play );
				p.owner = conf.getString(key + ".player");
				p.plotID = conf.getString(key + ".plotID");
				
				iPlots.add( p );
			}
		}
        
        /*
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
        		ResultSet rows;
        		
            	rows = db.query("SELECT * FROM `plots`;");
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
            	
            	
            	
            	rows = db.query("SELECT * FROM `locks`;");
            	cnt = 0;
            	
            	try {
					while (rows.next())
					{
						Vector v = new Vector(rows.getInt("x"), rows.getInt("y"), rows.getInt("z"));
						iLocks.add(v, rows.getString("keyName"));
						cnt++;
					}
					
					rows.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
            	
            	System.out.println("Loaded " + cnt + " locks(s) from table `locks`.");
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
        */
		
		WGCustomFlagsPlugin wgCF = getWGCustomFlags();
        
        //wgCustomFlagsPlugin.addCustomFlag(FLAG_NAME);
        wgCF.addCustomFlag(TEST_FLAG);
    }
    
    private WGCustomFlagsPlugin getWGCustomFlags()
    {
      Plugin plugin = getServer().getPluginManager().getPlugin("WGCustomFlags");
      
      if (plugin == null || !(plugin instanceof WGCustomFlagsPlugin))
      {
        return null;
      }

      return (WGCustomFlagsPlugin) plugin;
    }
    
    public List<Player> adminsOnline (World w)
    {
    	List<Player> op = Collections.emptyList();
    	
    	for ( Player p : getServer().getOnlinePlayers() )
    	{
    		if ( p.isOp() || p.hasPermission("inations.admin") )
    		{
    			op.add(p);
    		}
    	}
    	
    	return op;
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
    	BadMobs.add(EntityType.SILVERFISH);
    	
    	// Define Animal Mobs
    	
    	List<EntityType> AniMobs = new ArrayList<EntityType>();
    	
    	AniMobs.add(EntityType.CHICKEN);
    	AniMobs.add(EntityType.COW);
    	AniMobs.add(EntityType.MUSHROOM_COW);
    	AniMobs.add(EntityType.PIG);
    	AniMobs.add(EntityType.SHEEP);
    	AniMobs.add(EntityType.OCELOT);
    	
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
    
    public void sendDebug (String d) // Temporary Method to send debug information to my username.
    {
    	Player p = getServer().getPlayer("ChioriGreene");
    	
    	if ( p != null )
    		p.sendMessage( ChatColor.DARK_AQUA + "[iNations Debug] " + ChatColor.WHITE + d);
    }
    
    // \/ Class Events \/
    
    // Called when fuel burned in furnace
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnaceBurnEvent ( FurnaceBurnEvent event )
    {
    	
    }
    
    // Called when something smelts in furnace
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnaceSmeltEvent ( FurnaceSmeltEvent event )
    {
    	
    }
    
    // Called when player clicks on inventory item
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickEvent ( InventoryClickEvent event )
    {
    	//MaterialData md = new MaterialData(30209, (byte) 14);
    	
    	//event.getWhoClicked().getInventory().setChestplate( md.toItemStack(1) );
    	
    	//ItemStack i = event.getWhoClicked().getInventory().getChestplate(); 
    	
    	//ItemStack ii = event.getWhoClicked().getItemInHand();
    	
    	//Map<String, Object> imap = i.serialize();
    	
   		//sendDebug( imap.toString() );
    	
    	//sendDebug( i.toString() + " " + i.getData().toString() + " " + i.() );
    	//sendDebug( ii.toString() + " " + ii.getData().toString() + " " + ii.hashCode() );
    	
    	/*
    	ItemStack[] i = event.getWhoClicked().getInventory().getContents();
    	int cnt = 0;
    	
    	for (ItemStack it : i)
    	{
    		cnt++;
    		//sendDebug( "Count " + cnt + ": " + it.toString() + " " + it.getData().toString() );
    	}
    	
    	if ( event.getRawSlot() == 6 )
    	{
    		//event.setCancelled(true);
    	}
    	*/
    }
    
    // Called when player opens their inventory
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpenEvent ( InventoryOpenEvent event )
    {

    }
    
    // Called when player closes their inventory
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryCloseEvent ( InventoryCloseEvent event )
    {

    }
    
    // Called before item is crafted
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareItemCraftEvent ( PrepareItemCraftEvent event )
    {
    	
    }
    
    // Called when item is put in enchantment table
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareItemEnchantEvent ( PrepareItemEnchantEvent event )
    {
    	
    }
    
    // Called when item was successfully enchanted
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantItemEvent ( EnchantItemEvent event )
    {
    	
    }
    
    // \/ World & Weather Events \/
    
    // Call when portal is created
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalCreateEvent ( PortalCreateEvent event )
    {
    	
    }
    
    // Called when tree or mushroom grows
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrowEvent ( StructureGrowEvent event )
    {
    	
    }
    
    // Called when thunder state changes
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThunderChangeEvent ( ThunderChangeEvent event )
    {
    	
    }
    
    // Called when world weather changes
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeatherChangeEvent ( WeatherChangeEvent event )
    {
    	
    }
    
    // \/ Block Events \/
    
    // Called when block ignites
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockIgniteEvent ( BlockIgniteEvent event )
    {
    	
    }
    
    // Called when a note block plays
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNotePlayEvent ( NotePlayEvent event )
    {
    	//sendDebug( event.getNote().toString() );
    }
    
    public String getPlayerClass ( Player p )
    {
    	String className = getConfig().getString("players." + p.getName() + ".class");
        
        if ( className == null || className == "null" || className == "" )
        {
        	className = "Awakened";
        	getConfig().set("players." + p.getName() + ".class", className);
        }
        
        if ( className == "JustAwakened" )
        {
        	className = "Awakened";
        	getConfig().set("players." + p.getName() + ".class", className);
        }
        
        return className;
    }
    
    // \/ Player Events \/
    
    // Called when player sprints
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSprintEvent ( PlayerToggleSprintEvent event )
    {
    	Player player = event.getPlayer();
    	
    	if (event.isSprinting())
    	{
    		String className = getPlayerClass(player);
			int speed = getConfig().getInt("classes." + className + ".speed", -1);
			
    		if ( player.hasPermission("inations.admin") )
    		{
    			if ( player.getItemInHand().getTypeId() == 57 )
    			{
    				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 9), true);
    			}
    			else if ( player.getItemInHand().getTypeId() == 41 )
    			{
    				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 7), true);
    			}
    			else if ( player.getItemInHand().getTypeId() == 42 )
    			{
    				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 5), true);
    			}
    			else
    			{
    				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 3), true);
    			}
    		}
    		else
    		{
    			if ( speed > -1 )
    				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, speed), true);
    		}
    	}
    	else
    	{
    		event.getPlayer().removePotionEffect(PotionEffectType.SPEED);
    	}
    }
    
    // Called when player sneaks
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSneakEvent ( PlayerToggleSneakEvent event )
    {
    	
    }
    
    // Called when player changes flight state
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlightEvent ( PlayerToggleFlightEvent event )
    {
    	/*
    	Player p = event.getPlayer();
    	
    	if ( p.isFlying() )
    	{
    		p.sendMessage("You are no longer flying.");
    	}
    	else
    	{
    		p.sendMessage("You are now flying.");
    	}
    	*/
    }
    
    // Called when player changes location
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent ( PlayerTeleportEvent event )
    {
    	
    }
    
    // Called on player respawn
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawnEvent ( PlayerRespawnEvent event )
    {
    	
    }
    
    // Called on player leave
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent ( PlayerQuitEvent event )
    {
    	
    }
    
    // Called before offical player login
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLoginEvent ( PlayerPreLoginEvent event )
    {
    	
    }
    
    // Called when player uses a portal
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortalEvent ( PlayerPortalEvent event )
    {
    	
    }
    
    // Called when player picks and item off the ground
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItemEvent ( PlayerPickupItemEvent event )
    {
    	
    }
    
    // Called when player tries to move
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMoveEvent ( PlayerMoveEvent event )
    {
    	Player p = event.getPlayer();
		Location l = event.getTo();
		
		// Speed Gel
		if ( l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() - 1, l.getBlockZ()).getTypeId() == 41 )
		{
			setMetadata( p, "speed_gel", 1 );
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000, 10), true);
		}
		else
		{
			if ( getMetadata( p, "speed_gel" ) != null )
			{
				event.getPlayer().removePotionEffect(PotionEffectType.SPEED);
				setMetadata( p, "speed_gel", null );
			}
		}
		
		// Heaven Fall
		if (l.getBlockY() < -20 && l.getWorld().getName().equals("skylands"))
		{
			l.setWorld( getServer().getWorld("world") );
			l.setY( l.getWorld().getHighestBlockYAt(l) );
			
			p.teleport(l);
			
			p.setFallDistance(0);
			
			p.sendMessage(ChatColor.AQUA + "You have fallen from Pegesus Homeworld and landed on Earth.");
			p.setHealth(1);
		}
		else if ( l.getBlockY() > 265 && l.getWorld().getName().equals("world") )
		{
			l.setWorld( getServer().getWorld("skylands") );
			l.setY( l.getWorld().getHighestBlockYAt(l) );
			
			if ( l.getWorld().getHighestBlockYAt(l) == 0 )
			{
				int degrees = (Math.round(l.getYaw()) + 270) % 360;
				
				int cnt = 0;
				
				while ( l.getWorld().getHighestBlockYAt(l) == 0 && cnt <= 50 )
				{
					if (degrees <= 22 || (degrees > 292 && degrees <= 259)) // North = -X
					{
						l.setX( l.getX() - 1 );
					}
					else if (degrees <= 112) // East = -Z
					{
						l.setZ( l.getZ() - 1 );
					}
					else if (degrees <= 202) // South = +X
					{
						l.setX( l.getX() + 1 );
					}
					else if (degrees <= 292) // West = +Z
					{
						l.setZ( l.getZ() + 1 );
					}
				}
				
				l.setY( l.getWorld().getHighestBlockYAt(l) );
				
				//sendDebug(l.getWorld().getBlockTypeIdAt(l) + "  " + cnt + "  " + l.getWorld().getHighestBlockYAt(l) );
				
				if ( cnt > 20 )
				{
					p.sendMessage(ChatColor.RED + "There are no visible landing spots within 50 blocks ahead of you.");
				}
				else
				{
					p.sendMessage(ChatColor.AQUA + "You have landed on the Pegesus Homeland.");
					p.teleport(l);
				}
			}
			else
			{
				p.sendMessage(ChatColor.AQUA + "You have landed on the Pegesus Homeland.");
				p.teleport(l);
			}
		}
    	
    	/*
	    	Player player = event.getPlayer();
	    	World world = player.getWorld();
	    	
	    	Location l = player.getLocation();
	    	l.setY(l.getY() - 1);
	    	
	    	int x = l.getBlockX();
			int y = l.getBlockY();
			int z = l.getBlockZ();
			
			Block b1 = world.getBlockAt(x, y, z);
			Block b2 = world.getBlockAt(x - 1, y, z);
			Block b3 = world.getBlockAt(x - 1, y, z + 1);
			Block b4 = world.getBlockAt(x - 1, y, z - 1);
			Block b5 = world.getBlockAt(x, y, z - 1);
			Block b6 = world.getBlockAt(x + 1, y, z);
			Block b7 = world.getBlockAt(x + 1, y, z - 1);
			Block b8 = world.getBlockAt(x, y, z + 1);
			Block b9 = world.getBlockAt(x + 1, y, z + 1);
			
			if ( b1.getType() == Material.STATIONARY_WATER )
				b1.setType( Material.ICE );
			
			if ( b2.getType() == Material.STATIONARY_WATER )
				b2.setType( Material.ICE );
			
			if ( b3.getType() == Material.STATIONARY_WATER )
				b3.setType( Material.ICE );
			
			if ( b4.getType() == Material.STATIONARY_WATER )
				b4.setType( Material.ICE );
			
			if ( b5.getType() == Material.STATIONARY_WATER )
				b5.setType( Material.ICE );
			
			if ( b6.getType() == Material.STATIONARY_WATER )
				b6.setType( Material.ICE );
			
			if ( b7.getType() == Material.STATIONARY_WATER )
				b7.setType( Material.ICE );
			
			if ( b8.getType() == Material.STATIONARY_WATER )
				b8.setType( Material.ICE );
			
			if ( b9.getType() == Material.STATIONARY_WATER )
				b9.setType( Material.ICE );
    	*/
    }
    
    // Call on player level change
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLevelChangeEvent ( PlayerLevelChangeEvent event )
    {
    	event.getPlayer().sendMessage("Your level has changed to " + event.getNewLevel() + ".");
    	
    	if ( event.getNewLevel() > 10 && !getConfig().isSet("players." + event.getPlayer().getDisplayName() + ".whitelisted") )
    	{
    		getConfig().set("players." + event.getPlayer().getDisplayName() + ".whitelisted", 1);
        	saveConfig();
    	}
    }
    
    // Call on player kick
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKickEvent ( PlayerKickEvent event )
    {
    	
    }
    
    // Called when item in hand changes
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeldEvent ( PlayerItemHeldEvent event )
    {
    	
    }
    
    // Called when tool breaks
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemBreakEvent ( PlayerItemBreakEvent event )
    {
    	
    }
    
    // Called when player enters a bed
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBedEnterEvent ( PlayerBedEnterEvent event )
    {
    	
    }
    
    // Called when players right-clicks entity
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntityEvent ( PlayerInteractEntityEvent event )
    {
    	
    }
    
    // Use to check for player world change
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedWorldEvent( PlayerChangedWorldEvent event )
    {	
    
    }
    
    // Use to check for redstone power event
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstoneEvent(BlockRedstoneEvent event)
    {
    	Block b = event.getBlock();
    	
    	if ( b.getType() == Material.WOODEN_DOOR || b.getType() == Material.IRON_DOOR_BLOCK || b.getTypeId() == 229 )
    	{
    		Block door1Top = null;
    		Block door1Bottom = null;
    		
    		Block door2Bottom = null;
    		
    		Boolean allowed = true;
    		
    		if ( doorIsTopHalf(b.getData()) )
			{
				door1Top = b;
				door1Bottom = b.getRelative(BlockFace.DOWN);
			}
			else
			{
				door1Top = b.getRelative(BlockFace.UP);
				door1Bottom = b;
			}
    		
	    	if ( door1Bottom != null && door1Bottom.getRelative(BlockFace.DOWN).getType() == Material.WOOL )
	    	{
	    		String color = getWoolColor( door1Bottom.getRelative(BlockFace.DOWN) );
	    		
	    		if ( event.getNewCurrent() > 0 ) // Door is opening
	    		{
	    			allowed = false;
	    			
	    			for ( Player p : getServer().getOnlinePlayers() )
			    	{
			    		if ( event.getBlock().getLocation().getWorld() == p.getWorld() && event.getBlock().getLocation().distance( p.getLocation() ) < 2 )
			    		{
			    			if ( p.hasPermission("inations." + color.toLowerCase()) || p.hasPermission("inations.admin") || p.isOp() )
							{
								allowed = true;
								break;
							}
							else
							{
								allowed = false;
							}
			    		}
			    	}
	    			
	    			for ( Player p : getServer().getOnlinePlayers() )
			    	{
	    				if ( event.getBlock().getLocation().getWorld() == p.getWorld() && event.getBlock().getLocation().distance( p.getLocation() ) < 2 )
	    				{
	    					if ( allowed )
	    					{
	    						p.sendMessage(ChatColor.AQUA + "Access granted to " + color + " Wool colored door.");
	    					}
	    					else
	    					{
	    						p.sendMessage(ChatColor.RED + "Access denied to " + color + " Wool colored door.");
	    					}
	    				}
			    	}
	    			
	    			if ( !allowed )
	    				event.setNewCurrent(0);
	    				
	    		}
	    		else // Door is closing
	    		{
	    			
	    		}
	    	}
	    	
	    	if ( door1Bottom != null && allowed )
	    	{
		    	if ( ( door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.NORTH).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.NORTH).getData())) {
					door2Bottom = door1Bottom.getRelative(BlockFace.NORTH);
				}
				else if ( ( door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.SOUTH).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.SOUTH).getData())) {
					door2Bottom = door1Bottom.getRelative(BlockFace.SOUTH);
				}
				else if ( ( door1Bottom.getRelative(BlockFace.EAST).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.EAST).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.EAST).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.EAST).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.EAST).getData())) {
					door2Bottom = door1Bottom.getRelative(BlockFace.EAST);
				}
				else if ( ( door1Bottom.getRelative(BlockFace.WEST).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.WEST).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.WEST).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.WEST).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.WEST).getData())) {
					door2Bottom = door1Bottom.getRelative(BlockFace.WEST);
				}
				
				if (door2Bottom != null)
				{
					if ( doorIsOpen(door1Bottom.getData()) == doorIsOpen(door2Bottom.getData()) )
					{
						if ( door2Bottom.getBlockPower() == 0 )
						{
							door2Bottom.setData(flipDoor(door2Bottom.getData()));
						}
						else
						{
							event.setNewCurrent( event.getOldCurrent() );
						}
					}
				}
	    	}
    	}
    }
    
    // Use to check players
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLoginEvent( AsyncPlayerPreLoginEvent event )
    {
    	
    }
    
    // Use to filter player chats
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatEvent(PlayerChatEvent event)
    {
    	String group = "PluginError"; 
    	
    	if ( perm == null )
    	{
    		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        	
            if (permissionProvider == null)
            {
            	System.out.println("Vault plugin was not detected. Please install it or else we can not properly format chat.");
            }
            else
            {
                perm = permissionProvider.getProvider();
            }
    	}

    	if ( perm != null )
    	{
    		group = perm.getPrimaryGroup(event.getPlayer().getWorld(), event.getPlayer().getName());
            
            if ( group == null || group == "null" )
            {
            	group = "Guest";
            	perm.playerAddGroup(event.getPlayer(), "Guest");
            }
    	}
        
        String prefix = getConfig().getString("groups." + group + ".prefix", "&f");
        
        String className = getPlayerClass(event.getPlayer());
        
        String m = event.getMessage();

    	//m = ChatColor.DARK_RED + "[This message has been Ponified]";
        
        if ( m.contains("fuck") )
        {
        	getServer().getConsoleSender().sendMessage(ChatColor.RED + "Censored Chat: " + m);
        	m = "I like to dress in dresses on Saturday Night and play with dolls :)";
        }
        	

        if ( m.contains("nigger") )
        {
        	getServer().getConsoleSender().sendMessage(ChatColor.RED + "Censored Chat: " + m);
        	m = "I pleasure myself to your avatar at night :)";
        }

        if ( m.contains("penis") )
        {
        	getServer().getConsoleSender().sendMessage(ChatColor.RED + "Censored Chat: " + m);
        	m = "I still wet my bed since I'm only 6 :)";
        }
        
        m = m.replaceFirst( "everybody", "evenypony" );
        m = m.replaceFirst( "everyone", "evenypony" );
        m = m.replaceFirst( "anybody", "anypony" );
        m = m.replaceFirst( "anyone", "anypony" );
        
        event.setFormat( "<" + ChatColor.DARK_AQUA + "[" + className.trim() + "] " + ChatColor.translateAlternateColorCodes("&".charAt(0), prefix) + "[" + group.trim() + "] " + ChatColor.RED + event.getPlayer().getDisplayName() + ChatColor.RESET + "> " + ChatColor.translateAlternateColorCodes("&".charAt(0), m) );
    }
    
    @EventHandler
    public void onBlockBreakEvent (BlockBreakEvent event)
    {
    	if ( event.isCancelled() )
    		return;
    	
    	Player player = event.getPlayer();
		Block b = event.getBlock();
		
    	if ( b.getType() == Material.WOOL )
    	{
    		Block d = b.getRelative(BlockFace.UP);
    		
    		if ( d != null && ( d.getType() == Material.WOODEN_DOOR || d.getType() == Material.IRON_DOOR_BLOCK || d.getTypeId() == 229 ) )
    		{
    			String color = getWoolColor( b );
				
				if ( player != null && player.hasPermission("inations." + color.toLowerCase()) )
				{
					if ( player instanceof Player )
						player.sendMessage(ChatColor.AQUA + "You have permission to remove " + color + " Wool colored doors.");
				}
				else if ( player != null && ( player.hasPermission("inations.admin") || player.isOp() ) )
				{
					if ( player instanceof Player )
						player.sendMessage(ChatColor.DARK_AQUA + "You don't have permission to remove " + color + " Wool colored doors. But you have the override perm.");	
				}
				else
				{
					if ( player instanceof Player )
					{
						player.sendMessage(ChatColor.RED + "You don't have permission to remove " + color + " Wool colored doors.");	
					}
					else
					{
						getServer().getConsoleSender().sendMessage(ChatColor.RED + "[iNations] Breaking of " + color + " Wool colored door prevented.");
					}
					event.setCancelled(true);
					return;
				}
    		}
    	}
    	
    	if ( player instanceof Player )
    	{
    	
    	// Drop Rates Modifier
	    	
    		if ( getConfig().getBoolean("classes." + getPlayerClass( player ) + ".earthDrop") )
    		{
    			double n = Math.random();
    			
    			if (b.getType() == Material.IRON_ORE)
    			{
    				if (n < 0.6)
    				{
	    				event.setCancelled(true);
	                    b.setType(Material.AIR); 
	                    b.getWorld().dropItem( 
	                            b.getLocation(), 
	                            new ItemStack(Material.IRON_INGOT, 2)); 

    				}
    				if (n < 0.03)
    				{
    					event.setCancelled(true);
	                    b.setType(Material.AIR); 
    					b.getWorld().dropItem( 
	                            b.getLocation(), 
	                            new ItemStack(Material.DIAMOND, 1));
    				}
    			}
    			
    			if (b.getType() == Material.LEAVES)
    			{
    				if ( n < 0.3 )
    				{
    					event.setCancelled(true);
	                    b.setType(Material.AIR); 
    					b.getWorld().dropItem( 
	                            b.getLocation(), 
	                            new ItemStack(Material.APPLE, 1));
    				}
    			}
    		}
    		
			// Remaining iNations
		
			if (event.getPlayer().getGameMode() == GameMode.CREATIVE && !event.isCancelled())
			{
				event.getBlock().setTypeId(0);
				event.getBlock().breakNaturally();
			}
			
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
						if ( event.getPlayer().isOp() || event.getPlayer().hasPermission("inations.admin") )
						{
							event.getPlayer().sendMessage(ChatColor.DARK_RED + "You have permission to override the build protections of this plot.");
						}
						else
						{
							event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break blocks on this plot.");
							event.setCancelled(true);  					
						}
					}
				}
			}
    	}
    }
    
    @EventHandler
    public void onBlockPlaceEvent (BlockPlaceEvent event)
    {
    	//Player player = event.getPlayer();
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
    				if ( event.getPlayer().isOp() || event.getPlayer().hasPermission("inations.admin") )
    				{
    					event.getPlayer().sendMessage(ChatColor.DARK_RED + "You have permission to override the build protections of this plot.");
    				}
    				else
    				{
    					event.getPlayer().sendMessage(ChatColor.DARK_RED + "You don't have permission to build on this plot.");
        				event.setBuild(false);
        				return;
    				}
    			}
    		}
    	}
		
		if ( event.isCancelled() )
			return;
		
		//if ( event.getBlockPlaced().getType() == Material.WALL_SIGN )
			//sendDebug( event.getBlockPlaced().toString() );
    }
    
    public String getWoolColor (Block b)
    {
    	String color = "";
    	
    	switch ( b.getData() )
		{
			case (byte) 0: color = "White"; break;
			case (byte) 1: color = "Orange"; break;
			case (byte) 2: color = "Magenta"; break;
			case (byte) 3: color = "Light Blue"; break;
			case (byte) 4: color = "Yellow"; break;
			case (byte) 5: color = "Lime Green"; break;
			case (byte) 6: color = "Pink"; break;
			case (byte) 7: color = "Gray"; break;
			case (byte) 8: color = "Light Gray"; break;
			case (byte) 9: color = "Cyan"; break;
			case (byte) 10: color = "Purple"; break;
			case (byte) 11: color = "Blue"; break;
			case (byte) 12: color = "Brown"; break;
			case (byte) 13: color = "Green"; break;
			case (byte) 14: color = "Red"; break;
			case (byte) 15: color = "Black"; break;
		}
    	
    	return color;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChangeEvent(SignChangeEvent event) 
    {
    	Block b = event.getBlock();
   		//Sign s = (Sign) b.getState();
   		String m = event.getLine(0);
    	Player p = event.getPlayer();
    		
		if ( m.equalsIgnoreCase("[locked]") )
		{
			event.setLine(0, ChatColor.GOLD + m);
			event.setLine(3, p.getName());
			
			// TODO: Let colors not interfer with checks
			//event.setLine(1, ChatColor.AQUA + event.getLine(1));
			//event.setLine(2, ChatColor.AQUA + event.getLine(2));
			//event.setLine(3, ChatColor.AQUA + event.getLine(3));
			
			OfflinePlayer offP1 = getServer().getOfflinePlayer(event.getLine(1));
			OfflinePlayer offP2 = getServer().getOfflinePlayer(event.getLine(2));
			
			if ( !offP1.hasPlayedBefore() && !offP1.getName().isEmpty() )
			{
				p.sendMessage( ChatColor.RED + "Player " + offP1.getName() + " has never played on this server." );
				event.setLine(1, "");
			}
			
			if ( !offP2.hasPlayedBefore() && !offP2.getName().isEmpty() )
			{
				p.sendMessage( ChatColor.RED + "Player " + offP2.getName() + " has never played on this server." );
				event.setLine(2, "");
			}
			
			if ( offP1.getName() == p.getName() )
				event.setLine(1, "");
			
			if ( offP2.getName() == p.getName() )
				event.setLine(2, "");
			
			List<Block> doors = new ArrayList<Block>();
			
			Block bottom = b.getRelative(BlockFace.DOWN);
			if ( bottom != null )
			{
				doors.add( bottom.getRelative(BlockFace.NORTH) );
				doors.add( bottom.getRelative(BlockFace.EAST) );
				doors.add( bottom.getRelative(BlockFace.SOUTH) );
				doors.add( bottom.getRelative(BlockFace.WEST) );
				doors.add( bottom.getRelative(BlockFace.NORTH_EAST) );
				doors.add( bottom.getRelative(BlockFace.NORTH_WEST) );
				doors.add( bottom.getRelative(BlockFace.SOUTH_EAST) );
				doors.add( bottom.getRelative(BlockFace.SOUTH_WEST) );
				doors.add( bottom );
			}
			
			if ( bottom.getRelative(BlockFace.DOWN) != null )
				doors.add( bottom.getRelative(BlockFace.DOWN) );
			
			doors.add( b.getRelative(BlockFace.NORTH) );
			doors.add( b.getRelative(BlockFace.EAST) );
			doors.add( b.getRelative(BlockFace.SOUTH) );
			doors.add( b.getRelative(BlockFace.WEST) );
			doors.add( b.getRelative(BlockFace.NORTH_EAST) );
			doors.add( b.getRelative(BlockFace.NORTH_WEST) );
			doors.add( b.getRelative(BlockFace.SOUTH_EAST) );
			doors.add( b.getRelative(BlockFace.SOUTH_WEST) );
			
			Block top = b.getRelative(BlockFace.UP);
			if ( top != null )
			{
				doors.add( top.getRelative(BlockFace.NORTH) );
				doors.add( top.getRelative(BlockFace.EAST) );
				doors.add( top.getRelative(BlockFace.SOUTH) );
				doors.add( top.getRelative(BlockFace.WEST) );
				doors.add( top.getRelative(BlockFace.NORTH_EAST) );
				doors.add( top.getRelative(BlockFace.NORTH_WEST) );
				doors.add( top.getRelative(BlockFace.SOUTH_EAST) );
				doors.add( top.getRelative(BlockFace.SOUTH_WEST) );
				doors.add( top );
			}
			
			if ( top.getRelative(BlockFace.UP) != null )
				doors.add( top.getRelative(BlockFace.UP) );
			
			Block door = null;
			
			for ( Block d : doors )
			{
				Material mm = d.getType();
				if ( mm == Material.WOOD_DOOR || mm == Material.WOODEN_DOOR || mm == Material.IRON_DOOR || mm == Material.IRON_DOOR_BLOCK || mm.getId() == 229 || mm.getId() == 30185 )
				{
					if ( d.getRelative(BlockFace.DOWN).getType() == Material.WOOL )
					{
						door = d;
					}
					else
					{
						door = d;
						break;
					}
				}
			}
			
			if ( door == null )
			{
				p.sendMessage(ChatColor.RED + "Sign creation denied as no elegible door was found.");
				event.setCancelled(true);
				b.breakNaturally();
				return;
			}
			
			if ( door.getRelative(BlockFace.DOWN).getType() == Material.WOOL || door.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.WOOL )
			{
				p.sendMessage(ChatColor.RED + "Sign creation denied as the door is already locked w/ wool.");
				event.setCancelled(true);
				b.breakNaturally();
				return;
			}
			
			if ( p != null )
				p.sendMessage(ChatColor.AQUA + "You have successfully created a locked door sign. =>");
			
		}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //World world = player.getWorld();
        
        if ( event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK )
        {
        	Material m = event.getClickedBlock().getType();
		
			if ( event.getBlockFace() == BlockFace.UP && m == Material.WOOL && event.getAction() == Action.RIGHT_CLICK_BLOCK )
			{
				Material b = player.getItemInHand().getType();
				
				if ( b == Material.WOOD_DOOR || b == Material.IRON_DOOR || b.getId() == 229 || b.getId() == 30185 )
				{
					String color = getWoolColor( event.getClickedBlock() );
					
					if ( player.hasPermission("inations." + color.toLowerCase()) )
					{
						player.sendMessage(ChatColor.AQUA + "You have permission to build " + color + " Wool colored doors.");
					}
					else if ( player.hasPermission("inations.admin") || player.isOp() )
					{
						player.sendMessage(ChatColor.DARK_AQUA + "You don't have permission to build " + color + " Wool colored doors. But you have the override perm.");
					}
					else
					{
						player.sendMessage(ChatColor.RED + "You don't have permission to build " + color + " Wool colored doors. For then it would become unbreakable and unaccessable by you.");
						event.setCancelled(true);
						return;
					}
				}
			}
			
	    	if ( m == Material.WOOD_DOOR || m == Material.WOODEN_DOOR || m == Material.IRON_DOOR || m == Material.IRON_DOOR_BLOCK || m.getId() == 229 || m.getId() == 30185 )
	    	{
	    		if ( event.getAction() == Action.LEFT_CLICK_BLOCK )
				{
	    			player.sendMessage(ChatColor.RED + "Sorry, Left clicking of doors is disabled. You must remove the floor under the door to remove it.");
	    			event.setCancelled(true);
	    			return;
				}
	    		
	    		Block doorBlockClicked = event.getClickedBlock();
	    		Block door1Top = null;
	    		Block door1Bottom = null;
	    		
	    		Block door2Bottom = null;
	    		
	    		if ( doorIsTopHalf(doorBlockClicked.getData()) )
				{
					door1Top = doorBlockClicked;
					door1Bottom = doorBlockClicked.getRelative(BlockFace.DOWN);
				}
				else
				{
					door1Top = doorBlockClicked.getRelative(BlockFace.UP);
					door1Bottom = doorBlockClicked;
				}
	    		
	    		if ( door1Bottom != null )
				{
					Block ck = door1Bottom.getRelative(BlockFace.DOWN);
					
					if ( ck.getType() == Material.WOOL )
					{
						String color = getWoolColor( ck );
						
						if ( player.hasPermission("inations." + color.toLowerCase()) )
						{
							player.sendMessage(ChatColor.AQUA + "You have permission to open " + color + " Wool colored doors.");
						}
						else if ( player.hasPermission("inations.admin") || player.isOp() )
						{
							player.sendMessage(ChatColor.DARK_AQUA + "You don't have permission to open " + color + " Wool colored doors. But you have the override perm.");
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You don't have permission to open " + color + " Wool colored doors.");
							event.setCancelled(true);
							return;
						}
					}
					else
					{
						List<Block> signs = new ArrayList<Block>();
						String[] msg = null;
						
						Block top = door1Top.getRelative(BlockFace.UP);
						if ( top != null )
						{
							signs.add( top.getRelative(BlockFace.NORTH) );
							signs.add( top.getRelative(BlockFace.EAST) );
							signs.add( top.getRelative(BlockFace.SOUTH) );
							signs.add( top.getRelative(BlockFace.WEST) );
							signs.add( top.getRelative(BlockFace.NORTH_EAST) );
							signs.add( top.getRelative(BlockFace.NORTH_WEST) );
							signs.add( top.getRelative(BlockFace.SOUTH_EAST) );
							signs.add( top.getRelative(BlockFace.SOUTH_WEST) );
							signs.add( top );
						}
						
						if ( top.getRelative(BlockFace.UP) != null )
							signs.add( top.getRelative(BlockFace.UP) );
						
						if ( door1Top != null )
						{
							signs.add( door1Top.getRelative(BlockFace.NORTH) );
							signs.add( door1Top.getRelative(BlockFace.EAST) );
							signs.add( door1Top.getRelative(BlockFace.SOUTH) );
							signs.add( door1Top.getRelative(BlockFace.WEST) );
							signs.add( door1Top.getRelative(BlockFace.NORTH_EAST) );
							signs.add( door1Top.getRelative(BlockFace.NORTH_WEST) );
							signs.add( door1Top.getRelative(BlockFace.SOUTH_EAST) );
							signs.add( door1Top.getRelative(BlockFace.SOUTH_WEST) );
						}
						
						signs.add( door1Bottom.getRelative(BlockFace.NORTH) );
						signs.add( door1Bottom.getRelative(BlockFace.EAST) );
						signs.add( door1Bottom.getRelative(BlockFace.SOUTH) );
						signs.add( door1Bottom.getRelative(BlockFace.WEST) );
						signs.add( door1Bottom.getRelative(BlockFace.NORTH_EAST) );
						signs.add( door1Bottom.getRelative(BlockFace.NORTH_WEST) );
						signs.add( door1Bottom.getRelative(BlockFace.SOUTH_EAST) );
						signs.add( door1Bottom.getRelative(BlockFace.SOUTH_WEST) );
						
						Block bottom = door1Bottom.getRelative(BlockFace.DOWN);
						if ( bottom != null )
						{
							signs.add( bottom.getRelative(BlockFace.NORTH) );
							signs.add( bottom.getRelative(BlockFace.EAST) );
							signs.add( bottom.getRelative(BlockFace.SOUTH) );
							signs.add( bottom.getRelative(BlockFace.WEST) );
							signs.add( bottom.getRelative(BlockFace.NORTH_EAST) );
							signs.add( bottom.getRelative(BlockFace.NORTH_WEST) );
							signs.add( bottom.getRelative(BlockFace.SOUTH_EAST) );
							signs.add( bottom.getRelative(BlockFace.SOUTH_WEST) );
							signs.add( bottom );
						}
						
						if ( bottom.getRelative(BlockFace.DOWN) != null )
							signs.add( bottom.getRelative(BlockFace.DOWN) );
						
						for ( Block sign : signs )
						{
							if ( sign.getState() instanceof Sign )
							{
								Sign s = (Sign) sign.getState();
					        	
					        	if ( s.getLine(0).replaceAll(ChatColor.GOLD.toString(), "").equals("[locked]") )
					        	{
					        		msg = s.getLines();
									break;
					        	}
							}
						}
						
						if ( msg != null )
						{
							Boolean allowed = false;
							
							for ( String mm : msg )
							{
								if ( getServer().getPlayer(mm) == player )
								{
									allowed = true;
									break;
								}
							}
							
							if ( allowed )
							{
								player.sendMessage(ChatColor.AQUA + "You have been permitted to open this locked door.");
							}
							else
							{
								if ( player.isOp() || player.hasPermission("inations.admin") )
								{
									player.sendMessage(ChatColor.RED + "Not permitted to open but you have override permissions.");
								}
								else
								{
									player.sendMessage(ChatColor.RED + "You are not permitted to open this locked door.");
									event.setCancelled(true);
									return;
								}
							}
						}
					}
					
					if ( m == Material.IRON_DOOR || m == Material.IRON_DOOR_BLOCK || m.getId() == 229 || m.getId() == 30185 ) // 229 - Tekkit Reinforced Doors
		        	{
		    			if ( player.isOp() || player.hasPermission("inations.admin") )
		    			{
		    				if ( ( door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.NORTH).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.NORTH).getData())) {
		    					door2Bottom = door1Bottom.getRelative(BlockFace.NORTH);
		    				}
		    				else if ( ( door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.SOUTH).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.SOUTH).getData())) {
		    					door2Bottom = door1Bottom.getRelative(BlockFace.SOUTH);
		    				}
		    				else if ( ( door1Bottom.getRelative(BlockFace.EAST).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.EAST).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.EAST).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.EAST).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.EAST).getData())) {
		    					door2Bottom = door1Bottom.getRelative(BlockFace.EAST);
		    				}
		    				else if ( ( door1Bottom.getRelative(BlockFace.WEST).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.WEST).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.WEST).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.WEST).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.WEST).getData())) {
		    					door2Bottom = door1Bottom.getRelative(BlockFace.WEST);
		    				}
		    				
		    				if (door2Bottom != null)
		    				{
		    					if ( doorIsOpen(door1Bottom.getData()) == doorIsOpen(door2Bottom.getData())) {
		    						door2Bottom.setData(flipDoor(door2Bottom.getData()));
		    					}
		    				}
		    				
		    				if (door1Bottom != null)
		    				{
		    					door1Bottom.setData(flipDoor(door1Bottom.getData()));
		    				}
		    	    		
		    	    		event.setCancelled(true);
		    	    	}
		        	}
		        	
		    		if ( m == Material.WOOD_DOOR || m == Material.WOODEN_DOOR ) 
		    		{
		    			if ( ( door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.NORTH).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.NORTH).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.NORTH).getData())) {
		    				door2Bottom = door1Bottom.getRelative(BlockFace.NORTH);
		    			}
		    			else if ( ( door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.SOUTH).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.SOUTH).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.SOUTH).getData())) {
		    				door2Bottom = door1Bottom.getRelative(BlockFace.SOUTH);
		    			}
		    			else if ( ( door1Bottom.getRelative(BlockFace.EAST).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.EAST).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.EAST).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.EAST).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.EAST).getData())) {
		    				door2Bottom = door1Bottom.getRelative(BlockFace.EAST);
		    			}
		    			else if ( ( door1Bottom.getRelative(BlockFace.WEST).getType() == Material.IRON_DOOR_BLOCK || door1Bottom.getRelative(BlockFace.WEST).getType() == Material.WOODEN_DOOR || door1Bottom.getRelative(BlockFace.WEST).getType() == Material.IRON_DOOR || door1Bottom.getRelative(BlockFace.WEST).getTypeId() == 229 ) && doorIsConnected(door1Top.getData(), door1Top.getRelative(BlockFace.WEST).getData())) {
		    				door2Bottom = door1Bottom.getRelative(BlockFace.WEST);
		    			}
		    			
		    			if (door2Bottom != null) {
		    				if ( doorIsOpen(door1Bottom.getData()) == doorIsOpen(door2Bottom.getData())) {
		    					door2Bottom.setData(flipDoor(door2Bottom.getData()));
		    				}
		    			}
		    		}
				}
	    	}
        }
    	
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockRightClick(event);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            handleAirRightClick(event);
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleBlockLeftClick(event);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
            handleAirLeftClick(event);
        } else if (event.getAction() == Action.PHYSICAL) {
            handlePhysicalInteract(event);
        }
    }
    
    // WorldGuard Event Handlers
    
    @EventHandler
    public void onRegionLeave(RegionLeaveEvent e)
    {
      if (e.getRegion().getId().equals("jail") && e.isCancellable()) // you cannot cancel the event if the player leaved the region because he died
      {
        e.setCancelled(true);
        e.getPlayer().sendMessage("You cannot leave the jail!");
      }
    }
    
    @EventHandler
    public void onRegionEnter(RegionEnterEvent e)
    {
      e.getPlayer().sendMessage("You just entered " + e.getRegion().getId());
    }
    
    /**
     * Called when a player left clicks air.
     *
     * @param event Thrown event
     */
    private void handleAirLeftClick(PlayerInteractEvent event)
    {
         // I don't think we have to do anything here yet.
         return;
    }
    
    /**
     * Called when a player left clicks a block.
     *
     * @param event Thrown event
     */
    private void handleBlockLeftClick(PlayerInteractEvent event)
    {
    	//Player player = event.getPlayer();
    	
    	if ( event.getPlayer().getItemInHand().getTypeId() == 271 )
    	{
    		if ( event.getClickedBlock() != null )
    		{
    			Location loc = event.getClickedBlock().getLocation();
        		Vector vector = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        		
        		setMetadata(event.getPlayer(), "position1", vector);
    			event.getPlayer().sendMessage(ChatColor.AQUA + "[iNations]" + ChatColor.WHITE + " You have set position #1! (X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ")" );
    			event.setCancelled(true);
    			return;
    		}
    	}
    }
    
    /**
     * Called when a player right clicks air.
     *
     * @param event Thrown event
     */
    private void handleAirRightClick(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;
        
        //I don't think we have to do anything here yet.
    }
    
    /**
     * Called when a player right clicks a block.
     *
     * @param event Thrown event
     */
    private void handleBlockRightClick(PlayerInteractEvent event)
    {
    	//Player player = event.getPlayer();
    	//int type = event.getClickedBlock().getTypeId();
    	
    	/*
    	if ( player.getItemInHand().getTypeId() == 288 && ( player.isOp() || player.hasPermission("inations.admin") ) )
    	{
    		Block b = event.getClickedBlock();
    		
    		if ( b == null )
    		{
    			player.sendMessage(ChatColor.RED + "There is no block within sight.");
    		}
    		else
    		{
    			player.sendMessage(ChatColor.AQUA + b.toString());
    		}
    	}
    	else if ( player.getItemInHand().getTypeId() == 271 )
    	{
    		if ( event.getClickedBlock() != null )
    		{
    			if (lockWithKey == null || lockWithKey.isEmpty())
    			{
        			Location loc = event.getClickedBlock().getLocation();
            		Vector vector = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            		
            		setMetadata(event.getPlayer(), "position2", vector);
        			event.getPlayer().sendMessage(ChatColor.AQUA + "[iNations]" + ChatColor.WHITE + " You have set position #2! (X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ")" );
        			event.setCancelled(true);
    			}
    			else
    			{
    				 if (type == BlockID.CHEST
    						 || type == BlockID.JUKEBOX
    						 || type == BlockID.DISPENSER
    						 || type == BlockID.FURNACE
    						 || type == BlockID.BURNING_FURNACE
    						 || type == BlockID.BREWING_STAND
    						 || type == BlockID.ENCHANTMENT_TABLE
    						 || type == BlockID.CAULDRON)
    				 {
    					 if (iLocks.getLock(ChangeCordType(event.getClickedBlock().getLocation()), event.getClickedBlock().getLocation().getWorld()) == null)
    					 {
	    					 iLocks.add(ChangeCordType(event.getClickedBlock().getLocation()), lockWithKey);
	    		        		
	    					 db.query("INSERT INTO `locks` (" +
	    							 "`x`, " +
	    							 "`y`, " +
	    							 "`z`, " +
	    							 "`keyName`" +
	    							 ") VALUES (" +
	    							 "'" + event.getClickedBlock().getX() + "', " +
	    							 "'" + event.getClickedBlock().getY() + "', " +
	    							 "'" + event.getClickedBlock().getZ() + "', " +
	    							 "'" + lockWithKey + "');");
	    					 
	    					 player.sendMessage(ChatColor.AQUA + "You have successfully locked this " + event.getClickedBlock().getType().toString() + " with key \"" + lockWithKey + "\".");
	    					 
	    					 lockWithKey = null;
    					 }
    					 else
    					 {
    						 player.sendMessage(ChatColor.DARK_RED + "This block has been previously locked. Type \"/iadmin lock\" again to disable lock mode.");
    					 }
    				 }
    				 else
    				 {
    					 player.sendMessage(ChatColor.DARK_RED + "This block can not be locked. Type \"/iadmin lock\" again to disable lock mode.");
    				 }
    			}
    			
    			event.setCancelled(true);
    			return;
    		}
    	}
    	/*
    	else if ( player.getItemInHand().getType() == Material.AIR && event.getClickedBlock().getType() == Material.BOOKSHELF )
    	{
    			//player.sendMessage(ChatColor.DARK_AQUA + "You have taken a new book from this bookshelf");
    			//player.setItemInHand(new ItemStack(Material.BOOK, 1));
    			
    			
    	}
    	else if ( player.getItemInHand().getType() == Material.BOOK && event.getClickedBlock().getType() == Material.BOOKSHELF )
    	{
    		player.sendMessage(ChatColor.DARK_AQUA + "You have put that book away in this bookshelf");
    		
    		if ( player.getItemInHand().getAmount() == 1 )
    		{
    			player.setItemInHand(new ItemStack(Material.AIR));
    		}
    		else
    		{
    			player.getItemInHand().setAmount( player.getItemInHand().getAmount() - 1 );
    		}
    	}*/
    	
    	// TODO: Auto-close timer
    	
    	/*
    	if ( type == BlockID.BOOKCASE && !event.getBlockFace().equals( BlockFace.UP ) && !event.getBlockFace().equals( BlockFace.DOWN ) )
    	{
    		Inventory i = getServer().createInventory(null, 18, "Bookshelf");
    		i.setMaxStackSize(1);
    		i.setItem(0, new ItemStack(Material.BOOK, 1));
    		i.setItem(1, new ItemStack(Material.BOOK, 1));
    		i.setItem(2, new ItemStack(Material.BOOK, 1));
    		
    		i.addItem(new ItemStack(Material.BOOK, 3));
    		
			player.openInventory(i);
			
			event.setCancelled(true);
			
			String x = "";
			
			for(int c=0; c<18; c++)
			{
				ItemStack ii = i.getItem(c);
				if ( ii != null )
				{
					x += " " + ii.toString();
				}
				else
				{
					x += " X";
				}
			}
			
			sendDebug( x );
			
			sendDebug( i.getItem(0).serialize().toString() );
    	}
    	
//    	if (event.isCancelled())
//    		return;
    	
		if (type == BlockID.CHEST
				 || type == BlockID.JUKEBOX
				 || type == BlockID.DISPENSER
				 || type == BlockID.FURNACE
				 || type == BlockID.BURNING_FURNACE
				 || type == BlockID.BREWING_STAND
				 || type == BlockID.ENCHANTMENT_TABLE
				 || type == BlockID.CAULDRON)
		 {
			 Lock lock = iLocks.getLock(ChangeCordType(event.getClickedBlock().getLocation()), event.getClickedBlock().getLocation().getWorld()); 
			 
			 if ( lock != null )
			 {
				 if (!lock.playerHasKey(event.getPlayer()))
				 {
					 player.sendMessage(ChatColor.DARK_RED + "You're now permitted to use that " + event.getClickedBlock().getType().toString().toLowerCase() + ". It requires a key.");
					 event.setUseInteractedBlock(Result.DENY);
					 event.setCancelled(true);
					 return;
				 }
				 else
				 {
					 player.sendMessage(ChatColor.DARK_GREEN + "You have the key to unlock this " + event.getClickedBlock().getType().toString().toLowerCase() + ".");
				 }
			 }
			 else
			 {
				 player.sendMessage(ChatColor.DARK_GREEN + "This " + event.getClickedBlock().getType().toString().toLowerCase() + " is unlocked. Please respect others property.");
			 }
		 }
		 */
    }
    
	private boolean doorIsConnected(byte door1Top, byte door2Top) {
		if ((door1Top & 0x1) == (door2Top & 0x1)) //If hinges are both the same side.
			return false;
		else 
			return true;
		
	}
	
	private boolean doorIsTopHalf(byte data) {
		if((data & 0x8) == 0x08)
			return true;
		else 
			return false;

	}
	
	private boolean doorIsOpen(byte data) {
		if((data & 0x4) == 0x4) 
			return true;
		else 
			return false;

	}
	
	private byte flipDoor(byte data) {
		return (byte) (doorIsOpen(data) ? (data & ~0x4) : (data | 0x4));
	}
    
    /**
     * Called when a player steps on a pressure plate or tramples crops.
     *
     * @param event Thrown event
     */
    private void handlePhysicalInteract(PlayerInteractEvent event) {
        if (event.isCancelled())
        	return;
        
        Location l = event.getClickedBlock().getLocation();
        
        l.setY( l.getY() - 2 );
        
        if ( l.getBlock().getState() instanceof Sign )
        {
        	Sign s = (Sign) l.getBlock().getState();
        	
        	String[] msg = s.getLines();
        	
        	Boolean oo = false;
        	String ms = "";
        	
        	for ( String m : msg )
        	{
        		if ( !m.isEmpty() && m.substring(0, 1).equals("/") )
        		{
        			Bukkit.dispatchCommand(event.getPlayer(), m.substring(1));
        		}
        		else
        		{
        			if ( oo )
        			{
        				ms += m + " ";
        			}
        		}
        			
        		if ( m.toLowerCase().equals("[tell]") )
        		{
        			oo = true;
        		}
        	}
        	
        	if ( !ms.isEmpty() )
        		event.getPlayer().sendMessage( ChatColor.DARK_AQUA + ChatColor.translateAlternateColorCodes("&".charAt(0), ms) );
        }
    }
    
    /**
     * Called when a player drops an item. i.e. Presses the Q key.
     *
     * @param event Thrown event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin (PlayerJoinEvent event)
    {
    	// Clear player position metadata
    	setMetadata(event.getPlayer(), "position1", null);
		setMetadata(event.getPlayer(), "position2", null);
		
		if (event.getPlayer().isOp())
		{
			
		}
		
		// Retrieve and send "Message of the Day" to user
        String msg = getConfig().getString("global.motd");
        if (!msg.isEmpty())
        {
        	event.getPlayer().sendMessage(msg);
        }
		
        // Find plots that belong to player and reference the player for future use.
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
        	event.getPlayer().sendMessage("You don't currently seem to be a citizen of any nation.");
        	
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
        		event.getPlayer().sendMessage("You may use /gohome to return to your land.");
        	}
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
package com.chiorichan.bukkit.plugin.iNations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.permission.Permission;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
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
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.applebloom.api.WebSocketService;

import com.chiorichan.bukkit.plugin.Cuboid;
import com.chiorichan.bukkit.plugin.DataStore;
import com.sk89q.worldedit.Vector;

public class INations extends JavaPlugin implements Listener
{
	public Boolean pluginEnabled = false;
	// public SQLite db;
	
	public static Permission perm = null;
	public Boolean broadEnabled = true;
	public int syncId = -1;
	
	public static GriefPrevention gp = null;
	
	public Map<String, Cuboid> regions = new HashMap<String, Cuboid>();
	
	public DataStore iDataStore = new DataStore( this );
	
	public static INations instance = null;
	
	public Map<String, Player[]> ChatRooms = new HashMap<String, Player[]>();
	
	private static long lastRedstomeCommandBlock = 0;
	
	public HashMap<String, Integer> slotQue = new HashMap<String, Integer>();
	public String motd_pre = null;
	public String motd_suf = "";
	
	public int lastId = -1;
	
	public WebSocketService wss = null;
	
	public void onDisable()
	{
		wss.disable();
		wss = null;
		// db.close();
		saveConfig();
		iDataStore.saveRegions();
		
		System.out.println( this.toString() + " has been unloaded." );
	}
	
	public void broadcastMessage()
	{
		Player[] players = Bukkit.getOnlinePlayers();
		Random rand = new Random();
		
		ConfigurationSection conf = getConfig().getConfigurationSection( "messages" );
		Map<String, Object> msgs = conf.getValues( true );
		
		String broadcast = "";
		
		if ( msgs.size() > 0 )
		{
			int choice = -1;
			int x = 0;
			
			while ( lastId == choice )
			{
				choice = rand.nextInt( msgs.size() );
			}
			
			lastId = choice;
			
			for ( Object msg : msgs.values() )
			{
				if ( x == choice )
				{
					broadcast = msg.toString();
					break;
				}
				
				x++;
			}
			
			if ( broadcast.equals( "" ) )
				return;
			
			/*
			 * for (Player p : players) { p.sendMessage( ChatColor.RED + "[Announcement] " + ChatColor.RESET +
			 * broadcast.toString() ); }
			 */
			
			Boolean playersOnline = false;
			
			for ( Player p : players )
				if ( !p.hasPermission( "inations.admin" ) && !p.isOp() )
					playersOnline = true;
			
			if ( playersOnline )
				getServer().broadcastMessage( ChatColor.translateAlternateColorCodes( '&', getConfig().getString( "global.msgTitle", "&d[Announcement]" ) ) + " " + ChatColor.RESET + ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), broadcast.toString() ) );
			
			// getServer().getConsoleSender().sendMessage( ChatColor.RED +
			// "[Announcement] " + ChatColor.RESET + broadcast.toString() );
		}
	}
	
	public void onEnable()
	{
		instance = this;
		
		getServer().getPluginManager().registerEvents( this, this );
		
		getConfig().options().copyDefaults( true );
		
		INationsCommandExecutor ExeClass = new INationsCommandExecutor( this );
		
		getCommand( "iplayer" ).setExecutor( ExeClass );
		getCommand( "iadmin" ).setExecutor( ExeClass );
		getCommand( "inv" ).setExecutor( ExeClass );
		getCommand( "clear" ).setExecutor( ExeClass );
		getCommand( "t" ).setExecutor( ExeClass );
		getCommand( "o" ).setExecutor( ExeClass );
		getCommand( "e" ).setExecutor( ExeClass );
		getCommand( "c" ).setExecutor( ExeClass );
		getCommand( "q" ).setExecutor( ExeClass );
		getCommand( "g" ).setExecutor( ExeClass );
		getCommand( "tell" ).setExecutor( ExeClass );
		getCommand( "goto" ).setExecutor( ExeClass );
		
		Long ticks = getConfig().getLong( "globals.msgFreq", 5000L );
		
		syncId = Bukkit.getScheduler().scheduleAsyncRepeatingTask( this, new Runnable()
		{
			public void run()
			{
				if ( broadEnabled )
					broadcastMessage();
			}
			
		}, 200L, ticks );
		
		if ( syncId == -1 )
		{
			System.out.println( "We have failed to initalize a synced task." );
		}
		
		/*
		 * Load GriefPrevention Plugin
		 */
		if ( !gp() )
			System.out.println( "[iNations] Grief Prevention plugin was not detected. Please install it so iNations can work properly." );
		
		/*
		 * Load Vault Plugin
		 */
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration( net.milkbowl.vault.permission.Permission.class );
		
		if ( permissionProvider == null )
		{
			System.out.println( "Vault plugin was not detected. Please install it or else we can not properly format chat." );
		}
		else
		{
			perm = permissionProvider.getProvider();
		}
		
		System.out.println( this.toString() + " has been loaded." );
		pluginEnabled = true;
		
		if ( getConfig().getBoolean( "global.remote_enabled", false ) )
			wss = new WebSocketService( getConfig().getString( "global.server_addr", "localhost" ), getConfig().getLong( "global.server_port", 1040 ), this );
		
		loadSlotQue();
	}
	
	public Boolean isAdmin( Player p )
	{
		if ( p.isOp() || p.hasPermission( "iNations.admin" ) )
			return true;
		
		return false;
	}
	
	public Boolean isStaff( Player p )
	{
		if ( isAdmin( p ) || p.hasPermission( "iNations.staff" ) )
			return true;
		
		return false;
	}
	
	public List<Player> adminsOnline( World w )
	{
		List<Player> op = Collections.emptyList();
		
		for ( Player p : getServer().getOnlinePlayers() )
		{
			if ( p.isOp() || p.hasPermission( "inations.admin" ) )
			{
				op.add( p );
			}
		}
		
		return op;
	}
	
	public Vector ChangeCordType( Location loc )
	{
		return new Vector( loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() );
	}
	
	public Location ChangeCordType( Vector vec )
	{
		return new Location( null, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ() );
	}
	
	@EventHandler
	public void onCreatureSpawnEvent( CreatureSpawnEvent event )
	{
		
	}
	
	public void sendDebug( String d ) // Temporary Method to send debug
	// information to my username.
	{
		for ( Player p : getServer().getOnlinePlayers() )
		{
			if ( p.isOp() )
				p.sendMessage( ChatColor.DARK_AQUA + "[iNations Debug] " + ChatColor.WHITE + d );
		}
	}
	
	// \/ Class Events \/
	
	// Called when fuel burned in furnace
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onFurnaceBurnEvent( FurnaceBurnEvent event )
	{
		
	}
	
	// Called when something smelts in furnace
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onFurnaceSmeltEvent( FurnaceSmeltEvent event )
	{
		
	}
	
	// Called when player clicks on inventory item
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onInventoryClickEvent( InventoryClickEvent event )
	{
		
	}
	
	// Called when player opens their inventory
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onInventoryOpenEvent( InventoryOpenEvent event )
	{
		
	}
	
	// Called when player closes their inventory
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onInventoryCloseEvent( InventoryCloseEvent event )
	{
		
	}
	
	// Called before item is crafted
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPrepareItemCraftEvent( PrepareItemCraftEvent event )
	{
		
	}
	
	// Called when item is put in enchantment table
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPrepareItemEnchantEvent( PrepareItemEnchantEvent event )
	{
		
	}
	
	// Called when item was successfully enchanted
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onEnchantItemEvent( EnchantItemEvent event )
	{
		
	}
	
	// \/ World & Weather Events \/
	
	// Call when portal is created
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPortalCreateEvent( PortalCreateEvent event )
	{
		
	}
	
	// Called when tree or mushroom grows
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onStructureGrowEvent( StructureGrowEvent event )
	{
		
	}
	
	// Called when thunder state changes
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onThunderChangeEvent( ThunderChangeEvent event )
	{
		
	}
	
	// Called when world weather changes
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onWeatherChangeEvent( WeatherChangeEvent event )
	{
		
	}
	
	// \/ Block Events \/
	
	// Called when block ignites
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onBlockIgniteEvent( BlockIgniteEvent event )
	{
		
	}
	
	// Called when a note block plays
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onNotePlayEvent( NotePlayEvent event )
	{
		// sendDebug( event.getNote().toString() );
	}
	
	public String getPlayerClass( Player p )
	{
		if ( perm == null )
		{
			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration( net.milkbowl.vault.permission.Permission.class );
			
			if ( permissionProvider == null )
			{
				System.out.println( "Vault plugin was not detected. Please install it or else we can not properly format chat." );
			}
			else
			{
				perm = permissionProvider.getProvider();
			}
		}
		
		if ( perm != null )
		{
			String[] groups;
			try
			{
				groups = perm.getPlayerGroups( p );
			}
			catch ( UnsupportedOperationException e )
			{
				getLogger().info( "Valut Exception: " + e.getMessage() );
				groups = new String[0];
			}
			
			List<String> classes = getConfig().getStringList( "classes" );
			
			if ( groups.length > 1 )
				for ( String grp : groups )
				{
					if ( classes.contains( grp.toLowerCase() ) )
					{
						return grp;
					}
				}
		}
		
		return getConfig().getString( "global.defaultClass", "Awakened" );
	}
	
	// \/ Player Events \/
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerGameModeChangeEvent( PlayerGameModeChangeEvent event )
	{
		if ( !event.isCancelled() )
			setMetadata( event.getPlayer(), "adminRun", event.getNewGameMode() == GameMode.CREATIVE );
	}
	
	// Called when player sprints
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerToggleSprintEvent( PlayerToggleSprintEvent event )
	{
		Player player = event.getPlayer();
		
		if ( event.isSprinting() )
		{
			String className = getPlayerClass( player );
			int speed = getConfig().getInt( "classes." + className + ".speed", -1 );
			
			if ( getMetadata( player, "adminRun" ) == null )
				setMetadata( player, "adminRun", player.getGameMode() == GameMode.CREATIVE );
			
			if ( player.hasPermission( "inations.admin" ) && ( (Boolean) getMetadata( player, "adminRun" ) ) )
			{
				if ( player.getItemInHand().getTypeId() == 57 )
				{
					event.getPlayer().addPotionEffect( new PotionEffect( PotionEffectType.SPEED, 100000, 9 ), true );
				}
				else if ( player.getItemInHand().getTypeId() == 41 )
				{
					event.getPlayer().addPotionEffect( new PotionEffect( PotionEffectType.SPEED, 100000, 7 ), true );
				}
				else if ( player.getItemInHand().getTypeId() == 42 )
				{
					event.getPlayer().addPotionEffect( new PotionEffect( PotionEffectType.SPEED, 100000, 5 ), true );
				}
				else
				{
					event.getPlayer().addPotionEffect( new PotionEffect( PotionEffectType.SPEED, 100000, 3 ), true );
				}
			}
			else
			{
				if ( speed > -1 )
					event.getPlayer().addPotionEffect( new PotionEffect( PotionEffectType.SPEED, 100000, speed ), true );
			}
		}
		else
		{
			event.getPlayer().removePotionEffect( PotionEffectType.SPEED );
		}
	}
	
	// Called when player sneaks
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerToggleSneakEvent( PlayerToggleSneakEvent event )
	{
		
	}
	
	// Called when player changes flight state
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerToggleFlightEvent( PlayerToggleFlightEvent event )
	{
		
	}
	
	// Called when player changes location
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerTeleportEvent( PlayerTeleportEvent event )
	{
		
	}
	
	// Called on player respawn
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerRespawnEvent( PlayerRespawnEvent event )
	{
		
	}
	
	// Called on player leave
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerQuitEvent( PlayerQuitEvent event )
	{
		if ( !getConfig().getString( "chat.leaveFormat", "" ).isEmpty() )
		{
			String format = getConfig().getString( "chat.leaveFormat", "" );
			
			format = format.replace( "{display_name}", event.getPlayer().getDisplayName() );
			
			event.setQuitMessage( ChatColor.translateAlternateColorCodes( '&', format ) );
		}
		
		WebSocketService.send( "MSG &6" + getConfig().getString( "global.remote_prefix" ) + event.getQuitMessage() );
	}
	
	// Called before offical player login
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerPreLoginEvent( PlayerPreLoginEvent event )
	{
		
	}
	
	// Called when player uses a portal
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerPortalEvent( PlayerPortalEvent event )
	{
		
	}
	
	// Called when player picks and item off the ground
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerPickupItemEvent( PlayerPickupItemEvent event )
	{
		
	}
	
	public Boolean gp()
	{
		gp = GriefPrevention.instance;
		
		if ( gp == null )
		{
			getServer().getConsoleSender().sendMessage( "[iNations] Grief Prevention plugin was not detected. Please install it so iNations can work properly." );
			return false;
		}
		
		return true;
	}
	
	// Called when player tries to move
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerMoveEvent( PlayerMoveEvent event )
	{
		Player p = event.getPlayer();
		Location l = event.getTo();
		
		if ( p.getItemInHand().getType() == Material.FIRE && p.getGameMode() == GameMode.CREATIVE && p.isOp() )
		{
			p.getLocation().getBlock().setType( Material.FIRE );
		}
		
		// TODO: Create configuration options
		// Heaven Fall
		if ( l.getBlockY() < -20 && l.getWorld().getName().equals( "skylands" ) )
		{
			l.setWorld( getServer().getWorld( "world" ) );
			l.setY( l.getWorld().getHighestBlockYAt( l ) );
			
			p.teleport( l );
			
			p.setFallDistance( 0 );
			
			p.sendMessage( ChatColor.AQUA + "You have fallen from Pegasus Homeworld and landed on Earth." );
			p.setHealth( 1 );
		}
		else if ( l.getBlockY() > 265 && l.getWorld().getName().equals( "world" ) && getServer().getWorld( "skylands" ) != null )
		{
			String skylands = getConfig().getString( "global.pegasus_homeworld", "skylands" );
			
			l.setWorld( getServer().getWorld( skylands ) );
			l.setY( l.getWorld().getHighestBlockYAt( l ) );
			
			if ( l.getWorld().getHighestBlockYAt( l ) == 0 )
			{
				int degrees = ( Math.round( l.getYaw() ) + 270 ) % 360;
				
				int cnt = 0;
				
				while ( l.getWorld().getHighestBlockYAt( l ) == 0 && cnt <= 50 )
				{
					if ( degrees <= 22 || ( degrees > 292 && degrees <= 259 ) ) // North
					// =
					// -X
					{
						l.setX( l.getX() - 1 );
					}
					else if ( degrees <= 112 ) // East = -Z
					{
						l.setZ( l.getZ() - 1 );
					}
					else if ( degrees <= 202 ) // South = +X
					{
						l.setX( l.getX() + 1 );
					}
					else if ( degrees <= 292 ) // West = +Z
					{
						l.setZ( l.getZ() + 1 );
					}
				}
				
				l.setY( l.getWorld().getHighestBlockYAt( l ) );
				
				if ( cnt > 20 )
				{
					p.sendMessage( ChatColor.RED + "There are no visible landing spots within 50 blocks ahead of you." );
				}
				else
				{
					p.sendMessage( ChatColor.AQUA + "You have landed on the Pegasus Homeland." );
					p.teleport( l );
				}
			}
			else
			{
				p.sendMessage( ChatColor.AQUA + "You have landed on the Pegasus Homeland." );
				p.teleport( l );
			}
		}
	}
	
	// Call on player level change
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerLevelChangeEvent( PlayerLevelChangeEvent event )
	{
		
	}
	
	// Call on player kick
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerKickEvent( PlayerKickEvent event )
	{
		WebSocketService.send( "MSG &6" + getConfig().getString( "global.remote_prefix" ) + event.getLeaveMessage() );
	}
	
	// Called when item in hand changes
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerItemHeldEvent( PlayerItemHeldEvent event )
	{
		
	}
	
	// Called when tool breaks
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerItemBreakEvent( PlayerItemBreakEvent event )
	{
		
	}
	
	// Called when player enters a bed
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerBedEnterEvent( PlayerBedEnterEvent event )
	{
		
	}
	
	// Called when players right-clicks entity
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerInteractEntityEvent( PlayerInteractEntityEvent event )
	{
		
	}
	
	// Use to check for player world change
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerChangedWorldEvent( PlayerChangedWorldEvent event )
	{
		
	}
	
	// Use to check for redstone power event
	@SuppressWarnings( "unchecked" )
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onBlockRedstoneEvent( BlockRedstoneEvent event )
	{
		final Block b = event.getBlock();
		
		class ObjectRunnable implements Runnable
		{
			Object o;
			
			ObjectRunnable(Object s)
			{
				o = s;
			}
			
			@SuppressWarnings( "static-access" )
			public void run()
			{
				for ( String ll : (ArrayList<String>) o )
				{
					String[] ls = ll.toLowerCase().split( "[:]" );
					
					if ( ls.length == 2 && ls[0].startsWith( "@" ) )
					{
						if ( ls[0].equals( "@delay" ) )
						{
							// sendDebug("Delay:" + Long.parseLong(ls[1]));
							
							try
							{
								Thread.currentThread().sleep( Long.parseLong( ls[1] ) );
							}
							catch ( NumberFormatException e )
							{
								e.printStackTrace();
							}
							catch ( InterruptedException e )
							{
								e.printStackTrace();
							}
						}
					}
					else
					{
						Player[] op = getServer().getOnlinePlayers();
						
						for ( Player p : op )
						{
							if ( p.getLocation().distance( b.getLocation() ) < 10 )
							{
								p.sendMessage( ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), ll ) );
							}
						}
					}
				}
			}
		}
		
		if ( b.getState() instanceof Sign && event.getNewCurrent() == 15 )
		{
			Sign s = (Sign) b.getState();
			
			if ( s.getLine( 0 ).toLowerCase().equals( "[redstone]" ) && lastRedstomeCommandBlock + 100 < System.currentTimeMillis() )
			{
				lastRedstomeCommandBlock = System.currentTimeMillis();
				
				for ( String l : s.getLines() )
				{
					if ( !l.isEmpty() && l != null )
					{
						Object als = getConfig().get( "redstone." + l );
						
						if ( als != null && als instanceof ArrayList )
						{
							getServer().getScheduler().scheduleAsyncDelayedTask( this, new ObjectRunnable( als ) );
						}
					}
				}
			}
		}
		else if ( MyDoor.isDoor( b ) )
		{
			MyDoor var1 = MyDoor.getDoor( b );
			
			if ( var1 == null )
				return;
			
			Boolean allowed = false;
			
			for ( Player p : getServer().getOnlinePlayers() )
			{
				if ( event.getBlock().getLocation().getWorld() == p.getWorld() && event.getBlock().getLocation().distance( p.getLocation() ) < 5 )
				{
					if ( p.hasPermission( "inations.admin" ) || p.isOp() || var1.isPermitted( p ) )
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
			
			if ( event.getNewCurrent() > 0 && ( var1.isLocked() || var1.isWoolLocked() ) )
				for ( Player p : getServer().getOnlinePlayers() )
				{
					if ( b.getLocation().getWorld() == p.getWorld() && b.getLocation().distance( p.getLocation() ) < 5 )
					{
						if ( allowed )
						{
							p.sendMessage( ChatColor.AQUA + "Access granted to " + b.getType() + "." );
						}
						else
						{
							p.sendMessage( ChatColor.RED + "Access denied to " + b.getType() + "." );
						}
					}
				}
			
			if ( allowed )
				if ( event.getNewCurrent() > 0 )
					var1.setOpen( true );
				else
					var1.setOpen( false );
			else
			{
				event.setNewCurrent( 0 );
				var1.setOpen( false );
			}
		}
	}
	
	// Use to check players
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onAsyncPlayerPreLoginEvent( AsyncPlayerPreLoginEvent event )
	{
		
	}
	
	public void notifyAdmins( String msg )
	{
		Player players[] = getServer().getOnlinePlayers();
		
		for ( Player p : players )
		{
			if ( p.hasPermission( "iadmin.admin" ) || p.isOp() )
			{
				p.sendMessage( ChatColor.RED + "[iNations] " + ChatColor.WHITE + msg );
			}
		}
	}
	
	@EventHandler( priority = EventPriority.MONITOR )
	public void onAsyncPlayerChatEventMonitor( AsyncPlayerChatEvent event )
	{
		if ( event.isCancelled() )
			return;
		
		WebSocketService.send( "MSG " + event.getFormat() );
	}
	
	// Use to filter player chats
	@EventHandler( priority = EventPriority.LOWEST )
	public void onAsyncPlayerChatEvent( AsyncPlayerChatEvent event )
	{
		if ( event.isCancelled() )
			return;
		
		String group = "Error";
		String className = getConfig().getString( "classes.default", "Awakened" );
		Player p = event.getPlayer();
		
		if ( perm == null )
		{
			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration( net.milkbowl.vault.permission.Permission.class );
			
			if ( permissionProvider == null )
			{
				System.out.println( "Vault plugin was not detected. Please install it or else we can not properly format chat." );
			}
			else
			{
				perm = permissionProvider.getProvider();
			}
		}
		
		if ( perm != null )
		{
			String[] groups;
			try
			{
				groups = perm.getPlayerGroups( event.getPlayer() );
			}
			catch ( UnsupportedOperationException e )
			{
				getLogger().info( "Valut Exception: " + e.getMessage() );
				groups = new String[0];
			}
			
			Set<String> classes = getConfig().getConfigurationSection( "classes" ).getKeys( false );
			
			if ( groups.length < 1 )
			{
				group = "Guest";
				perm.playerAddGroup( event.getPlayer(), group );
				perm.playerAddGroup( event.getPlayer(), className );
			}
			else
			{
				for ( String grp : groups )
				{
					if ( classes.contains( grp ) )
					{
						className = grp;
					}
					else
					{
						group = grp;
					}
				}
			}
		}
		
		/*
		 * if ( className == null ) { className = getPlayerClass( event.getPlayer() ); perm.playerAddGroup(
		 * event.getPlayer(), className ); }
		 */
		
		String m = event.getMessage();
		
		ArrayList<String> saying = new ArrayList<String>();
		saying.add( "I just wet the bed last. My mom says I act like a 6 year old. :)" );
		saying.add( "I still play with barbie dolls :)" );
		saying.add( "I like Justin Bieber" );
		saying.add( "Quick, Shoot me in the face" );
		saying.add( "Everyone is welcomed to all the stuff in my house and if I deny saying this later then I'm lying." );
		saying.add( "I love this server so much and would really like to continue playing with no end in sight." );
		
		if ( m.contains( "fuck" ) || m.contains( "f*ck" ) || m.contains( "f**k" ) || m.contains( "nigger" ) )
		{
			// notifyStaff();
			for ( Player p1 : getServer().getOnlinePlayers() )
				if ( p1.isOp() || p1.hasPermission( "inations.staff" ) )
					p1.sendMessage( ChatColor.AQUA + "[iNations]" + ChatColor.RED + " The server censored " + p.getDisplayName() + ChatColor.RED + "'s chat \"" + m + "\"" );
			
			getServer().getConsoleSender().sendMessage( ChatColor.RED + "Censored Chat: " + m );
			m = saying.get( new Random().nextInt( saying.size() ) );
		}
		
		if ( getConfig().getBoolean( "global.ponifyChat", true ) )
		{
			m = m.replaceFirst( "everybody", "evenypony" );
			m = m.replaceFirst( "everyone", "everypony" );
			m = m.replaceFirst( "anybody", "anypony" );
			m = m.replaceFirst( "anyone", "anypony" );
			m = m.replaceFirst( "every body", "eveny pony" );
			m = m.replaceFirst( "every one", "every pony" );
			m = m.replaceFirst( "any body", "any pony" );
			m = m.replaceFirst( "any one", "any pony" );
			m = m.replaceFirst( "Everybody", "Evenypony" );
			m = m.replaceFirst( "Everyone", "Everypony" );
			m = m.replaceFirst( "Anybody", "Anypony" );
			m = m.replaceFirst( "Anyone", "Anypony" );
			m = m.replaceFirst( "Every body", "Eveny pony" );
			m = m.replaceFirst( "Every one", "Every pony" );
			m = m.replaceFirst( "Any body", "Any pony" );
			m = m.replaceFirst( "Any one", "Any pony" );
		}
		
		String chatMode = (String) getMetadata( p, "ChatMode", "G" );
		String chatTitle = getConfig().getString( "chat.globalChatName", "" );
		Boolean shortTitle = false;
		
		if ( chatMode.equalsIgnoreCase( "o" ) )
		{
			chatTitle = "OP Chat";
			shortTitle = true;
		}
		else if ( chatMode.equalsIgnoreCase( "e" ) )
		{
			chatTitle = "Staff Chat";
			shortTitle = true;
		}
		
		Long lastMsg = (Long) getMetadata( p, "lastMessage" );
		
		if ( lastMsg == null )
			lastMsg = 0L;
		
		setMetadata( p, "lastMessage", System.currentTimeMillis() );
		
		// Fixes a bug with bukkit when you set a message with a percent symbol.
		m = m.replace( "%", "%%" );
		
		String format = getConfig().getString( "chat.longFormat", "<{class_color}[{class_name}] {group_color}[{group_name}] &c{display_name}&r> {msg}" );
		
		if ( ( getConfig().getLong( "chat.shortTimeout", 10000L ) > 0 && lastMsg >= System.currentTimeMillis() - getConfig().getLong( "chat.shortTimeout", 10000L ) ) || shortTitle )
			format = getConfig().getString( "chat.shortFormat", "<&c{display_name}&r> {msg}" );
		
		format = format.replace( "{chat_name}", chatTitle );
		format = format.replace( "{class_color}", getConfig().getString( "classes." + className.trim() + ".color", "&3" ) );
		format = format.replace( "{class_name}", className.trim() );
		format = format.replace( "{group_color}", getConfig().getString( "groups." + group.trim() + ".color", "&f" ) );
		format = format.replace( "{group_name}", group.trim() );
		format = format.replace( "{display_name}", event.getPlayer().getDisplayName() );
		format = format.replace( "{msg}", m );
		
		if ( getConfig().getBoolean( "chat.enabled", true ) )
			event.setFormat( ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), format ) );
		
		event.setMessage( ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), m ) );
		
		if ( chatMode.equalsIgnoreCase( "g" ) )
		{
			// This result was moved to the MONITOR listen level since plugins like ChestShop were interfering.
		}
		else
		{
			Player[] players = getServer().getOnlinePlayers();
			
			getServer().getConsoleSender().sendMessage( event.getFormat() );
			if ( chatMode.equalsIgnoreCase( "o" ) )
			{
				for ( Player player : players )
				{
					if ( player.isOp() )
						player.sendMessage( event.getFormat() );
				}
				
				WebSocketService.send( "OCHAT " + getConfig().getString( "global.remote_prefix" ) + event.getFormat() );
			}
			else if ( chatMode.equalsIgnoreCase( "e" ) )
			{
				for ( Player player : players )
				{
					if ( player.isOp() || player.hasPermission( "inations.staff" ) )
						player.sendMessage( event.getFormat() );
				}
				
				WebSocketService.send( "SCHAT " + getConfig().getString( "global.remote_prefix" ) + event.getFormat() );
			}
			else
			{
				p.sendMessage( ChatColor.RED + "[iNations] " + ChatColor.WHITE + "Your message was not seen by anyone due to some unknown error." );
				setMetadata( p, "ChatMode", "G" );
			}
			
			event.setMessage( "" );
			event.setFormat( "" );
			event.setCancelled( true );
			
			// Double make sure that this message does not show up anyplace else. ie. Dynmap seems to ignore isCancelled.
		}
	}
	
	@EventHandler
	public void onBlockBreakEvent( BlockBreakEvent event )
	{
		if ( event.isCancelled() )
			return;
		
		Player player = event.getPlayer();
		Block b = event.getBlock();
		
		if ( b.getState() instanceof Sign && ( (Sign) b.getState() ).getLines()[0].replaceAll( ChatColor.GOLD.toString(), "" ).equalsIgnoreCase( "[locked]" ) )
		{
			Block door = findNearByLockable( b );
			
			if ( door != null )
			{
				String[] var3 = ( (Sign) b.getState() ).getLines();
				if ( var3 != null )
				{
					Boolean allowed = false;
					
					for ( String mm : var3 )
					{
						mm = mm.replaceAll( ChatColor.AQUA.toString(), "" );
						
						if ( !mm.isEmpty() && player.getName().startsWith( mm ) )
						{
							allowed = true;
							break;
						}
					}
					
					if ( allowed )
					{
						player.sendMessage( ChatColor.AQUA + "You have been permitted to remove this locking sign." );
					}
					else
					{
						if ( player.isOp() || player.hasPermission( "inations.admin" ) )
						{
							player.sendMessage( ChatColor.RED + "Not permitted to use but you have override permissions." );
						}
						else
						{
							player.sendMessage( ChatColor.RED + "You are not permitted to remove this locking sign." );
							event.setCancelled( true );
							return;
						}
					}
				}
			}
		}
		
		if ( b.getType() == Material.WOOL )
		{
			Block d = b.getRelative( BlockFace.UP );
			
			if ( d != null && ( MyDoor.isDoor( d ) || d.getType() == Material.CHEST || d.getTypeId() == 146 ) )
			{
				String color = getWoolColor( b );
				
				if ( player != null && player.hasPermission( "inations." + color.toLowerCase() ) )
				{
					if ( player instanceof Player )
						player.sendMessage( ChatColor.AQUA + "You have permission to remove " + color + " wool colored " + d.getType().name() + "." );
				}
				else if ( player != null && ( player.hasPermission( "inations.admin" ) || player.isOp() ) )
				{
					if ( player instanceof Player )
						player.sendMessage( ChatColor.DARK_AQUA + "You don't have permission to remove " + color + " wool colored " + d.getType().name() + ". But you have the override permissions." );
				}
				else
				{
					if ( player instanceof Player )
					{
						player.sendMessage( ChatColor.RED + "You don't have permission to remove " + color + " wool colored " + d.getType().name() + "." );
					}
					else
					{
						getServer().getConsoleSender().sendMessage( ChatColor.RED + "[iNations] Breaking of " + color + " wool colored " + d.getType().name() + " prevented." );
					}
					event.setCancelled( true );
					return;
				}
			}
		}
		
		if ( player instanceof Player )
		{
			// Drop Rates Modifier for Earth Pony Classes
			if ( getConfig().getBoolean( "classes." + getPlayerClass( player ) + ".earthDrop" ) )
			{
				double n = Math.random();
				
				if ( b.getType() == Material.IRON_ORE )
				{
					if ( n < 0.6 )
					{
						event.setCancelled( true );
						b.setType( Material.AIR );
						b.getWorld().dropItem( b.getLocation(), new ItemStack( Material.IRON_INGOT, 2 ) );
						
					}
					if ( n < 0.03 )
					{
						event.setCancelled( true );
						b.setType( Material.AIR );
						b.getWorld().dropItem( b.getLocation(), new ItemStack( Material.DIAMOND, 1 ) );
					}
				}
				
				if ( b.getType() == Material.LEAVES )
				{
					if ( n < 0.3 )
					{
						event.setCancelled( true );
						b.setType( Material.AIR );
						b.getWorld().dropItem( b.getLocation(), new ItemStack( Material.APPLE, 1 ) );
					}
				}
			}
			
			// Fixes a strange issue with some mods in Tekkit Classic still dropping items even when in creative.
			if ( event.getPlayer().getGameMode() == GameMode.CREATIVE && !event.isCancelled() )
				event.getBlock().setTypeId( 0 );
		}
	}
	
	@EventHandler
	public void onBlockPlaceEvent( BlockPlaceEvent event )
	{
		if ( event.isCancelled() )
			return;
		
		Block var1 = event.getBlockPlaced();
		Player player = event.getPlayer();
		
		if ( ( MyDoor.isDoor( var1 ) || var1.getType() == Material.CHEST || var1.getTypeId() == 146 ) && var1.getRelative( BlockFace.DOWN ) != null && var1.getRelative( BlockFace.DOWN ).getType() == Material.WOOL && player != null )
		{
			String var2 = getWoolColor( var1.getRelative( BlockFace.DOWN ) ).toLowerCase();
			
			if ( player.hasPermission( "inations." + var2.toLowerCase().replaceAll( " ", "" ) ) )
			{
				player.sendMessage( ChatColor.AQUA + "You have permission to build " + var2 + " wool locked " + var1.getType() + "." );
			}
			else if ( player.hasPermission( "inations.admin" ) || player.isOp() )
			{
				player.sendMessage( ChatColor.DARK_AQUA + "You don't have permission to build " + var2 + " wool locked " + var1.getType() + ". But you have the override perm." );
			}
			else
			{
				player.sendMessage( ChatColor.RED + "You don't have permission to build " + var2 + " wool locked " + var1.getType() + ". For then it would become unbreakable and unaccessable by you." );
				event.setCancelled( true );
				return;
			}
		}
	}
	
	public String getWoolColor( Block b )
	{
		String color = "";
		
		switch ( b.getData() )
		{
			case (byte) 0:
				color = "White";
				break;
			case (byte) 1:
				color = "Orange";
				break;
			case (byte) 2:
				color = "Magenta";
				break;
			case (byte) 3:
				color = "Light Blue";
				break;
			case (byte) 4:
				color = "Yellow";
				break;
			case (byte) 5:
				color = "Lime Green";
				break;
			case (byte) 6:
				color = "Pink";
				break;
			case (byte) 7:
				color = "Gray";
				break;
			case (byte) 8:
				color = "Light Gray";
				break;
			case (byte) 9:
				color = "Cyan";
				break;
			case (byte) 10:
				color = "Purple";
				break;
			case (byte) 11:
				color = "Blue";
				break;
			case (byte) 12:
				color = "Brown";
				break;
			case (byte) 13:
				color = "Green";
				break;
			case (byte) 14:
				color = "Red";
				break;
			case (byte) 15:
				color = "Black";
				break;
		}
		
		return color;
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onSignChangeEvent( SignChangeEvent event )
	{
		Block b = event.getBlock();
		String m = event.getLine( 0 );
		Player p = event.getPlayer();
		
		if ( m.equalsIgnoreCase( "[locked]" ) )
		{
			event.setLine( 0, ChatColor.GOLD + "[locked]" );
			event.setLine( 3, p.getName() );
			
			OfflinePlayer offP1 = getServer().getOfflinePlayer( event.getLine( 1 ).replaceAll( ChatColor.AQUA.toString(), "" ) );
			OfflinePlayer offP2 = getServer().getOfflinePlayer( event.getLine( 2 ).replaceAll( ChatColor.AQUA.toString(), "" ) );
			
			if ( !offP1.getName().isEmpty() )
			{
				Player pp = getServer().getPlayer( offP1.getName() );
				if ( pp != null )
					event.setLine( 1, pp.getName() );
			}
			
			if ( !offP2.getName().isEmpty() )
			{
				Player pp = getServer().getPlayer( offP2.getName() );
				if ( pp != null )
					event.setLine( 2, pp.getName() );
			}
			
			if ( offP1.getName() == p.getName() )
				event.setLine( 1, "" );
			
			if ( offP2.getName() == p.getName() )
				event.setLine( 2, "" );
			
			event.setLine( 1, ChatColor.AQUA + event.getLine( 1 ) );
			event.setLine( 2, ChatColor.AQUA + event.getLine( 2 ) );
			event.setLine( 3, ChatColor.AQUA + event.getLine( 3 ) );
			
			Block found = findNearByLockable( b );
			
			if ( found == null )
			{
				p.sendMessage( ChatColor.RED + "Sign creation denied as no elegible block was found. Only doors, trapdoors and chests can be locked." );
				event.setCancelled( true );
				b.breakNaturally();
				return;
			}
			
			if ( p != null )
				p.sendMessage( ChatColor.AQUA + "You have successfully created a locked block sign. =>" );
			
		}
	}
	
	/**
	 * Indicates if the specified block can be locked or not
	 * 
	 * @param var1
	 * @return
	 */
	public boolean isLockable( Block var1 )
	{
		// TODO: Add the ability to specify additional lockable blocks in the configs.
		
		Material m = var1.getType();
		
		return ( m == Material.CHEST || m == Material.TRAP_DOOR || m == Material.LEVER || m == Material.JUKEBOX || m == Material.FURNACE || m == Material.DISPENSER || m.getId() == 145 // Anvil
				|| m.getId() == 146 // Trap Chest
				|| m.getId() == 143 // Wood Button
				|| m.getId() == 77 // Stone Button
				|| m.getId() == 158 // Dropper
		|| MyDoor.isDoor( var1 ) );
	}
	
	/**
	 * Searches around a provided block for the existence of a lockable block.
	 * 
	 * @param var1
	 * @return
	 */
	public Block findNearByLockable( Block var1 )
	{
		Block found = null;
		
		List<Block> possible = new ArrayList<Block>();
		
		if ( var1 == null )
			return null;
		
		possible.add( var1.getRelative( BlockFace.UP ) );
		possible.add( var1.getRelative( BlockFace.NORTH ) );
		possible.add( var1.getRelative( BlockFace.EAST ) );
		possible.add( var1.getRelative( BlockFace.SOUTH ) );
		possible.add( var1.getRelative( BlockFace.WEST ) );
		possible.add( var1.getRelative( BlockFace.NORTH_EAST ) );
		possible.add( var1.getRelative( BlockFace.NORTH_WEST ) );
		possible.add( var1.getRelative( BlockFace.SOUTH_EAST ) );
		possible.add( var1.getRelative( BlockFace.SOUTH_WEST ) );
		
		Block level1 = var1.getRelative( BlockFace.UP );
		if ( level1 != null )
		{
			possible.add( level1 );
			possible.add( level1.getRelative( BlockFace.UP ) );
			possible.add( level1.getRelative( BlockFace.NORTH ) );
			possible.add( level1.getRelative( BlockFace.EAST ) );
			possible.add( level1.getRelative( BlockFace.SOUTH ) );
			possible.add( level1.getRelative( BlockFace.WEST ) );
			possible.add( level1.getRelative( BlockFace.NORTH_EAST ) );
			possible.add( level1.getRelative( BlockFace.NORTH_WEST ) );
			possible.add( level1.getRelative( BlockFace.SOUTH_EAST ) );
			possible.add( level1.getRelative( BlockFace.SOUTH_WEST ) );
		}
		
		Block level2 = var1.getRelative( BlockFace.DOWN );
		if ( level2 != null )
		{
			possible.add( level2 );
			possible.add( level2.getRelative( BlockFace.UP ) );
			possible.add( level2.getRelative( BlockFace.NORTH ) );
			possible.add( level2.getRelative( BlockFace.EAST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH ) );
			possible.add( level2.getRelative( BlockFace.WEST ) );
			possible.add( level2.getRelative( BlockFace.NORTH_EAST ) );
			possible.add( level2.getRelative( BlockFace.NORTH_WEST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH_EAST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH_WEST ) );
		}
		
		if ( level2.getRelative( BlockFace.DOWN ) != null )
			possible.add( level2.getRelative( BlockFace.DOWN ) );
		
		for ( Block var2 : possible )
		{
			if ( isLockable( var2 ) )
			{
				found = var2;
				break;
			}
		}
		
		return found;
	}
	
	/**
	 * Searches around a provided block for the existence of a sign with the text parameter on it
	 * 
	 * @param block
	 *           = Specifies what block we are searching around.
	 * @param text
	 *           = Specifies what text we expect to see on the first line. null to ignore.
	 * @return Sign = Returns the found sign
	 */
	public Sign findNearBySign( Block var1, String var2 )
	{
		Sign found = null;
		
		List<Block> possible = new ArrayList<Block>();
		
		if ( var1 == null )
			return null;
		
		possible.add( var1.getRelative( BlockFace.UP ) );
		possible.add( var1.getRelative( BlockFace.NORTH ) );
		possible.add( var1.getRelative( BlockFace.EAST ) );
		possible.add( var1.getRelative( BlockFace.SOUTH ) );
		possible.add( var1.getRelative( BlockFace.WEST ) );
		possible.add( var1.getRelative( BlockFace.NORTH_EAST ) );
		possible.add( var1.getRelative( BlockFace.NORTH_WEST ) );
		possible.add( var1.getRelative( BlockFace.SOUTH_EAST ) );
		possible.add( var1.getRelative( BlockFace.SOUTH_WEST ) );
		
		Block level1 = var1.getRelative( BlockFace.UP );
		if ( level1 != null )
		{
			possible.add( level1 );
			possible.add( level1.getRelative( BlockFace.UP ) );
			possible.add( level1.getRelative( BlockFace.NORTH ) );
			possible.add( level1.getRelative( BlockFace.EAST ) );
			possible.add( level1.getRelative( BlockFace.SOUTH ) );
			possible.add( level1.getRelative( BlockFace.WEST ) );
			possible.add( level1.getRelative( BlockFace.NORTH_EAST ) );
			possible.add( level1.getRelative( BlockFace.NORTH_WEST ) );
			possible.add( level1.getRelative( BlockFace.SOUTH_EAST ) );
			possible.add( level1.getRelative( BlockFace.SOUTH_WEST ) );
		}
		
		Block level2 = var1.getRelative( BlockFace.DOWN );
		if ( level2 != null )
		{
			possible.add( level2 );
			possible.add( level2.getRelative( BlockFace.UP ) );
			possible.add( level2.getRelative( BlockFace.NORTH ) );
			possible.add( level2.getRelative( BlockFace.EAST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH ) );
			possible.add( level2.getRelative( BlockFace.WEST ) );
			possible.add( level2.getRelative( BlockFace.NORTH_EAST ) );
			possible.add( level2.getRelative( BlockFace.NORTH_WEST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH_EAST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH_WEST ) );
		}
		
		if ( level2.getRelative( BlockFace.DOWN ) != null )
			possible.add( level2.getRelative( BlockFace.DOWN ) );
		
		for ( Block sign : possible )
		{
			if ( sign.getState() instanceof Sign )
			{
				found = (Sign) sign.getState();
				
				if ( var2 == null )
					break; // Any found sign is okay
					
				if ( found.getLine( 0 ).replaceAll( ChatColor.GOLD.toString(), "" ).equalsIgnoreCase( var2 ) )
					break;
				else
					found = null;
			}
		}
		
		return found;
	}
	
	@EventHandler( priority = EventPriority.MONITOR )
	public void onPlayerInteractMonitor( PlayerInteractEvent event )
	{
		if ( event.isCancelled() )
			return;
		
		if ( event.getAction() == Action.RIGHT_CLICK_BLOCK && MyDoor.isDoor( event.getClickedBlock() ) )
		{
			final MyDoor var1 = MyDoor.getDoor( event.getClickedBlock() );
			
			if ( var1 == null )
				return;
			
			event.setCancelled( true );
			
			if ( var1.isAdminDoor() )
			{
				if ( isAdmin( event.getPlayer() ) )
					var1.setOpen( !var1.isOpen() );
			}
			else
				var1.setOpen( !var1.isOpen() );
			
			if ( var1.isOpen() )
				Bukkit.getScheduler().scheduleSyncDelayedTask( this, new Runnable()
				{
					public void run()
					{
						var1.setOpen( false );
					}
				}, 50L );
		}
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerInteract( PlayerInteractEvent event )
	{
		Player player = event.getPlayer();
		
		if ( event.getAction() == Action.RIGHT_CLICK_BLOCK )// || event.getAction() == Action.LEFT_CLICK_BLOCK )
		{
			Block var1 = event.getClickedBlock();
			Material m = event.getClickedBlock().getType();
			
			if ( MyDoor.isDoor( var1 ) )
			{
				final MyDoor var2 = MyDoor.getDoor( event.getClickedBlock() );
				
				if ( var2 == null )
					return;
				
				if ( var2.isLocked() || var2.isWoolLocked() )
					if ( var2.isPermitted( player ) )
					{
						player.sendMessage( ChatColor.AQUA + "You have permission to open this locked " + var1.getType() + "." );
					}
					else if ( player.hasPermission( "inations.admin" ) || player.isOp() )
					{
						player.sendMessage( ChatColor.DARK_AQUA + "You don't have permission to open locked " + var1.getType() + ". But you have the override permission." );
					}
					else
					{
						player.sendMessage( ChatColor.RED + "You don't have permission to open this locked " + var1.getType() + "." );
					}
			}
			
			if ( m == Material.CHEST || m == Material.TRAP_DOOR || m == Material.LEVER || m == Material.JUKEBOX || m == Material.FURNACE || m == Material.DISPENSER || m.getId() == 145 || m.getId() == 146 || m.getId() == 143 || m.getId() == 77 || m.getId() == 158 )
			// 158 - Dropper, 77 - Stone Button, 143 - Wood Button, 146 - Trap Chest, 145 - Anvil
			{
				Sign found = findNearBySign( var1, "[locked]" );
				
				if ( found != null )
				{
					String[] var3 = found.getLines();
					
					if ( var3 != null )
					{
						Boolean allowed = false;
						
						for ( String mm : var3 )
						{
							mm = mm.replaceAll( ChatColor.AQUA.toString(), "" );
							
							if ( !mm.isEmpty() && player.getName().startsWith( mm ) )
							{
								allowed = true;
								break;
							}
						}
						
						if ( allowed )
						{
							player.sendMessage( ChatColor.AQUA + "You have been permitted to use this locked " + m + "." );
						}
						else
						{
							if ( player.isOp() || player.hasPermission( "inations.admin" ) )
							{
								player.sendMessage( ChatColor.RED + "Not permitted to use this but you have override permissions." );
							}
							else
							{
								player.sendMessage( ChatColor.RED + "You are not permitted to use this locked " + m + "." );
								event.setCancelled( true );
								return;
							}
						}
					}
				}
			}
		}
		
		if ( event.getAction() == Action.RIGHT_CLICK_BLOCK )
		{
			handleBlockRightClick( event );
		}
		else if ( event.getAction() == Action.RIGHT_CLICK_AIR )
		{
			handleAirRightClick( event );
		}
		else if ( event.getAction() == Action.LEFT_CLICK_BLOCK )
		{
			handleBlockLeftClick( event );
		}
		else if ( event.getAction() == Action.LEFT_CLICK_AIR )
		{
			handleAirLeftClick( event );
		}
		else if ( event.getAction() == Action.PHYSICAL )
		{
			handlePhysicalInteract( event );
		}
	}
	
	/**
	 * Called when a player left clicks air.
	 * 
	 * @param event
	 *           Thrown event
	 */
	private void handleAirLeftClick( PlayerInteractEvent event )
	{
		// I don't think we have to do anything here yet.
		return;
	}
	
	/**
	 * Called when a player left clicks a block.
	 * 
	 * @param event
	 *           Thrown event
	 */
	private void handleBlockLeftClick( PlayerInteractEvent event )
	{
		
	}
	
	/**
	 * Called when a player right clicks air.
	 * 
	 * @param event
	 *           Thrown event
	 */
	private void handleAirRightClick( PlayerInteractEvent event )
	{
		
	}
	
	/**
	 * Called when a player right clicks a block.
	 * 
	 * @param event
	 *           Thrown event
	 */
	private void handleBlockRightClick( PlayerInteractEvent event )
	{
		
	}
	
	/**
	 * Called when a player steps on a pressure plate or tramples crops.
	 * 
	 * @param event
	 *           Thrown event
	 */
	private void handlePhysicalInteract( PlayerInteractEvent event )
	{
		if ( event.isCancelled() )
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
				if ( !m.isEmpty() && m.substring( 0, 1 ).equals( "/" ) )
				{
					Bukkit.dispatchCommand( event.getPlayer(), m.substring( 1 ) );
				}
				else
				{
					if ( oo )
					{
						ms += m + " ";
					}
				}
				
				if ( m.toLowerCase().equals( "[tell]" ) )
				{
					oo = true;
				}
			}
			
			if ( !ms.isEmpty() )
				event.getPlayer().sendMessage( ChatColor.DARK_AQUA + ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), ms ) );
		}
	}
	
	/**
	 * Called when a player drops an item. i.e. Presses the Q key.
	 * 
	 * @param event
	 *           Thrown event
	 */
	@EventHandler( priority = EventPriority.HIGH )
	public void onPlayerDropItem( PlayerDropItemEvent event )
	{
		
	}
	
	public void loadSlotQue()
	{
		slotQue.put( "Guest", 6 );
		slotQue.put( "Member", 20 );
		slotQue.put( "Trusted", 30 );
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onServerListPingEvent( ServerListPingEvent event )
	{
		if ( getConfig().getBoolean( "global.motd_enabled" ) )
		{
			String motd = "&5";
			int maxSlots = 200;
			Player[] players = getServer().getOnlinePlayers();
			
			motd_pre = getConfig().getString( "global.motd_prefix", "UFoH" );
			motd_suf = getConfig().getString( "global.motd_suffix", "<-- 20% Bonus Size! (http://ufharmony.com)" );
			
			motd += ( motd_pre == null ) ? event.getMotd() : motd_pre;
			
			motd += " &2[ ";
			for ( Entry<String, Integer> slot : slotQue.entrySet() )
			{
				int online = 0;
				
				for ( Player p : players )
				{
					if ( !isStaff( p ) && p.hasPermission( "inations." + slot.getKey() ) )
					{
						online++;
						players = (Player[]) ArrayUtils.removeElement( players, p );
					}
				}
				
				motd += slot.getKey().substring( 0, 1 ).toUpperCase() + online + "/" + slot.getValue() + ", ";
				maxSlots += slot.getValue();
			}
			motd = motd + players.length + " ] ";
			
			if ( motd_suf != null || !motd_suf.isEmpty() )
				motd += StringUtils.repeat( " ", 50 - motd.length() ) + "&f" + motd_suf;
			
			event.setMotd( motd.replaceAll( "(&([a-f0-9]))", "§$2" ) );
			event.setMaxPlayers( maxSlots );
		}
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerJoin( PlayerJoinEvent event )
	{
		Player p = event.getPlayer();
		
		Boolean slotFull = false;
		
		if ( !isStaff( p ) && !p.hasPermission( "inations.ignoreSlots" ) )
		{
			for ( Entry<String, Integer> slot : slotQue.entrySet() )
			{
				if ( p.hasPermission( "inations." + slot.getKey() ) )
				{
					int cnt = 0;
					
					for ( Player pp : getServer().getOnlinePlayers() )
					{
						if ( !isStaff( p ) && pp.hasPermission( "inations." + slot.getKey() ) )
							cnt++;
					}
					
					if ( cnt >= slot.getValue() )
						slotFull = true;
					
					break;
				}
			}
		}
		
		if ( slotFull )
		{
			notifyAdmins( "&2[iNations] &4" + p.getDisplayName() + " tried to join but was kicked because no slots were available to them." );
			p.kickPlayer( "Sorry, There are no available slots left for Your assigned group. Try again soon." );
			return;
		}
		
		if ( !getConfig().getString( "chat.joinFormat", "" ).isEmpty() )
		{
			String format = getConfig().getString( "chat.joinFormat", "" );
			
			format = format.replace( "{display_name}", event.getPlayer().getDisplayName() );
			
			event.setJoinMessage( ChatColor.translateAlternateColorCodes( '&', format ) );
		}
		
		WebSocketService.send( "MSG &6" + getConfig().getString( "global.remote_prefix" ) + event.getJoinMessage() );
		
		// Clear player position metadata
		setMetadata( event.getPlayer(), "position1", null );
		setMetadata( event.getPlayer(), "position2", null );
		setMetadata( event.getPlayer(), "lastMessage", System.currentTimeMillis() );
		
		if ( getConfig().getBoolean( "global.motd_enabled" ) )
		{
			// Retrieve and send "Message of the Day" to user
			String msg = getConfig().getString( "global.motd" );
			if ( !msg.isEmpty() )
			{
				event.getPlayer().sendMessage( msg );
			}
		}
	}
	
	public void setMetadata( Player player, String key, Object value )
	{
		player.setMetadata( key, new FixedMetadataValue( this, value ) );
	}
	
	public Object getMetadata( Player player, String key )
	{
		return getMetadata( player, key, null );
	}
	
	public Object getMetadata( Player player, String key, String def )
	{
		List<MetadataValue> values = player.getMetadata( key );
		for ( MetadataValue value : values )
		{
			if ( value.getOwningPlugin().getDescription().getName().equals( this.getDescription().getName() ) )
			{
				return value.value();
			}
		}
		
		return def;
	}
}

package com.chiorichan.bukkit.plugin.iNations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.applebloom.api.WebSocketService;

import com.chiorichan.bukkit.plugin.Area;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;

public class INationsCommandExecutor implements CommandExecutor
{
	private INations plugin;
	
	public INationsCommandExecutor(INations plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args )
	{
		Player player = null;
		if ( sender instanceof Player )
		{
			player = (Player) sender;
		}
		
		String consoleWarning = ChatColor.RED + "[iNations] " + ChatColor.WHITE + "You can not use that command from within console.";
		
		if ( cmd.getName().equalsIgnoreCase( "g" ) )
		{
			if ( player == null )
			{
				sender.sendMessage( consoleWarning );
				return true;
			}
			
			plugin.setMetadata( player, "ChatMode", "G" );
			player.sendMessage( ChatColor.RED + "[iNations] " + ChatColor.WHITE + "We switched your chat mode to Global!" );
			return true;
		}
		else if ( cmd.getName().equalsIgnoreCase( "e" ) )
		{
			if ( player == null )
			{
				sender.sendMessage( consoleWarning );
				return true;
			}
			
			if ( !player.isOp() && !player.hasPermission( "inations.staff" ) )
			{
				sender.sendMessage( ChatColor.RED + "You must have the proper permissions to use this command." );
				return true;
			}
			
			plugin.setMetadata( player, "ChatMode", "E" );
			player.sendMessage( ChatColor.RED + "[iNations] " + ChatColor.WHITE + "We switched your chat mode to Staff Chat!" );
			return true;
		}
		else if ( cmd.getName().equalsIgnoreCase( "o" ) )
		{
			if ( player == null )
			{
				sender.sendMessage( consoleWarning );
				return true;
			}
			
			if ( !player.isOp() )
			{
				sender.sendMessage( ChatColor.RED + "You must have the proper permissions to use this command." );
				return true;
			}
			
			plugin.setMetadata( player, "ChatMode", "O" );
			player.sendMessage( ChatColor.RED + "[iNations] " + ChatColor.WHITE + "We switched your chat mode to OP!" );
			return true;
		}
		else if ( cmd.getName().equalsIgnoreCase( "inv" ) ) // View inventory of storage blocks and other players
		{
			if ( player == null )
			{
				sender.sendMessage( consoleWarning );
				return true;
			}
			
			if ( !player.hasPermission( "inations.extras.inv" ) )
			{
				sender.sendMessage( ChatColor.RED + "You must have the proper permissions to use this command." );
				return true;
			}
			
			if ( args.length < 1 || args[0].equals( "c" ) )
			{
				HashSet<Byte> hs = new HashSet<Byte>();
				hs.add( (byte) 0 );
				hs.add( (byte) 8 ); // Water
				hs.add( (byte) 9 ); // Water
				hs.add( (byte) 20 );
				hs.add( (byte) 102 );
				hs.add( (byte) 136 ); // Redpower Glass
				
				Block b = player.getTargetBlock( hs, 100 );
				
				// Block b = player.getTargetBlock(null, 100);
				
				if ( b.getState() instanceof Chest )
				{
					if ( args.length == 1 && args[0].equals( "c" ) )
					{
						Chest c = (Chest) b.getState();
						
						Inventory i = c.getInventory();
						
						if ( i == null )
						{
							player.sendMessage( ChatColor.RED + "Could not open chest inventory." );
							return true;
						}
						
						ItemStack[] iss = i.getContents();
						
						Inventory ii = plugin.getServer().createInventory( null, i.getSize(), "Chest (Cloned)" );
						
						ii.setContents( iss );
						
						player.openInventory( ii );
						
						player.sendMessage( ChatColor.AQUA + "Successfully cloned chest inventory." );
					}
					else if ( args.length == 1 && args[0].equals( "p" ) )
					{
						Chest c = (Chest) b.getState();
						
						Inventory i = c.getInventory();
						
						if ( i == null )
						{
							player.sendMessage( ChatColor.RED + "Could not open chest inventory." );
							return true;
						}
						
						ItemStack[] iss = i.getContents();
						
						for ( int x = 0; x < iss.length; x++ )
						{
							player.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.RED + "(" + iss[x].getAmount() + ")" + ChatColor.WHITE + " #" + iss[x].getTypeId() );
						}
						
						player.sendMessage( ChatColor.AQUA + "Successfully printed chest inventory." );
					}
					else
					{
						Chest c = (Chest) b.getState();
						player.openInventory( c.getInventory() );
						player.sendMessage( ChatColor.AQUA + "Successfully openned chest inventory." );
					}
				}
				else
				{
					sender.sendMessage( ChatColor.RED + "You must specify a player name." );
					sender.sendMessage( ChatColor.RED + "/inv <player>" );
				}
				return true;
			}
			
			Player p = plugin.getServer().getPlayer( args[0] );
			
			if ( p == null )
			{
				sender.sendMessage( ChatColor.RED + "That player is either offline or never existed." );
				return true;
			}
			
			Inventory i = p.getInventory();
			
			player.openInventory( i );
			
			return true;
		}
		else if ( cmd.getName().equalsIgnoreCase( "clear" ) ) // Clear out inventory
		{
			if ( player == null )
			{
				sender.sendMessage( consoleWarning );
				return true;
			}
			
			if ( !player.hasPermission( "inations.extras.clear" ) )
			{
				sender.sendMessage( ChatColor.RED + "You must have the proper permissions to use this command." );
				return true;
			}
			
			Inventory i = player.getInventory();
			
			if ( args.length < 1 )
			{
				i.clear();
			}
			else if ( args[0].equals( "h" ) )
			{
				int x;
				for ( x = 0; x < 9; x++ )
				{
					i.setItem( x, null );
				}
			}
			else if ( args[0].equals( "i" ) )
			{
				int x;
				for ( x = 9; x < 36; x++ )
				{
					i.setItem( x, null );
				}
			}
			
			player.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.GOLD + "Successfully cleared your inventory. :D" );
			return true;
		}
		else if ( cmd.getName().equalsIgnoreCase( "t" ) || cmd.getName().equalsIgnoreCase( "tell" ) ) // Private chat
																																		// players even
																																		// hidden ones
		{
			if ( player != null && !player.hasPermission( "inations.extras.tell" ) )
			{
				sender.sendMessage( ChatColor.RED + "You must have the proper permissions to use this command." );
				return true;
			}
			
			if ( args.length < 1 )
			{
				sender.sendMessage( ChatColor.RED + "You must specify a player name and message." );
				sender.sendMessage( ChatColor.RED + "/t <player> <msg>" );
				return true;
			}
			
			if ( args.length < 2 )
			{
				sender.sendMessage( ChatColor.RED + "You must specify a message." );
				sender.sendMessage( ChatColor.RED + "/t <player> <msg>" );
				return true;
			}
			
			CommandSender target;
			
			if ( args[0].equalsIgnoreCase( "console" ) )
			{
				target = (CommandSender) plugin.getServer().getConsoleSender();
			}
			else
			{
				target = (CommandSender) plugin.getServer().getPlayer( args[0] );
				
				if ( target == null )
				{
					sender.sendMessage( ChatColor.RED + "That player is either offline or never existed." );
					return true;
				}
			}
			
			if ( target == null )
				return false;
			
			int x;
			String msg = "";
			
			for ( x = 0; x < args.length - 1; x++ )
			{
				msg = msg + " " + args[x + 1];
			}
			
			if ( target instanceof ConsoleCommandSender || ( target instanceof Player && player == null ) || ( target instanceof Player && player.canSee( (Player) target ) ) )
			{
				for ( Player p : plugin.getServer().getOnlinePlayers() )
					if ( p.isOp() && p != sender && p != target )
						p.sendMessage( ChatColor.RED + "[" + sender.getName() + " -> " + target.getName() + "] " + ChatColor.RESET + ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), msg.trim() ) );
				
				sender.sendMessage( ChatColor.RED + "[me -> " + target.getName() + "] " + ChatColor.RESET + ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), msg.trim() ) );
				target.sendMessage( ChatColor.RED + "[" + sender.getName() + " -> me] " + ChatColor.RESET + ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), msg.trim() ) );
				
				if ( !( target instanceof ConsoleCommandSender ) && !( sender instanceof ConsoleCommandSender ) )
					plugin.getServer().getConsoleSender().sendMessage( ChatColor.RED + "[" + sender.getName() + " -> " + target.getName() + "] " + ChatColor.RESET + ChatColor.translateAlternateColorCodes( "&".charAt( 0 ), msg.trim() ) );
			}
			else
			{
				sender.sendMessage( ChatColor.RED + "That player is either offline or never existed." );
			}
			
			return true;
		}
		else if ( cmd.getName().equalsIgnoreCase( "goto" ) )
		{
			if ( player == null )
			{
				sender.sendMessage( consoleWarning );
				return true;
			}
			
			if ( !player.hasPermission( "inations.extras.goto" ) )
			{
				sender.sendMessage( ChatColor.RED + "You must have the proper permissions to use this command." );
				return true;
			}
			
			if ( args.length == 2 )
			{
				sender.sendMessage( ChatColor.RED + "You must specify a player to teleport to." );
				return true;
			}
			
			Player target = plugin.getServer().getPlayer( args[0] );
			
			if ( target == null )
			{
				sender.sendMessage( ChatColor.RED + "That player is either offline or never existed." );
				return true;
			}
			
			final Player p = player;
			final Player t = target;
			
			if ( target.isOp() || target.hasPermission( "inations.admin" ) )
			{
				p.sendMessage( ChatColor.AQUA + "Teleporting you to " + t.getDisplayName() + ChatColor.AQUA + " soon." );
				target.sendMessage( ChatColor.RED + "Warning: Teleporting " + player.getDisplayName() + " to your location soon." );
				
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask( plugin, new Runnable()
				{
					public void run()
					{
						p.teleport( t.getLocation() );
						p.sendMessage( ChatColor.AQUA + "Successfully teleported to " + t.getDisplayName() );
						p.sendMessage( ChatColor.AQUA + "Successfully teleported " + t.getDisplayName() + ChatColor.AQUA + " to you." );
					}
				}, 100L );
			}
			else
			{
				p.teleport( t.getLocation() );
				p.sendMessage( ChatColor.AQUA + "Successfully teleported to " + t.getDisplayName() );
			}
			
			return true;
		}
		else if ( cmd.getName().equalsIgnoreCase( "iadmin" ) ) // iNations admin commands
		{
			if ( args.length < 1 )
			{
				sender.sendMessage( ChatColor.DARK_RED + "You must specify a valid sub-command of iNations Admin Command Subroutine. See \"/iadmin help\"." );
				return true;
			}
			
			if ( player == null && ( args[0].equals( "lock" ) || args[0].equals( "fly" ) || args[0].equals( "run" ) ) )
			{
				sender.sendMessage( consoleWarning );
				return true;
			}
			
			if ( player != null && ( !player.isOp() && !player.hasPermission( "inations.admin" ) ) )
			{
				sender.sendMessage( ChatColor.DARK_RED + "You must have the 'inations.admin' permission to use the iNations Admin Command." );
				return true;
			}
			
			if ( args[0].equals( "help" ) ) // Help of Sub Commands
			{
				sender.sendMessage( "/iadmin" );
				sender.sendMessage( "<motd|config|save|reload|resume|pause|sample|restart>" );
				return true;
			}
			else if ( args[0].equals( "version" ) )
			{
				sender.sendMessage( "You are running iNations version 2.0" );
				return true;
			}
			else if ( args[0].equals( "sample" ) ) // Start Message Broadcaster
			{
				plugin.broadcastMessage();
				
				return true;
			}
			else if ( args[0].equals( "status" ) )
			{
				sender.sendMessage( "&bAttempted to request the player stats from the other servers. Please Wait..." );
				WebSocketService.send( "STATUS" );
			}
			else if ( args[0].equals( "-sickall" ) )
			{
				for ( Player p : plugin.getServer().getOnlinePlayers() )
				{
					if ( !p.isOp() && !p.hasPermission( "inations.staff" ) )
					{
						p.removePotionEffect( PotionEffectType.BLINDNESS );
						p.removePotionEffect( PotionEffectType.CONFUSION );
						p.removePotionEffect( PotionEffectType.SLOW );
						p.removePotionEffect( PotionEffectType.WEAKNESS );
					}
					else
					{
						p.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.GRAY + "All players have healed from Sickness thanks to Nurse Heart." );
					}
				}
				
				player.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.WHITE + "All players have healed from Sickness thanks to Nurse Heart." );
				
				return true;
			}
			else if ( args[0].equals( "sickall" ) )
			{
				for ( Player p : plugin.getServer().getOnlinePlayers() )
				{
					if ( !p.isOp() && !p.hasPermission( "inations.staff" ) )
					{
						p.addPotionEffect( new PotionEffect( PotionEffectType.BLINDNESS, 3000, 10 ) );
						p.addPotionEffect( new PotionEffect( PotionEffectType.CONFUSION, 3000, 10 ) );
						p.addPotionEffect( new PotionEffect( PotionEffectType.SLOW, 3000, 10 ) );
						p.addPotionEffect( new PotionEffect( PotionEffectType.WEAKNESS, 3000, 10 ) );
					}
					else
					{
						p.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.GRAY + "All players are now sick for about 2 minues and 20 seconds." );
					}
				}
				
				player.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.WHITE + "All players are now sick for about 2 minutes and 20 seconds." );
				
				return true;
			}
			else if ( args[0].equals( "ebag" ) )
			{
				/*
				 * if ( player == null ) { sender.sendMessage(consoleWarning); return true; }
				 * 
				 * player.sendMessage( ChatColor.AQUA + "Syntax: /iadmin ebag playername bag_no" ); player.sendMessage(
				 * ChatColor.WHITE + "0=White, " + ChatColor.YELLOW + "1=Organge, " + ChatColor.LIGHT_PURPLE +
				 * "2=LitePurple, " + ChatColor.AQUA + "3=LiteBlue, " + ChatColor.YELLOW + "4=Yellow" ); player.sendMessage(
				 * ChatColor.GREEN + "5=LiteGreen, " + ChatColor.RED + "6=Pink, " + ChatColor.GRAY + "7=Gray, " +
				 * ChatColor.GRAY + "8=LiteGray, " + ChatColor.AQUA + "9=Aqua, " + ChatColor.LIGHT_PURPLE + "10=Purple" );
				 * player.sendMessage( ChatColor.BLUE + "11=Blue, " + ChatColor.BLACK + "12=Brown, " + ChatColor.GREEN +
				 * "13=Green, " + ChatColor.RED + "14=Red, " + ChatColor.BLACK + "15=Black" );
				 */
				
				try
				{
					// Might need to be different if the servers default world is not WORLD.
					File BaseFolder = new File( Bukkit.getServer().getWorld( "world" ).getWorldFolder(), "data" );
					
					GZIPInputStream is = new GZIPInputStream( new FileInputStream( BaseFolder.getAbsolutePath() + "/bag_" + player.getName() + "0.dat" ) );
					
					NBTInputStream nbt = new NBTInputStream( is );
					
					Tag t = nbt.readTag();
					// ListTag tt = new ListTag( t.getValue() );
					
					sender.sendMessage( "NBT: " + t.getValue() );
					
					nbt.close();
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
				
				return true;
			}
			else if ( args[0].equals( "sick" ) )
			{
				player.addPotionEffect( new PotionEffect( PotionEffectType.BLINDNESS, 3000, 10 ) );
				player.addPotionEffect( new PotionEffect( PotionEffectType.CONFUSION, 3000, 10 ) );
				player.addPotionEffect( new PotionEffect( PotionEffectType.SLOW, 3000, 10 ) );
				player.addPotionEffect( new PotionEffect( PotionEffectType.WEAKNESS, 3000, 10 ) );
				
				return true;
			}
			else if ( args[0].equals( "pure" ) )
			{
				player.removePotionEffect( PotionEffectType.BLINDNESS );
				player.removePotionEffect( PotionEffectType.CONFUSION );
				player.removePotionEffect( PotionEffectType.SLOW );
				player.removePotionEffect( PotionEffectType.WEAKNESS );
				
				return true;
			}
			else if ( args[0].equals( "pureall" ) )
			{
				for ( Player p : plugin.getServer().getOnlinePlayers() )
				{
					p.removePotionEffect( PotionEffectType.BLINDNESS );
					p.removePotionEffect( PotionEffectType.CONFUSION );
					p.removePotionEffect( PotionEffectType.SLOW );
					p.removePotionEffect( PotionEffectType.WEAKNESS );
				}
				
				return true;
			}
			else if ( args[0].equals( "resume" ) ) // Start Message Broadcaster
			{
				plugin.broadEnabled = true;
				
				sender.sendMessage( ChatColor.AQUA + "[iNations] Message Broadcaster has been resumed." );
				return true;
			}
			else if ( args[0].equals( "rego" ) )
			{
				Plugin p = plugin.getServer().getPluginManager().getPlugin( "iNations" );
				plugin.getServer().getPluginManager().disablePlugin( p );
				plugin.getServer().getPluginManager().enablePlugin( p );
				
				plugin.sendDebug( "Plugin Reloaded" );
				return true;
			}
			else if ( args[0].equals( "restart" ) ) // Start Message Broadcaster
			{
				if ( plugin.syncId != -1 )
					Bukkit.getScheduler().cancelTask( plugin.syncId );
				
				Long ticks = plugin.getConfig().getLong( "globals.msgFreq", 5000L );
				
				plugin.broadEnabled = true;
				plugin.syncId = Bukkit.getScheduler().scheduleAsyncRepeatingTask( plugin, new Runnable()
				{
					public void run()
					{
						if ( plugin.broadEnabled )
							plugin.broadcastMessage();
					}
					
				}, 200L, ticks );
				
				if ( plugin.syncId == -1 )
				{
					sender.sendMessage( ChatColor.AQUA + "[iNations] We have failed to initalize a synced task." );
				}
				else
				{
					sender.sendMessage( ChatColor.AQUA + "[iNations] Message Broadcaster has been restarted." );
				}
				
				return true;
			}
			else if ( args[0].equals( "pause" ) ) // Start Message Broadcaster
			{
				plugin.broadEnabled = false;
				
				sender.sendMessage( ChatColor.AQUA + "[iNations] Message Broadcaster has been paused." );
				return true;
			}
			else if ( args[0].equals( "value" ) )
			{
				List<Entity> en = player.getWorld().getEntities();
				
				for ( Entity e : en )
				{
					if ( e.getType() == EntityType.DROPPED_ITEM )
					{
						// player.teleport( e.getLocation() );
						
						// break;
						
						// e.teleport( player.getLocation() );
						
						player.sendMessage( "Removed " + e.getEntityId() + " at " + e.getLocation().toString() );
						e.remove();
					}
				}
				
				return true;
			}
			else if ( args[0].equals( "chunk" ) )
			{
				Chunk c = player.getLocation().getChunk();
				final ChunkSnapshot s = c.getChunkSnapshot();
				final Player p = player;
				
				/*
				 * plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() { public void run() {
				 */
				Map<Integer, Integer> materialValues = new HashMap<Integer, Integer>();
				
				materialValues.put( Material.COAL_ORE.getId(), 10 );
				materialValues.put( Material.DIAMOND.getId(), 30 );
				materialValues.put( Material.DIAMOND_BLOCK.getId(), 30 );
				materialValues.put( Material.DIAMOND_ORE.getId(), 25 );
				materialValues.put( Material.GOLD_BLOCK.getId(), 25 );
				materialValues.put( Material.GOLD_ORE.getId(), 20 );
				materialValues.put( Material.IRON_BLOCK.getId(), 15 );
				materialValues.put( Material.IRON_ORE.getId(), 15 );
				materialValues.put( Material.WOOD.getId(), 7 );
				materialValues.put( Material.REDSTONE_ORE.getId(), 10 );
				materialValues.put( Material.LAPIS_BLOCK.getId(), 10 );
				materialValues.put( Material.LAPIS_ORE.getId(), 10 );
				materialValues.put( 140, 25 ); // Tekkit Ores
				
				int x, y, z, chunkValue = 0;
				
				for ( x = 0; x < 17; x++ )
				{
					for ( z = 0; z < 17; z++ )
					{
						for ( y = 0; y < 256; y++ )
						{
							int blockID = s.getBlockTypeId( x, y, z );
							if ( s.getBlockTypeId( x, y, z ) != Material.AIR.getId() )
							{
								if ( materialValues.containsKey( blockID ) )
								{
									chunkValue = chunkValue + materialValues.get( blockID );
								}
							}
						}
					}
				}
				
				p.sendMessage( "Estimated Chunk Value is: " + chunkValue );
				/*
				 * } });
				 */
				
				return true;
			}
			else if ( args[0].equals( "run" ) )
			{
				if ( INations.instance.getMetadata( player, "adminRun" ) == null )
					INations.instance.setMetadata( player, "adminRun", player.getGameMode() == GameMode.CREATIVE );
				
				INations.instance.setMetadata( player, "adminRun", !( (Boolean) INations.instance.getMetadata( player, "adminRun" ) ) );
				sender.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.WHITE + "We attempted to forcefully changed your admin run ability" );
			}
			else if ( args[0].equals( "class" ) )
			{
				if ( args.length == 1 )
				{
					sender.sendMessage( ChatColor.RED + "You must specify a player and class name.\n/iadmin class <player> <className>" );
					return true;
				}
				
				if ( args.length == 2 )
				{
					Player target = plugin.getServer().getPlayer( args[1] );
					
					sender.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.WHITE + "Player is currently in the following groups." );
					String[] groups = INations.perm.getPlayerGroups( target );
					int c = 0;
					for ( String group : groups )
					{
						sender.sendMessage( c + ": " + group );
						c++;
					}
					sender.sendMessage( ChatColor.YELLOW + "NOTE: Use your permissions plugin if any of this is incorrect." );
					
					return true;
				}
				
				Player target = plugin.getServer().getPlayer( args[1] );
				
				if ( target == null )
				{
					sender.sendMessage( ChatColor.RED + "That player is either offline or never existed." );
					return true;
				}
				
				Set<String> classes = plugin.getConfig().getConfigurationSection( "classes" ).getKeys( false );
				
				Boolean classExists = false;
				
				for ( String cls : classes )
				{
					INations.perm.playerRemoveGroup( target, cls );
					INations.perm.playerRemoveGroup( target, cls.toLowerCase() );
					
					if ( cls.equals( args[2] ) )
						classExists = true;
				}
				
				if ( classExists )
				{
					INations.perm.playerAddGroup( target, args[2] );
					
					sender.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.WHITE + "Success. Player is currently in the following groups." );
					String[] groups = INations.perm.getPlayerGroups( target );
					int c = 0;
					for ( String group : groups )
					{
						sender.sendMessage( c + ": " + group );
						c++;
					}
					sender.sendMessage( ChatColor.YELLOW + "NOTE: Use your permissions plugin if any of this is incorrect." );
				}
				else
				{	
					sender.sendMessage( ChatColor.AQUA + "[iNations] " + ChatColor.WHITE + "Failure. It would appear that there is no class by the name of " + args[2] + "." );
					sender.sendMessage( "Please create this class inside the iNations config file or choose one of the following choices." );
					
					for ( String cls : classes )
						sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', plugin.getConfig().getString( "classes." + cls + ".color", "&3" ) + cls ) );
				}
				
				return true;
			}
			else if ( args[0].equals( "test" ) ) // Test Command
			{
				// player.getItemInHand().setData( new MaterialData( 1 ) );
				
				// player.setFlying( false );
				
				// player.getItemInHand().addEnchantment(Enchantment.DIG_SPEED, 5);
				
				// player.getItemInHand().removeEnchantment(Enchantment.ARROW_DAMAGE);
				
				// player.getLocation().getWorld().playEffect( player.getLocation() , Effect.EXTINGUISH, 1000);
				
				// for ( Plot plot : plugin.iPlots.values() )
				// {
				// sender.sendMessage("Player Count in " + plot.plotID + ": " + plot.presentPlayerCount());
				// }
				
				// sender.sendMessage(ChatColor.AQUA + "[iNations] Testing Command has been executed. Goodbye.");
				return true;
			}
			else if ( args[0].equals( "fly" ) ) // Test Command
			{
				player.setAllowFlight( true );
				player.setFlying( true );
				
				sender.sendMessage( ChatColor.AQUA + "We tried to enable fly. Goodday." );
				return true;
			}
			else if ( args[0].equals( "reload" ) ) // Reload Configuration
			{
				// plugin.iPlots = (PlotDataStore) plugin.getConfig().get("plots");
				// plugin.getConfig().set("locks", plugin.iLocks);
				// plugin.getConfig().set("communities", plugin.iCommunity);
				
				plugin.reloadConfig();
				sender.sendMessage( ChatColor.AQUA + "[iNations] Configuration was reloaded from file." );
				
				return true;
			}
			else if ( args[0].equals( "save" ) ) // Save of Sub-Commands
			{
				plugin.iDataStore.saveRegions();
				
				plugin.saveConfig();
				player.sendMessage( ChatColor.AQUA + "[iNations] Configuration was successfully saved." );
				return true;
			}
			else if ( args[0].equals( "config" ) ) // Configuration Sub Command
			{
				if ( args.length == 2 && args[1] == "list" )
				{
					sender.sendMessage( "Configuration Options:" );
					sender.sendMessage( "autofence: Once user declares a plot of land, it is automaticly fenced in. Default: true." );
					sender.sendMessage( "ownerdepth: How deep in the ground does ownership expand. Default: 0 = to bedrock" );
					sender.sendMessage( "ownerheight: How high in the air dies the ownership expand. Default 0 = to sky." );
					sender.sendMessage( "minpop: Minimum population required to be reconized as incorporated. Default: 3" );
					sender.sendMessage( "maxpop: Maximum population any incorporated area can handle. Default: 50" );
					sender.sendMessage( "minarea: Minimum sum area of x*z required to be reconized as incorporated. Default: 100" );
					sender.sendMessage( "maxarea: Maximum sum area of x*z that can be incorporated. Default: 10000" );
					sender.sendMessage( "protectbuilds: Should iNations prevent non-owners from build or destroying on plots. Default: true" );
				}
				else if ( args.length < 3 )
				{
					sender.sendMessage( "You must specify a key and value." );
					sender.sendMessage( "/iadmin config <key> <value>" );
					sender.sendMessage( "/iadmin config list" );
				}
				else
				{	
					
				}
				
				return true;
			}
			else if ( args[0].equals( "motd" ) ) // Message of the Day
			{
				if ( args.length < 2 )
				{
					plugin.getConfig().set( "global.motd", "" );
					sender.sendMessage( "Message of the Day has been blanked." );
				}
				else
				{
					int x;
					String motd = "";
					
					for ( x = 0; x < args.length - 1; x++ )
					{
						motd = motd + " " + args[x + 1];
					}
					
					plugin.getConfig().set( "global.motd", motd.trim() );
					sender.sendMessage( "Message of the Day has been set to \"" + motd.trim() + "\"." );
				}
				return true;
			}
			
			// End of Admin Commands.
			
			sender.sendMessage( "You must specify a valid sub-command of iNations Admin Command Subroutine \"/iadmin help\"." );
			return false;
		}
		else if ( cmd.getName().equalsIgnoreCase( "iplayer" ) ) // iNations player commands
		{
			if ( player == null )
			{
				sender.sendMessage( "You can not use that command from within console." );
				return false;
			}
			
			if ( args.length < 1 )
			{
				sender.sendMessage( "You must specify a valid sub-command of iNations Player Command Subroutine. See \"/iplayer help\"." );
				return false;
			}
			
			if ( args[0].equals( "help" ) ) // Help of Sub-Commands
			{
				sender.sendMessage( "/iplayer" );
				sender.sendMessage( "<declare|disban|info|laws|flag|addmember|delmember|teleport|buy|sell>" );
				return true;
			}
			else if ( args[0].equals( "poke" ) )
			{
				if ( args.length == 3 )
				{
					sender.sendMessage( ChatColor.RED + "You must specify a player to poke" );
					return true;
				}
				
				Player target = plugin.getServer().getPlayer( args[1] );
				
				if ( target == null )
				{
					sender.sendMessage( ChatColor.RED + "That player is either offline or never existed." );
					return true;
				}
				
				String sdn = ( player == null ) ? sender.getName() : player.getDisplayName();
				
				target.playEffect( EntityEffect.HURT );
				plugin.getServer().broadcastMessage( ChatColor.AQUA + sdn + " just poked " + target.getDisplayName() );
				
				return true;
				
			}
			/*
			 * else if ( args[0].equals("addmember") ) { if ( args.length == 2) { sender.sendMessage(ChatColor.RED +
			 * "You must specify a player to add to this plot"); return false; }
			 * 
			 * Vector v = plugin.ChangeCordType(player.getLocation()); Area plot = plugin.iDataStore.getPlotFromVector(v);
			 * 
			 * if ( plot == null ) { player.sendMessage(ChatColor.RED +
			 * "You currently are not standing on any claimed land/plot."); } else { if ( plot.getPlayer() == null ||
			 * plot.getPlayer() != player ) { if ( player.isOp() || player.hasPermission("inations.admin") ) {
			 * player.sendMessage(ChatColor.DARK_RED + "You have permission to override the owner of this plot."); } else {
			 * player.sendMessage(ChatColor.DARK_RED + "You don't have permission to add members to this plot."); return
			 * false; } }
			 * 
			 * if ( args[1] == null || args[1].isEmpty() ) return false;
			 * 
			 * Player p = plugin.getServer().getPlayerExact(args[1]);
			 * 
			 * if ( p == null ) return false;
			 * 
			 * plot.addMember(p); player.sendMessage(ChatColor.AQUA + "You have successfully addedd player " +
			 * p.getDisplayName() + " to this plot."); }
			 * 
			 * return true; }
			 */
			else if ( args[0].equals( "info" ) ) // Info of Sub-Commands
			{
				Vector v = plugin.ChangeCordType( player.getLocation() );
				Area plot = plugin.iDataStore.getRegionFromVector( v );
				if ( plot == null )
				{
					player.sendMessage( ChatColor.RED + "You currently are not standing on any claimed land/plot." );
				}
				else
				{
					player.sendMessage( ChatColor.AQUA + "Plot Positions: " + plot.toString() );
					// player.sendMessage(ChatColor.AQUA + "Plot Owner: " + plot.owner);
				}
				return true;
			}
			else if ( args[0].equals( "disban" ) ) // Disban of Sub-Commands
			{
				// TODO: Add Permissions Check
				
				Vector v = plugin.ChangeCordType( player.getLocation() );
				Area plot = plugin.iDataStore.getRegionFromVector( v );
				
				if ( plot == null )
				{
					player.sendMessage( ChatColor.RED + "You currently are not standing on any areas." );
				}
				else
				{
					plugin.iDataStore.remove( plot.ID );
					player.sendMessage( ChatColor.AQUA + "You have successfully disbaned this area." );
				}
				return true;
			}
			else if ( args[0].equals( "declare" ) ) // Declare of Sub-Commands
			{
				Vector loc1 = (Vector) plugin.getMetadata( player, "position1" );
				Vector loc2 = (Vector) plugin.getMetadata( player, "position2" );
				
				if ( loc1 == null )
				{
					sender.sendMessage( ChatColor.RED + "You have not set position #1 using the Wooden Axe Left-Click" );
					return true;
				}
				
				if ( loc2 == null )
				{
					sender.sendMessage( ChatColor.RED + "You have not set position #2 using the Wooden Axe Right-Click" );
					return true;
				}
				
				int height = plugin.getConfig().getInt( "global.ownershipheight", 50 );
				int depth = plugin.getConfig().getInt( "global.ownershipdepth", 20 );
				
				// Check the expansion requirements.
				if ( height == 0 && loc1.getY() > loc2.getY() )
				{
					loc1.setY( 255 );
					if ( depth == 0 )
						loc2.setY( 0 );
				}
				else if ( height == 0 && loc1.getY() < loc2.getY() )
				{
					loc2.setY( 255 );
					if ( depth == 0 )
						loc1.setY( 0 );
				}
				else if ( depth == 0 && loc1.getY() > loc2.getY() )
				{
					loc2.setY( 0 );
				}
				else if ( depth == 0 && loc1.getY() < loc2.getY() )
				{
					loc1.setY( 0 );
				}
				
				Area cube = new Area( loc1, loc2 );
				
				if ( height != 0 )
					height = ( cube.getHeight() < height ) ? height - cube.getHeight() : 0;
				
				for ( Area cubeit : plugin.iDataStore.values() )
				{
					if ( cube.intersects( cubeit ) )
					{
						sender.sendMessage( ChatColor.RED + "This new region intersects with an existing area. Placement is not allowed." );
						return true;
					}
				}
				
				/*
				 * Vector pos1 = cube.getPos1(); Vector pos4 = cube.getPos2();
				 * 
				 * Vector pos2 = new Vector(pos1.getBlockX(), 100, pos4.getBlockZ()); Vector pos3 = new
				 * Vector(pos4.getBlockX(), 100, pos1.getBlockZ());
				 */
				
				cube.expand( new Vector( 0, height, 0 ), new Vector( 0, 0 - depth, 0 ) );
				cube.world = player.getWorld();
				
				plugin.iDataStore.add( cube );
				
				plugin.getConfig().set( "areas." + cube.ID, cube );
				
				sender.sendMessage( ChatColor.AQUA + "You have just successfully declared land with a sum area of " + cube.getArea() + " blocks." );
				
				plugin.setMetadata( player, "position1", null );
				plugin.setMetadata( player, "position2", null );
				
				return true;
			}
			else if ( args[0].equals( "pos" ) ) // Pos of Sub-Commands
			{	
				
			}
			
			// End of iPlayer Commands.
			
			sender.sendMessage( ChatColor.RED + "You must specify a valid sub-command of iNations Player Command Subroutine. See \"/iplayer help\"." );
			return false;
		}
		
		return false;
	}
}

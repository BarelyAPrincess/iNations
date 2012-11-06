package com.chiorichan.bukkit.plugin.iNations;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.chiorichan.bukkit.plugin.Plot;

public class INationsCommandExecutor implements CommandExecutor {
	private INations plugin;
	 
	public INationsCommandExecutor(INations plugin) {
		this.plugin = plugin;
	}
 
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	Player player = null;
    	if (sender instanceof Player) {
    		player = (Player) sender;
    	}
    	
    	if (player == null)
    	{
    		sender.sendMessage("You can not use that command from within console.");
    		return false;
    	}
    
    	if ( cmd.getName().equalsIgnoreCase("iadmin") ) // iNations admin commands
    	{
    		if (args.length < 1)
    		{
    			sender.sendMessage("You must specify a valid sub-command of iNations Admin Command Subroutine. See \"/iadmin help\".");
    			return false;
    		}
    		
    		if ( args[0].equals("help") ) // Help of Sub Commands
    		{
    			sender.sendMessage("/iadmin");
    			sender.sendMessage("<reload|motd|config>");
    			return true;
    		}
    		else if ( args[0].equals("reload") ) // Reload Configuration
    		{
    			plugin.reloadConfig();
    			sender.sendMessage("iNations configuration data has been reloaded from file.");
    			return true;
    		}
    		else if ( args[0].equals("config") ) // Configuration Sub Command
    		{
    			if (args.length == 2 && args[1] == "list")
    			{
    				sender.sendMessage("Configuration Options:");
    				sender.sendMessage("autofence: Once user declares a plot of land, it is automaticly fenced in. Default: true.");
    				sender.sendMessage("ownerdepth: How deep in the ground does ownership expand. Default: 0 = to bedrock");
    				sender.sendMessage("ownerheight: How high in the air dies the ownership expand. Default 0 = to sky.");
    				sender.sendMessage("minpop: Minimum population required to be reconized as incorporated. Default: 3");
    				sender.sendMessage("maxpop: Maximum population any incorporated area can handle. Default: 50");
    				sender.sendMessage("minarea: Minimum sum area of x*z required to be reconized as incorporated. Default: 100");
    				sender.sendMessage("maxarea: Maximum sum area of x*z that can be incorporated. Default: 10000");
    				sender.sendMessage("protectbuilds: Should iNations prevent non-owners from build or destroying on plots. Default: true");
    			}
    			else if (args.length < 3)
    			{
    				sender.sendMessage("You must specify a key and value.");
    				sender.sendMessage("/iadmin config <key> <value>");
    				sender.sendMessage("/iadmin config list");
    			}
    			else
    			{
    				
    			}
    			return true;
    		}
    		else if ( args[0].equals("motd") ) // Message of the Day
    		{
    			if (args.length < 2)
    			{
    				plugin.getConfig().set("global.motd", "");
    				sender.sendMessage("Message of the Day has been blanked.");
    			}
    			else
    			{
    				int x;
    				String motd = "";
    				
    				for (x=0;x<args.length-1;x++)
    				{
    					motd = motd + " " + args[x + 1];    					
    				}
    				
    				plugin.getConfig().set("global.motd", motd.trim());
    				sender.sendMessage("Message of the Day has been set to \"" + motd.trim() + "\".");
    			}
    			return true;
    		}
    		
    		// End of Admin Commands.
    		
    		sender.sendMessage("You must specify a valid sub-command of iNations Admin Command Subroutine \"/iadmin help\".");
    		return false;
    	}
    	else if ( cmd.getName().equalsIgnoreCase("iplayer") ) // iNations player commands
    	{
    		if (args.length < 1)
    		{
    			sender.sendMessage("You must specify a valid sub-command of iNations Player Command Subroutine. See \"/iplayer help\".");
    			return false;
    		}
    		
    		if ( args[0].equals("help") ) // Help of Sub-Commands
    		{
    			sender.sendMessage("/iplayer");
    			sender.sendMessage("<declare|disban|info|laws|flag|addmember|delmember|teleport|buy|sell>");
    			return true;
    		}
    		else if ( args[0].equals("info") ) // Info of Sub-Commands
    		{
    			Vector v = plugin.ChangeCordType(player.getLocation());
    			Plot plot = plugin.iPlots.getPlotFromVector(v);
    			if ( plot == null )
    			{
    				player.sendMessage(ChatColor.RED + "You currently are not standing on any claimed land/plot.");
    			}
    			else
    			{
    				player.sendMessage(ChatColor.AQUA + "Plot Positions: " + plot.toString());
    				player.sendMessage(ChatColor.AQUA + "Plot Owner: " + plot.owner);
    			}
    			return true;
    		}
    		else if ( args[0].equals("disban") ) // Disban of Sub-Commands
    		{
    			// TODO: Add Permissions Check
    			
    			Vector v = plugin.ChangeCordType(player.getLocation());
    			Plot plot = plugin.iPlots.getPlotFromVector(v);
    			if ( plot == null )
    			{
    				player.sendMessage(ChatColor.RED + "You currently are not standing on any claimed land/plot.");
    			}
    			else
    			{
    				plugin.db.query("DELETE FROM `plots` WHERE `plotID` = '" + plot.plotID + "';");
    				plugin.iPlots.remove(plot.plotID);
    				player.sendMessage(ChatColor.AQUA + "You have successfully disbaned this plot.");
    			}
    			return true;
    		}
    		else if ( args[0].equals("declare") ) // Declare of Sub-Commands
    		{
    			Vector loc1 = (Vector) plugin.getMetadata(player, "position1");
    			Vector loc2 = (Vector) plugin.getMetadata(player, "position2");
        		
        		if (loc1 == null)
        		{
        			sender.sendMessage(ChatColor.RED + "You have not set position #1 using the Wooden Axe Left-Click");
        			return true;
        		}
        		
        		if (loc2 == null)
        		{
        			sender.sendMessage(ChatColor.RED + "You have not set position #2 using the Wooden Axe Right-Click");
        			return true;
        		}
        		
        		Boolean forceStacks = false;
        		Boolean forceFence = false;
        		Boolean forceTorches = false;
        		int x;
        		
        		if (args.length > 1)
        		{
	        		for (x=1;x<=args.length-1;x++)
	        		{
	        			if ( args[x].equals("stacks") )
	        			{
	        				forceStacks = true;
	        			}
	        			else if ( args[x].equals("fences") )
	        			{
	        				forceFence = true;
	        			}
	        			else if ( args[x].equals("torches") )
	        			{
	        				forceTorches = true;
	        			}
	        			else
	        			{
	        				sender.sendMessage(ChatColor.YELLOW + "Warning: " + args[x] + " was not detected as a valid parameter of Plot Declare.");	        				
	        			}
	        		}
	    		}
        		
        		String plotID = loc1.getBlockX() + "_" + loc1.getBlockY() + "_" + loc1.getBlockZ() + "_" + loc2.getBlockX() + "_" + loc2.getBlockY() + "_" + loc2.getBlockZ();
        		
        		int height = plugin.getConfig().getInt("global.ownershipheight", 50);
        		int depth = plugin.getConfig().getInt("global.ownershipdepth", 20);
        		
        		// Check the expansion requirements.
        		if (height == 0 && loc1.getY() > loc2.getY())
        		{
        			loc1.setY(255);
        			if (depth == 0)
        				loc2.setY(0);
        		}
        		else if (height == 0 && loc1.getY() < loc2.getY())
        		{
        			loc2.setY(255);
        			if (depth == 0)
        				loc1.setY(0);
        		}
        		else if (depth == 0 && loc1.getY() > loc2.getY())
        		{
        			loc2.setY(0);
        		}
        		else if (depth == 0 && loc1.getY() < loc2.getY())
        		{
        			loc1.setY(0);
        		}
        		
        		Plot cube = new Plot(loc1, loc2, player);
        		
        		if ( height != 0 )
       				height = (cube.getHeight() < height) ? height - cube.getHeight() : 0;
        		
        		for (Plot cubeit : plugin.iPlots.values())
            	{
            		if (cube.intersects(cubeit))
            		{
            			sender.sendMessage(ChatColor.RED + "This new plot intersects with an existing plot. Placement is not allowed.");
            			return true;
            		}
            	}
        		
        		Vector pos1 = cube.getPos1();
        		Vector pos4 = cube.getPos2();
        		
    			Vector pos2 = new Vector(pos1.getBlockX(), 100, pos4.getBlockZ());
    			Vector pos3 = new Vector(pos4.getBlockX(), 100, pos1.getBlockZ());
        		
        		if ( plugin.getConfig().getBoolean("global.autostacks") || forceStacks ) // Place Stacks in the corners of the plot
           		{
        			Location b1 = new Location(player.getWorld(), pos1.getBlockX(), player.getWorld().getHighestBlockYAt(pos1.getBlockX(), pos1.getBlockZ()) + 1, pos1.getBlockZ());
        			Location b2 = new Location(player.getWorld(), pos2.getBlockX(), player.getWorld().getHighestBlockYAt(pos2.getBlockX(), pos2.getBlockZ()) + 1, pos2.getBlockZ());
        			Location b3 = new Location(player.getWorld(), pos3.getBlockX(), player.getWorld().getHighestBlockYAt(pos3.getBlockX(), pos3.getBlockZ()) + 1, pos3.getBlockZ());
        			Location b4 = new Location(player.getWorld(), pos4.getBlockX(), player.getWorld().getHighestBlockYAt(pos4.getBlockX(), pos4.getBlockZ()) + 1, pos4.getBlockZ());
        			
        			b1.getBlock().setType(Material.WOOD);
        			b2.getBlock().setType(Material.WOOD);
        			b3.getBlock().setType(Material.WOOD);
        			b4.getBlock().setType(Material.WOOD);
        			b1.getBlock().setData((byte) 3);
        			b2.getBlock().setData((byte) 3);
        			b3.getBlock().setData((byte) 3);
        			b4.getBlock().setData((byte) 3);
        			
        			b1.setY(b1.getY() - 1);
        			b2.setY(b2.getY() - 1);
        			b3.setY(b3.getY() - 1);
        			b4.setY(b4.getY() - 1);
        			
        			b1.getBlock().setType(Material.WOOD);
        			b2.getBlock().setType(Material.WOOD);
        			b3.getBlock().setType(Material.WOOD);
        			b4.getBlock().setType(Material.WOOD);
        			b1.getBlock().setData((byte) 3);
        			b2.getBlock().setData((byte) 3);
        			b3.getBlock().setData((byte) 3);
        			b4.getBlock().setData((byte) 3);
        			
        			Location tmp1 = null;
        			Location tmp2 = null;
        			Location tmp3 = null;
        			Location tmp4 = null;
        			World w = b1.getWorld();
        					
                	Location bc = b1;
                	Location bl = b2;
                	Location br = b3;
                	Location bs = b4;
        			
        			// Cross X
        					
        			if (bc.getBlockX() > bl.getBlockX() && bc.getBlockZ() == bl.getBlockZ())
        			{
        				tmp1 = new Location(w, bc.getX() - 1, bc.getY(), bc.getZ());
        				tmp2 = new Location(w, bl.getX() + 1, bl.getY(), bl.getZ());
        				tmp3 = new Location(w, br.getX() - 1, br.getY(), br.getZ());
        				tmp4 = new Location(w, bs.getX() + 1, bs.getY(), bs.getZ());
        			}
        			else if (bc.getBlockX() > br.getBlockX() && bc.getBlockZ() == br.getBlockZ())
        			{
        				tmp1 = new Location(w, bc.getX() - 1, bc.getY(), bc.getZ());
        				tmp2 = new Location(w, br.getX() + 1, br.getY(), br.getZ());
        				tmp3 = new Location(w, bl.getX() - 1, bl.getY(), bl.getZ());
        				tmp4 = new Location(w, bs.getX() + 1, bs.getY(), bs.getZ());
        			}
        			else if (bc.getBlockX() < bl.getBlockX() && bc.getBlockZ() == bl.getBlockZ())
        			{
        				tmp1 = new Location(w, bc.getX() + 1, bc.getY(), bc.getZ());
        				tmp2 = new Location(w, bl.getX() - 1, bl.getY(), bl.getZ());
        				tmp3 = new Location(w, br.getX() + 1, br.getY(), br.getZ());
        				tmp4 = new Location(w, bs.getX() - 1, bs.getY(), bs.getZ());
        			}
        			else if (bc.getBlockX() < br.getBlockX() && bc.getBlockZ() == br.getBlockZ())
        			{
        				tmp1 = new Location(w, bc.getX() + 1, bc.getY(), bc.getZ());
        				tmp2 = new Location(w, br.getX() - 1, br.getY(), br.getZ());
        				tmp3 = new Location(w, bl.getX() + 1, bl.getY(), bl.getZ());
        				tmp4 = new Location(w, bs.getX() - 1, bs.getY(), bs.getZ());
        			}
        			else
        			{
        				tmp1 = bc;
        				tmp2 = bl;
        				tmp3 = br;
        				tmp4 = bs;
        			}
        			
        			tmp1.getBlock().setType(Material.WOOD);
        			tmp1.getBlock().setData((byte) 3);
        			tmp2.getBlock().setType(Material.WOOD);
        			tmp2.getBlock().setData((byte) 3);
        			tmp3.getBlock().setType(Material.WOOD);
        			tmp3.getBlock().setData((byte) 3);
        			tmp4.getBlock().setType(Material.WOOD);
        			tmp4.getBlock().setData((byte) 3);
        			
        			// Cross Z
        					
        			if (bc.getBlockZ() > bl.getBlockZ() && bc.getBlockX() == bl.getBlockX())
        			{
        				tmp1 = new Location(w, bc.getX(), bc.getY(), bc.getZ() - 1);
        				tmp2 = new Location(w, bl.getX(), bl.getY(), bl.getZ() + 1);
        				tmp3 = new Location(w, br.getX(), br.getY(), br.getZ() - 1);
        				tmp4 = new Location(w, bs.getX(), bs.getY(), bs.getZ() + 1);
        			}
        			else if (bc.getBlockZ() > br.getBlockZ() && bc.getBlockX() == br.getBlockX())
        			{
        				tmp1 = new Location(w, bc.getX(), bc.getY(), bc.getZ() - 1);
        				tmp2 = new Location(w, br.getX(), bl.getY(), br.getZ() + 1);
        				tmp3 = new Location(w, bl.getX(), br.getY(), bl.getZ() - 1);
        				tmp4 = new Location(w, bs.getX(), bs.getY(), bs.getZ() + 1);
        			}
        			else if (bc.getBlockZ() < bl.getBlockZ() && bc.getBlockX() == bl.getBlockX())
        			{
        				tmp1 = new Location(w, bc.getX(), bc.getY(), bc.getZ() + 1);
        				tmp2 = new Location(w, bl.getX(), bl.getY(), bl.getZ() - 1);
        				tmp3 = new Location(w, br.getX(), br.getY(), br.getZ() + 1);
        				tmp4 = new Location(w, bs.getX(), bs.getY(), bs.getZ() - 1);
        			}
        			else if (bc.getBlockZ() < br.getBlockZ() && bc.getBlockX() == br.getBlockX())
        			{
        				tmp1 = new Location(w, bc.getX(), bc.getY(), bc.getZ() + 1);
        				tmp2 = new Location(w, br.getX(), br.getY(), br.getZ() - 1);
        				tmp3 = new Location(w, bl.getX(), bl.getY(), bl.getZ() + 1);
        				tmp4 = new Location(w, bs.getX(), bs.getY(), bs.getZ() - 1);
        			}
        			else
        			{
        				tmp1 = bc;
        				tmp2 = bl;
        				tmp3 = br;
        				tmp4 = bs;
        			}
        			
        			tmp1.getBlock().setType(Material.WOOD);
        			tmp1.getBlock().setData((byte) 3);
        			tmp2.getBlock().setType(Material.WOOD);
        			tmp2.getBlock().setData((byte) 3);
        			tmp3.getBlock().setType(Material.WOOD);
        			tmp3.getBlock().setData((byte) 3);
        			tmp4.getBlock().setType(Material.WOOD);
        			tmp4.getBlock().setData((byte) 3);
        		}
        		
        		if ( plugin.getConfig().getBoolean("global.autofence") || forceFence ) // Inline Plot with Fences
        		{
        			int phase = 1;
        			int min = 0;
    				int max = 0;
    				int tmp = 0;
    				int stat = 0;
    				//int maxY = (pos1.getBlockY() > pos2.getBlockY()) ? pos1.getBlockY() : pos2.getBlockY();
    				//int minY = (pos1.getBlockY() < pos2.getBlockY()) ? pos1.getBlockY() : pos2.getBlockY();
        			
        			while (phase < 5)
        			{
        				if ( phase == 1 )
        				{
        					min = pos1.getBlockX();
        					max = (pos2.getBlockX() == min) ? pos3.getBlockX() : pos2.getBlockX();
        					stat = pos1.getBlockZ();
        				}
        				else if (phase == 2)
        				{
        					min = pos1.getBlockZ();
        					max = (pos2.getBlockZ() == min) ? pos3.getBlockZ() : pos2.getBlockZ();
        					stat = pos1.getBlockX();
        				}
        				else if (phase == 3)
        				{
        					min = pos4.getBlockX();
        					max = (pos2.getBlockX() == min) ? pos3.getBlockX() : pos2.getBlockX();
        					stat = pos4.getBlockZ();
        				}
        				else if (phase == 4)
        				{
        					min = pos4.getBlockZ();
        					max = (pos2.getBlockZ() == min) ? pos3.getBlockZ() : pos2.getBlockZ();
        					stat = pos4.getBlockX();
        				}
        				
    					if (min > max)
    					{
    						tmp = max;
    						max = min;
    						min = tmp;
    					}
        				
    					while (min <= max)
    					{
    						Vector v = (phase == 1 || phase == 3) ? new Vector(min, 100, stat) : new Vector(stat, 100, min); 
    						
    						Block b = player.getWorld().getHighestBlockAt(v.getBlockX(), v.getBlockZ());
    						b.setType(Material.FENCE);
    						
    						min++;
    						
    						// TODO: Improve Top Detection System
    						
    						/*
    						v = plugin.ChangeCordType(b.getLocation());
    						if (v.getBlockY() > minY && v.getBlockY() < maxY)
    						{
    							b.setType(Material.FENCE);
    						}
    						else
    						{
    							v.setY(maxY);
    							Vector lastAirBender = null;
    							while (v.getBlockY() > minY)
    							{
    								b = player.getWorld().getBlockAt(plugin.ChangeCordType(v));
    								if (b.getType() == Material.AIR)
    									lastAirBender = v;
    								
    								v.setY(v.getBlockY() - 1);
    							}
    							if (lastAirBender != null)
    							{
    								b = player.getWorld().getBlockAt(plugin.ChangeCordType(v));
    								b.setType(Material.FENCE);
    							}
    						}
    						*/
    					}
        				
        				phase++;
        			}
        		}
        		
        		if ( plugin.getConfig().getBoolean("global.autotorches") || forceTorches ) // Place Torch in the corners of the plot
        		{
        			Block b = player.getWorld().getHighestBlockAt(pos1.getBlockX(), pos1.getBlockZ());
					b.setType(Material.TORCH);
        			
					b = player.getWorld().getHighestBlockAt(pos2.getBlockX(), pos2.getBlockZ());
					b.setType(Material.TORCH);
					
					b = player.getWorld().getHighestBlockAt(pos3.getBlockX(), pos3.getBlockZ());
					b.setType(Material.TORCH);
        			
					b = player.getWorld().getHighestBlockAt(pos4.getBlockX(), pos4.getBlockZ());
					b.setType(Material.TORCH);
        		}
        		
        		cube.expand(new Vector(0, height, 0), new Vector(0, 0 - depth, 0));
        		cube.plotID = plotID;
        		
        		plugin.iPlots.add(cube);
        		
    			plugin.db.query("INSERT INTO `plots` (" +
    				"`plotID`, " +
    				"`owner`, " +
    				"`members`, " +
    				"`world`, " +
    				"`type`, " +
    				"`address`, " +
    				"`displayName`, " +
    				"`parentID`, " +
    				"`x1`, " +
    				"`y1`, " +
    				"`z1`, " +
    				"`x2`, " +
    				"`y2`, " +
    				"`z2`" +
    				") VALUES (" +
    				"'" + plotID + "', " +
    				"'" + sender.getName() + "', " +
    				"'', " +
    				"'" + player.getWorld().getUID() + "', " +
    				"'1', " +
    				"'', " +
    				"'', " +
    				"'', " +
    				"'" + cube.getPos1().getBlockX() + "', " +
    				"'" + cube.getPos1().getBlockY() + "', " +
    				"'" + cube.getPos1().getBlockZ() + "', " +
    				"'" + cube.getPos2().getBlockX() + "', " +
    				"'" + cube.getPos2().getBlockY() + "', " +
    				"'" + cube.getPos2().getBlockZ() + "');");
        		
        		sender.sendMessage(ChatColor.AQUA + "You have just successfully declared land with a sum area of " + cube.getArea() + " blocks.");
        		
        		plugin.setMetadata(player, "position1", null);
        		plugin.setMetadata(player, "position2", null);
        		
        		return true;
    		}
    		else if ( args[0].equals("pos") ) // Pos of Sub-Commands
    		{
        		
    		}
    		
    		// End of iPlayer Commands.
    		
    		sender.sendMessage(ChatColor.RED + "You must specify a valid sub-command of iNations Player Command Subroutine. See \"/iplayer help\".");
    		return false;
    	}
    	
    	return false; 
    }
}
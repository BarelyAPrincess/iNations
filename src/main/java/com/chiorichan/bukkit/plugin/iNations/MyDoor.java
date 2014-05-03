package com.chiorichan.bukkit.plugin.iNations;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class MyDoor
{
	private Block door1;
	private Block door2 = null;
	
	public static MyDoor getDoor( Block var1 )
	{
		if ( !isDoor( var1 ) )
			return null;
		
		if ( isTopHalf( var1 ) )
		{
			var1 = var1.getRelative( BlockFace.DOWN );
			
			if ( var1 == null || !isDoor( var1 ) )
				return null;
		}
		
		Block var2 = null;
		
		if ( isDoor( var1.getRelative( BlockFace.NORTH ) ) && isConnected( var1, var1.getRelative( BlockFace.NORTH ) ) )
			var2 = var1.getRelative( BlockFace.NORTH );
		else if ( isDoor( var1.getRelative( BlockFace.SOUTH ) ) && isConnected( var1, var1.getRelative( BlockFace.SOUTH ) ) )
			var2 = var1.getRelative( BlockFace.SOUTH );
		else if ( isDoor( var1.getRelative( BlockFace.EAST ) ) && isConnected( var1, var1.getRelative( BlockFace.EAST ) ) )
			var2 = var1.getRelative( BlockFace.EAST );
		else if ( isDoor( var1.getRelative( BlockFace.WEST ) ) && isConnected( var1, var1.getRelative( BlockFace.WEST ) ) )
			var2 = var1.getRelative( BlockFace.WEST );
		
		Boolean foundDoubleDoor = ( var2 != null );
		
		MyDoor var3 = ( foundDoubleDoor ) ? new MyDoor( var1, var2 ) : new MyDoor( var1, null );
		
		if ( !var3.isValid() )
			return null;
		
		return var3;
	}
	
	/**
	 * Determines if a sign is near by that locks this door.
	 */
	public Boolean isLocked()
	{
		if ( isDoubleDoor() )
			return ( isLocked( door1 ) || isLocked( door2 ) );
		else
			return isLocked( door1 );
	}
	
	private Boolean isLocked( Block var1 )
	{
		return ( findNearBySign( var1, "[locked]" ) != null );
	}
	
	public Boolean isPermitted( Player var1 )
	{
		if ( isWoolLocked() )
		{
			String color;
			
			if ( isWoolLocked( door1 ) )
				color = getWoolColor( door1.getRelative( BlockFace.DOWN ) );
			else
				color = getWoolColor( door2.getRelative( BlockFace.DOWN ) );
			
			if ( !var1.hasPermission( "inations." + color.toLowerCase().replaceAll( " ", "" ) ) )
				return false;
		}
		
		Sign found = findNearBySign( door1, "[locked]" );
		
		if ( found == null && isDoubleDoor() )
			found = findNearBySign( door2, "[locked]" );
		
		if ( found == null )
			return true;
		
		String[] var3 = found.getLines();
		
		if ( var3 != null )
		{
			Boolean allowed = false;
			
			for ( String mm : var3 )
			{
				mm = mm.replaceAll( ChatColor.AQUA.toString(), "" );
				
				if ( !mm.isEmpty() && var1.getName().startsWith( mm ) )
				{
					allowed = true;
					break;
				}
			}
			
			return allowed;
		}
		
		return false;
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
	private Sign findNearBySign( Block var1, String var2 )
	{
		Sign found = null;
		
		List<Block> possible = new ArrayList<Block>();
		
		if ( var1 == null )
			return null;
		
		possible.add( var1 );
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
			possible.add( level2.getRelative( BlockFace.NORTH ) );
			possible.add( level2.getRelative( BlockFace.EAST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH ) );
			possible.add( level2.getRelative( BlockFace.WEST ) );
			possible.add( level2.getRelative( BlockFace.NORTH_EAST ) );
			possible.add( level2.getRelative( BlockFace.NORTH_WEST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH_EAST ) );
			possible.add( level2.getRelative( BlockFace.SOUTH_WEST ) );
		}
		
		Block level3 = var1.getRelative( BlockFace.DOWN );
		if ( level3 != null && level3.getRelative( BlockFace.DOWN ) != null )
		{
			level3 = level3.getRelative( BlockFace.DOWN );
			
			possible.add( level3 );
			possible.add( level3.getRelative( BlockFace.DOWN ) );
			possible.add( level3.getRelative( BlockFace.NORTH ) );
			possible.add( level3.getRelative( BlockFace.EAST ) );
			possible.add( level3.getRelative( BlockFace.SOUTH ) );
			possible.add( level3.getRelative( BlockFace.WEST ) );
			possible.add( level3.getRelative( BlockFace.NORTH_EAST ) );
			possible.add( level3.getRelative( BlockFace.NORTH_WEST ) );
			possible.add( level3.getRelative( BlockFace.SOUTH_EAST ) );
			possible.add( level3.getRelative( BlockFace.SOUTH_WEST ) );
		}
		
		for ( Block sign : possible )
		{
			if ( sign != null && sign.getState() instanceof Sign )
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
	
	/**
	 * Determines if there is a colored wool block under the door.
	 */
	public Boolean isWoolLocked()
	{
		Boolean locked = false;
		
		if ( door1.getRelative( BlockFace.DOWN ) != null && door1.getRelative( BlockFace.DOWN ).getType() == Material.WOOL )
			locked = true;
		
		if ( isDoubleDoor() && door2.getRelative( BlockFace.DOWN ) != null && door2.getRelative( BlockFace.DOWN ).getType() == Material.WOOL )
			locked = true;
		
		return locked;
	}
	
	public Boolean isWoolLocked( Block var1 )
	{
		if ( isTopHalf( var1 ) )
			return ( var1.getRelative( BlockFace.DOWN ) != null && var1.getRelative( BlockFace.DOWN ).getRelative( BlockFace.DOWN ) != null && var1.getRelative( BlockFace.DOWN ).getRelative( BlockFace.DOWN ).getType() == Material.WOOL );
		else
			return ( var1.getRelative( BlockFace.DOWN ) != null && var1.getRelative( BlockFace.DOWN ).getType() == Material.WOOL );
	}
	
	public static String getWoolColor( Block b )
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
	
	private MyDoor(Block var1, Block var2)
	{
		door1 = var1;
		door2 = var2;
	}
	
	public Boolean isValid()
	{
		if ( isDoubleDoor() )
			return ( isDoor( door1 ) && !isTopHalf( door1 ) && isDoor( door2 ) && !isTopHalf( door2 ) );
		else
			return ( isDoor( door1 ) && !isTopHalf( door1 ) );
	}
	
	public static boolean isDoor( Block var1 )
	{
		if ( var1 == null )
			return false;
		
		Material m = var1.getType();
		
		if ( m == Material.WOOD_DOOR || m == Material.WOODEN_DOOR || m == Material.IRON_DOOR || m == Material.IRON_DOOR_BLOCK )
			return true;
		
		for ( int blockId : INations.instance.getConfig().getIntegerList( "lockable_doors" ) )
			if ( m.getId() == blockId )
				return true;
		
		return isAdminDoor( var1 );
	}
	
	public static boolean isAdminDoor( Block var1 )
	{
		if ( var1 == null )
			return false;
		
		Material m = var1.getType();
		
		if ( m == Material.IRON_DOOR || m == Material.IRON_DOOR_BLOCK )
			return true;
		
		for ( int blockId : INations.instance.getConfig().getIntegerList( "admin_doors" ) )
			if ( m.getId() == blockId )
				return true;
		
		return false;
	}
	
	public static Boolean isTopHalf( Block var1 )
	{
		if ( ( var1.getData() & 0x8 ) == 0x08 )
			return true;
		else
			return false;
	}
	
	public static boolean isConnected( Block door1Top, Block door2Top )
	{
		if ( !isTopHalf( door1Top ) )
			door1Top = door1Top.getRelative( BlockFace.UP );
		
		if ( !isTopHalf( door2Top ) )
			door2Top = door2Top.getRelative( BlockFace.UP );
		
		if ( door1Top == null || door2Top == null )
			return false;
		
		if ( door1Top.getY() != door2Top.getY() )
			return false;
		
		if ( door1Top.getLocation().distance( door2Top.getLocation() ) > 1 )
			return false;
		
		if ( ( door1Top.getData() & 0x1 ) == ( door2Top.getData() & 0x1 ) ) // If hinges are both the same side.
			return false;
		else
			return true;
	}
	
	public Boolean isDoubleDoor()
	{
		return ( door1 != null && door2 != null && isConnected( door1, door2 ) );
	}
	
	/**
	 * If there is a second door attached to this class then we will force it to follow what the first door does.
	 */
	public void setOpen( Boolean var1 )
	{
		setOpen( var1, door1 );
		
		if ( isDoubleDoor() )
			setOpen( var1, door2 );
	}
	
	public static void setOpen( Boolean var1, Block var2 )
	{
		if ( var1 && !isOpen( var2.getData() ) )
			var2.setData( (byte) ( var2.getData() | 0x4 ) );
		else if ( !var1 && isOpen( var2.getData() ) )
			var2.setData( (byte) ( var2.getData() & ~0x4 ) );
		// else = Door is already in the state that we want it in.
	}
	
	public static Boolean isOpen( Byte var1 )
	{
		if ( ( var1 & 0x4 ) == 0x4 )
			return true;
		else
			return false;
	}
	
	public static Boolean isOpen( Block var1 )
	{
		if ( ( var1.getData() & 0x4 ) == 0x4 )
			return true;
		else
			return false;
	}
	
	public Boolean isOpen()
	{
		return isOpen( door1.getData() );
	}
	
	public static void flipDoor( Block var1 )
	{
		setOpen( !isOpen( var1 ), var1 );
	}
	
	public void flipDoor()
	{
		setOpen( !isOpen() );
	}
	
	public String toString()
	{
		return "Door1 " + door1 + ", Door2 " + door2 + ", isDoubled " + isDoubleDoor() + ", isValid " + isValid() + ", DoorOpen " + isOpen();
	}

	public boolean isAdminDoor()
	{
		if ( isDoubleDoor() )
			return ( isAdminDoor( door1 ) && isAdminDoor( door2 ) );
		else
			return ( isAdminDoor( door1 ) );
	}
}

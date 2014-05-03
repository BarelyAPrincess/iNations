package co.applebloom.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import com.chiorichan.bukkit.plugin.iNations.INations;

public class WebsocketHandler
{
	public static Long lastData = 0L;
	private WebsocketExtended weh = null;
	
	public Boolean isConnected = false;
	
	public Boolean preCheck()
	{
		if ( isConnected )
			return true;
		else
		{
			makeConnection();
		}
		
		if ( isConnected )
		{
			return true;
		}
		else
		{
			WebSocketService.console.sendMessage( "[iNations] It seems our attempt to connect with the Web Socket has timed out. Try again later." );
			return false;
		}
	}
	
	public void disable()
	{
		if ( weh != null )
			weh.close();
	}
	
	public void makeConnection()
	{
		try
		{
			if ( weh != null )
				weh.close();
			
			weh = new WebsocketExtended();
			weh.connect();
		}
		catch ( Throwable e )
		{
			e.printStackTrace();
		}
	}
	
	public void send( String text )
	{
		// WebSocketService.console.sendMessage( "We attempted to send \"" + text + "\" to the Web Socket." );
		
		if ( weh != null )
			weh.send( text );
	}
	
	class WebsocketExtended extends WebSocketClient
	{
		public WebsocketExtended()
		{
			super( URI.create( "ws://" + WebSocketService.server_addr + ":" + WebSocketService.server_port + "/websocket" ), new Draft_17() );
		}
		
		@Override
		public void onOpen( ServerHandshake arg0 )
		{
			WebSocketService.console.sendMessage( "Status: Connected to " + "ws://" + WebSocketService.server_addr + ":" + WebSocketService.server_port + "/websocket" );
			isConnected = true;
		}
		
		@Override
		public void onMessage( String payload )
		{
			String arr[] = payload.split( " ", 2 );
			String cmd = arr[0].toUpperCase();
			payload = ( arr.length > 1 ) ? arr[1].trim() : "";
			
			// WebSocketService.console.sendMessage( "Got Message: " + cmd + " Payload: " + payload );
			
			if ( cmd.equals( "PONG" ) )
			{
				// WebSocketService.console.sendMessage(
				// "Receive a message of good health from the Apple Bloom Rewards Web Socket. :)" );
				WebSocketService.deviceState = "Received a message of good health from the Web Socket. :)";
				
				if ( WebSocketService.lastPing > 0 )
					WebSocketService.lastLatency = System.currentTimeMillis() - WebSocketService.lastPing;
			}
			else if ( cmd.equals( "MSG" ) )
			{
				Bukkit.getLogger().info( payload );
				
				if ( INations.instance != null )
					for ( Player p : INations.instance.getServer().getOnlinePlayers() )
					{
						p.sendMessage( payload );
					}
			}
			else if ( cmd.equals( "OCHAT" ) )
			{
				Bukkit.getLogger().info( payload );
				
				if ( INations.instance != null )
					for ( Player p : INations.instance.getServer().getOnlinePlayers() )
					{
						if ( p.isOp() )
							p.sendMessage( payload );
						
					}
			}
			else if ( cmd.equals( "SCHAT" ) )
			{
				Bukkit.getLogger().info( payload );
				
				if ( INations.instance != null )
					for ( Player p : INations.instance.getServer().getOnlinePlayers() )
					{
						if ( p.isOp() || p.hasPermission( "inations.staff" ) )
							p.sendMessage( payload );
					}
			}
			else if ( cmd.equals( "STATUS" ) )
			{
				if ( INations.instance != null )
				{
					String stat = "";
					
					for ( Player p : INations.instance.getServer().getOnlinePlayers() )
					{
						stat += "," + p.getDisplayName();
					}
					
					WebSocketService.send( "RESPOND " + INations.instance.getConfig().getString( "global.motd_prefix" ) + ": " + stat.substring( 1 ) );
				}
			}
			else if ( cmd.equals( "RESPOND" ) )
			{
				if ( INations.instance != null )
					for ( Player p : INations.instance.getServer().getOnlinePlayers() )
					{
						if ( p.isOp() || p.hasPermission( "inations.staff" ) )
							p.sendMessage( payload );
					}
			}
			
			lastData = System.currentTimeMillis();
		}
		
		@Override
		public void onClose( int code, String reason, boolean arg2 )
		{
			isConnected = false;
			WebSocketService.console.sendMessage( "Websocket Connection was Lost! :(" );
		}
		
		@Override
		public void onError( Exception arg0 )
		{
			isConnected = false;
			arg0.printStackTrace();
		}
	}
}

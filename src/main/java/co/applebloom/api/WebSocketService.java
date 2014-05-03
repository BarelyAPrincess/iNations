package co.applebloom.api;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.command.ConsoleCommandSender;

import com.chiorichan.bukkit.plugin.iNations.INations;

public class WebSocketService
{
	public static Boolean changesMade = true;
	public static long lastPing = 0;
	public static long lastLatency = 0;
	public static String deviceState = "The Web Socket is Idle!";
	public static String server_addr = "localhost";
	public static long server_port = 1040;
	public static INations plugin = null;
	public static ConsoleCommandSender console = null;
	private static Timer timer1 = new Timer();
	
	public static WebsocketHandler chi = new WebsocketHandler();
	
	public WebSocketService(String addr, long port, INations parent)
	{
		server_addr = addr;
		server_port = port;
		plugin = parent;
		console = parent.getServer().getConsoleSender();
		
		console.sendMessage( "Web Socket Started! :D" );
		
		timer1.scheduleAtFixedRate( new TimerTask()
		{
			@Override
			public void run()
			{
				if ( chi.preCheck() )
				{
					chi.send( "PING " + lastLatency );
					lastPing = System.currentTimeMillis();
				}
			}
		}, 0, 5000 );
	}
	
	public void disable()
	{
		timer1.cancel();
		chi.disable();
		chi = null;
	}
	
	public static Boolean send( String msg )
	{
		if ( INations.instance.getConfig().getBoolean( "remote_enabled", false ) )
			return false;
		
		if ( !chi.isConnected )
			return false;
		
		chi.send( msg );
		
		return true;
	}
}

package uk.co.mobsoc.chat.plugin;

import org.bukkit.Bukkit;
/**
 * Will try every second to reconnect to the main MobsChatServer
 * @author triggerhapp
 *
 */
public class Reconnect implements Runnable{
	public static boolean connected = false;
	public Reconnect(ChatPlugin main){
		if(connected){
			Bukkit.broadcastMessage("Connection to Chat Server lost... please wait");
		}
		connected = false;
		System.out.println("No connection ... waiting 1 second");
		Bukkit.getScheduler().scheduleSyncDelayedTask(main, this, 20);
	}

	@Override
	public void run() {
		ChatPlugin.inst.connect();	
	}

}

package uk.co.mobsoc.chat.plugin;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import uk.co.mobsoc.chat.common.packet.PrivateMessagePacket;
/**
 * Synchronized dealing with Private messages, to avoid any issues with async-ness of chat plugin
 * @author triggerhapp
 *
 */
public class PMTimer implements Runnable{

	private ChatPlugin main;

	public PMTimer(ChatPlugin chatPlugin) {
		main = chatPlugin;
		setTimer();
	}

	private void setTimer() {
		main.getServer().getScheduler().scheduleSyncDelayedTask(main, this, 1);		
	}

	@Override
	public void run() {
		setTimer();
		ArrayList<PrivateMessagePacket> list;
		synchronized(PluginCallbacks.delayedPMs){
			list = PluginCallbacks.delayedPMs;
			PluginCallbacks.delayedPMs = new ArrayList<PrivateMessagePacket>();	
		}
		for(PrivateMessagePacket pmp : list){
			Player to = Bukkit.getPlayer(pmp.toString);
			Player from = Bukkit.getPlayer(pmp.fromChosenName);
			String msg = "[PM] "+PUtils.InternalMarkupToBukkit(pmp.fromChosenName)+" > "+PUtils.InternalMarkupToBukkit(pmp.toString)+" : "+PUtils.InternalMarkupToBukkit(pmp.message);
			if(to!=null && from!=null && to.getName().equalsIgnoreCase(from.getName())){
				to.sendMessage(msg);
				continue;
			}
			if(to!=null){
				to.sendMessage(msg);
			}
			if(from!=null){
				from.sendMessage(msg);
			}
		}
		
	}

}

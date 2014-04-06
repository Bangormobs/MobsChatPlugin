package uk.co.mobsoc.chat.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import uk.co.mobsoc.chat.common.packet.ChatPacket;
/**
 * A class to run on the next tick so that A-sync chat won't cause problems by using Bukkit sync-only commands
 * @author triggerhapp
 *
 */
public class ChatRunnable implements Runnable{
	ChatPacket cp;

	public ChatRunnable(ChatPacket cp) {
		this.cp = cp;
		Bukkit.getScheduler().scheduleSyncDelayedTask(ChatPlugin.inst, this);
	}

	@Override
	public void run() {
		String c = PUtils.getBukkitColour(cp.sourceColour)+"";
		//System.out.println(cp.name+" "+cp.message);
		//System.out.println(PUtils.InternalMarkupToBukkit(cp.message));
		Bukkit.broadcastMessage(c+"<"+ChatColor.RESET+PUtils.InternalMarkupToBukkit(cp.name)+c+"> "+ChatColor.RESET+PUtils.InternalMarkupToBukkit(cp.message));
	}
	

}

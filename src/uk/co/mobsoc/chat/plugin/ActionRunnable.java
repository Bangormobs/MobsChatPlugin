package uk.co.mobsoc.chat.plugin;

import org.bukkit.Bukkit;

import uk.co.mobsoc.chat.common.packet.ActionPacket;
/**
 * A class to run on the next tick so that A-sync chat won't cause problems by using Bukkit sync-only commands
 * @author triggerhapp
 *
 */
public class ActionRunnable implements Runnable{
	ActionPacket ap;
	public ActionRunnable(ActionPacket ap) {
		this.ap = ap;
		Bukkit.getScheduler().scheduleSyncDelayedTask(ChatPlugin.inst, this);
	}

	@Override
	public void run() {
		String c = PUtils.getBukkitColour(ap.sourceColour);
		Bukkit.broadcastMessage(c+PUtils.InternalMarkupToBukkit(ap.action));
	}

}

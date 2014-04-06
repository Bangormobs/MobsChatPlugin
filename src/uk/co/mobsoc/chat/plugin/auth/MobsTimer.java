package uk.co.mobsoc.chat.plugin.auth;

import org.bukkit.Bukkit;

import uk.co.mobsoc.chat.plugin.ChatPlugin;

public class MobsTimer implements Runnable{
	AuthMain main;
	public MobsTimer(AuthMain main){
		this.main= main;
		setTimer();
	}
	/**
	 * Call this class's run function every 10 seconds. (20 ticks a second)
	 */
	private void setTimer() {
		if(AuthMain.permission!=null){
			Bukkit.getScheduler().scheduleSyncDelayedTask(ChatPlugin.inst, this, 20*10);
		}
	}
	@Override
	public void run() {
		setTimer();
		new DoAllOnline();
	}
}

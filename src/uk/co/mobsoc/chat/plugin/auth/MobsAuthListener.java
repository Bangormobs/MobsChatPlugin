package uk.co.mobsoc.chat.plugin.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import uk.co.mobsoc.chat.plugin.ChatPlugin;

public class MobsAuthListener implements Listener{

	public MobsAuthListener(){
		ChatPlugin.inst.getServer().getPluginManager().registerEvents(this, ChatPlugin.inst);
	}
	
	/**
	 * Check for players who are banned
	 * @param event
	 */
	public void onPlayerLogin(PlayerLoginEvent event){
		PlayerData fb = AuthMain.getPlayerData(event.getPlayer().getUniqueId());
		if(fb.rank.equalsIgnoreCase("B")){
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You are banned");
		}
	}
}

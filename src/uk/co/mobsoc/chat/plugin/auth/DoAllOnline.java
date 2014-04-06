package uk.co.mobsoc.chat.plugin.auth;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import uk.co.mobsoc.chat.plugin.ChatPlugin;

public class DoAllOnline implements Runnable {

	/**
	 * If this server has Vault and Permissions, re-adjust users ranks next tick
	 */
	public DoAllOnline(){
		if(AuthMain.permission != null){
			Bukkit.getScheduler().scheduleSyncDelayedTask(ChatPlugin.inst, this);
		}
	}

	/**
	 * Itterate over all online users and update their rank inline with the cached rank data from Server
	 */
	@Override
	public void run() {
		for(Player p : Bukkit.getOnlinePlayers()){
			PlayerData fb = AuthMain.getPlayerData(p.getUniqueId());
			String newRank = null;
			//if(fb.isValid()){
				if(fb.rank!=null && fb.rank.length()==1){
					newRank = ChatPlugin.inst.getConfig().getString("ranks."+fb.rank.toLowerCase());

				}
			//}
			if(newRank == null || newRank.length()==0){
				newRank = "visitor";
			}
			setRank(p, newRank);
			
			for(String group: AuthMain.permission.getPlayerGroups(p)){
				if(group.equalsIgnoreCase(newRank)){ continue; }
				removeRank(p, group);
			}
		}
		
	}
	
	/**
	 * Remove the rank from a player. Since players can technically belong to multiple groups, all others must be removed from the player after giving the correct rank
	 * @param p
	 * @param rank
	 */
	public void removeRank(Player p, String rank){
		for(World w: Bukkit.getWorlds()){
			AuthMain.permission.playerRemoveGroup(w.getName(), p.getName(), rank);
		}
	}
	/**
	 * Add the player to given group
	 * @param p
	 * @param rank
	 */
	public void setRank(Player p, String rank){
		for(World w : Bukkit.getWorlds()){
			AuthMain.permission.playerAddGroup(w.getName(), p.getName(), rank);
		}
	}

}

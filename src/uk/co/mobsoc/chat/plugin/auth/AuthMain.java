package uk.co.mobsoc.chat.plugin.auth;


import java.util.HashMap;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;
import uk.co.mobsoc.chat.plugin.ChatPlugin;
/**
 * Since there is no guarantee that Vault is running on any given server, this is seperated out.
 * @author triggerhapp
 *
 */
public class AuthMain {
	/**
	 * Cached User information from server.
	 */
	static HashMap<UUID, PlayerData> userList = new HashMap<UUID, PlayerData>();
	public static Permission permission = null;
	public static AuthMain main;

	public AuthMain(){
		main = this;
		setupPermissions();
		new MobsAuthListener();
		new MobsTimer(this);
		//new ReminderTimer(ChatPlugin.inst);
	}
	
	/**
	 * Yes, eww I'm catching NoClassDefFoundError. Deal with it.
	 * @return
	 */
    private boolean setupPermissions()
    {
    	permission= null;
    	try{
    		RegisteredServiceProvider<Permission> permissionProvider = ChatPlugin.inst.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
    		if (permissionProvider != null) {
    			permission = permissionProvider.getProvider();
    		}
    	}catch(NoClassDefFoundError e){
    		System.out.println("No Vault Found! Not enforcing groups!");
    	}
        return (permission != null);
    }
    
	/**
	 * Gets any cached information on the player with a given UUID
	 * @param uuid
	 * @return
	 */
	public static PlayerData getPlayerData(UUID uuid){
		PlayerData fbd = new PlayerData();
		if(userList.containsKey(uuid)){ return userList.get(uuid); }
		return fbd;
	}

	/**
	 * Not sure why I should keep this since it's been made redundant!
	 * @param sender
	 * @param cmd
	 * @param args
	 * @return
	 */
	public static boolean command(CommandSender sender, String cmd, String[] args) {
		cmd = cmd.replace("/", "");
     	if(args.length==0){
    		return true;
    	}
     	if(cmd.equalsIgnoreCase("fbdemote") || cmd.equalsIgnoreCase("fbpromote")){
     		sender.sendMessage("This function has been moved to the website - sorry!");
			return true;
     	}
     	if(cmd.equalsIgnoreCase("setrank")){
     		sender.sendMessage("This function has been moved to the website - sorry!");
			return true;
		}
     	return false;
	}

	/**
	 * Adds user to the cache. Should only be called in response to a server packet
	 * @param uuid
	 * @param rank
	 */
	public static void setRankI(UUID uuid, String rank) {
		PlayerData pd = new PlayerData();
		if(userList.containsKey(uuid)){ pd = userList.get(uuid); }
		pd.rank = rank;
		userList.put(uuid, pd);
	}
    	
}

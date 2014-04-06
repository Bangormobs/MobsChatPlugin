package uk.co.mobsoc.chat.plugin;

import org.bukkit.Bukkit;

import uk.co.mobsoc.chat.common.packet.ServerData;

import com.trigg.fp.FakePlayer;
/**
 * If FakePlayer plugin is running, update the list of players
 * @author triggerhapp
 *
 */
public class UpdateFakePlayer implements Runnable{
	
	public UpdateFakePlayer(){
		Bukkit.getScheduler().scheduleSyncDelayedTask(ChatPlugin.inst, this,1);
	}

	@Override
	public void run() {
		if(Bukkit.getPluginManager().getPlugin("FakePlayer")!=null){
			FakePlayer.clearAllNames();
			synchronized(PluginCallbacks.servers){
				for(ServerData sd : PluginCallbacks.servers){
					String c =sd.connectionColour.toInternal();
					FakePlayer.addListName(c, sd.connectionName);
					for(String user : sd.userList){
						FakePlayer.addPlayer(PUtils.InternalMarkupToBukkit(c+user));
					}
				}
			}
		}
		
	}

}

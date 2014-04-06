package uk.co.mobsoc.chat.plugin;
/**
 * A timer to send new information about the server state to MobsChatServer
 * @author triggerhapp
 *
 */
public class ServerDataTimer implements Runnable{
	private ChatPlugin main;
	boolean isFirst = true;
	public ServerDataTimer(ChatPlugin chatPlugin) {
		main = chatPlugin;
		setTimer();
	}
	private void setTimer() {
		main.getServer().getScheduler().scheduleSyncDelayedTask(main, this, 20 * 15); // 15 second intervals .. too much?		
	}
	
	@Override
	public void run() {
		setTimer();
		ChatPlugin.updateServerData(isFirst);
		isFirst = false;
	}

}

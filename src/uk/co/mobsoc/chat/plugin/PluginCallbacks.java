package uk.co.mobsoc.chat.plugin;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import uk.co.mobsoc.chat.common.Connector;
import uk.co.mobsoc.chat.common.SocketCallbacks;
import uk.co.mobsoc.chat.common.packet.ActionPacket;
import uk.co.mobsoc.chat.common.packet.ChatPacket;
import uk.co.mobsoc.chat.common.packet.ConnectionPacket;
import uk.co.mobsoc.chat.common.packet.Packet;
import uk.co.mobsoc.chat.common.packet.PingPacket;
import uk.co.mobsoc.chat.common.packet.PrivateMessagePacket;
import uk.co.mobsoc.chat.common.packet.ServerData;
import uk.co.mobsoc.chat.common.packet.SetRankPacket;
import uk.co.mobsoc.chat.common.packet.UserListUpdatePacket;
import uk.co.mobsoc.chat.plugin.auth.AuthMain;
/**
 * A callback implementation to deal with all packets sent from server
 * @author triggerhapp
 *
 */
public class PluginCallbacks implements SocketCallbacks {
	public static ArrayList<ServerData> servers = new ArrayList<ServerData>();
	public static ArrayList<PrivateMessagePacket> delayedPMs = new ArrayList<PrivateMessagePacket>();
	Connector conn;
	public PluginCallbacks(Connector sConn) {
		this.conn = sConn;
	}

	@Override
	public boolean packetRecieved(Connector conn, Packet packet) {
		conn.debug(packet.toString());
		if(packet instanceof PingPacket){
			// Ping packets should be instantly returned
			PingPacket pp = (PingPacket) packet;
			conn.send(pp);
			return true;
		}else if(packet instanceof ChatPacket){
			// New chat messages! Add to chat on next tick!
			ChatPacket cp = (ChatPacket) packet;
			new ChatRunnable(cp);
			return true;
		}else if(packet instanceof ActionPacket){
			// New action messages! Add to chat on next tick!
			ActionPacket ap = (ActionPacket) packet;
			new ActionRunnable(ap);
			return true;
		}else if(packet instanceof UserListUpdatePacket){
			// A new list of users - store them for later
			UserListUpdatePacket ulup = (UserListUpdatePacket) packet;
			servers = ulup.servers;
			new UpdateFakePlayer();
			return true;
		}else if(packet instanceof PrivateMessagePacket){
			// A new Private message!
			PrivateMessagePacket pmp = (PrivateMessagePacket) packet;
			synchronized(delayedPMs){
				delayedPMs.add(pmp);
			}
			return true;
		}else if(packet instanceof ConnectionPacket){
			// Sent back on reconnect
			Bukkit.broadcastMessage("Connected to Chat Server again!");
			Reconnect.connected=true;
			return true;
		}else if(packet instanceof SetRankPacket){
			// A players rank has changed
			SetRankPacket srp = (SetRankPacket) packet;
			AuthMain.setRankI(srp.uuid, srp.rank);
			return true;
		}
		return false;
	}

	@Override
	public void connectionLost(Connector conn) {
		new Reconnect(ChatPlugin.inst);
	}

}

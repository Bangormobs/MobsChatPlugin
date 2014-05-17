package uk.co.mobsoc.chat.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.ConfigurationNode;
import org.dynmap.DynmapCore;
import org.dynmap.bukkit.DynmapPlugin;

import uk.co.mobsoc.chat.common.Colour;
import uk.co.mobsoc.chat.common.Connector;
import uk.co.mobsoc.chat.common.SocketCallbacks;
import uk.co.mobsoc.chat.common.packet.ConnectionPacket;
import uk.co.mobsoc.chat.common.packet.Packet;
import uk.co.mobsoc.chat.common.packet.ServerInfoPacket;
import uk.co.mobsoc.chat.common.packet.VersionPacket;
import uk.co.mobsoc.chat.plugin.auth.AuthMain;

public class ChatPlugin extends JavaPlugin{
	public static Colour colour;
	static Connector sConn;
	public static ChatPlugin inst;
	SocketCallbacks callback;
	private String USERNAME, PASSWORD, HOST;
	public static boolean IGNOREVAULT=false;
	public static ArrayList<Colour> colourList = new ArrayList<Colour>();
	public static String THISHOST="", NAME="";
	public static int PORT=0, MAPPORT=-1, HIDDEN=0;
	
	/**
	 * I was using it from CLI for testing. Not anymore!
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println("Please use this as a Bukkit plugin...");
	}
	
	public void onEnable(){
		PORT = Bukkit.getServer().getPort();
		URL whatismyip=null;
		String ip = null;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com/");
			BufferedReader in = new BufferedReader(new InputStreamReader(
	                whatismyip.openStream()));
			ip = in.readLine(); //you get the IP as a String
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(ip == null){ ip = Bukkit.getServer().getIp(); }
		THISHOST = ip;
		NAME = Bukkit.getServerName();
		inst = this;
		System.out.println(NAME+" '"+THISHOST+":"+PORT+"'");
		MAPPORT = attemptDynmapConfig();
		doConfig();
		Packet.init();
		connect();
		new ChatListener(this);
		new PMTimer(this);
		if(HIDDEN==0){
			new ServerDataTimer(this);
		}
		new AuthMain();
	}
	
	/**
	 * Try to find out Dynmap setup from config file
	 * @param path
	 * @return
	 */
	private int attemptDynmapConfig(String path){
		int val = -1;
		File f = null;
		BufferedReader br = null; 
		try {
			f = new File(path);
			br = new BufferedReader(new FileReader(f));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ( (line = br.readLine() ) != null ){
				sb.append(line);
				sb.append("\n");
			}
			String contents = sb.toString();
			int startIndex = contents.indexOf("webserver-port:");
			startIndex+=15;
			contents = contents.substring(startIndex);
			startIndex = contents.indexOf("\n");
			contents = contents.substring(0,startIndex);
			val = Integer.parseInt(contents.trim());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return val;
	}
	/**
	 * Try to find out Dynmap setup from any config file
	 * @param path
	 * @return
	 */
	private int attemptDynmapConfig() {
		int i = attemptDynmapConfig("dynmap/configuration.txt");
		if(i>0){
			return i;
		}
		i= attemptDynmapConfig("plugins/dynmap/configuration.txt");
		if(i>0){
			return i;
		}
		return -1;
		
	}
	
	/**
	 * Get MobsChatServer information from Config file
	 */
	private void doConfig() {
		saveConfig();
		USERNAME = getConfig().getString("servername");
		PASSWORD = getConfig().getString("password");
		HOST = getConfig().getString("host");
		HIDDEN = getConfig().getInt("hidden");
		if(getConfig().getBoolean("ignorevault")==true){
			IGNOREVAULT=true;
		}
	}
	
	/**
	 * Initialise a new connection to given server
	 */
	public void connect(){
		Socket s = new Socket();
		try{
			s.connect(new InetSocketAddress(HOST, 4242));
		}catch(ConnectException e){
			new Reconnect(this);
			return;
		}catch (Exception e){
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
			new Reconnect(this);
			return;
		}
		colourList.add(new Colour("§0",0,0,0));
		colourList.add(new Colour("§1",0,0,170));
		colourList.add(new Colour("§2",0,170,0));
		colourList.add(new Colour("§3",0,170,170));
		colourList.add(new Colour("§4",170,0,0));
		colourList.add(new Colour("§5",170,0,170));
		colourList.add(new Colour("§6",255,170,0));
		colourList.add(new Colour("§7",170,170,170));
		colourList.add(new Colour("§8",85,85,85));
		colourList.add(new Colour("§9",85,85,255));
		colourList.add(new Colour("§a",85,255,85));
		colourList.add(new Colour("§b",85,255,255));
		colourList.add(new Colour("§c",255,85,85));
		colourList.add(new Colour("§d",255,85,255));
		colourList.add(new Colour("§e",255,255,85));
		Colour.dC = new Colour("§f",255,255,255);
		colourList.add(Colour.dC);
		sConn = new Connector(s,"Server");
		sConn.debug("Early debug");
		callback = new PluginCallbacks(sConn);
		sConn.addCallback(callback);
		sConn.debug("Callback added");
		Packet verPacket = new VersionPacket();
		sConn.send(verPacket);
		sConn.debug("Versioning added");
		Packet packet = new ConnectionPacket(USERNAME, PASSWORD,true, false, colourList);
		sConn.send(packet);
		sConn.debug("Connector sent");
	}
	
	/**
	 * Convenience. Send a packet to Server
	 * @param p
	 */
	public static void send(Packet p) {
		if(sConn!=null){
			sConn.send(p);
		}
	}
	
	/**
	 * Send information about how to connect to this server to MobsChatServer, for showing on website
	 * @param conn
	 */
	public static void updateServerData(boolean conn) {
		if(HIDDEN==1){ return; }
		String userNames = "", sep= "";
		for(Player p : Bukkit.getOnlinePlayers()){
			userNames = userNames+sep+p.getName();
			sep=", ";
		}
		ServerInfoPacket sip = new ServerInfoPacket();
		sip.hostname = THISHOST;
		sip.port = PORT;
		sip.mapport = MAPPORT;
		sip.isConnect=conn;
		sip.userlist = userNames;
		send(sip);
	}
	
}

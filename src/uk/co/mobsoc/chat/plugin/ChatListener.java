package uk.co.mobsoc.chat.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Spider;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import uk.co.mobsoc.chat.common.Util;
import uk.co.mobsoc.chat.common.packet.AccountAddPacket;
import uk.co.mobsoc.chat.common.packet.ActionPacket;
import uk.co.mobsoc.chat.common.packet.ChatPacket;
import uk.co.mobsoc.chat.common.packet.HostUserListUpdatePacket;
import uk.co.mobsoc.chat.common.packet.PlayerJoinedPacket;
import uk.co.mobsoc.chat.common.packet.PresetActionPacket;
import uk.co.mobsoc.chat.common.packet.PrivateMessagePacket;
import uk.co.mobsoc.chat.common.packet.ServerData;

@SuppressWarnings("deprecation")
public class ChatListener implements Listener{
	/**
	 * A hashmap containing strings to pre-fix the users chat. Used by admin to enforce a colour for their chat when they need attention
	 */
	HashMap<UUID, String> chatPrefixes = new HashMap<UUID, String>();
	
	/**
	 * Catch commands before they can be used by Bukkit officially
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
    	String msg = event.getMessage();
    	Player player = event.getPlayer();
    	String[] args = msg.split(" ");
    	if(msg.startsWith("/msg ") || msg.startsWith("/tell ") || msg.startsWith("/pm ")){
        	// Catch private messages and divert them into MobsChatServer
    		event.setCancelled(true);
    		if(args.length<3){ player.sendMessage("Usage : "+args[0]+" PlayerName Message here"); return; }
    		String message="",sep="";
    		for(int i = 2; i<args.length; i++){
    			message = message+sep+args[i];
    			sep=" ";
    		}
    		PrivateMessagePacket pmp = new PrivateMessagePacket();
    		pmp.from = player.getName();
    		pmp.to = args[1];
    		pmp.message = PUtils.bukkitMarkupToInternal(message);
    		ChatPlugin.sConn.send(pmp);
    	}else if(msg.startsWith("/who ") || msg.startsWith("/playerlist ") || msg.startsWith("/list ")){
    		// Catch calls to player list and show users from every server
    		ArrayList<ServerData> sdL = PluginCallbacks.servers;
    		for(ServerData sd: sdL){
    			String s = "";
    			String sep = "";
    			for(String user: sd.userList){
    				s = s + sep + user;
    				sep = ", ";
    			}
    			s = PUtils.InternalMarkupToBukkit(sd.connectionColour.toInternal() + sd.connectionName + " : "+ s);

    			player.sendMessage(s);
    		}
    		
    		event.setCancelled(true);
    	}else if(msg.startsWith("/me ")){
    		// Catch actions and send them to MobsChatServer
    		String restOf="", sep="";
    		if(args.length==1){ return; }
    		for(int i = 1; i<args.length; i++){
    			restOf = restOf + sep + args[i];
    			sep=" ";
    		}
    		ActionPacket ap = new ActionPacket();
    		ap.action = PUtils.bukkitMarkupToInternal(player.getDisplayName()+" "+restOf);
    		ChatPlugin.sConn.send(ap);
    		event.setCancelled(true);
    	}else if(msg.startsWith("/account add")){
    		// Catch attempts to add an account...
    		event.setCancelled(true);
    		if(args.length<4){ player.sendMessage("Incorrect command. https://mobsoc.co.uk/signup/accounts.php"); return; }
    		AccountAddPacket aap = new AccountAddPacket();
    		aap.id = args[2];
    		aap.code = args[3];
    		aap.uuid = player.getUniqueId();
    		aap.mcName = player.getName();
    		ChatPlugin.sConn.send(aap);
    		return;
    	}else if(msg.startsWith("/colour")){
    		// Set a chat prefix for users chat
    		event.setCancelled(true);
    		if(args.length!=2){
    			chatPrefixes.remove(player.getUniqueId());
    			player.sendMessage("This command requires a string to pre-pend to all chat. Colour codes advised (&[0-9a-f])");
    			return;
    		}
    		chatPrefixes.put(player.getUniqueId(), args[1]);
    		return;
    	}
    }

	/**
	 * Initialise this Listener
	 * @param plugin
	 */
	public ChatListener(ChatPlugin plugin){
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		// Set up a list of accepted codes
		allowAmps.put("&1","§1");
		allowAmps.put("&2","§2");
		allowAmps.put("&3","§3");
		allowAmps.put("&4","§4");
		allowAmps.put("&5","§5");
		allowAmps.put("&6","§6");
		allowAmps.put("&7","§7");
		allowAmps.put("&8","§8");
		allowAmps.put("&9","§9");
		allowAmps.put("&a","§a");
		allowAmps.put("&b","§b");
		allowAmps.put("&c","§c");
		allowAmps.put("&d","§d");
		allowAmps.put("&e","§e");
		allowAmps.put("&f","§f");
		allowAmps.put("&k","§k");
		allowAmps.put("&l","§l");
		allowAmps.put("&m","§m");
		allowAmps.put("&o","§o");
		allowAmps.put("&r","§r");
	}
	
	HashMap<String,String> allowAmps = new HashMap<String, String>();
	
	/**
	 * Catch chat events - Check into updating to Async.
	 * @param event
	 */
	@EventHandler
	public void onPlayerChat(PlayerChatEvent event){
		if(event.isCancelled()){ return; }
		event.setCancelled(true);
		event.setFormat("");
		String pref="";
		if(chatPrefixes.containsKey(event.getPlayer().getUniqueId())){
			pref = chatPrefixes.get(event.getPlayer().getUniqueId());
		}
		ChatPacket p = new ChatPacket();
		String msg = pref+ event.getMessage();
		for(String key : allowAmps.keySet()){
			msg = Util.replace(msg, key, allowAmps.get(key));
		}
		Player player = event.getPlayer();
		p.name = player.getName();
		p.message = PUtils.bukkitMarkupToInternal(msg);
		ChatPlugin.send(p);
	}
	
	/**
	 * Choose a Preset action based on the way a player dies.
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onDeathMessage(PlayerDeathEvent event){
		Player player = event.getEntity();
		EntityDamageEvent dE = player.getLastDamageCause();
		PresetActionPacket pap = null;
		if(dE.getCause()==DamageCause.VOID){
			pap = new PresetActionPacket("void", player.getName());
		}else if(dE.getCause()==DamageCause.CONTACT){
			pap = new PresetActionPacket("cactus", player.getName());
		}else if(dE.getCause()==DamageCause.STARVATION){
			pap = new PresetActionPacket("starvation", player.getName());
		}else if(dE.getCause()==DamageCause.DROWNING){
			pap = new PresetActionPacket("drown", player.getName());
		}else if(dE.getCause()==DamageCause.BLOCK_EXPLOSION){
			pap = new PresetActionPacket("explosion", player.getName());
		}else if(dE.getCause()==DamageCause.CUSTOM || dE.getCause()==DamageCause.MAGIC){
			pap = new PresetActionPacket("magic", player.getName());
		}else if(dE.getCause()==DamageCause.FALL){
			pap = new PresetActionPacket("fall", player.getName());
		}else if(dE.getCause()==DamageCause.FIRE || dE.getCause()==DamageCause.FIRE_TICK){
			pap = new PresetActionPacket("fire", player.getName());
		}else if(dE.getCause()==DamageCause.LAVA){
			pap = new PresetActionPacket("lava", player.getName());
		}else if(dE.getCause()==DamageCause.LIGHTNING){
			pap = new PresetActionPacket("lightning", player.getName());
		}else if(dE.getCause()==DamageCause.POISON){
			pap = new PresetActionPacket("poison", player.getName());
		}else if(dE.getCause()==DamageCause.SUFFOCATION){
			pap = new PresetActionPacket("suffocation", player.getName());
		}else if(dE.getCause()==DamageCause.SUICIDE){
			pap = new PresetActionPacket("anhero", player.getName());
		}else if(dE.getCause()==DamageCause.ENTITY_ATTACK){
			if(dE instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent dE2 = (EntityDamageByEntityEvent) dE;
				if(dE2.getDamager() instanceof Zombie){
					pap = new PresetActionPacket("mob_zombie", player.getName());
				}else if(dE2.getDamager() instanceof Wolf){
					pap = new PresetActionPacket("mob_wolf", player.getName());
				}else if(dE2.getDamager() instanceof Enderman){
					pap = new PresetActionPacket("mob_enderman", player.getName());
				}else if(dE2.getDamager() instanceof Cow){
					pap = new PresetActionPacket("mob_cow", player.getName());
				}else if(dE2.getDamager() instanceof Skeleton){
					pap = new PresetActionPacket("mob_skeleton", player.getName());
				}else if(dE2.getDamager() instanceof Golem){
					pap = new PresetActionPacket("mob_golem", player.getName());
				}else if(dE2.getDamager() instanceof LightningStrike){
					pap = new PresetActionPacket("lightning", player.getName());
				}else if(dE2.getDamager() instanceof PigZombie){
					pap = new PresetActionPacket("mob_pigman", player.getName());
				}else if(dE2.getDamager() instanceof Player){
					pap = new PresetActionPacket("player", player.getName(), ((Player) dE2.getDamager()).getName());
				}else if(dE2.getDamager() instanceof Silverfish){
					pap = new PresetActionPacket("mob_silverfish", player.getName());
				}else if(dE2.getDamager() instanceof Spider){
					pap = new PresetActionPacket("mob_spider", player.getName());
				}else if(dE2.getDamager() instanceof Villager){
					pap = new PresetActionPacket("mob_villager", player.getName());
				}else if(dE2.getDamager() instanceof Giant){
					pap = new PresetActionPacket("mob_giant", player.getName());
				}else if(dE2.getDamager() instanceof Blaze){
					pap = new PresetActionPacket("mob_blaze", player.getName());
				}else if(dE2.getDamager() instanceof Witch){
					pap = new PresetActionPacket("mob_witch", player.getName());
				}else if(dE2.getDamager() instanceof Ocelot){
					pap = new PresetActionPacket("mob_ocelot", player.getName());
				}else if(dE2.getDamager() instanceof Slime){
					pap = new PresetActionPacket("mob_slime", player.getName());
				}else if(dE2.getDamager() instanceof Wither){
					pap = new PresetActionPacket("mob_wither", player.getName());
				}else if(dE2.getDamager() instanceof WitherSkull){
					pap = new PresetActionPacket("mob_wither", player.getName());
				}
			}
		}else if(dE.getCause()==DamageCause.ENTITY_EXPLOSION){
			if(dE instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent dE2 = (EntityDamageByEntityEvent) dE;
				if(dE2.getDamager() instanceof Creeper){
					pap = new PresetActionPacket("mob_creeper_explosion", player.getName());
				}else if(dE2.getDamager() instanceof Projectile){
					Projectile proj = (Projectile) dE2.getDamager();
					if(proj.getShooter() instanceof Ghast){
						pap = new PresetActionPacket("mob_ghast_shot", player.getName());
					}else if(proj.getShooter() instanceof EnderDragon){
						pap = new PresetActionPacket("mob_ender_dragon", player.getName());
					}
				}else if(dE2.getDamager() instanceof TNTPrimed){
					pap = new PresetActionPacket("explosion", player.getName());
				}
			}else{
				pap = new PresetActionPacket("explosion", player.getName());
			}
		}else if(dE.getCause()==DamageCause.PROJECTILE){
			if(dE instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent dE2 = (EntityDamageByEntityEvent) dE;
				Entity owner = ((Projectile) dE2.getDamager()).getShooter();
				if(owner instanceof Zombie){
					pap = new PresetActionPacket("mob_zombie_shot", player.getName());
				}else if(owner instanceof Wolf){
					pap = new PresetActionPacket("mob_wolf_shot", player.getName());
				}else if(owner instanceof Enderman){
					pap = new PresetActionPacket("mob_enderman", player.getName());
				}else if(owner instanceof Cow){
					// SRSLY?
					pap = new PresetActionPacket("mob_cow_shot", player.getName());
				}else if(owner instanceof Skeleton){
					pap = new PresetActionPacket("mob_skeleton_shot", player.getName());
				}else if(owner instanceof Golem){
					pap = new PresetActionPacket("mob_golem_shot", player.getName());
				}else if(owner instanceof PigZombie){
					pap = new PresetActionPacket("mob_pigman_shot", player.getName());
				}else if(owner instanceof Player){
					if(dE2.getDamager() instanceof Arrow){
						pap = new PresetActionPacket("player_arrow", player.getName(), ((Player) owner).getName());
					}else if(dE2.getDamager() instanceof Snowball){
						pap = new PresetActionPacket("player_snowball", player.getName(), ((Player) owner).getName());
					}else if(dE2.getDamager() instanceof Egg){
						pap = new PresetActionPacket("player_egg", player.getName(), ((Player) owner).getName());
					}else if(dE2.getDamager() instanceof Fireball){
						pap = new PresetActionPacket("player_fireball", player.getName(), ((Player) owner).getName());
					}else{
						pap = new PresetActionPacket("player_shot", player.getName(), ((Player) owner).getName());
					}
				}else if(owner instanceof Silverfish){

				}else if(owner instanceof Spider){

				}else if(owner instanceof Villager){

				}else if(owner instanceof Wither){

				}else if(owner instanceof WitherSkull){
					
				}
			}
		}		
		event.setDeathMessage("");
		if(pap!=null){
			ChatPlugin.send(pap);
		}
	}

	/**
	 * Let the MobsChatServer know about a user joining
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onConnect(PlayerJoinEvent event){
		event.setJoinMessage(null);
		PlayerJoinedPacket pjp = new PlayerJoinedPacket();
		pjp.uuid = event.getPlayer().getUniqueId();
		pjp.name = event.getPlayer().getName();
		ChatPlugin.send(pjp);
		PresetActionPacket pap = new PresetActionPacket("login", event.getPlayer().getName());
		ChatPlugin.send(pap);
		HostUserListUpdatePacket hulup = new HostUserListUpdatePacket();
		for(Player p : Bukkit.getOnlinePlayers()){
			hulup.userList.add(p.getName());
		}
		ChatPlugin.send(hulup);
		new UpdateFakePlayer();
		if(!Reconnect.connected){
			event.getPlayer().sendMessage("Connection to Chat Server is lost.");
			event.getPlayer().sendMessage("You will not be able to chat until it returns");
		}
	}
	
	/**
	 * Let MobsChatServer know about a quitter
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onDisconnect(PlayerQuitEvent event){
		event.setQuitMessage(null);
		PresetActionPacket pap = new PresetActionPacket("logout", event.getPlayer().getName());
		ChatPlugin.send(pap);
		HostUserListUpdatePacket hulup = new HostUserListUpdatePacket();
		for(Player p : Bukkit.getOnlinePlayers()){
			if(p.getName().equalsIgnoreCase(event.getPlayer().getName())){ continue; }
			hulup.userList.add(p.getName());
		}
		ChatPlugin.send(hulup);
		new UpdateFakePlayer();
	}

}

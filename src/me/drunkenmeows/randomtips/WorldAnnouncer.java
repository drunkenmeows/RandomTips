package me.drunkenmeows.randomtips;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class WorldAnnouncer {
	
	private String name;
	private String prefix;
	
	private RandomTips plugin;
	private BukkitTask task;
	private boolean running = false;
	private boolean random;
	private List<String> messages = new ArrayList<String>();
	private int currentmsg = 0;
	private int numofmsgs = 0;
	
	private List<String> ignored = new ArrayList<String>();
	
	public WorldAnnouncer(RandomTips p, String worldname) {
		this.name = worldname;
		this.plugin = p;
		this.prefix = this.plugin.getConfig().getString(name+".Prefix", "&cTip &f[&6%i&f] ");
		this.random = this.plugin.getConfig().getBoolean(name+".Random", true);
		messages =  this.plugin.getConfig().getStringList(this.name+".Messages");
		if(messages.size() > 0) {
			runtask();
			running = true;
			numofmsgs = messages.size();
			if(name.equalsIgnoreCase("Broadcast"))
				plugin.logger.info("["+plugin.getDescription().getName()+"] Announcer for "+name+" worlds running "+numofmsgs+" messages");
			else
				plugin.logger.info("["+plugin.getDescription().getName()+"] Announcer for world: "+name+" running "+numofmsgs+" messages");
		} else {
			running = false;
			plugin.logger.warning("["+plugin.getDescription().getName()+"] World:"+this.name+" Doesn't have any messages defined");
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public void cancel() {
		this.task.cancel();
	}
	
	//stop the announcer
	public boolean stop()	{
		if(running) {
			cancel();
			running = false;
			return true;
		} else {
			return false;
		}
	}
	
	//start the announcer
	public boolean start()	{
		if(!running) {
			runtask();
			running = true;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean ignore(String name)	{
		if(ignored.contains(name)) {
			ignored.remove(name);
			return false;
		} else {
			ignored.add(name);
			return true;
		}
	}
	
	public boolean add(String msg){
		if(msg.length() > 0) {
			messages.add(msg);
			plugin.getConfig().set(name+".Messages", messages);
			plugin.saveConfig();
			numofmsgs++;
			return true;
		} else
			return false;
	}
	
	public String getMessage(int id) {
		if(id < numofmsgs) {
			return messages.get(id);
		} else {
			return null;
		}
	}
	
	public String remove(int id){
		String msg = "";
		if(numofmsgs > 1) {
			if(id < numofmsgs) {
				msg = getMessage(id);
				messages.remove(id);
				plugin.getConfig().set(name+".Messages", messages);
				plugin.saveConfig();
				numofmsgs--;
				return msg;
			} else
				return null;
		}
		return null;
	}
	
	//start the runnable task
	public void runtask() {
		task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable(){
			public void run(){
				messageLoop();
			}		
		}, plugin.getConfig().getLong(this.name+".Delay",0L)*20L, plugin.getConfig().getLong(this.name+".Interval",180L)*20L);
	}
	
	public String[] getmessages() {
		return messages.toArray(new String[0]);
	}
	
	//colourise string
	public String colourise(String msg) {
		return ChatColor.translateAlternateColorCodes('&',msg);
	}
	
	//send message to player in specific worlds
	public void worldBroadcast(String msg)	{
		World world = (World) plugin.getServer().getWorld(this.name);
		if(world != null) {
			List<Player> players = world.getPlayers();
			for(Player p : players) {
				if(!(ignored.contains(p.getName())))
					p.sendMessage(colourise(msg));
			}
		}
	}
	
	public void messageLoop() {	
		int lastmsg = currentmsg;
		//cancel task if world ceases to exist
		if(plugin.getServer().getWorld(this.name) == null && !(name.equalsIgnoreCase("Broadcast"))) {
			plugin.logger.info("["+plugin.getDescription().getName()+"] World:"+this.name+ "ceased to exist, it was deleted?");
			cancel();
		}
		
		//find a random number between 1000-4000
		int tip = new Random().nextInt((800-1)-(100+1))+100+1;

		//broadcaster the message to the players&f[&4Tip#%i&f]:
		if(name.equalsIgnoreCase("Broadcast")) {
			plugin.getServer().broadcastMessage(colourise(prefix.replaceFirst("%i",""+tip)+messages.get(currentmsg)));
		} else {
			worldBroadcast(prefix.replaceFirst("%i",""+tip)+messages.get(currentmsg));
		}
		
		if(random){
			do {
				currentmsg = new Random().nextInt(numofmsgs);
			} while (currentmsg == lastmsg);
		} else {
			currentmsg++;
			if(currentmsg >= numofmsgs)
				currentmsg = 0;
		}
	}

}


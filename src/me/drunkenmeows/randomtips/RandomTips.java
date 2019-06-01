package me.drunkenmeows.randomtips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomTips extends JavaPlugin {
	
	public HashMap<String,WorldAnnouncer> worlds = new HashMap<String,WorldAnnouncer>();
	
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public List<String> worldlist = new ArrayList<String>();
	
	public String prefix = "&f[&2RandomTips&f] ";
	
	public Metrics metrics;
	
	@Override
	public void onDisable()	{
		//output to console
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is now disabled" );
		
		for(WorldAnnouncer wa:worlds.values()) {
			wa.cancel();
		}
		
		worlds.clear();
	}	
	
	@Override
	public void onEnable()	{
		//output to console
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is now enabled" );
		
		this.saveDefaultConfig();
		
		worldlist = getConfig().getStringList("Worlds");
		for(String world:worldlist) {
			if(getServer().getWorld(world) != null || world.equalsIgnoreCase("Broadcast"))	{
				if(getConfig().contains(world))	{
					this.worlds.put(world.toLowerCase(), new WorldAnnouncer(this,world));
				} else {
					logger.warning("["+pdfFile.getName()+"] World: "+world+" setting are not defined in the config.yml");
				}
			} else {
				logger.warning("["+pdfFile.getName()+"] World: "+world+" does not exist");
			}	
			
		}
		
		//metrics
		try {
		    this.metrics = new Metrics(this);
		    //huntcount = metrics.createGraph("Hunt Played");
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		
	}
	
	public void reload(CommandSender sender) {
		PluginDescriptionFile pdfFile = this.getDescription();
		
		for(WorldAnnouncer wa:worlds.values()) {
			wa.cancel();
		}

		worlds.clear();
		
		this.reloadConfig();
		
		worldlist = getConfig().getStringList("Worlds");
		for(String world:worldlist) {
			if(getServer().getWorld(world) != null || world.equalsIgnoreCase("Broadcast"))	{
				if(getConfig().contains(world))	{
					this.worlds.put(world.toLowerCase(), new WorldAnnouncer(this,world));
				} else {
					logger.warning("["+pdfFile.getName()+"] World: "+world+" setting are not defined in the config.yml");
				}
			} else {
				logger.warning("["+pdfFile.getName()+"] World: "+world+" does not exist");
			}	
		}
		
		sender.sendMessage(col(prefix+"&aReloaded."));
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if(command.getLabel().equals("randomtips")) {
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("add")) {
					if(!(sender.hasPermission("randomtips.admin"))) {
						sender.sendMessage(col("&c- Insufficient Permissions!"));
						return true;
					}	
									
					add(sender, args);
					return true;
				}

				if(args[0].equalsIgnoreCase("remove")) {
					if(!(sender.hasPermission("randomtips.admin"))) {
						sender.sendMessage(col("&c- Insufficient Permissions!"));
						return true;
					}
					
						
					remove(sender, args);
					return true;					
				}
				
				if(args[0].equalsIgnoreCase("messages")) {
					if(!(sender.hasPermission("randomtips.admin"))) {
						sender.sendMessage(col("&c- Insufficient Permissions!"));
						return true;
					}
						
					messages(sender, args);
					return true;
				}
				
				if(args[0].equalsIgnoreCase("stop")) {
					if(!(sender.hasPermission("randomtips.admin"))) {
						sender.sendMessage(col("&c- Insufficient Permissions!"));
						return true;
					}
						
					stop(sender, args);
					return true;
				}
				
				if(args[0].equalsIgnoreCase("start")) {
					if(!(sender.hasPermission("randomtips.admin"))) {
						sender.sendMessage(col("&c- Insufficient Permissions!"));
						return true;
					}
						
					start(sender, args);
					return true;
				}
				
				if(args[0].equalsIgnoreCase("ignore")) {
					if(!(sender.hasPermission("randomtips.player"))) {
						sender.sendMessage(col("&c- Insufficient Permissions!"));
						return true;
					}	
					ignore(sender, args);
					return true;
				}
				
				if(args[0].equalsIgnoreCase("reload")) {
					if(!(sender.hasPermission("randomtips.admin"))) {
						sender.sendMessage(col("&c- Insufficient Permissions!"));
						return true;
					}
						
					reload(sender);
					return true;
				}
			}
			return true;
		}
		return false;
	}
	
	public void messages(CommandSender sender, String[] args) {
		if(sender instanceof Player)
		{
			WorldAnnouncer wa = null;
			Player p = (Player)sender;
			if(args.length > 1)
				wa = worlds.get(args[1].toLowerCase());
			else
				wa = worlds.get(p.getWorld().getName().toLowerCase());
		    if(wa != null) {
			    int i = 0;
			    p.sendMessage(col("&f[&6id&f]----------------&f[&6Messages&f]----------------"));
				for(String msg:wa.getmessages()) {
					p.sendMessage(col("&f[&6"+i+"&f] "+msg));
					i++;
				}
		    } else {
		    	sender.sendMessage(col("&cRT: World doesn't have an Announcer"));
		    }
		} else {
			if(args.length > 1)
			{
				WorldAnnouncer wa = worlds.get(args[1].toLowerCase());
				if(wa != null) {
					int i = 0;
					sender.sendMessage(col("&f[&6id&f]----------------&f[&6Messages&f]----------------")); 
					for(String msg:wa.getmessages()) {
						sender.sendMessage(col("&f[&6"+i+"&f] "+msg));
						i++;
					}
				} else {
			    	sender.sendMessage(col("&cRT: World doesn't have an Announcer"));
			    }
			} else {
				sender.sendMessage(col("RT: World not defined."));
			}
		}
	}
	
	public String getmsg(String[] args, String subcmd, String world) {
		String message = "";
		for(int i = 0; i<args.length; i++) {
			message = message.concat(args[i]).concat(" ");
		}
		message = message.replaceFirst(subcmd, "");
		message = message.replaceFirst(world, "");
		message = message.trim();
		return message;
	}
	
	public boolean isworld(String name) {
		return worldlist.contains(name);
	}
	
	public void add(CommandSender sender, String[] args) {
		//player
		if(sender instanceof Player) {
			Player p = (Player) sender;
				WorldAnnouncer wa = null;
				if(args.length > 1) {
					wa = worlds.get(args[1].toLowerCase());
					if(wa != null) 
					{ 
						if(wa.add(getmsg(args,args[0],args[1]))) {
							p.sendMessage(col("&cRT: Message:'"+getmsg(args,args[0],args[1]) +"' added."));
							return;
						} else {
							sender.sendMessage(col("&cRT: Messaged required."));
							return;
						}
					} else {
						wa = worlds.get(p.getWorld().getName().toLowerCase());
						if(wa != null) {
							if(wa.add(getmsg(args,args[0],""))) {
								p.sendMessage(col("&aRT: Message:'"+getmsg(args,args[0],args[1]) +"' added."));
								return;
							} else {
								p.sendMessage(col("&cRT: Messaged required."));
								return;
							}
							
						} else {
							sender.sendMessage(col("&cRT: World doesn't have an Announcer"));
							return;
						}
					}
				}		
		//console
		} else {
			if(args.length > 1) 
			{
				WorldAnnouncer wa = worlds.get(args[1].toLowerCase());
				if(wa != null) {
					if(wa.add(getmsg(args,args[0],args[1])))
						sender.sendMessage(col("&aRT: Message:'"+getmsg(args,args[0],args[1]) +"' added."));
					else
						sender.sendMessage(col("&cRT: Messaged required."));
				} else {
					sender.sendMessage(col("&cRT: World doesn't have an Announcer"));
				}
			} else {
				sender.sendMessage(col("&cRT: /rt add <world> <message>"));
			}
		}
		return;
	}
	
	public void remove(CommandSender sender, String[] args) {
		//player
		if(sender instanceof Player) {
			Player p = (Player) sender;
			WorldAnnouncer wa = null;
			if(args.length > 2) {
				wa = worlds.get(args[1].toLowerCase());
				if(wa != null) 	{
					String message = wa.remove(Integer.valueOf(args[2]));
					if(message != null)
						p.sendMessage(col("&cRT: Message: "+message+" removed."));
					else
						p.sendMessage(col("&cRT: Message &f[&6"+args[2]+"&f]&c doesn't exist."));
				} else {
					p.sendMessage(col("&cRT: World &f[&6"+args[1]+"&f] doesn't have an Announcer"));
				}
			} else if (args.length == 2) {
				wa = worlds.get(p.getWorld().getName().toLowerCase());
				if(wa != null) {
					String message = wa.remove(Integer.valueOf(args[1]));
					if(message != null)
						p.sendMessage(col("&cRT: Message: "+message+" removed."));
					else
						p.sendMessage(col("&cRT: Message &f[&6"+args[2]+"&f]&c doesn't exist."));
				} else {
					p.sendMessage(col("&cRT: World &f[&6"+args[1]+"&f] doesn't have an Announcer"));
				}
			}
		} else {
			WorldAnnouncer wa = null;
			if(args.length > 2) {
				wa = worlds.get(args[1].toLowerCase());
				if(wa != null) 	{
					String message = wa.remove(Integer.valueOf(args[2]));
					if(message != null)
						sender.sendMessage(col("&cRT: Message: "+message+" removed."));
					else
						sender.sendMessage(col("&cRT: Message &f[&6"+args[2]+"&f]&c doesn't exist."));
				} else {
					sender.sendMessage(col("&cRT: World &f[&6"+args[1]+"&f] doesn't have an Announcer"));
				}
			} else 	{
				sender.sendMessage(col("&cRT: /rt remove <world> <message-id>"));
			}
		}
	}
	
	public void stop(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player)sender;
			WorldAnnouncer wa = null;
			if(args.length > 1) {
				wa = worlds.get(args[1].toLowerCase());
				if(wa != null) {
					if(wa.stop())
						p.sendMessage(col("&cRT: Announcer for "+args[1].toLowerCase()+" stopped."));
					else
						p.sendMessage(col("&cRT: Announcer not running."));
				} else {
					p.sendMessage(col("&cRT: World doesn't have an announcer."));
				}
			}
			else {
				wa = worlds.get(p.getWorld().getName().toLowerCase());
				if(wa != null) {
					if(wa.stop())
						p.sendMessage(col("&cRT: Announcer for "+p.getWorld().getName()+" stopped."));
					else
						p.sendMessage(col("&cRT: Announcer not running."));
				} else {
					p.sendMessage(col("&cRT: World doesn't have an announcer."));
				}
			}
		} else {
			if(args.length > 1) {
				WorldAnnouncer wa = worlds.get(args[1].toLowerCase());
				if(wa != null) {
					if(wa.stop())
						sender.sendMessage(col("&cRT: Announcer for "+args[1]+" stopped."));
					else
						sender.sendMessage(col("&cRT: Announcer not running."));
				} else {
					sender.sendMessage(col("&cRT: World doesn't have an announcer."));
				}
			} else {
				sender.sendMessage(col("&cRT: /RT stop <world> World undefined."));
			}
		}
	}
	
	public void start(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player)sender;
			WorldAnnouncer wa = null;
			if(args.length > 1) {
				wa = worlds.get(args[1].toLowerCase());
				if(wa != null) {
					if(wa.start())
						p.sendMessage(col("&cRT: Announcer for "+args[1].toLowerCase()+" started."));
					else
						p.sendMessage(col("&cRT: Announcer already running."));
				} else {
					p.sendMessage(col("&cRT: World doesn't have an announcer."));
				}
			} else {
				wa = worlds.get(p.getWorld().getName().toLowerCase());
				if(wa != null) {
					if(wa.stop())
						p.sendMessage(col("&cRT: Announcer for "+p.getWorld().getName()+" stopped."));
					else
						p.sendMessage(col("&cRT: Announcer not running."));
				} else {
					p.sendMessage(col("&cRT: World doesn't have an announcer."));
				}
			}
		} else {
			if(args.length > 1) {
				WorldAnnouncer wa = worlds.get(args[1].toLowerCase());
				if(wa != null) {
					if(wa.start())
						sender.sendMessage(col("&cRT: Announcer for "+args[1]+" started."));
					else
						sender.sendMessage(col("&cRT: Announcer already running."));
				} else {
					sender.sendMessage(col("&cRT: World doesn't have an announcer."));
				}
			} else {
				sender.sendMessage(col("&cRT: /RT start <world> World undefined."));
			}
		}
	}
	
	public void ignore(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player)sender;
			WorldAnnouncer wa = worlds.get(p.getWorld().getName().toLowerCase());
			if(wa != null) {
				if(wa.ignore(p.getName()))
					p.sendMessage(col("&cRT: Ignoring "+ wa.getName() + " announcer!"));
				else
					p.sendMessage(col("&cRT: Listening to "+ wa.getName() + " announcer!"));
			} else {
				p.sendMessage(col("&cRT: World doesn't have an announcer."));
			}
		}
	}
	
	public String parse(String msg, String arg) {
		msg = msg.replaceFirst("%s", arg);
		return msg;
	}
	
	public String col(String msg) {
		return ChatColor.translateAlternateColorCodes('&',msg);
	}
}

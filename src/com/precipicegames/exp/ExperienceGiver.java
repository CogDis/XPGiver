package com.precipicegames.exp;



import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class ExperienceGiver extends JavaPlugin implements Runnable {
	public int interval;
	public int amount;
	private int taskid;
	public int maxlevel;
	public HashMap<Player,Hit> lasthits;
	private PermissionManager perms;
	public long hittimeout;
	public double xpdistance;
	public boolean pearlconversion;

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEnable() {
		taskid = -1;
		interval = this.getConfig().getInt("interval",20*60);
		amount = this.getConfig().getInt("amount",1);
		maxlevel = this.getConfig().getInt("maxlevel",5);
		hittimeout = this.getConfig().getLong("hittimeout",60000);
		xpdistance = this.getConfig().getInt("xpdistance",50);
		pearlconversion = this.getConfig().getBoolean("pearlconversion",false);
		lasthits = new HashMap<Player,Hit>();

        PluginManager pm = getServer().getPluginManager();
        
        
        DeathListener d = new DeathListener(this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, d, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, d, Event.Priority.Normal, this);
        PermissionsEx permsPlugin = (PermissionsEx) pm.getPlugin("PermissionsEx");
        if (permsPlugin == null) {
            System.out.println("EXP: PermissionsEx not found!");
            return;
        }
        perms = PermissionsEx.getPermissionManager();
		taskid = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this, interval, interval);
        
	}
	public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args)
	{
		if (command.getName().equalsIgnoreCase("xp")) {
			if(!(sender instanceof Player))
			{
				sender.sendMessage("Only players may check experience");
				return true;
			}
			
			if(args.length >= 1 && perms.has((Player)sender, "xp.admin"))
			{
				if(args[0].equalsIgnoreCase("interval") && args.length == 2)
				{
					int tmp = interval;
					try{
					tmp = Integer.parseInt(args[1]);
					}
					catch(Exception e)
					{
						return false;
					}
					this.getServer().getScheduler().cancelTask(taskid);
					interval = tmp;
					taskid = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this, interval, interval);
					return true;
				}
				if(args[0].equalsIgnoreCase("amount") && args.length == 2)
				{
					int tmp = amount;
					try{
					tmp = Integer.parseInt(args[1]);
					}
					catch(Exception e)
					{
						return false;
					}
					amount = tmp;
					return true;
				}
				if(args[0].equalsIgnoreCase("clear") && args.length == 1)
				{
					for(Player p : this.getServer().getOnlinePlayers())
					{
						p.setLevel(0);
						p.setExperience(0);
						p.setTotalExperience(0);
					}
					return true;
				}
				if(args[0].equalsIgnoreCase("clear") && args.length == 2)
				{
					Player p = this.getServer().getPlayer(args[1]);
					if(p == null)
					{
						sender.sendMessage("Player: " + args[1] + " cannot be found");
						return true;
					}
					p.setLevel(0);
					p.setExperience(0);
					p.setTotalExperience(0);
					return true;
				}
				return false;
			}
			else
			{
				if(!perms.has((Player)sender, "xp.check"))
				{
					sender.sendMessage("You do not have permission");
					return true;
				}
				
				if(sender instanceof Player)
				{
					Player p = (Player)sender;
					sender.sendMessage("Your current level is " + Integer.toString(p.getLevel()));
					sender.sendMessage(Integer.toString(p.getExperience()) + "\\" + Integer.toString((p.getLevel()+1)*10)
							+ " of the way to next level!");
				}

				return true;
			}
         }
		return false;
	}
	
	public void award(Player p ,int amount)
	{
		int total = check(p);
		total += amount;
		set(p,total);
	}
	public int check(Player p)
	{
		int level = p.getLevel();
		int xp = 0;
		for(int l = 0; l < level; l++)
		{
			xp += (l+1)*10;
		}
		xp += p.getExperience();
		return xp;
	}
	public void set(Player p, int amount)
	{
		int xp = amount;
		int level = 0;
		for(; xp > (level+1)*10; level++)
		{
			xp -= (level+1)*10;
		}
		p.setLevel(level);
		p.setExperience(xp);
	}
	public void remove(Player p, int amount)
	{
		int total = check(p);
		total -= amount;
		if(total <= 0)
			total = 0;
		set(p,total);
	}

	@Override
	public void run() {
		for(Player p : this.getServer().getOnlinePlayers())
		{
			if(!perms.has(p, "xp.gain"))
				continue;
			if(p.getLevel() >= this.maxlevel)
				continue;
			award(p,amount);
			
		}
	}
}

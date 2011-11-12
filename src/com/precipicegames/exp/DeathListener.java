package com.precipicegames.exp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mmo.Core.PartyAPI.Party;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

//import mmo.Core.PartyAPI.Party;
import mmo.Core.MMOMinecraft;

public class DeathListener extends EntityListener {
	private ExperienceGiver plugin;
	DeathListener(ExperienceGiver p)
	{
		plugin = p;
	}
	public void onEntityDamage (EntityDamageEvent rawevent)
	{
		if(rawevent instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)rawevent; 
			if(event.getDamager() instanceof Player && event.getEntity() instanceof Player)
			{
				Hit h = new Hit();
				h.hitter = (Player)event.getDamager();
				h.time = System.currentTimeMillis();
				plugin.lasthits.put((Player)event.getEntity(), h);
				//System.out.println("Player got hit");
			}
		}
	}
	public void onEntityDeath (EntityDeathEvent event)
	{
		//System.out.println("damage Event Fired");
		if(event.getEntity() instanceof Player)
		{
			//System.out.println("damage player Fired");
			Player p = (Player)event.getEntity();
			Hit h = plugin.lasthits.get(p);
			if(h == null)
				return;
			if((System.currentTimeMillis()-h.time) < plugin.hittimeout)
			{
				//System.out.println("Someone actually did damage " + Integer.toString(plugin.check(p)));
				//int level = p.getLevel();
				int xp = plugin.check(p);
				if(plugin.pearlconversion)
				{
					Iterator<ItemStack> items = event.getDrops().iterator();
					while(items.hasNext())
					{
						ItemStack item = items.next();
						if(item.getType() == Material.ENDER_PEARL)
						{
							xp += item.getAmount()*10;
							items.remove();
						}
					}
				}
				Party party = MMOMinecraft.getParty();
				party = (party == null) ? party : party.find(h.hitter);
				if(party == null)
				{
					plugin.award(h.hitter, xp);
				}
				else
				{
					List<Player> members = party.getMembers();
					List<Player> inrange = new ArrayList<Player>();
					for(Player m : members)
					{
						try
						{
						if(p.getLocation().distance(m.getLocation()) < plugin.xpdistance)
						{
							inrange.add(m);
						}
						}
						catch(IllegalArgumentException e)
						{} // do nothing	
					}
					int splitfactor = inrange.size();
					int split = xp/splitfactor; // Get xp per player
					int leftovers = xp%splitfactor; // Give the leftovers to the actual killer
					for(Player m : inrange)
					{
						if(m == h.hitter)
							plugin.award(m, split + leftovers);
						else
							plugin.award(m, split);
					}
				}
				
			}
			else
			{
				//int level = ((Player)event.getEntity()).getLevel();
				//int xp = ((Player)event.getEntity()).getExperience();
			}
			event.setDroppedExp(0);
		}
		else
		{
			event.setDroppedExp(0);
		}
	}
}

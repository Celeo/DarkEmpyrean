package com.darktidegames.empyrean;

import java.text.DecimalFormat;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class NetherListener implements Listener
{

	final WarsPlugin plugin;

	public NetherListener(WarsPlugin plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void onNetherMobSpawn(CreatureSpawnEvent event)
	{
		if (!event.getEntity().getWorld().getName().equals("world_nether"))
			return;
		if (event.getEntity() instanceof Enderman)
			event.setCancelled(true);
		if (event.getEntity() instanceof Wolf)
			event.setCancelled(true);
		if (event.getEntity() instanceof Skeleton
				&& ((Skeleton) event.getEntity()).getType().equals(Skeleton.SkeletonType.WITHER))
			event.setCancelled(true);
		if (event.getEntity() instanceof Wither)
		{
			Location loc = event.getLocation();
			event.setCancelled(true);
			String names = "";
			DecimalFormat df = new DecimalFormat("#.#");
			for (Player p : event.getLocation().getWorld().getPlayers())
			{
				double dist = loc.distance(p.getLocation());
				if (dist > 49)
					continue;
				if (names.equals(""))
					names = p.getName() + "(" + df.format(dist) + ")";
				else
					names += ", " + p.getName() + "(" + df.format(dist) + ")";
			}
			plugin.getLogger().severe(String.format("Wither skeleton attempted spawn. Loc: '"
					+ C.locationToString(event.getLocation())
					+ "', Players near: " + names));
		}
	}

	@EventHandler
	public void onNetherItemDrop(ItemSpawnEvent event)
	{
		if (!event.getEntity().getWorld().getName().equals("world_nether"))
			return;
		if (event.getEntity().getItemStack().getTypeId() == 369)
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteractWithBanned(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();
		if (event.getPlayer().hasPermission("wars.bypassBlockCheck"))
			return;
		if (block == null)
			return;
		if (block.getTypeId() == 115 || block.getTypeId() == 52
				|| block.getTypeId() == 372)
		{
			plugin.getLogger().info(String.format("%s interacted %s %s", event.getPlayer().getName(), block.getType().name(), C.locationToString(block.getLocation())));
			block.setTypeId(0);
			event.setCancelled(true);
		}
	}
}
package com.darktidegames.empyrean;

import java.util.List;

import org.bukkit.entity.Player;

public class HiddenPlayer
{

	public final WarsPlugin plugin;
	public final Player player;
	public final long now;
	public final List<Player> hiddenFrom;
	public final int task_hide;
	public final int task_damage;
	public boolean hidden = true;

	public HiddenPlayer(final WarsPlugin plugin, final Player player, final long now, final List<Player> hiddenFrom)
	{
		this.plugin = plugin;
		this.player = player;
		this.now = now;
		this.hiddenFrom = hiddenFrom;
		hide();
		task_hide = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				reveal();
			}
		}, 600);
		task_damage = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				close();
			}
		}, 1200);
	}

	private void hide()
	{
		for (Player p : hiddenFrom)
			if (p.canSee(player))
				p.hidePlayer(player);
	}

	public void reveal()
	{
		player.sendMessage("§7Your cloak has lifted");
		for (Player online : hiddenFrom)
		{
			if (online != null && online.isOnline())
			{
				online.showPlayer(player);
				plugin.getLogger().info("Showing " + player.getName() + " to "
						+ online.getName());
			}
			else
				plugin.getLogger().info("Could not show " + player.getName());
		}
		plugin.getServer().getScheduler().cancelTask(task_hide);
		hidden = false;
	}

	public void close()
	{
		player.sendMessage("§7Your sickness from the warp has ended.");
		plugin.removeHidden(this);
	}

	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof HiddenPlayer))
			return false;
		HiddenPlayer test = (HiddenPlayer) object;
		return test.player.getName().equals(player.getName())
				&& test.now == now;
	}

}
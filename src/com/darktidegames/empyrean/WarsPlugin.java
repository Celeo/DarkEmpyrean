package com.darktidegames.empyrean;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.google.common.io.Files;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * Random server-help code
 * 
 * @author Celeo
 */
public class WarsPlugin extends JavaPlugin implements Listener
{

	private List<Helper> helpers = new ArrayList<Helper>();
	private Map<String, Integer> claims = new HashMap<String, Integer>();
	private Map<String, Location> respawn = new HashMap<String, Location>();
	private List<String> allCanSetBed = new ArrayList<String>();

	private Essentials ess;
	private WorldGuardPlugin wg;

	private List<String> ignoreRespawnOnce = new ArrayList<String>();

	private List<HiddenPlayer> hiddenPlayers = new ArrayList<HiddenPlayer>();

	private List<String> newPlayers = new ArrayList<String>();

	public int snowBallChance = 25;
	public int snowBallDamage = 1;

	@Override
	public void onLoad()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
	}

	@Override
	public void onEnable()
	{
		Plugin test = getServer().getPluginManager().getPlugin("Essentials");
		if (test == null)
			getLogger().warning("Could not find Essentials!");
		else
			ess = (Essentials) test;
		test = getServer().getPluginManager().getPlugin("WorldGuard");
		if (test == null)
			getLogger().warning("Could not find WorldGuard!");
		else
			wg = (WorldGuardPlugin) test;
		test = getServer().getPluginManager().getPlugin("Factions");
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new NetherListener(this), this);
		load();
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			private boolean hasTriedDowntime = false;

			@Override
			public void run()
			{
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				String[] date = sdf.format(new Date(System.currentTimeMillis())).split(":");
				if (date[0].equals("09") && !hasTriedDowntime)
				{
					hasTriedDowntime = true;
					getLogger().info("It's downtime!");
					tryReboot();
					return;
				}
				if (getServer().getOnlinePlayers().length == 0)
				{
					tryReboot();
					return;
				}
				for (final Player p : getServer().getOnlinePlayers())
				{
					if (!ess.getUserMap().getUser(p.getName()).isAfk())
						return;
				}
				tryReboot();
			}
		}, 12000L, 3600L);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				for (String key : claims.keySet())
				{
					Player test = getServer().getPlayer(key);
					if (test != null && test.isOnline())
						test.sendMessage("§7You have a claim waiting for you. §6/help claim");
				}
			}
		}, 3600L, 3600L);
		ShapelessRecipe bookRecipe = new ShapelessRecipe(new ItemStack(Material.WRITTEN_BOOK, 2)).addIngredient(Material.WRITTEN_BOOK).addIngredient(Material.BOOK_AND_QUILL);
		getServer().addRecipe(bookRecipe);
		// detector.addListener(new CancelListener<CreatureSpawnEvent>()
		// {
		// @Override
		// public void onCancelled(Plugin plugin, CreatureSpawnEvent event)
		// {
		// System.out.println(event.getEventName() + " cancelled by "
		// + plugin.getName() + " of a " +
		// event.getEntityType().getName().toLowerCase() + " in world " +
		// event.getLocation().getWorld().getName());
		// }
		// });
		getLogger().info("Enabled");
	}

	private void load()
	{
		reloadConfig();
		getServer().getScheduler().cancelTasks(this);
		snowBallChance = getConfig().getInt("snow.chance", 25);
		snowBallDamage = getConfig().getInt("snow.damage", 1);
		newPlayers = getConfig().getStringList("newPlayers");
		if (newPlayers == null)
			newPlayers = new ArrayList<String>();
		allCanSetBed = getConfig().getStringList("allCanSetBed");
		if (allCanSetBed == null || allCanSetBed.isEmpty())
		{
			allCanSetBed = new ArrayList<String>();
			allCanSetBed.add("empyreal");
		}
		claims.clear();
		for (String key : getConfig().getStringList("claims"))
		{
			claims.put(key.split("-")[0], C.integerO(key.split("-")[1]));
		}
		helpers.clear();
		for (String name : getConfig().getStringList("helper.all"))
		{
			helpers.add(new Helper(name, getConfig().getString("helper." + name
					+ ".link"), getConfig().getStringList("helper." + name
					+ ".keywords")));
		}
		for (String key : getConfig().getStringList("respawn"))
		{
			respawn.put(key.split(";")[0], C.stringArrayToLocation(key.split(";")[1].split(",")));
		}
		getLogger().info("Loaded");
	}

	@Override
	public void onDisable()
	{
		save();
		getServer().getScheduler().cancelTasks(this);
		// detector.close();
		getLogger().info("Disabled");
	}

	@SuppressWarnings("boxing")
	private void save()
	{
		getConfig().set("snow.chance", snowBallChance);
		getConfig().get("snow.damage", snowBallDamage);
		getConfig().set("allCanSetBed", allCanSetBed);
		List<String> names = new ArrayList<String>();
		for (Helper helper : helpers)
		{
			names.add(helper.getName());
			getConfig().set("helper." + helper.getName() + ".link", helper.getLink());
			getConfig().set("helper." + helper.getName() + ".keywords", helper.getKeywords());
		}
		getConfig().set("helper.all", names);
		List<String> cl = new ArrayList<String>();
		for (String name : claims.keySet())
		{
			cl.add(name + "-" + claims.get(name).intValue());
		}
		getConfig().set("claims", null);
		getConfig().set("claims", cl);
		List<String> res = new ArrayList<String>();
		for (String key : respawn.keySet())
		{
			if (respawn.get(key) == null)
				continue;
			res.add(key + ";" + C.locationToString(respawn.get(key)));
		}
		getConfig().set("respawn", null);
		getConfig().set("respawn", res);
		getConfig().set("newPlayers", null);
		getConfig().set("newPlayers", newPlayers);
		saveConfig();
		getLogger().info("Saved");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
			return false;
		Player player = (Player) sender;
		if (label.equalsIgnoreCase("wars"))
		{
			if (args == null || args.length == 0)
				return false;
			args[0] = args[0].toLowerCase();
			if (args[0].equals("-reload"))
			{
				if (!hasPerms(player, "wars.control"))
					return true;
				load();
				player.sendMessage("§aReloaded from configuration!");
				return true;
			}
			if (args[0].equals("-save"))
			{
				if (!hasPerms(player, "wars.control"))
					return true;
				save();
				player.sendMessage("§aSaved to configuration!");
				return true;
			}
			if (args[0].equals("-nobadmobs"))
			{
				if (!hasPerms(player, "wars.control"))
					return true;
				Iterator<Entity> i = player.getWorld().getEntities().iterator();
				while (i.hasNext())
				{
					Entity e = i.next();
					if (e instanceof Wolf || e instanceof Squid)
					{
						e.remove();
						i.remove();
					}
				}
				player.sendMessage("§aDone");
				return true;
			}
			if (args[0].equals("-recentlyteleported"))
			{
				if (!hasPerms(player, "wars.control"))
					return true;
				String names = "";
				for (HiddenPlayer hidden : hiddenPlayers)
				{
					if (names.equals(""))
						names = hidden.player.getName();
					else
						names += ", " + hidden.player.getName();
				}
				player.sendMessage("§7List of players under the effects of a cloak: §6"
						+ names);
				return true;
			}
		}
		if (label.equalsIgnoreCase("claim"))
		{
			if (args == null || args.length == 0)
			{
				if (!claims.containsKey(player.getName()))
				{
					player.sendMessage("§7You have nothing to claim");
					return true;
				}
				List<String> regions = wg.getRegionManager(player.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(player.getLocation()));
				if (!regions.contains("empyreal"))
				{
					player.sendMessage("§cYou must be in the city of empyreal before you try to claim your portals.");
					return true;
				}
				if (!hasEmptySpot(player.getInventory()))
				{
					player.sendMessage("§cYou do not have any empty inventory slots");
					return true;
				}
				ItemStack i = new ItemStack(Material.PORTAL, claims.get(player.getName()).intValue());
				player.getInventory().addItem(i);
				claims.remove(player.getName());
				getLogger().info(player.getName() + " claimed their "
						+ i.getAmount() + " portals from the claim system");
				player.sendMessage("§dPortals §6claimed!");
				return true;
			}
			if (args.length == 1)
			{
				if (!hasPerms(player, "claims.seeOther"))
					return true;
				player.sendMessage("§7Unclaimed portals for §6"
						+ args[0]
						+ "§7: §6"
						+ (claims.containsKey(args[0]) ? claims.get(args[0]).toString() : "0"));
				return true;
			}
			else if (args.length == 2)
			{
				if (!hasPerms(player, "claims.setOther"))
					return true;
				claims.put(args[0], C.integerO(args[1]));
				player.sendMessage("§7Unclaimed portals for §6"
						+ args[0]
						+ "§7 set to §6"
						+ (claims.containsKey(args[0]) ? claims.get(args[0]).toString() : "0"));
			}
			else
				player.sendMessage("§c/claim");
			return true;
		}
		if (label.equalsIgnoreCase("startpvp"))
		{
			if (args == null || args.length == 0)
			{
				if (newPlayers.contains(player.getName()))
				{
					newPlayers.remove(player.getName());
					player.sendMessage("§aYou have enabled pvp for yourself!");
				}
				else
					player.sendMessage("§7This comand does nothing for you.");
			}
			else if (args.length == 1)
			{
				if (player.hasPermission("wars.control"))
				{
					if (newPlayers.contains(args[0]))
					{
						newPlayers.remove(args[0]);
						player.sendMessage("§aYou have enabled pvp for "
								+ args[0]);
					}
					else
						player.sendMessage("§7That player is not on the new player list.");
				}
				else
				{
					if (newPlayers.contains(player.getName()))
					{
						newPlayers.remove(player.getName());
						player.sendMessage("§aYou have enabled pvp for yourself!");
					}
					else
						player.sendMessage("§7This comand does nothing for you.");
				}
			}
			return true;
		}
		return false;
	}

	private boolean hasPerms(Player player, String node)
	{
		if (!player.hasPermission(node))
		{
			player.sendMessage("§cYou cannot use that command");
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param inventory
	 *            Invetory
	 * @return True if there are any empty spaces
	 */
	private boolean hasEmptySpot(Inventory inventory)
	{
		for (ItemStack i : inventory.getContents())
			if (i == null)
				return true;
		return false;
	}

	@EventHandler
	public void playerUsingHelpCommand(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		String command = event.getMessage();
		if (!command.startsWith("/help"))
			return;
		event.setCancelled(true);
		if (!command.contains(" "))
		{
			player.sendMessage("§c/help keyword §7- searches the help database for the keyword and returns a help link");
			return;
		}
		String[] args = command.replace("/help ", "").split(" ");
		boolean found = false;
		for (Helper helper : helpers)
			if (helper.isApplicable(args[0]))
			{
				helper.showHelp(player, args[0]);
				found = true;
			}
		if (!found)
			player.sendMessage("§cCould not find a help topic applicable for your keyword: §6"
					+ args[0]);
	}

	@EventHandler
	public void onLastPlayerQuit(PlayerQuitEvent event)
	{
		Player[] online = getServer().getOnlinePlayers();
		if (online == null || online.length == 0)
		{
			tryReboot();
			return;
		}
		if (online.length == 1
				&& online[0].getName().equals(event.getPlayer().getName()))
		{
			tryReboot();
			return;
		}
	}

	private void tryReboot()
	{
		getLogger().info("Checking if the server needs a restart...");
		File uploadingDir = new File(getDataFolder().getParentFile(), "/_commit/");
		if (uploadingDir.mkdirs())
		{
			getLogger().info("Server does not need a restart.");
			return;
		}
		if (uploadingDir.listFiles() == null
				|| uploadingDir.listFiles().length == 0)
		{
			getLogger().info("Server does not need a restart.");
			return;
		}
		getLogger().warning("Server is undergoing a restart ...");
		getLogger().warning("==============================================");
		for (File file : uploadingDir.listFiles())
		{
			try
			{
				File to = new File(getDataFolder().getParentFile(), file.getName());
				if (to.exists())
				{
					getLogger().warning("Destination file exists, deleting ...");
					to.delete();
					getLogger().warning("Destination file deleted.");
				}
				getLogger().warning(String.format("Copying '%s' from '%s' to '%s' ...", file.getName(), file.getPath(), to.getPath()));
				Files.copy(file, to);
				getLogger().warning("Copy complete.");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		File[] files = uploadingDir.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			new File(uploadingDir.getAbsoluteFile(), files[i].getName()).delete();
		}
		getLogger().warning("All files copied, bringing the server down ...");
		getLogger().warning("==============================================");
		for (Player online : getServer().getOnlinePlayers())
			online.kickPlayer("Server going down for an automatic reboot.");
		getServer().dispatchCommand(getServer().getConsoleSender(), "stop");
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		Entity e = event.getEntity();
		if (e instanceof Squid)
			event.setCancelled(true);
		if (e instanceof Wolf)
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		String name = event.getPlayer().getName();
		if (ignoreRespawnOnce.remove(name))
		{
			getLogger().info("Ignoring the respawning of player " + name
					+ " because a plugin told me to.");
			return;
		}
		if (respawn.containsKey(name) && respawn.get(name) != null)
			event.setRespawnLocation(respawn.get(name));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSetBed(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			return;
		if (event.getClickedBlock().getTypeId() != 26)
			return;
		Block block = event.getClickedBlock();
		event.setCancelled(true);
		List<String> inRegions = wg.getRegionManager(player.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(block.getLocation()));
		if (!wg.canBuild(player, block)
				&& !containsAny(inRegions, allCanSetBed))
		{
			player.sendMessage("§cYou cannot set your bed in enemy territory.");
			return;
		}
		player.sendMessage("§aYou will respawn at this bed.");
		respawn.put(player.getName(), block.getLocation());
	}

	public static boolean containsAny(List<String> haystack, List<String> needle)
	{
		for (String str : needle)
			if (haystack.contains(str))
				return true;
		return false;
	}

	@EventHandler
	public void onPlayerBreakBed(BlockBreakEvent event)
	{
		if (event.getPlayer() == null)
			return;
		if (event.isCancelled())
			return;
		for (String key : respawn.keySet())
			if (respawn.get(key) != null
					&& respawn.get(key).equals(event.getBlock().getLocation()))
				respawn.put(key, null);
	}

	@EventHandler
	public void onEnchant(EnchantItemEvent event)
	{
		Player player = event.getEnchanter();
		List<Enchantment> enchantments = new ArrayList<Enchantment>(event.getEnchantsToAdd().keySet());
		for (Enchantment e : enchantments)
			if (shouldBlockEnchantment(e))
			{
				player.sendMessage("§7An unallowed enchantment, "
						+ e.getName().toLowerCase()
						+ ", was bound to your item. Event cancelled.");
				event.setCancelled(true);
				return;
			}
	}

	public void resetBed(String name)
	{
		respawn.put(name, null);
	}

	@EventHandler
	public void playerUseEnchantedItem(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack i = player.getItemInHand();
		if (i.getEnchantments().isEmpty())
			return;
		for (Enchantment e : i.getEnchantments().keySet())
			if (shouldBlockEnchantment(e))
				i.removeEnchantment(e);
	}

	@EventHandler
	public void playerDropEnchantedItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		ItemStack i = player.getItemInHand();
		if (i.getEnchantments().isEmpty())
			return;
		for (Enchantment e : i.getEnchantments().keySet())
			if (shouldBlockEnchantment(e))
				i.removeEnchantment(e);
	}

	@EventHandler
	public void onItemDrop(ItemSpawnEvent event)
	{
		ItemStack i = event.getEntity().getItemStack();
		if (i.getEnchantments().isEmpty())
			return;
		for (Enchantment e : i.getEnchantments().keySet())
			if (shouldBlockEnchantment(e))
				i.removeEnchantment(e);
	}

	public static boolean shouldBlockEnchantment(Enchantment enchantment)
	{
		if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
			return true;
		if (enchantment.equals(Enchantment.ARROW_DAMAGE))
			return true;
		if (enchantment.equals(Enchantment.FIRE_ASPECT))
			return true;
		if (enchantment.equals(Enchantment.DAMAGE_ALL))
			return true;
		if (enchantment.equals(Enchantment.THORNS))
			return true;
		if (enchantment.equals(Enchantment.DAMAGE_ARTHROPODS))
			return true;
		return false;
	}

	@EventHandler
	public void onPlayerTeleportGrace(PlayerTeleportEvent event)
	{
		if (!event.getFrom().getWorld().equals(event.getTo().getWorld()))
		{
			final Player player = event.getPlayer();
			if (player.hasPermission("emp.igwldchng"))
				return;
			final List<Player> revealBack = new ArrayList<Player>();
			for (Player online : event.getTo().getWorld().getPlayers())
				if (!online.getName().equals(player.getName()))
					revealBack.add(online);
			addHidden(player, System.currentTimeMillis(), revealBack);
			player.sendMessage("§730 seconds of invisiblity started - attacking will break the cloak!");
		}
	}

	@EventHandler
	public void onInvisPlayerDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player)
		{
			Player damager = (Player) event.getDamager();
			if (isHidden(damager))
			{
				HiddenPlayer h = getHiddenObject(damager);
				if (h.now + 60000 > System.currentTimeMillis())
					event.setCancelled(true);
				if (h.hidden)
				{
					h.reveal();
					damager.sendMessage("§7You've ended your cloak from the portal. You stil cannot do damage.");
				}
				else
					damager.sendMessage("§7You're still too nauseous to focus on attacking.");
			}
		}
	}

	@EventHandler
	public void onEntityTargetInvis(EntityTargetEvent event)
	{
		if (event.getTarget() instanceof Player)
			if (isHidden((Player) event.getTarget()))
				event.setCancelled(true);
	}

	private boolean isHidden(Player player)
	{
		return getHiddenObject(player) != null;
	}

	private void addHidden(Player player, long now, List<Player> hiddenFrom)
	{
		if (isHidden(player))
			removeHidden(player);
		hiddenPlayers.add(new HiddenPlayer(this, player, now, hiddenFrom));
	}

	private HiddenPlayer getHiddenObject(Player player)
	{
		for (HiddenPlayer hidden : hiddenPlayers)
			if (hidden.player.getName().equals(player.getName()))
				return hidden;
		return null;
	}

	public void removeHidden(Player player)
	{
		hiddenPlayers.remove(getHiddenObject(player));
	}

	public void removeHidden(HiddenPlayer hidden)
	{
		hiddenPlayers.remove(hidden);
	}

	@EventHandler
	public void onSnowballThrow(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Snowball)
			if (snowBallChance < new Random().nextInt(101))
				event.setDamage(snowBallDamage);
	}

	@EventHandler
	public void onEntityHurt(EntityDamageEvent event)
	{
		Entity hurt = event.getEntity();
		if (hurt instanceof Snowman
				&& (event.getCause().equals(DamageCause.DROWNING) || event.getCause().equals(DamageCause.MELTING)))
			event.setCancelled(true);
	}

	@EventHandler
	public void onMiningCreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getEntity() instanceof Enderman)
			event.setCancelled(true);
	}

	public List<Helper> getHelpers()
	{
		return helpers;
	}

	public void ignoreOneRespawn(String name)
	{
		ignoreRespawnOnce.add(name);
	}

	@EventHandler
	public void onNomadPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (!player.hasPermission("empyreanwars.build"))
		{
			event.setCancelled(true);
			player.sendMessage("§7You need to be greylisted before building - §a/help greylist");
		}
	}

	@EventHandler
	public void onNomadBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (!player.hasPermission("empyreanwars.build"))
		{
			event.setCancelled(true);
			player.sendMessage("§7You need to be greylisted before building - §a/help greylist");
		}
	}

	@EventHandler
	public void onNewPlayerPVP(EntityDamageByEntityEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player hurt = (Player) event.getEntity();
			if (newPlayers.contains(hurt.getName()))
			{
				if (event.getDamager() instanceof Player)
				{
					((Player) event.getDamager()).sendMessage("§7That new player is under the protection of no pvp.");
					event.setCancelled(true);
				}
				else if (event.getDamager() instanceof Arrow)
				{
					Arrow arrow = (Arrow) event.getDamager();
					if (arrow.getShooter() instanceof Player)
					{
						((Player) arrow.getShooter()).sendMessage("§7That new player is under the protection of no pvp.");
						event.setCancelled(true);
					}
				}
				return;
			}
			if (event.getDamager() instanceof Player)
			{
				Player damager = (Player) event.getDamager();
				if (newPlayers.contains(damager.getName()))
				{
					damager.sendMessage("§7You are under the effect of no pvp. §a/startpvp §7will end that effect");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onNewPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore())
		{
			newPlayers.add(player.getName());
			getLogger().info("Adding " + player.getName()
					+ " to the nopvp new player list");
		}
	}

	@EventHandler
	public void onCraftItem(CraftItemEvent event)
	{
		ItemStack ret = event.getRecipe().getResult();
		if (!ret.getType().equals(Material.WRITTEN_BOOK))
			return;
		ItemStack written = null;
		ItemStack empty = null;
		for (ItemStack i : event.getInventory().getContents())
		{
			if (i == null || i.getTypeId() == 0)
				continue;
			if (i.getType().equals(Material.WRITTEN_BOOK))
				written = i;
			else if (i.getType().equals(Material.BOOK_AND_QUILL))
				empty = i;
		}
		if (written != null && empty != null)
		{
			ret.setItemMeta(written.getItemMeta());
			event.setCurrentItem(ret);
		}
	}

	// private CancellationDetector<CreatureSpawnEvent> detector = new
	// CancellationDetector<CreatureSpawnEvent>(CreatureSpawnEvent.class);

	@EventHandler
	public void spawnPigman(CreatureSpawnEvent event)
	{
		if (event.getLocation().getWorld().getName().equals("world"))
			if (event.getEntityType().equals(EntityType.SKELETON))
			{
				event.setCancelled(true);
				event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.PIG_ZOMBIE);
			}
	}

}
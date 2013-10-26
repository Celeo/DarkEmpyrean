package com.darktidegames.empyrean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * <b>ConfigAccessor</b><br>
 * 
 * @author Bukkit, Celeo
 */
public class ConfigAccessor
{

	private final String fileName;
	private final Plugin plugin;

	private File configFile;
	private FileConfiguration fileConfiguration;

	public ConfigAccessor(Plugin plugin, String fileName)
	{
		if (!fileName.contains(".yml"))
			fileName += ".yml";
		if (plugin == null)
			throw new IllegalArgumentException("Plugin cannot be null");
		this.plugin = plugin;
		this.fileName = fileName;
	}

	public void reloadConfig()
	{
		if (configFile == null)
		{
			File dataFolder = new File(plugin.getDataFolder() + "/data");
			dataFolder.mkdirs();
			configFile = new File(dataFolder, fileName);
		}
		fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
		InputStream defConfigStream = plugin.getResource("Script.yml");
		if (defConfigStream != null)
		{
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			fileConfiguration.setDefaults(defConfig);
		}
	}

	public FileConfiguration getConfig()
	{
		if (fileConfiguration == null)
			reloadConfig();
		return fileConfiguration;
	}

	public void saveConfig()
	{
		if (fileConfiguration == null || configFile == null)
		{
			Bukkit.getLogger().info("Could not save file "
					+ fileName
					+ " as the fileConfiguration and configFiles are both null!");
			return;
		}
		try
		{
			getConfig().save(configFile);
		}
		catch (IOException ex)
		{
			plugin.getLogger().log(Level.SEVERE, "Could not save config to "
					+ configFile, ex);
		}
	}

	public void saveDefaultConfig()
	{
		if (!configFile.exists())
			plugin.saveResource(fileName, false);
	}

	/*
	 * GET and SET
	 */

	public File getConfigFile()
	{
		return configFile;
	}

	public void setConfigFile(File configFile)
	{
		this.configFile = configFile;
	}

	public FileConfiguration getFileConfiguration()
	{
		return fileConfiguration;
	}

	public void setFileConfiguration(FileConfiguration fileConfiguration)
	{
		this.fileConfiguration = fileConfiguration;
	}

	public String getFileName()
	{
		return fileName;
	}

	public Plugin getPlugin()
	{
		return plugin;
	}

}
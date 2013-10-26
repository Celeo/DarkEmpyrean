package com.darktidegames.empyrean;

import java.util.List;

import org.bukkit.entity.Player;

public class Helper
{

	private final String name;
	private final String link;
	private final List<String> keywords;

	public Helper(String name, String link, List<String> keywords)
	{
		this.name = name;
		this.link = link;
		this.keywords = keywords;
	}

	public boolean isApplicable(String message)
	{
		return keywords.contains(message.toLowerCase());
	}

	public void showHelp(Player player, String input)
	{
		player.sendMessage(String.format("§7Help for §6%s §7links to §6%s§7: Click here §4--> §6%s §4<--", input, name, link));
	}

	public String getName()
	{
		return name;
	}

	public String getLink()
	{
		return link;
	}

	public List<String> getKeywords()
	{
		return keywords;
	}

}
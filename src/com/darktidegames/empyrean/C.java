package com.darktidegames.empyrean;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class C
{

	/*
	 * Generic variables
	 */

	/**
	 * 
	 * @param string
	 *            String
	 * @return True if int
	 */
	public static final boolean isInt(String string)
	{
		try
		{
			Integer.valueOf(string);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return True if double
	 */
	public static final boolean isDouble(String string)
	{
		try
		{
			Double.valueOf(string);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return True if long
	 */
	public static final boolean isLong(String string)
	{
		try
		{
			Long.valueOf(string);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return int value of string
	 */
	public static final int i(String string)
	{
		return Integer.valueOf(string).intValue();
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return Integer of string
	 */
	public static final Integer integerO(String string)
	{
		return Integer.valueOf(string);
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return double value of string
	 */
	public static final double d(String string)
	{
		return Double.valueOf(string).doubleValue();
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return Double of string
	 */
	public static final Double doubleO(String string)
	{
		return Double.valueOf(string);
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return long value of string
	 */
	public static final long l(String string)
	{
		return Long.valueOf(string).longValue();
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return Long of string
	 */
	public static final Long longO(String string)
	{
		return Long.valueOf(string);
	}

	/**
	 * 
	 * @param i
	 *            int
	 * @return String valueOf i
	 */
	public static final String s(int i)
	{
		return String.valueOf(i);
	}

	/**
	 * 
	 * @param d
	 *            double
	 * @return String valueOf d
	 */
	public static final String s(double d)
	{
		return String.valueOf(d);
	}

	/**
	 * 
	 * @param l
	 *            long
	 * @return String valueOf l
	 */
	public static final String s(long l)
	{
		return String.valueOf(l);
	}

	/**
	 * 
	 * @param o
	 *            Object
	 * @return object.toString()
	 */
	public static final String s(Object o)
	{
		return o.toString();
	}

	/**
	 * 
	 * @param loc
	 *            Location
	 * @return String, formatted
	 */
	public static final String locationToString(Location loc)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		try
		{
			return loc.getWorld().getName() + "," + df.format(loc.getX()) + ","
					+ df.format(loc.getY()) + "," + df.format(loc.getZ());
		}
		catch (Exception e)
		{
			return "world" + "," + df.format(loc.getX()) + ","
					+ df.format(loc.getY()) + "," + df.format(loc.getZ());
		}
	}

	/**
	 * 
	 * @param str
	 *            String
	 * @return Location, formatted form String
	 */
	public static Location stringToLocation(String str)
	{
		return stringArrayToLocation(str.split(","));
	}

	/**
	 * 
	 * @param data
	 *            String[]
	 * @return Location, formatted from String[]
	 */
	public static Location stringArrayToLocation(String[] data)
	{
		return new Location(Bukkit.getWorld(data[0]), d(data[1]), d(data[2]), d(data[3]));
	}

	/**
	 * 
	 * @param str
	 *            String, formatted for <i>stringToLocation</i>
	 * @return True if the String can be cast to Location using
	 *         <i>stringToLocation</i>
	 */
	public static boolean isLocation(String str)
	{
		try
		{
			stringToLocation(str);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * 
	 * @param list
	 *            List of String objects
	 * @return String, formatted from List
	 */
	public static String listToString(List<String> list)
	{
		String ret = "";
		for (String str : list)
		{
			if (ret.equals(""))
				ret = str;
			else
				ret += "," + str;
		}
		return ret;
	}

	/**
	 * 
	 * @param list
	 *            List of Objects
	 * @return String, formatted from List
	 */
	public static String listObjectToString(List<Object> list)
	{
		String ret = "";
		for (Object o : list)
		{
			if (ret.equals(""))
				ret = o.toString();
			else
				ret += "," + o.toString();
		}
		return ret;
	}

	/**
	 * 
	 * @param string
	 *            String
	 * @return List of String objects, formatted from String
	 */
	public static List<String> stringToList(String string)
	{
		return new ArrayList<String>(Arrays.asList(string.split(",")));
	}

	/**
	 * 
	 * @param master
	 *            List of String objects
	 * @param check
	 *            List of String objects
	 * @return True if the master list contains anything from the check list
	 */
	public static boolean containsAny(List<String> master, List<String> check)
	{
		for (String str : master)
			if (check.contains(str))
				return true;
		return false;
	}

}
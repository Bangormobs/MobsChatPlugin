package uk.co.mobsoc.chat.plugin;

import java.util.HashMap;
import java.util.UUID;

import org.json.simple.parser.JSONParser;


import uk.co.mobsoc.chat.common.Colour;
import uk.co.mobsoc.chat.common.Util;
/**
 * A collection of useful static Utility functions
 * @author triggerhapp
 *
 */
public class PUtils {
	/**
	 * Get the bukkit colour nearest to given colour
	 * @param colour
	 * @return
	 */
	public static String getBukkitColour(Colour colour) {
		return Util.getClosestColour(ChatPlugin.colourList, colour).getString();
	}
	/**
	 * Convert a bukkit-coloured string into a MobsChat Protocol coloured string
	 * @param message
	 * @return
	 */
	public static String bukkitMarkupToInternal(String message){
		String sep = "§";
		String f = "";
		boolean bold = false, italic = false, strike = false, under = false, magic = false;
		Colour c = new Colour("axxxxxx");
		boolean first = true;
		for(String s : message.split(sep)){
			if(first){
				//First can never be marked up...
				f = f + c.toInternal() + s;
				first=false;
			}else{
				String single = s.substring(0,1);
				String rest = s.substring(1,s.length());
				if(single.equalsIgnoreCase("r")){
					c = new Colour("axxxxxx");
					bold = false; italic = false; strike = false; under = false; magic = false;
				}else if(single.equalsIgnoreCase("k")){
					magic = true;
					c = c.with(bold, italic, under, strike, magic); 
				}else if(single.equalsIgnoreCase("l")){
					bold = true;
					c = c.with(bold, italic, under, strike, magic);
				}else if(single.equalsIgnoreCase("m")){
					strike = true;
					c = c.with(bold, italic, under, strike, magic);
				}else if(single.equalsIgnoreCase("n")){
					under = true;
					c = c.with(bold, italic, under, strike, magic);
				}else if(single.equalsIgnoreCase("o")){
					italic = true;
					c = c.with(bold, italic, under, strike, magic);
				}else{
					c = getColour("§"+single).with(bold,italic,under,strike,magic);
				}
				f = f + c.toInternal() + rest;
			}
		}
		return f;
	}
	
	/**
	 * Convert a MobsChat Protocol coloured string to Bukkit coloured string
	 * @param message
	 * @return
	 */
	public static String InternalMarkupToBukkit(String message){
		String sep = "§";
		String f = "";
		boolean first = true;
		for(String s : message.split(sep)){
			if(first){
				f=f+s;
				first = false;
			}else{
				String single = s.substring(0,7);
				String rest = s.substring(7,s.length());
				Colour c = new Colour(single);
				String formating = "";
				if(c.isBold()){
					formating = formating + "§l";
				}
				if(c.isItalic()){
					formating = formating + "§o";
				}
				if(c.isUnderline()){
					formating = formating + "§n";
				}
				if(c.isMagic()){
					formating = formating + "§k";
				}
				if(c.isStrikethrough()){
					formating = formating + "§m";
				}
				f = f + getBukkitColour(c)+ formating + rest + "§r";
			}
		}
		return f;		
	}

	public static Colour getColour(String string) {
		for(Colour c : ChatPlugin.colourList){
			if(c.getString().equalsIgnoreCase(string)){
				return c;
			}
		}
		return null;
	}

}

/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server.lib;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

	public static void paintGameField(Graphics2D g, Color c, int x, int y, int width, int height) {
		Color b4 = g.getColor();
		GradientPaint gradient1 = new GradientPaint(x, y, c, x, y+height, c.darker().darker());
		g.setPaint(gradient1);

		g.fillRect(x, y, width, height);
		
		g.setColor(c.darker().darker().darker());
		g.drawRect(x, y, width, height);
		
		GradientPaint gradient2 = new GradientPaint(x, y, new Color(255, 255, 255, 40), x, y+height, new Color(255, 255, 255, 60));
		g.setPaint(gradient2);
		
		g.fillRect(x+3, y+3, width-5, height-5);
		g.setColor(b4);
	}
	
	public static boolean isColor(int r, int g, int b) {
		if (Util.isInIntervall(0, 255, r) &&
				Util.isInIntervall(0, 255, g) &&
				Util.isInIntervall(0, 255, b)) {
			return true;
		}
		return false;
	}
	
	public static boolean isInIntervall(int start, int end, int value) {
		if (start <= value && value <= end) {
			return true;
		}
		return false;
	}
	
	public static String startChars(int length, String str) {
		return str.substring(0, length);
	}
	
	public static boolean isBadCommandSyntax(String cmd) {
		if (cmd.matches("[a-z0-9]+")) {
			return true;
		}
		return false;
	}
	
	
	public static String getFormatedDate() {
		
		return "";
		
	}
	
	public static boolean contains(String[] array, String delimiter) {
		
		for (int i = 0; i < array.length; i++) {
			
			if (array[i].equals(delimiter)) {
				return true;
			}
			
		}
		
		
		return false;
	}
	
	
	public static String replaceFlags(String[][] replaceMap, String command) {
		
		String result = command;
		
		try {
			for (int i = 0; i < replaceMap.length; i++) {
				
				String search = replaceMap[i][0];
				String replace = replaceMap[i][1];
				
				result = result.replaceAll(search, replace);
				
			}
		} catch (Exception e) {
			
		}

		return result;
	}
	
	public static String[] readParameters(String parameters) {
		
		if (parameters == null) {
			return null;
		}

		int args = Util.contains(parameters, NeptuneProtocol.SEPARATOR);
		
		String[] result = new String[args + 1];
		String rest = parameters;
		for (int i = 0; i < result.length - 1; i++) {
			result[i] = rest.substring(0, rest.indexOf(NeptuneProtocol.SEPARATOR));
			rest = rest.substring(rest.indexOf(NeptuneProtocol.SEPARATOR) + 1);
		}
		result[result.length - 1] = rest;
		
		for (int i = 0; i < result.length; i++) {
			result[i] = Util.decodeUTF8(result[i]);
		}
		
		return result;
	}
	
	public static int contains(String string, String pattern) {
		int args = 0;
		String rest = string;
		while (rest.contains(pattern)) {
			args++;
			rest = rest.substring(rest.indexOf(pattern) + 1);
		}
		return args;
	}
	
	public static String decodeUTF8(String input) {
		try {
			return URLDecoder.decode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.info("sss", true);
			return input;
		}
	}
	
	public static String md5(String pString) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch( NoSuchAlgorithmException e ) {
			return null;
		}
		char[] charArray = pString.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for(int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if(val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}
	
}

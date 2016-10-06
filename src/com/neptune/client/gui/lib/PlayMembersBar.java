/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.client.gui.lib;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.neptune.client.NeptuneClientUser;
import com.neptune.model.GameFieldType;
import com.neptune.server.lib.Log;
import com.neptune.server.lib.Util;

public class PlayMembersBar extends JPanel {

	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private String name;
	private boolean otherError;
	private ArrayList<NeptuneClientUser> ncu;
	
	public PlayMembersBar(String name) {
		this.name = name;
		this.otherError = false;
		this.ncu = new ArrayList<NeptuneClientUser>();
		
		try {
			this.image = ImageIO.read(this.getClass().getResource("/res/images/nc-footer.png"));
		} catch (Exception e) {
			this.image = null;
		}
	}
	
	public void setOtherError(boolean error) {
		this.otherError = error;
	}
	
	public void clear() {
		this.setOtherError(true);
		this.ncu = null;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUsers(NeptuneClientUser[] ncu) {
		this.ncu = new ArrayList<NeptuneClientUser>();
		
		for (int i = 0; i < ncu.length; i++) {
			this.ncu.add(ncu[i]);
		}
	}
	
	public void removeUser(String name) {
		if (this.ncu == null) {
			return;
		}
		Log.info("Going to remove the user: " + name,
				Boolean.getBoolean(System.getProperty("neptune.debug")));
		
		for (int i = 0; i < this.ncu.size(); i++) {
			if (this.ncu.get(i).name.equals(name)) {
				this.ncu.remove(i);
				break;
			}
		}
	}
		
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		int height = this.getSize().height;
		int width = this.getSize().width;

		if (this.image != null) {
			g2d.drawImage(this.image, 0, 0, this);
		} else {
			g2d.setColor(Color.black);
			g2d.setBackground(Color.black);
		}

		int totalBorder = 10;
		if (this.ncu == null) {
			g.setColor(Color.white);
			if (!this.otherError) {
				g.drawString("Fehler: Spieler nicht anzeigbar!", 10, (totalBorder / 2)
						+ g.getFont().getSize());
			} else {
				g.drawString("Bitte warten ...", 10, (totalBorder / 2)
						+ g.getFont().getSize());
			}
		} else {
			int xTotal = totalBorder;
			
			for (int i = 0; i < this.ncu.size(); i++) {
				
				NeptuneClientUser ncuTemp = this.ncu.get(i);
				String nameAsString = ncuTemp.name;
				if (name.equals(ncuTemp.name)) {
					nameAsString += " (Sie selbst)";
				}
				
				char[] name = nameAsString.toCharArray();
				int widthName = g.getFontMetrics(g.getFont()).charsWidth(name, 0, name.length);
				
				if (widthName > (((width-(totalBorder+totalBorder))/4)-10)) {
					while (widthName > (((width-(totalBorder+totalBorder))/4)-10)) {
						nameAsString = nameAsString.substring(0, nameAsString.length()-1);
						name = nameAsString.toCharArray();
						widthName = g.getFontMetrics(g.getFont()).charsWidth(name, 0, name.length);
					}
					
					nameAsString = nameAsString.substring(0, nameAsString.length()-3) + "...";
					name = nameAsString.toCharArray();
					widthName = g.getFontMetrics(g.getFont()).charsWidth(name, 0, name.length);
				}
				
				g.setColor(Color.DARK_GRAY);
				int widthDiv = (((width - (totalBorder + totalBorder)) / 4) - 10);
				int endWidthDiv = xTotal + widthDiv;
				g.drawRoundRect(xTotal, totalBorder / 2, widthDiv, height
						- totalBorder, 15, 15);

				g.setColor(Color.white);

				int start = ((Math.max(endWidthDiv, xTotal) - Math.min(
						endWidthDiv, xTotal)) - widthName) / 2;
				g.drawString(nameAsString, start + xTotal, (totalBorder / 2)
						+ g.getFont().getSize());
					
					
				// draw the fields
				String[] gftTemp = ncuTemp.fieldTypes;
				GameFieldType[] gft = ncuTemp.gft;
				
				int expectedSize = gftTemp.length*30+(gftTemp.length-1)*10;
				int startToDraw = ((Math.max(endWidthDiv, xTotal) - Math.min(
						endWidthDiv, xTotal)) - expectedSize) / 2;
				int ypos = (totalBorder / 2) + g.getFont().getSize() + 10;
				int xPosTemp = 0;
				for (int j = 0; j < gftTemp.length; j++) {
					for (int j2 = 0; j2 < gft.length; j2++) {
						if (gft[j2].getId().equals(gftTemp[j])) {
							Util.paintGameField(g2d, gft[j2].getFieldColor(), xTotal+xPosTemp+startToDraw, ypos, 30, 30);
							xPosTemp += 30 + 10;
						}
					}
				}
				xTotal += (((width-(totalBorder+totalBorder))/4)-10) + totalBorder;
			}
		}
	}
	
}

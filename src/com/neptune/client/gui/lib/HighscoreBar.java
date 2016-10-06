/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.client.gui.lib;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.neptune.server.lib.Log;


public class HighscoreBar extends JPanel {

	private static final long serialVersionUID = 1L;
	private Object[][] hs;
	private BufferedImage bg;
	private BufferedImage win;
	
	public HighscoreBar() {
		try {
			this.bg = ImageIO.read(this.getClass().getResource("/res/images/nc-bg.png"));
			this.win = ImageIO.read(this.getClass().getResource("/res/images/winner-avatar-big.png"));
		} catch (Exception e) {
			Log.error("Cannot load images: " + e.toString(),
					Boolean.parseBoolean(System.getProperty("neptune.debug")));
		}
	}
	
	public void setHighscore(Object[][] hs) {
		this.hs = hs;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		if (this.bg != null) {
			g2d.drawImage(this.bg, 0, 0, this);
		} else {
			g2d.setColor(Color.black);
			g2d.fillRect(0, 0, 1000, 1000);
		}
		
		int height = this.getSize().height;
		int width = this.getSize().width;
		Font b4 = g2d.getFont();
		g2d.setColor(Color.lightGray);
		
		int boxHeight = (20+147)+(this.hs.length-1)*16+(this.hs.length-2)*10;
		int y = (height-boxHeight)/2;
		
		// 1. place
		String title = this.hs[0][0] + " (" + this.hs[0][2] + " Punkte in " + this.hs[0][3] + " Sekunden)";
		g2d.setFont(new Font(b4.getFamily(), Font.BOLD, 20));
		int widthFont = g2d.getFontMetrics(g2d.getFont()).charsWidth(title.toCharArray(), 0, title.toCharArray().length);
		g2d.drawString(title, (width-widthFont)/2, y);
		y += g.getFont().getSize()-25;
		g2d.drawImage(this.win, (width-330)/2, y, this);
		y += 200;
		g2d.setFont(new Font(b4.getFamily(), Font.BOLD, 16));
		// others
		for (int i = 1; i < this.hs.length; i++) {
			title = (i+1) + ". Platz: " + this.hs[i][0] + " (" + this.hs[i][2] + " Punkte in " + this.hs[i][3] + " Sekunden)";
			widthFont = g2d.getFontMetrics(g2d.getFont()).charsWidth(title.toCharArray(), 0, title.toCharArray().length);
			g2d.drawString(title, (width-widthFont)/2, y);
			y += g.getFont().getSize()+10;
		}
	}
	
}

/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.model;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import com.neptune.server.lib.Util;


public class NeptuneView extends JComponent implements MouseListener {

	private static final long serialVersionUID = 1L;
	private NeptuneModel model;
	private NeptuneActionListener actionListener;
	private boolean antiAlias;
	private Color backgroundColor;
	private NeptuneClickPositionListener clickListener;
	private boolean thirdPartyGameHandling = false;

	public NeptuneView(NeptuneModel m) {
		this.model = m;
		
		
		// set properties
		this.setPreferredSize(new Dimension(m.getWidth(), m.getHeight()));
		this.setAntiAliasEnabled(false);
		this.setBackgroundColor(Color.black);
	}
	
	public void addClickPositionListener(NeptuneClickPositionListener listener) {
		this.clickListener = listener;
	}
	
	public void fireClickPositionListener() {
		this.clickListener = null;
	}
	
	public boolean thirdPartyGameHandlingEnabled() {
		return this.thirdPartyGameHandling;
	}
	
	public void updateCourt(String[][] newCourt) {
		if (!this.thirdPartyGameHandling) {
			return;
		}
		
		this.model.updateGameCourt(newCourt);
		this.repaint();
	}
	
	public void setThirdPartyGameHandlingEnabled(boolean enabled) {
		this.thirdPartyGameHandling  = enabled;
	}
	
	public void resetModel(NeptuneModel m) {
		
		this.model = m;
		
		
		// set properties
		if (m != null) {
			this.setPreferredSize(new Dimension(m.getWidth(), m.getHeight()));
			this.setAntiAliasEnabled(false);
			this.setBackgroundColor(Color.black);
		}
		
		
	}
	
	
	public void installMouseListener() {
		this.addMouseListener(this);
	}
	
	public void unInstallMouseListener() {
		this.addMouseListener(null);
	}
	
	public NeptuneModel getModel() {
		return this.model;
	}
	
	private void onClicked(int x, int y) {
		if (this.model.removeFields(x, y, this)) {
			// repaint the gameArea
			this.repaint();
		}
		return;
	}
	
	public boolean antiAliasIsEnabled() {
		return this.antiAlias;
	}
	
	public void setAntiAliasEnabled(boolean enabled) {
		this.antiAlias = enabled;
	}
	
	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}
	
	public Color getBackgroundColor() {
		return this.backgroundColor;
	}
	
	public void addActionListener(NeptuneActionListener al) {
		this.actionListener = al;
	}
	
	public NeptuneActionListener getActionListener() {
		return this.actionListener;
	}
	
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		
		
		// check if anti alias is enabled
		if (this.antiAlias) {
			((Graphics2D)g).setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
			);
		} else {
			((Graphics2D)g).setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF
			);
		}

		// set background
		int totalWidth = g.getClipBounds().width;
		int totalHeight = g.getClipBounds().height;
		
		GradientPaint gradient1 = new GradientPaint(0, 0, Color.darkGray, 0, totalHeight, Color.black);
		g2d.setPaint(gradient1);
		
		g2d.fillRect(0, 0, totalWidth, totalHeight);
		
		if (this.model == null) {
			return;
		}
		
		// paint the court
		NeptuneGameCourt gmeFild = this.model.getGameField();
		int size = this.model.getFieldSize();
		
		int xoffset = 1;
		int x = 0;
		int y = 0;
		int length = this.model.getCourt().getXFields()*this.model.getCourt().getYFields();
		while ((x*y) < length) {
			// get the field ...
			NeptuneGameField ngf = gmeFild.getField(x, y);
			if (ngf == null || ngf.isNull()) {
				// there is no field
			} else {
				Util.paintGameField(g2d, ngf.getColor(), xoffset+x*size, y*size, size, size);
			}
			
			// take care of the cnt's
			if (x < this.model.getGameField().getXFields()) {
				x++;
			} else {
				x = 0;
				y++;
			}
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		// call the event-handler
		if (this.thirdPartyGameHandling && this.clickListener != null) {
			this.clickListener.onClick(e.getX(), e.getY());
			return;
		}
		this.onClicked(e.getX(), e.getY());
	}

	public void mouseEntered(MouseEvent e) {
		// nothing happens here
	}

	public void mouseExited(MouseEvent e) {
		// nothing happens here
	}

	public void mousePressed(MouseEvent e) {
		// nothing happens here
	}

	public void mouseReleased(MouseEvent e) {
		// nothing happens here
	}

}

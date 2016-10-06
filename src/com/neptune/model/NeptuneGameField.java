/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.model;
import java.awt.Color;


public class NeptuneGameField {

	private Color color;
	private String type;
	private boolean active = false;
	private boolean isNull;

	public NeptuneGameField(String type, Color color) {
		this.type = type;
		this.color = color;
	}
	
	public void setActive(boolean active) {
		this.active  = active;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public String getType() {
		return this.type;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}
	
	public boolean isNull() {
		return this.isNull;
	}
	
}

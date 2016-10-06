/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.model;

import java.awt.Color;

public class GameFieldType {
	
	private Color color;
	private String id;

	public GameFieldType(String id, Color color) {
		this.id = id;
		this.color = color;
	}
	
	public String getId() {
		return this.id;
	}
	
	public Color getFieldColor() {
		return this.color;
	}
	
}
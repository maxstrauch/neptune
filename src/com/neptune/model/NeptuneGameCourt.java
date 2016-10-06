/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.model;

public class NeptuneGameCourt {

	private int y;
	private int x;
	
	private NeptuneGameField[][] court;

	public NeptuneGameCourt(int x, int y) {
		// save the information
		this.x = x;
		this.y = y;
		// create a new court
		this.court = new NeptuneGameField[y][x];
	}
	
	private boolean outOfRange(int x, int y) {
		if (x >= this.x || y >= this.y || x < 0 || y < 0) {
			return true;
		}
		return false;
	}
	
	public NeptuneGameField getField(int x, int y) {
		// check weather it's out of range
		if (this.outOfRange(x, y)) {
			return null;
		}
		return this.court[y][x];
	}
	
	public int getXFields() {
		return this.x;
	}
	
	public int getYFields() {
		return this.y;
	}

	public void setField(NeptuneGameField ngf, int x, int y) {
		// check weather it's out of range
		if (this.outOfRange(x, y)) {
			return;
		}
		// set the field null
		this.court[y][x] = null;
		this.court[y][x] = ngf;
	}
	
}

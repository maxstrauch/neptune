/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server;

import java.util.ArrayList;

public class NeptuneGameResult {

	private String name;
	private long execTime;
	private int totalPoints;
	private ArrayList<String[]> fieldPoints;

	public NeptuneGameResult(String name) {
		this.name = name;
		this.fieldPoints = new ArrayList<String[]>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setGameTime(long time) {
		this.execTime = time;
	}
	
	public void addFieldTypesAndPoints(String type, int points) {
		this.totalPoints += points;
		this.fieldPoints.add(new String[]{type, String.valueOf(points)});
	}
	
	public int getTotalPoints() {
		return this.totalPoints;
	}
	
	public long getTotalTime() {
		return this.execTime;
	}
	
	public String getTotalTimeAsSeconds() {
		int result = Math.round(this.execTime/1000f);
		return String.valueOf(result);
	}
	
}

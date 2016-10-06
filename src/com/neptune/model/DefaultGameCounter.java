/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.model;


public class DefaultGameCounter /*implements DefaultCountProfile*/ {
	
	private int totalPoints;
	public long startTime = -1;
	public long endTime = -1;
	
	public DefaultGameCounter() {
		this.totalPoints = 0;
	}
	
	private int getPoints(int removedFields) {
		return Math.round((float) (
				150.0f*Math.atan(removedFields/100.0f)
		));
	}
	
	public void fieldsRemoved(String type, int cnt) {
		totalPoints += this.getPoints(cnt);
	}

	public int getPointsForFieldType(String type) {
		return totalPoints;
	}

	public int getTotalPoints() {
		return totalPoints;
	}
	
}

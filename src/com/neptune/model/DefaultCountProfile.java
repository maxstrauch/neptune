/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.model;

public interface DefaultCountProfile {
	
	public void fieldsRemoved(NeptuneGameField base, int cnt);
	
	public int getTotalPoints();
	
	public int getPointsForFieldType(String field);
	
}
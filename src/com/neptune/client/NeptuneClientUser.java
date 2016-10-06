/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.client;

import com.neptune.model.GameFieldType;

public class NeptuneClientUser {

	public String[] fieldTypes;
	public String name;
	public GameFieldType[] gft;

	public NeptuneClientUser(String[] fieldTypes, String name, GameFieldType[] gft) {
		this.fieldTypes = fieldTypes;
		this.name = name;
		this.gft = gft;
	}
	
}

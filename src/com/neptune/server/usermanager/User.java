/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server.usermanager;

import java.util.ArrayList;

import com.neptune.model.DefaultGameCounter;
import com.neptune.server.NeptuneGameResult;
import com.neptune.server.lib.NeptuneProtocol;

public class User {

	private String ip;
	private int port;
	private String name;
	
	public boolean userFriendly = false;
	public int cmdFalseCnt = 1;
	public boolean usesDepricatedCommands = false;
	
	private DefaultGameCounter counter;
	private ArrayList<String> fieldTypes;
	private ArrayList<NeptuneGameResult> games;
	private boolean playing;
	private boolean playSend = false;
	
	public User(String ip, int port, String name) {
		this.ip = ip;
		this.port = port;
		this.name = name;
		this.fieldTypes = new ArrayList<String>();
		this.games = new ArrayList<NeptuneGameResult>();
		this.counter = new DefaultGameCounter();
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void addFieldType(String type) {
		this.fieldTypes.add(type);
	}
	
	public String[] getFieldTypesAsArray() {
		String[] fieldTypes = new String[this.fieldTypes.size()];
		this.fieldTypes.toArray(fieldTypes);
		return fieldTypes;
	}
	
	public String getFieldTypes() {
		if (this.fieldTypes.size() < 1) {
			return null;
		}
		
		String res = "";
		
		for (int i = 0; i < this.fieldTypes.size(); i++) {
			
			res += this.fieldTypes.get(i);
			
			if (i < this.fieldTypes.size() - 1) {
				res += NeptuneProtocol.FIELD_SEPARATOR;
			}
			
		}
		return res;
	}
	
	public DefaultGameCounter getCounter() {
		return this.counter;
	}
	
	public boolean isPlaying() {
		return this.playing;
	}
	
	public void setPlaying(boolean enabled) {
		this.playing = enabled;
	}
	
	
	
	public void setPlayCmdSend(boolean send) {
		this.playSend = send;
	}
	
	public boolean isPlayCmdSend() {
		return this.playSend;
	}
	
	
	
	
	
	public NeptuneGameResult assembleInformation() {
		
		NeptuneGameResult g = new NeptuneGameResult(this.name);
		long totalSeconds = this.counter.endTime - this.counter.startTime;
		g.setGameTime(totalSeconds);
		
		for (int i = 0; i < this.fieldTypes.size(); i++) {
			String type = this.fieldTypes.get(i);
			g.addFieldTypesAndPoints(type, this.counter.getPointsForFieldType(type));
		}
		
		this.games.add(g);
		
		
		return g;
		
	}
	
	
	public void clearAll() {
		this.counter = null;
		System.gc();
		this.counter = new DefaultGameCounter();
		this.fieldTypes.clear();
	}
	
	
	
}

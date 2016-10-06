/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import com.neptune.model.GameFieldType;
import com.neptune.model.NeptuneActionListener;
import com.neptune.model.NeptuneGameField;
import com.neptune.model.NeptuneModel;
import com.neptune.server.lib.Log;
import com.neptune.server.lib.NeptuneProtocol;
import com.neptune.server.lib.Util;
import com.neptune.server.usermanager.User;
import com.neptune.server.usermanager.UserManager;

public class NeptuneGameServer implements NeptuneActionListener {

	private NeptuneModel model;
	private int fieldId;
	private int charId;
	private boolean debug;
	private NeptuneServer main;
	
	public NeptuneGameServer(NeptuneServer ns) {
		this.model = null;
		this.main = ns;
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
	}
	
	public void clearAll() {
		this.model = null;
		System.gc();
	}
	
	public boolean createNewCourt(int xsize, int ysize, User[] users, int fieldTypesPerUser) {
		// pre-check
		if (users == null) {
			return false;
		}

		// create new model object
		this.model = new NeptuneModel(xsize, ysize);
		this.model.setThirdPartyGameHandlingEnabled(true);
		this.model.addGameActionListener(this);
		
		// create a court
		
		Color[] colors = this.createColorSession(fieldTypesPerUser * users.length);
		int ccnt = 0;
		ArrayList<GameFieldType> types = new ArrayList<GameFieldType>();
		
		this.fieldId = 0;
		this.charId = 0;
		
		for (int i = 0; i < users.length; i++) {
			
			users[i].clearAll();
			
			for (int j = 0; j < fieldTypesPerUser; j++) {
				String fieldType = this.createFieldId();
				
				types.add(new GameFieldType(fieldType, colors[ccnt]));
				ccnt++;
				
				users[i].addFieldType(fieldType);
				
				
			}
			
			
			
		}

		GameFieldType[] gfts = new GameFieldType[types.size()];
		types.toArray(gfts);
		// set the field types
		this.model.setFieldTypes(gfts);
		
		// create the court
		this.model.createRandomGameCourt();
		return true;
	}
	
	public void userQuitted(String[] fieldTypes) {
		this.model.removeFieldsByType(fieldTypes);
	}
	
	
	public String getFieldAndColorsString() {
		GameFieldType[] gfts = this.model.getFieldTypes();
		String result = "";
		
		for (int i = 0; i < gfts.length; i++) {
			
			try {
				result += Util.replaceFlags(new String[][]{
						{"%typ%", gfts[i].getId()},
						{"%r%", gfts[i].getFieldColor().getRed() + ""},
						{"%g%", gfts[i].getFieldColor().getGreen() + ""},
						{"%b%", gfts[i].getFieldColor().getBlue() + ""}
				}, NeptuneProtocol.COLOR_FIELD);
			} catch (Exception e) {
				// debug
				Log.error("NullPointer: a color is null (" + e.toString() + ")", debug);
			}
			
			if (i < gfts.length - 1) {
				result += NeptuneProtocol.FIELD_SEPARATOR;
			}
			
		}
		
		return result;
	}
	
	
	public String getEnemiesString() {
		String result = "";
		
		User[] usrs = this.main.getUM().getAllUsers();
		if (usrs == null) {
			return null;
		}
		
		for (int i = 0; i < usrs.length; i++) {
			
			try {
				result += Util.replaceFlags(new String[][]{
						{"%usr%", usrs[i].getName()},
						{"%csv%", usrs[i].getFieldTypes()}
				}, NeptuneProtocol.ENEMY_FIELD);
			} catch (Exception e) {
				// debug
				Log.error("NullPointer: the user is null (" + e.toString() + ")", debug);
			}
			
			
			if (i < usrs.length - 1) {
				result += NeptuneProtocol.FIELD_SEPARATOR;
			}
		}
		
		return result;
	}
	
	public String[][] getCourtAsArray() {
		String[][] court = new String[this.model.getGameField().getYFields()][this.model.getGameField().getXFields()];
		
		for (int i = 0; i < court.length; i++) {
			for (int j = 0; j < court[i].length; j++) {
				NeptuneGameField ngf = this.model.getGameField().getField(j, i);
				
				if (ngf == null) {
					court[i][j] = "-";
				} else {
					court[i][j] = ngf.getType();
				}
				
			}
		}
		
		return court;
	}
	
	public String getCourtAsString() {
		String court = "";
		
		if (this.model == null) {
			return null;
		}
		
		int x = this.model.getGameField().getXFields();
		int y = this.model.getGameField().getYFields();
		
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				NeptuneGameField ngf = this.model.getGameField().getField(j, i);
				
				if (ngf == null) {
					court += NeptuneProtocol.GAME_FIELD_NULL;
				} else {
					court += ngf.getType();
				}
				
				if (j < x - 1) {
					court += NeptuneProtocol.FIELD_SEPARATOR;
				}
				
			}
			court += NeptuneProtocol.LINE_SEPARATOR;
		}
		
		return court;
	}
	
	public Dimension getDimension() {
		return new Dimension(this.model.getCourt().getXFields(), this.model.getCourt().getYFields());
	}
	
	private boolean isAllowedToRemove(String[] userFieldTypes, String fieldType) {
		
		if (userFieldTypes == null || fieldType == null) {
			return false;
		}
		
		for (int i = 0; i < userFieldTypes.length; i++) {
			if (userFieldTypes[i].equals(fieldType)) {
				return true;
			}
		}
		
		return false;
	}
	
	// 1 if some mod for all
	// 0 if some mod for all without this user (this user is finished)
	// -1 no mod
	public boolean removeField(int x, int y, String name) {
		
		// get the manager
		UserManager um = this.main.getUM();
		if (um == null) {
			return false;
		}
		
		// get the user
		User u = um.getUser(name);
		if (u == null) {
			return false;
		}
		
		// check if the user has a permission
		if (!this.isAllowedToRemove(u.getFieldTypesAsArray(), this.model.getFieldType(x, y))) {
			// debug
			Log.info("User '" + name + "' tried to access a not allowed field", debug);
			return false;
		}
		
		
		//this.model.
		if (this.model.userNotifiedFieldRemove(x, y)) {
			
//			User usr = this.main.getUM().getUser(name);
//			
//			if (usr == null) {
//				return false;
//			}
//			
//			String[] types = usr.getFieldTypesAsArray();
//			int anyOthers = 0;
//			
//			for (int i = 0; i < types.length; i++) {
//			
//				if (this.model.anyOtherClickOptionsForPlayer(types[i])) {
//					anyOthers++;
//				}
//				
//			}
//			
//			if (anyOthers < 1) {
//			//	this.gameEnd(name, types);
//				return 0;
//			}
			
			return true;
		}
		
		return false;
	}
	
	
	public boolean anyCombinationsLeft() {
		return this.anyBodyOtherCanPlay();
	}
	
	
	private boolean anyBodyOtherCanPlay() {
		User[] usr = this.main.getUM().getAllUsers();
		
		int totalUsersCan = 0;
		
		for (int i = 0; i < usr.length; i++) {
			
			if (usr[i] == null) {
				return true;
			}
			
			String[] types = usr[i].getFieldTypesAsArray();
			int anyOthers = 0;
			
			for (int s = 0; s < types.length; s++) {
			
				if (this.model.anyOtherClickOptionsForPlayer(types[s])) {
					anyOthers++;
				}
				
			}
			
			//System.err.println(usr[i].getName() + " can play? " + anyOthers);
			
			if (anyOthers > 0) {
				totalUsersCan++;
			}
			
			
		}

		
		if (totalUsersCan < 1) {
			return false;
		}
		
		return true;
	}
	
	
	
	


	public void actionArrangementDone(int fields, ArrayList<int[]> coords, String type) {
		
		// get the user
		User[] usrs = this.main.getUM().getAllUsers();
		User usr = null;
		for (int i = 0; i < usrs.length; i++) {
			
			if (Util.contains(usrs[i].getFieldTypesAsArray(), type)) {
				usr = usrs[i];
				break;
			}
			
			
		}
		
		if (usr == null) {
			return;
		}
		
		if (usr.getCounter().startTime == -1) {
			usr.getCounter().startTime = System.currentTimeMillis();
		}
		
		usr.getCounter().fieldsRemoved(type, fields);
		
		
		//System.out.println("ARRANGEMENT: " + fields + ", TYPE=" + type);
		
	}



//	public void gameEnd(String name, String[] type) {
//		
//		this.main.getUM().getUser(name).getCounter().endTime = System.currentTimeMillis();
//		
//		System.out.println("GAME END: " + name + "TYPE=" + type[0]);
//		
//		User usr = this.main.getUM().getUser(name);
//		
//		this.main.getServer().send(usr.getIp(), usr.getPort(), "+OK Game finished.");
//		
//		this.main.gameRunning = false;
//		
//	}

	public void actionGameEnd() {
		// call alias method
		this.main.gameFinishedForAll();
	}
	
	
	
	public void actionClickPositonOutOfBox() {
		// ignore
	}
	
	
	private Color[] createColorSession(int length) {
		ArrayList<Color> clrs = new ArrayList<Color>();
		clrs.add(new Color(215, 20, 14));
		clrs.add(new Color(255, 0, 108));
		clrs.add(new Color(57, 122, 176));
		clrs.add(new Color(112, 159, 47));
		clrs.add(new Color(72, 206, 218));
		clrs.add(new Color(249, 106, 2));
		clrs.add(new Color(185, 64, 167));
		clrs.add(new Color(217, 218, 26));
		
		// weather more colors are neeeded
		if (length > 8) {
			for (int i = 0; i < (length-8); i++) {
				int r = (int) Math.round(Math.random()*255);
				int g = (int) Math.round(Math.random()*255);
				int b = (int) Math.round(Math.random()*255);
				Color temp = new Color(
						(r > 255 ? 255 : r),
						(g > 255 ? 255 : g),
						(b > 255 ? 255 : b)
				);
				clrs.add(temp);
			}
		}
		
		Color[] res = new Color[clrs.size()];
		clrs.toArray(res);
		return res;
	}
	
	private String createFieldId() {
		String fieldId = "";
		int chars = (this.fieldId/26)+1;
		
		for (int i = 0; i < chars; i++) {
			fieldId += (char)(65+this.charId);
		}
		
		this.fieldId++;
		if (this.charId > 24) {
			this.charId = 0;
		} else {
			this.charId++;
		}
		
		return fieldId;
	}
	
}

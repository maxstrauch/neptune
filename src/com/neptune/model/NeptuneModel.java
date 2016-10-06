/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.model;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;


public class NeptuneModel {

	public final static int SHIFT_LEFT = 1;
	public final static int SHIFT_RIGHT = 2;
	public final static Color DEFAULT_COLOR = Color.YELLOW;
	
	private NeptuneActionListener listener;
	private NeptuneGameCourt court;
	private DefaultCountProfile counter;
	
	private int fieldSize;
	
	private GameFieldType[] fieldTypes;
	
	private boolean thirdPartyGameHandling;
	private int shiftDirection;

	
	
	
	public NeptuneModel(int x, int y) {
		// create court
		this.court = new NeptuneGameCourt(x, y);
		//this.counter = new DefaultGameCounter();
		
		// definitions
		this.shiftDirection = NeptuneModel.SHIFT_RIGHT;
		this.setFieldSize(30);
		this.setThirdPartyGameHandlingEnabled(false);
		this.fireGameActionListener();
		//this.counter = new DefaultGameCounter();
	}
	
	public NeptuneModel(int x, int y, int size) {
		// call the constructor
		this(x, y);		
		
		// definitions
		this.setFieldSize(size);
	}

	
	// private


	public void removeFieldsByType(String[] fieldTypes) {
		int x = this.court.getXFields();
		int y = this.court.getYFields();
		
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				String type = (this.court.getField(j, i) == null ? null : this.court.getField(j, i).getType());
				if (type != null) {
					if (this.isIn(type, fieldTypes)) {
						this.court.setField(null, j, i);
					}
				}
			}
		}
		
		// rearrange the Field
		this.reArrange();
	}
	
	private boolean isIn(String type, String[] fieldTypes) {
		for (int i = 0; i < fieldTypes.length; i++) {
			if (fieldTypes[i].equals(type)) {
				return true;
			}
		}
		return false;
	}
	
	private void reArrange() {
		
		// create a fake gravity and repeat it for
		// every column
		for (int i = 0; i < this.court.getXFields(); i++) {
			// create new temp columns
			NeptuneGameField[] column = new NeptuneGameField[this.court.getYFields()];
			NeptuneGameField[] columnReArranged = new NeptuneGameField[this.court.getYFields()];
			
			// save the column
			for (int j = 0; j < this.court.getYFields(); j++) {
				column[j] = this.court.getField(i, j);				
			}
			
			// shift all fields to the right
			int j = column.length-1;
			int reArrCnt = column.length-1;
			while (j > -1) {
				if (column[j] != null && !column[j].isNull()) {
					columnReArranged[reArrCnt] = column[j];
					reArrCnt--;
				}
				j--;
			}
			
			// set the new column
			for (int k = 0; k < columnReArranged.length; k++) {
				this.court.setField(columnReArranged[k], i, k);
			}
		}
		
		// shift all column to the right hand side if
		// there is an empty column
		for (int i = 0; i < this.court.getXFields(); i++) {
			// check whether there is a complete empty
			// column
			boolean columnIsEmpty = false;
			
			for (int j = 0; j < this.court.getYFields(); j++) {
				if (this.court.getField(i, j) == null ||
						this.court.getField(i, j).isNull()) {
					columnIsEmpty = true;
				} else {
					columnIsEmpty = false;
				}
			}
			
			if (columnIsEmpty) {
				// there is an empty column; shift:
				if (this.shiftDirection == NeptuneModel.SHIFT_RIGHT) {
					// shift to the right hand side
					int targetX = i;
					int fromX = i-1;
					
					while (fromX > -1) {
						for (int j = 0; j < this.court.getYFields(); j++) {
							// set the new position
							this.court.setField(
								this.court.getField(fromX, j), 
								targetX, j);
							// clear the old
							this.court.setField(null, fromX, j);
						}
						targetX--;
						fromX--;
					}
				} else {
					// shift to the left hand side
					int targetX = i;
					int fromX = i+1;
					
					while (fromX < this.court.getXFields()) {
						for (int j = 0; j < this.court.getYFields(); j++) {
							// set the new position
							this.court.setField(
									this.court.getField(fromX, j), 
									targetX, j);
							// clear the old
							this.court.setField(null, fromX, j);
						}
						targetX++;
						fromX++;
					}
				}
					
			}
		}
		// court rearranged
	}
	
	private boolean fieldChoosed(int x, int y) {
		
		int removedFields = 0;
		
		
		// clear the selection and the counter
//		this.selectedArrangement = null;
//		this.selectedArrangementBase = null;
//		this.currentFieldsRemoved = 0;
		
		// remove the field and the arrangement
		ArrayList<int[]> arrangement = new ArrayList<int[]>();
		this.selectedFields(x, y, arrangement, this.court.getField(x, y).getType());
		
		Object[] arrangementArray = arrangement.toArray();
		
//		// set important variables
//		this.selectedArrangement = arrangement;
//		this.selectedArrangementBase = new int[]{x, y};
		
		// arrangement smaller than 2 fields aren't allowed
		if (arrangement.size() < 2) {
			return false;
		}
		
		
		// deactivate the fields
//		this.selectedFieldType = this.court.getField(x, y);
		this.court.getField(x, y).setNull(true);
		for (int i = 0; i < arrangementArray.length; i++) {
			int[] temp = (int[])arrangementArray[i];
			this.court.getField(temp[0], temp[1]).setNull(true);
//			this.currentFieldsRemoved++;
			
			removedFields++;
		}
//		this.totalFieldsRemoved += this.currentFieldsRemoved;	
		
		// count ...
		if (this.counter != null && this.court.getField(x, y) != null) {
			this.counter.fieldsRemoved(this.court.getField(x, y), removedFields);
		}
		
		if (this.listener != null) {
			// call the arrangement-done "event"
			this.listener.actionArrangementDone(removedFields, arrangement, this.court.getField(x, y).getType());
		}
		
		
		
		
		
		// rearrange the Field
		this.reArrange();
		
		return true;
	}
		
	private boolean anyOtherClickOptions() {
		int x = 0;
		int y = 0;
		int length = this.court.getXFields()*this.court.getYFields();
		
		// loop
		while ((x*y) < length) {

			// the fields shouldn't be null and symbolic null
			if (this.court.getField(x, y) != null &&
					!this.court.getField(x, y).isNull()) {
				String type = this.court.getField(x, y).getType();
				
					// check the arrangement types
					if (this.arrangementIsPossible(x, y+1, type) ||
							this.arrangementIsPossible(x, y-1, type) ||
							this.arrangementIsPossible(x+1, y, type) ||
							this.arrangementIsPossible(x-1, y, type)
							) {
						return true;
				}
				
				
				
			}
			
			// take care of the cnt's
			if (x < this.court.getXFields()) {
				x++;
			} else {
				x = 0;
				y++;
			}
		}

		// return false - there aren't any combis
		return false;
	}
	
	
	private boolean arrangementIsPossible(int x, int y, String type) {
		// check ...
		if (this.court.getField(x, y) != null && 
				this.court.getField(x, y).getType().equals(type) &&
				!this.court.getField(x, y).isNull()) {
			return true;
		}
		return false;
	}
	
	private void selectedFields(int x, int y, ArrayList<int[]> list, String type) {
		NeptuneGameField field = null;
		
		if (!this.isInArrayList(list, new int[]{x, y-1})) {
			// x|y-1 is left
			field = this.court.getField(x, y-1);
			if (field != null && field.getType().equals(type)) {
				//System.out.println("LEFT (" + field.getType() + ")");
				//field.setActive(true);
				list.add(new int[]{x, y-1});
				// get into recursion
				this.selectedFields(x, y-1, list, type);
			}
		}
		
		if (!this.isInArrayList(list, new int[]{x, y+1})) {
			// x|y+1 is right
			field = this.court.getField(x, y+1);
			if (field != null && field.getType().equals(type)) {
				//System.out.println("RIGHT (" + field.getType() + ")");
				//field.setActive(true);
				list.add(new int[]{x, y+1});
				// get into recursion
				this.selectedFields(x, y+1, list, type);
			}
		}
		
		if (!this.isInArrayList(list, new int[]{x-1, y})) {
			// x-1|y is top
			field = this.court.getField(x-1, y);
			if (field != null && field.getType().equals(type)) {
				//System.out.println("TOP (" + field.getType() + ")");
				//field.setActive(true);
				list.add(new int[]{x-1, y});
				// get into recursion
				this.selectedFields(x-1, y, list, type);
			}	
		}
		
		if (!this.isInArrayList(list, new int[]{x+1, y})) {
			// x+1|y is bottom
			field = this.court.getField(x+1, y);
			if (field != null && field.getType().equals(type)) {
				//System.out.println("BOTTOM (" + field.getType() + ")");
				//field.setActive(true);
				list.add(new int[]{x+1, y});
				// get into recursion
				this.selectedFields(x+1, y, list, type);
			}
		}
	}
	
	private boolean isInArrayList(ArrayList<int[]> list, int[] koords) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i)[0] == koords[0] &&
					list.get(i)[1] == koords[1]) {
				return true;
			}
		}
		return false;
	}
	
	private boolean idIsAvailable(String id) {
		for (int i = 0; i < this.fieldTypes.length; i++) {
			if (this.fieldTypes[i].getId().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	private Color getColorById(String id) {
		for (int i = 0; i < this.fieldTypes.length; i++) {
			if (this.fieldTypes[i].getId().equals(id)) {
				return this.fieldTypes[i].getFieldColor();
			}
		}
		return NeptuneModel.DEFAULT_COLOR;
	}
	
	//////////////////////////////////////////////////////////////
	
	public void addGameActionListener(NeptuneActionListener listener) {
		this.listener = listener;
	}
	
	public void fireGameActionListener() {
		this.listener = null;
	}
	
	// create
	
	public void createRandomGameCourt(/*int differentFields*/) {
/*
		this.fieldTypes = new GameFieldType[differentFields];
		
		
		String[] fields = new String[differentFields];
		
		for (int i = 0; i < this.fieldTypes.length; i++) {
			fields[i] = (char)(65+i)+"";
		}
		
		for (int i = 0; i < this.fieldTypes.length; i++) {
			int r = (int) Math.round(Math.random()*255);
			int g = (int) Math.round(Math.random()*255);
			int b = (int) Math.round(Math.random()*255);
			Color temp = new Color(
					(r > 255 ? 255 : r),
					(g > 255 ? 255 : g),
					(b > 255 ? 255 : b)
			);
			
			this.fieldTypes[i] = new GameFieldType(fields[i], temp);
			
		}
		

		*/
		
		
		
		Random r = new Random();
		// create a game pane
		for (int i = 0; i < this.court.getYFields(); i++) {
			for (int j = 0; j < this.court.getXFields(); j++) {
				int index = r.nextInt(this.fieldTypes.length);
				
				this.court.setField(new NeptuneGameField(this.fieldTypes[index].getId(),
										this.fieldTypes[index].getFieldColor()), j, i);
				
				System.out.print(this.fieldTypes[index].getId());
			}
			System.out.println();
		}

		
	}
	
	/*
	 * only network use
	 */
	
	
	public boolean userNotifiedFieldRemove(int x, int y) {
		
		// check weather network game is set
		if (!this.thirdPartyGameHandlingEnabled()) {
			//System.out.println("ERR 1");
			return false;
		}


		if (this.court.getField(x, y) == null) {
			// handle the click
			if (this.listener != null) {
				this.listener.actionClickPositonOutOfBox();
			}
			// no repaint
			return false;
		}
		
		// remove fields
		if (!this.fieldChoosed(x, y)) {
			return false;
		}
		
		
		for (int i = 0; i < court.getYFields(); i++) {
			for (int j = 0; j < court.getXFields(); j++) {
				
				NeptuneGameField ngf = court.getField(j, i);
				
				if (ngf != null && !ngf.isNull()) {
					System.out.print(ngf.getType());
				} else {
					System.out.print("-");
				}
				
			}
			System.out.println();
		}
		
		return true;
	}
	
	public boolean anyOtherClickOptionsForPlayer(String forType) {
		
		// check weather network game is set
		if (!this.thirdPartyGameHandlingEnabled()) {
			System.out.println("ERR 1");
			return false;
		}
		
		int x = 0;
		int y = 0;
		int length = this.court.getXFields()*this.court.getYFields();
		
		// loop
		while ((x*y) < length) {

			// the fields shouldn't be null and symbolic null
			if (this.court.getField(x, y) != null &&
					!this.court.getField(x, y).isNull()) {
				String type = this.court.getField(x, y).getType();
				
				if (type.equals(forType)) {
					// check the arrangement types
					if (this.arrangementIsPossible(x, y+1, type) ||
							this.arrangementIsPossible(x, y-1, type) ||
							this.arrangementIsPossible(x+1, y, type) ||
							this.arrangementIsPossible(x-1, y, type)
							) {
						return true;
					}
				}
				
			}
			
			// take care of the cnt's
			if (x < this.court.getXFields()) {
				x++;
			} else {
				x = 0;
				y++;
			}
		}

		// return false - there aren't any combis
		return false;
	}
	
	
	
	
	
	
	
	public void createGameCourt(String[][] court) {
		// check weather network game is set
		if (!this.thirdPartyGameHandlingEnabled()) {
			System.out.println("ERR 1");
			return;
		}
		
		// check the field size
		if (this.court.getYFields() != court.length ||
				this.court.getXFields() != court[0].length) {
			System.out.println("ERR");
			return;
		}
		
		// loop ..
		for (int i = 0; i < court.length; i++) {
			for (int j = 0; j < court[i].length; j++) {
				// check the field and create it if possible
				if (this.idIsAvailable(court[i][j])) {
					this.court.setField(
							new NeptuneGameField(
									court[i][j],
									this.getColorById(court[i][j])
								), 
							j, i
					);
				}
			}
		}
		return;
	}
	
	public boolean updateGameCourt(String[][] court) {
		// check weather network game is set
		if (!this.thirdPartyGameHandlingEnabled()) {
			return false;
		}

		// check the field size
		if (this.court.getYFields() != court.length ||
				this.court.getXFields() != court[0].length) {
			return false;
		}
		
//		System.out.println("============================");
		
		boolean sthChanged = false;


		int x = this.court.getXFields();
		int y = this.court.getYFields();
		
//		System.out.println();
		
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
		
				
//				System.out.print(court[i][j] + " ");

				if (court[i][j].equals("-")) {
					this.court.setField(null, j, i);
				} else {
					
					if (this.idIsAvailable(court[i][j])) {
						sthChanged = true;
						this.court.setField(new NeptuneGameField(
								court[i][j],
								this.getColorById(court[i][j])
							), j, i);
					}
					
				}
				
				/*
				NeptuneGameField ngf = this.court.getField(j, i);
				
				if (court[i][j].equals("-")) {
					// there is no field any longer
					if (ngf != null &&
							!ngf.getType().equals(court[i][j])) {
						// change field ...
						sthChanged = true;
						this.court.setField(null, j, i);
					}
				} else {
					if (ngf != null &&
							!ngf.getType().equals(court[i][j])) {
						// change field ...
						if (this.idIsAvailable(court[i][j])) {
							sthChanged = true;
							this.court.setField(new NeptuneGameField(
									court[i][j],
									this.getColorById(court[i][j])
								), j, i);
						}
					}
				}
				*/
			}
//			System.out.println();
			
		}
		
//		System.out.println("zuuuuuuuuuuuu: ");
		
		// debuuuuuuuuuuuuuuuuuuuuug
		
		
		
		x = this.court.getXFields();
		y = this.court.getYFields();
		
//		System.out.println();
		
		for (int i = 0; i < y; i++) {
			
			for (int j = 0; j < x; j++) {
				
				
				NeptuneGameField ngf = this.court.getField(j, i);
				
				
				if (ngf == null || ngf.isNull()) {
//					System.out.print("- ");
					
				} else {
					
//					System.out.print(ngf.getType() + " ");
					
				}
				
				
				if (ngf == null) {
					if (court[i][j].equals("-")) {
						
					} else {
						System.err.println("UNGLEICH");
						break;
					}
				} else {
					if (court[i][j].equals(ngf.getType())) {
						
					} else {
						System.err.println("UNGLEICH");
						break;
					}
				}
				
			}
//			System.out.println();
			
		}
		
		
		// return if there were any changes ...
		return sthChanged;
	}
	
	
	
	
	// get
	
	public int getFieldSize() {
		return this.fieldSize;
	}
	
	public NeptuneGameCourt getGameField() {
		return this.court;
	}
	
	public int getWidth() {
		return (this.court.getXFields()*this.fieldSize);
	}
	
	public int getHeight() {
		return (this.court.getYFields()*this.fieldSize);
	}
	
	public GameFieldType[] getFieldTypes() {
		return this.fieldTypes;
	}
	
	public NeptuneGameCourt getCourt() {
		return this.court;
	}
	
	public DefaultCountProfile getCounter() {
		return this.counter;
	}
	
	public String getFieldType(int x, int y) {
		try {
			return this.court.getField(x, y).getType();
		} catch (Exception e) {
			return null;
		}
	}
	
	
	
	// set
	
	public void setFieldTypes(GameFieldType[] fieldTypes) {
		this.fieldTypes = fieldTypes;
	}
	
	public void setFieldSize(int size) {
		this.fieldSize = size;
	}
	
	public void setThirdPartyGameHandlingEnabled(boolean enabled) {
		this.thirdPartyGameHandling = enabled;
	}
	
	public void setShiftDirection(int direction) {
		if (direction == NeptuneModel.SHIFT_LEFT ||
				direction == NeptuneModel.SHIFT_RIGHT) {
			this.shiftDirection = direction;
		}
	}
	
	public boolean thirdPartyGameHandlingEnabled() {
		return this.thirdPartyGameHandling;
	}
	
	public boolean removeFields(int x, int y, NeptuneView view) {
		x = (int) x/this.fieldSize;
		y = (int) y/this.fieldSize;

		// listen for wrong click event
		if (this.court.getField(x, y) == null) {
			// handle the click
			if (this.listener != null) {
				this.listener.actionClickPositonOutOfBox();
			}
			// no repaint
			return false;
		}
		// remove fields
		this.fieldChoosed(x, y);
		// listen for the game end event
		if (!this.anyOtherClickOptions() && this.listener != null) {
			// repaint ...
			view.repaint();
			// end of the game ... call the listener for event handling
			this.listener.actionGameEnd();
			return false;
		}
		// no repaint
		return true;
	}

	
	

	
	

}

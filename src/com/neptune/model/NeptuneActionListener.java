/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.model;
import java.util.ArrayList;


public interface NeptuneActionListener {
	
	/**
	 * Is called if all combinations are removed
	 * @param totalFieldsRemoved number of fields removed (total)
	 */
	public void actionGameEnd();
	
	/**
	 * Is called if a combination is done and exec
	 * @param fields number of fields in the combination
	 * @param coords all coords
	 */
	public void actionArrangementDone(int fields, ArrayList<int[]> coords, String type);
	
	/**
	 * Is called if the user clicks out of the game-box
	 */
	public void actionClickPositonOutOfBox();
	
}

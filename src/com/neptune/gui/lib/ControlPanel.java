/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.gui.lib;

import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.border.*;

/**
 * A ControlPanel is a JPanel which has "RiverLayout" as default layout manager.
 * This is the preferred panel for making user interfaces in a simple way.
 * 
 * @author David Ekholm
 * @version 1.0
 * @see RiverLayout
 */
public class ControlPanel extends JPanel implements JComponentHolder {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a plain ControlPanel
	 */
	public ControlPanel() {
		super(new RiverLayout());
	}

	/**
	 * Create a control panel framed with a titled border
	 */
	public ControlPanel(String title) {
		this();
		setTitle(title);
	}

	public void setTitle(String title) {
		setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), title));
	}
}
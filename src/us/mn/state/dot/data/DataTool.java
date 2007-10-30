/*
 * DataExtract
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package us.mn.state.dot.data;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import us.mn.state.dot.data.SystemConfig;

/**
 * @author John3Tim
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DataTool extends JFrame implements Constants {

	/** About dialog box */
	protected About about;

	/** The Configurations that contain information about the systems */
	protected SystemConfig[] configs = null;
	

	public DataTool(String title, SystemConfig[] cfgs, String factLocation){
		super(title);
		configs = cfgs;
		about = new About( this, configs, factLocation );
	}

	/**
	 * Create the Help menu
	 *
	 * @return   The menu
	 */
	public JMenu createHelpMenu() {
		JMenu file = new JMenu( "Help" );
		Action popAbout =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					about.setVisible( true );
				}
			};
		popAbout.putValue( Action.NAME, "About..." );
		file.add( new JMenuItem( popAbout ) );
		return file;
	}
}

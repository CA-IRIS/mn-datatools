/*
 * DataExtract
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * Provides an about dialog for the DataExtract program.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.7 $ $Date: 2005/08/09 18:30:47 $
 */
public class About extends JDialog {

	/**
	 * Creates an About dialog with the specified <code>Frame</code> as it's owner.
	 *
	 * @param frame  The parent <code>Frame</code>.
	 */
	public About( Frame frame, SystemConfig[] configs, String factLocation ) {
		super( frame, "About", true );
		JLabel mndotLogo = new JLabel();
//		URL url = this.getClass().getResource( "/images/dot.gif" );
//		mndotLogo.setIcon( new ImageIcon( url ) );
		JLabel tmcLogo = new JLabel();
//		url = this.getClass().getResource( "/images/tmc.gif" );
//		tmcLogo.setIcon( new ImageIcon( url ) );
		JPanel topPanel = new JPanel();
		topPanel.setBackground( Color.white );
		topPanel.setLayout( new BoxLayout( topPanel, BoxLayout.X_AXIS ) );
		topPanel.add( Box.createHorizontalStrut( 10 ) );
		topPanel.add( mndotLogo );
		topPanel.add( Box.createHorizontalGlue() );
		topPanel.add( tmcLogo );
		topPanel.add( Box.createHorizontalStrut( 10 ) );
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout( new BoxLayout( centerPanel, BoxLayout.Y_AXIS ) );
		centerPanel.add( Box.createVerticalStrut( 10 ) );
		JLabel label = new JLabel("Application: " + frame.getTitle());
		centerPanel.add( label );
		centerPanel.add( Box.createVerticalStrut( 10 ) );
		label = new JLabel("Version: @@version@@");
		centerPanel.add( label );
		for(int i=0; i<configs.length; i++){
			centerPanel.add( Box.createHorizontalStrut( 10 ) );
			label = new JLabel("System " + i + " Configuration Date: " +
					configs[i].getTimeStamp());
			centerPanel.add( label );
			centerPanel.add( Box.createHorizontalStrut( 10 ) );
			label = new JLabel("System " + i + " Configuration URL: " +
					configs[i].getURL().toString());
			centerPanel.add( label );
			centerPanel.add( Box.createHorizontalStrut( 10 ) );
		}
		label = new JLabel("Traffic Data Location: " + factLocation);
		centerPanel.add( label );
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
		panel.add( topPanel );
		panel.add( centerPanel );
		panel.setBorder( BorderFactory.createBevelBorder(
				BevelBorder.LOWERED ) );
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( panel, BorderLayout.CENTER );
		pack();
		this.setSize( this.getPreferredSize() );
	}

}

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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class NetworkView extends JPanel{

	/** List of registered listeners to this stream. */
	private ArrayList listeners = new ArrayList();

	private JTree tree;

	private SystemTree[] branches;
	
	private TreeSelectionModel model;
	
	public NetworkView(SystemTree[] systems) {
		super(new GridLayout(1, 0));
		branches = systems;
		DefaultMutableTreeNode top = new SystemNode(null, null);
		tree = new JTree(top);
		tree.setCellRenderer(new SystemRenderer());
		for(int i=0; i<branches.length; i++){
			top.add(branches[i]);
		}
		model = tree.getSelectionModel();
		model.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		//Listen for double clicks
		tree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent me){
				if(me.getClickCount()==2){
					notifyNodeSelectionListeners();
				}
			}
		});
		JScrollPane treeView = new JScrollPane(tree);
		add(treeView);
		treeView.setMinimumSize(new Dimension(100, 50));
	}

	public void addSystemNodeSelectionListener(SystemNodeSelectionListener l){
		listeners.add(l);
	}

	private void notifyNodeSelectionListeners(){
		Iterator it = listeners.iterator();
		while(it.hasNext()){
			SystemNodeSelectionListener l = (SystemNodeSelectionListener)it.next();
			DefaultMutableTreeNode node =
				(DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (node == null)
				return;
			SystemNode n = (SystemNode)node;
			if(n.isSensor()) {
				l.addNode(n);
			}
		}
	}

	public SystemNode[] getSelectedNodes(){
		TreePath[] paths = tree.getSelectionPaths();
		if(paths == null){
			return null;
		}
		SystemNode[] nodes = new SystemNode[paths.length];
		for(int i=0; i<nodes.length; i++){
			nodes[i] = (SystemNode)(paths[i].getLastPathComponent());
		}
		return nodes;
	}
	
	public SystemNode find(String id){
		if(id == null || id.length()==0) return null;
		if(!Character.isLetter(id.charAt(0))){
			id = "D" + id;
		}
		id = id.toUpperCase();
		String prefix = ""; //default system is RTMC (no prefix)
		if(!(id.charAt(0) == 'D' || id.charAt(0) == 'S')){
			prefix = id.substring(0, 1);
		}
		for(int i=0; i<branches.length; i++){
			if(branches[i].getDetectorPrefix().equals(prefix)){
				return (SystemNode)branches[i].find(id);
			}
		}
		return null;
	}
	
	public void addListSelectionListener(ListSelectionListener l){
		listeners.add(l);
	}
	
	public void removeListSelectionListener(ListSelectionListener l){
		listeners.remove(l);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager
					.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't use system look and feel.");
		}
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("SystemView");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		NetworkView view = null;
		try {
			URL url = new URL("http://data.dot.state.mn.us/dds/tms.xml.gz");
			TmsConfig rtmc = new TmsConfig("RTMC", url);
			url = new URL("http://data.dot.state.mn.us/dds/arterials.xml.gz");
			ArterialConfig arterials = new ArterialConfig("Arterials", url);
			url = new URL("http://data.dot.state.mn.us/tms/tms-rochester.xml.gz");
			TmsConfig roch = new TmsConfig("Rochester", url);
			SystemTree[] systems = new SystemTree[3];
			systems[0] = SystemTree.createTree(rtmc);
			systems[1] = SystemTree.createTree(arterials);
			systems[2] = SystemTree.createTree(roch);
			view = new NetworkView(systems);
			view.setOpaque(true); //content panes must be opaque
			frame.setContentPane(view);
		} catch (Exception e) {

		}
		frame.pack();
		frame.setVisible(true);
		view.find("S12");
		view.find("S13");
		view.find("S14");
		view.find("S16");
	}

	public JTree getTree(){
		return tree;
	}

}
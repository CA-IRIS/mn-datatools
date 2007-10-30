/*
 * DataTools
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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

import java.io.File;
import java.net.URL;
import java.util.Properties;

import us.mn.state.dot.data.extract.DataExtract;
import us.mn.state.dot.data.plot.DataPlot;

/**
 * Main starting point for data tools. Processes command line parameters and
 * forwards them to the appropriate application
 * 
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson </a>
 * @version $Revision: 1.20 $ $Date: 2006/02/09 13:32:28 $
 */
public class Main {

	private final String DATAPLOT = "dataplot";

	private final String DATAEXTRACT = "dataextract";

	/**
	 * Default location where the traffic files can be found when using a local
	 * data factory
	 */
	protected String DEFAULT_DATA_ROOT = File.separator + "traffic";

	protected String dataRoot = DEFAULT_DATA_ROOT;

	protected String server = null;

	protected String application = null;

	/** Creates a new instance of Main */
	public Main (String[] args) {
		setProxy();
		parseArgs(args);
    	launchApp();
    }

	protected void setProxy(){
		Properties p = System.getProperties();
		p.setProperty("proxyHost", "proxy.dot.state.mn.us");
		p.setProperty("proxyPort", "3128");
		p.setProperty("proxySet", "true");
	}
	
	protected void parseArgs(String args[]) {
		server = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-s") && i < (args.length - 1)) {
				server = args[i + 1];
			} else if (args[i].equals("-d") && i < (args.length - 1)) {
				dataRoot = args[i + 1];
			}
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-a") && i < (args.length - 1)) {
				application = args[i + 1].toLowerCase();
				break;
			}
		}
		if (application == null) {
			printHelp();
			System.exit(-1);
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-h")) {
				printHelp();
				System.exit(-1);
			}
		}
	}

	private void printHelp() {
		System.out.println("DataTools Help");
		System.out.println("usage: java -jar datatools-<version>.jar "
				+ "[-a application] [-d dataServer] [-s server]");
		System.out.println();
		System.out.println(
				"  -s : Server name or ip. Required when using a data server.");
		System.out.println(
				"  -a : Application to run.  Options are dataplot and dataextract.");
		System.out.println(
				"  -d : Location ( path ) of local traffic data archives.");
	}

	private void launchApp() {
		try {
			DataFactory factory = null;
			SystemConfig[] cfgs = new SystemConfig[3];
			URL url =
				new URL("http://data.dot.state.mn.us/dds/arterials.xml.gz");
			cfgs[0] = new ArterialConfig("Arterials", url);
			url = 
				new URL("http://data.dot.state.mn.us/dds/tms_config.xml.gz");
			cfgs[1] = new TmsConfig("RTMC", url);
			url =
				new URL("http://data.dot.state.mn.us/dds/tms-rochester.xml.gz");
			cfgs[2] = new TmsConfig("Rochester", url);
			String factLocation = null;
			if (server != null) {
				factory = new HttpDataFactory(server, cfgs);
				factLocation = server;
			} else {
				factory = new LocalDataFactory(dataRoot, cfgs);
				factLocation = dataRoot;
			}
			if (application.equals(DATAPLOT)) {
				new DataPlot(factory, cfgs, factLocation);
			} else if (application.equals(DATAEXTRACT)) {
				new DataExtract(factory, cfgs, factLocation);
			}
		} catch (InstantiationException ie) {
			ie.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		new Main(args);
	}
}
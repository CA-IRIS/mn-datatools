/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

/**
 * LocalDataFactory is a class for retrieving volume and occupancy
 * data from a local (disk) data source.
 *
 * @author Douglas Lau
 */
public final class HttpDataFactory extends DataFactory {

	protected final HashMap dates = new HashMap();

	protected int currentYear = 0;
	
	protected final String baseURL;
	
	/** Create a new Http data factory */
	public HttpDataFactory( String servletLoc, SystemConfig[] cfgs ) {
		super(cfgs);
		baseURL = servletLoc;
	}

	/** Create a hash of dates with valid data */
	public void updateDates(Calendar c) {
		dates.clear();
		InputStream in = null;
		try{
			URL req = new URL( baseURL + "/" + c.get(Calendar.YEAR) );
			in = req.openStream();
			InputStreamReader inReader = new InputStreamReader(in);
			BufferedReader r = new BufferedReader(inReader);
			String date = r.readLine();
			while(date != null){
				dates.put(date, null);
				date = r.readLine();
			}
			currentYear = c.get(Calendar.YEAR);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	protected InputStream getDataStream( Calendar c, String file )
			throws IOException {
		StringBuffer y = new StringBuffer( 4 );
		y.append( c.get( Calendar.YEAR ) );
		while( y.length() < 4 ) y.insert( 0, '0' );
		String loc = baseURL + "/" + createPath(c) + "/" + file;
		try{
			return new URL(loc).openStream();
		}catch(MalformedURLException mue){
			throw new IOException("Invalid URL: " + loc);
		}
	}
	
	/** Create a path for the specified calendar (date) */
	protected String createPath(Calendar c) {
		return c.get(Calendar.YEAR) + "/" + createKey(c);
	}

	/** Check if a date is valid (file exists in the directory) */
	public boolean isAvailable( Calendar c ) {
		if(currentYear != c.get(Calendar.YEAR)){
			updateDates(c);
		}
		return dates.containsKey(createKey(c));
	}

	/** Create a (local) detector data object */
	public DetectorData createDetectorData( String id ) {
		return new Data( id );
	}

	/** Local detector data class */
	protected class Data implements DetectorData {

		/** Detector label */
		protected final String label;

		/** Detector id */
		protected final String id;

		/** Average field length */
		protected float fieldLength;

		/** Create a new local detector data object */
		protected Data( String id ) {
			this.id = id;
			Sensor s = null;
			if(configs != null){
				for(int i=0; i<configs.length; i++){
					s = configs[i].getSensor(id);
					if(s != null) break;
				}
			}
			if(s != null){
				fieldLength = s.getField();
				label = s.getLabel();
			}else{
				fieldLength = DEFAULT_FIELD_LENGTH;
				label = null;
			}
		}

		/** Get the detector label */
		public String getLabel( boolean statName ) {
			return null;
		}

		/** Get the detector label */
		public String getLabel() {
			return label;
		}

		/** Create the filename of the data archive given the extension */
		private String createFileName(String ext){
			String fileName = id + ext;
			if(fileName.startsWith("D")) fileName = fileName.substring(1);
			return fileName;
		}

		/** Get the average field length */
		public float getFieldLength() { return fieldLength; }

		/** Get all volume data for the specified day */
		public float[] getVolumeSet( Calendar c )
			throws IOException
		{
			InputStream in = getDataStream( c, createFileName(".v30") );
			return readVolumeStream(in);
		}

		/** Get all occupancy data for the specified day */
		public float[] getOccupancySet( Calendar c )
			throws IOException
		{
			InputStream in = getDataStream( c, createFileName(".c30") );
			return createOccupancySet(readScanStream(in));
		}

		/** Get all speed data for the specified day */
		public float[] getSpeedSet( Calendar c )
			throws IOException
		{
			InputStream in = getDataStream( c, createFileName(".s30") );
			return readSpeedStream(in);
		}
	}
}

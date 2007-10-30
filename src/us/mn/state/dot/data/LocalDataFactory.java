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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * LocalDataFactory is a class for retrieving volume and occupancy
 * data from a local (disk) data source.
 *
 * @author Douglas Lau
 */
public final class LocalDataFactory extends DataFactory {

	/** Traffic file extension */
	static protected final String EXT = ".traffic";

	/** Path to directory containing traffic data files */
	protected final File path;

	/** Create a new local data factory */
	public LocalDataFactory( String p, SystemConfig[] cfgs ) throws FileNotFoundException {
		super(cfgs);
		if( !p.endsWith( File.separator ) )
			p = p + File.separator;
		path = new File( p );
		if( !path.isDirectory() ) throw
			new FileNotFoundException( "Not directory: " + p );
	}

	/** Create a hash of dates with valid data */
	public HashMap createDateHash() {
		HashMap dates = new HashMap();
		String[] years = path.list();
		for(int y = 0; y < years.length; y++) {
			String year = years[y];
			if(year.length() != 4) continue;
			File p = new File(path, year);
			if(!p.canRead() || !p.isDirectory()) continue;
			String[] list = p.list();
			for(int i = 0; i < list.length; i++) {
				String date = checkTrafficFile(p, list[i]);
				if(date != null) dates.put(date, null);
			}
		}
		return dates;
	}

	/** Check if the given file is a vaild traffic file */
	protected String checkTrafficFile(File p, String n) {
		if(n.length() < 8) return null;
		String date = n.substring(0, 8);
		try { Integer.parseInt(date); }
		catch(NumberFormatException e) { return null; }
		File file = new File(p, n);
		if(!file.canRead()) return null;
		if(n.length() == 8 && file.isDirectory()) return date;
		if(n.length() == 16 && n.endsWith(EXT)) return date;
		return null;
	}

	/** Create a path for the specified calendar (date) */
	protected String createPath(Calendar c) {
		return path.getPath() + File.separator + c.get(Calendar.YEAR) +
			File.separator + createKey(c);
	}

	/** Check if a date is valid (file exists in the directory) */
	public boolean isAvailable( Calendar c ) {
		String dir = createPath(c);
		File file = new File( dir );
		if( file.isDirectory() && file.canRead() ) return true;
		file = new File( dir + EXT );
		if( file.isFile() && file.canRead() ) return true;
		return false;
	}


	/** Get a volume set from a regular file */
	protected float[] getVolumeSetFile( String dir, String file )
		throws IOException
	{
		FileInputStream is = new FileInputStream( dir +
			File.separator + file );
		try { return readVolumeStream(is); }
		finally {
			is.close();
		}
	}

	/** Get a volume set from a zip (traffic) file */
	protected float[] getVolumeSetZip( String dir, String file )
		throws IOException
	{
		ZipFile zip = new ZipFile( dir + EXT );
		try {
			ZipEntry entry = zip.getEntry( file );
			if( entry == null ) throw new
				FileNotFoundException( file );
			InputStream is = zip.getInputStream( entry );
			return readVolumeStream(is);
		}
		finally {
			zip.close();
		}
	}

	/** Get 24 hours of volume data for one detector */
	public float[] getRawVolumeSet( Calendar c, String id )
		throws IOException
	{
		String dir = createPath(c);
		String file = id + ".v30";
		try { return getVolumeSetFile( dir, file ); }
		catch( IOException e ) {
			return getVolumeSetZip( dir, file );
		}
	}


	/** Get a scan set from a regular file */
	protected float[] getScanSetFile( String dir, String file )
		throws IOException
	{
		FileInputStream is = new FileInputStream( dir +
			File.separator + file );
		try { return readScanStream(is); }
		finally {
			is.close();
		}
	}

	/** Get a scan set from a zip (traffic) file */
	protected float[] getScanSetZip( String dir, String file )
		throws IOException
	{
		ZipFile zip = new ZipFile( dir + EXT );
		try {
			ZipEntry entry = zip.getEntry( file );
			if( entry == null ) throw new
				FileNotFoundException( file );
			InputStream is = zip.getInputStream( entry );
			return readScanStream(is);
		}
		finally {
			zip.close();
		}
	}

	/** Get 24 hours of scan data for one detector */
	public float[] getRawScanSet( Calendar c, String id )
		throws IOException
	{
		String dir = createPath(c);
		String file = id + ".c30";
		try { return getScanSetFile( dir, file ); }
		catch( IOException e ) {
			return getScanSetZip( dir, file );
		}
	}

	/** Parse an occupancy set from an input stream */
	protected float[] parseOccupancySet( InputStream is )
		throws IOException
	{
		DataInputStream dis = new DataInputStream( is );
		float[] set = new float[ SAMPLES_PER_DAY ];
		for( int r = 0; r < SAMPLES_PER_DAY; r++ ) {
			int occ = dis.readShort();
			if( occ >= 0 ) set[ r ] = occ / 10.0f;
			else set[ r ] = MISSING_DATA;
		}
		return set;
	}

	/** Get 24 hours of occupancy data for one detector */
	public float[] getRawOccupancySet( Calendar c, String id )
		throws IOException
	{
		try { return createOccupancySet( getRawScanSet( c, id ) ); }
		catch( IOException e ) {}
		String dir = createPath(c);
		String file = id + ".o30";
		ZipFile zip = new ZipFile( dir + EXT );
		try {
			ZipEntry entry = zip.getEntry( file );
			if( entry == null ) throw new
				FileNotFoundException( file );
			InputStream is = zip.getInputStream( entry );
			return parseOccupancySet( is );
		}
		finally {
			zip.close();
		}
	}

	/** Get a speed set from a regular file */
	protected float[] getSpeedSetFile( String dir, String file )
		throws IOException
	{
		FileInputStream is = new FileInputStream( dir +
			File.separator + file );
		try { return readSpeedStream(is); }
		finally {
			is.close();
		}
	}

	/** Get a speed set from a zip (traffic) file */
	protected float[] getSpeedSetZip( String dir, String file )
		throws IOException
	{
		ZipFile zip = new ZipFile( dir + EXT );
		try {
			ZipEntry entry = zip.getEntry( file );
			if( entry == null ) throw new
				FileNotFoundException( file );
			InputStream is = zip.getInputStream( entry );
			return readSpeedStream(is);
		}
		finally {
			zip.close();
		}
	}

	/** Get 24 hours of speed data for one detector */
	public float[] getRawSpeedSet( Calendar c, String id )
		throws IOException
	{
		String dir = createPath(c);
		String file = id + ".s30";
		try { return getSpeedSetFile( dir, file ); }
		catch( IOException e ) {
			return getSpeedSetZip( dir, file );
		}
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
			fieldLength = DEFAULT_FIELD_LENGTH;
			label = null;
		}

		/** Get the detector label */
		public String getLabel() {
			return label;
		}

		/** Get the average field length */
		public float getFieldLength() { return fieldLength; }

		/** Get all volume data for the specified day */
		public float[] getVolumeSet( Calendar c )
			throws IOException
		{
			return getRawVolumeSet( c, id );
		}

		/** Get all occupancy data for the specified day */
		public float[] getOccupancySet( Calendar c )
			throws IOException
		{
			return getRawOccupancySet( c, id );
		}
		/** Get all speed data for the specified day */
		public float[] getSpeedSet( Calendar c )
			throws IOException
		{
			return getRawSpeedSet( c, id );
		}
	}
}

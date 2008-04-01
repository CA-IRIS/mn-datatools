/*
 * DataExtract
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
 */
package us.mn.state.dot.data;

import java.net.URL;
import java.util.Hashtable;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;

/**
 * @author john3tim
 *
 */
public abstract class SystemConfig extends XmlParser {

	protected final Hashtable<String, Sensor> sensors =
		new Hashtable<String, Sensor>();

	private String timeStamp;

	protected final String name;
	protected final Element system;
	protected String detectorPrefix = "";

	public SystemConfig(String name, URL url)
		throws ParserConfigurationException
	{
		super(url);
		this.name = name;
		system = document.getDocumentElement();
		String pre = system.getAttribute("detector_prefix");
		if(pre != null && pre.length() > 0)
			detectorPrefix = pre;
		timeStamp = system.getAttribute("time_stamp");
		if(timeStamp == null)
			timeStamp = "not available";
	}

	public Sensor getSensor(String id) {
		return (Sensor)sensors.get(id);
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public String getStationLabel(int stationId) {
		return null;
	}

	public String getName() {
		return name;
	}

	public String getDetectorPrefix() {
		return detectorPrefix;
	}
}

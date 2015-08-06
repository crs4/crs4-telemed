/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

package it.crs4.most.ehrlib.example.models;

public class Patient {
	
	private String uuid = null;
	private String demographicUuid = null;
	private String ehrUuid = null;
	
	
    public Patient(String uuid, String demographicUuid, String ehrUuid)
    {
    	this.uuid = uuid;
    	this.demographicUuid = demographicUuid;
    	this.ehrUuid = ehrUuid;
    }


	public String getUuid() {
		return uuid;
	}


	public String getDemographicUuid() {
		return demographicUuid;
	}


	public String getEhrUuid() {
		return ehrUuid;
	}
}

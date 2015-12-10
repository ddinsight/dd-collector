/*
* Copyright 2015 AirPlug Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package io.ddinsight;

class APEvent {
	private String eventUrl;

	final long eventId;
	final int screenWidth;
	final int screenHeight;
	final String pkgName;
	final String eventType;
	final String logType;
	final int versionCode;

	private String key;
	private String value;
	private int randomVal;
	private int tmFirst;
	private int tmPrevious;
	private int tmCurrent;
	private int visits;
	private int userId;
	private int nTotalLogEvents;
	private int nStoredLogs;
	private int syncId;
	private String faultKey;
	private String faultVal;

	Object eData;

	APEvent(long eventID, String pkgName, String evntType, String logType, int versionCode, String key, String value, Object eData, int screenH, int screenW) {
		this(eventID, -1, -1, -1, -1, -1, pkgName, evntType, logType, versionCode, key, value, eData, screenH, screenW);
	}

	APEvent(long eventID, int randomVal, int tmFirst, int tmPrevious, int tmCurrent, int visits, String pkgName, String eventType, String logType, int versionCode, String key, String value, Object eData, int screenH, int screenW) {
		this.eventId = eventID;
		this.randomVal = randomVal;
		this.tmFirst = tmFirst;
		this.tmPrevious = tmPrevious;
		this.tmCurrent = tmCurrent;
		this.visits = visits;
		this.pkgName = pkgName;
		this.eventType = eventType;
		this.logType = logType;
		this.versionCode = versionCode;
		this.key = key;
		this.value = value;
		this.eData = eData;
		this.screenHeight = screenH;
		this.screenWidth = screenW;
		this.userId = -1;
		this.faultKey = "";
		this.faultVal = "";
	}

	public String getFaultKey() {
		return faultKey;
	}

	public void setFaultKey(String faultKey) {
		this.faultKey = faultKey;
	}

	public String getFaultVal() {
		return faultVal;
	}

	public void setFaultVal(String faultVal) {
		this.faultVal = faultVal;
	}

	void setRandomVal(int randomVal)
	{
		this.randomVal = randomVal;
	}
	
	int getRandomVal()
	{
		return this.randomVal;
	}
	
	String getPkgName()
	{
		return this.pkgName;
	}
	
	String getKey()
	{
		return this.key;
	}
	
	String getValue()
	{
		return this.value;
	}
	
	int getVersionCode()
	{
		return this.versionCode;
	}
	
	void setSyncId(int syncId)
	{
		this.syncId = syncId;
	}
	
	int getSyncId()
	{
		return this.syncId;
	}

	void setTimestampFirst(int paramInt)
	{
		this.tmFirst = paramInt;
	}

	int getTimestampFirst()
	{
		return this.tmFirst;
	}

	void setTimestampPrevious(int paramInt)
	{
		this.tmPrevious = paramInt;
	}

	int getTimestampPrevious()
	{
		return this.tmPrevious;
	}

	void setTimestampCurrent(int paramInt)
	{
		this.tmCurrent = paramInt;
	}
	
	int getTimestampCurrent()
	{
		return this.tmCurrent;
	}

	void setVisits(int paramInt)
	{
		this.visits = paramInt;
	}

	int getVisits()
	{
		return this.visits;
	}

	void setUserId(int paramInt)
	{
		this.userId = paramInt;
	}

	int getUserId()
	{
		return this.userId;
	}
	
	public void setNumTotalLogs(int totalLogs)
	{
		this.nTotalLogEvents = totalLogs;
	}
	
	public int getNumTotalLogs()
	{
		return this.nTotalLogEvents;
	}
	
	public void setNumStoredLogs(int num)
	{
		this.nStoredLogs = num;
	}
	
	public int getNumStoredLogs()
	{
		return this.nStoredLogs;
	}

	public void setUrl(String pkgName, EventType eventType, String logType, int versionCode)
	{
		try{
			this.eventUrl = "/" + logType +
							"/" + eventType.eventType +
							"/" + pkgName + 
							"/" + versionCode;
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String getEventUrl()
	{
		return this.eventUrl;
	}

	public Object getExtData() {
		return this.eData;
	}

	public boolean isSessionInitialized()
	{
		return this.tmFirst != -1;
	}

	public String toString() {
		return "id:" + this.eventId + " " + "key:" + this.key + "value:" + this.value + " "
				+ "category:" + this.pkgName + " " + "action:" + this.eventType
				+ " " + "label:" + this.logType + " " + "width:"
				+ this.screenWidth + " " + "height:" + this.screenHeight;
	}
}

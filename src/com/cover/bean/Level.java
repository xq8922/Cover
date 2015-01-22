package com.cover.bean;

public class Level extends Entity {
	private String reportTime = null;
	private String alarmFrequency = null;
	
	public String getReportTime() {
		return reportTime;
	}

	public void setReportTime(String reportTime) {
		this.reportTime = reportTime;
	}

	public String getAlarmFrequency() {
		return alarmFrequency;
	}

	public void setAlarmFrequency(String alarmFrequency) {
		this.alarmFrequency = alarmFrequency;
	}

	public Level() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Level(String id,String name, Status status, String tag, String longtitude,
			String latitude) {
		super(id,name, status, tag, longtitude, latitude);
		// TODO Auto-generated constructor stub
	}

}

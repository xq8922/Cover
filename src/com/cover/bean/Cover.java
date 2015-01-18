package com.cover.bean;

public class Cover extends Msg{
	private String alarmAngle = null;
	private String reportTime = null;
	private String alarmFrequency = null;

	public String getAlarmAngle() {
		return alarmAngle;
	}

	public void setAlarmAngle(String alarmAngle) {
		this.alarmAngle = alarmAngle;
	}

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

	public Cover() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Cover(String id,String name, Status status, String tag, String longtitude,
			String latitude) {
		super(id,name, status, tag, longtitude, latitude);
		// TODO Auto-generated constructor stub
	}

}

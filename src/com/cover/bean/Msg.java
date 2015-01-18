package com.cover.bean;

/**
 * @NORMAL 正常
 * @Repair 维修状态
 * @Exception 1,2,3分别代表两种的异常状态
 * @author W
 *
 */
enum Status{
	NORMAL,REPAIR,EXCEPTION_1,EXCEPTION_2,EXCEPTION_3
}
/**
 * @TAG identify Message is Cover or Level
 * @author W
 *
 */
public class Msg {	
	String id = null;
	String name = null;
	Status status = null; 
	String tag = null;
	String longtitude = null;
	String latitude = null;
	public Msg(String id,String name, Status status, String tag, String longtitude,
			String latitude) {
		super();
		this.id = id;
		this.name = name;
		this.status = status;
		this.tag = tag;
		this.longtitude = longtitude;
		this.latitude = latitude;
	}
	public String getId() {
		return id;
	}
	public Msg() {
		super();
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public String getTag() {
		return tag;
	}
	public String getLongtitude() {
		return longtitude;
	}
	public String getLatitude() {
		return latitude;
	}
	
	
}

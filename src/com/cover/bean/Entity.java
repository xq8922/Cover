package com.cover.bean;

import java.io.Serializable;

/**
 * @TAG identify Message is Cover or Level
 * @author W
 * 
 */
public class Entity implements Serializable {

	private static final long serialVersionUID = -6348868576723913291L;
	short id = 0;
	String name = null;
	Status status = null;
	String tag = null;
	double longtitude = 0;
	double latitude = 0;

	public Entity(short id, String name, Status status, String tag,
			double longtitude, double latitude) {
		super();
		this.id = id;
		this.name = name;
		this.status = status;
		this.tag = tag;
		this.longtitude = longtitude;
		this.latitude = latitude;
	}

	public short getId() {
		return id;
	}

	public Entity() {
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

	public double getLongtitude() {
		return longtitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setId(short id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @NORMAL 正常
	 * @Repair 维修状�??
	 * @Exception 1,2,3分别代表两种的异常状�?
	 * @author W
	 * 
	 */
	public enum Status {
		NORMAL, REPAIR, EXCEPTION_1, EXCEPTION_2, EXCEPTION_3
	}

}

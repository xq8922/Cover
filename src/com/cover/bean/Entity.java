package com.cover.bean;

/**
 * @NORMAL 正常
 * @Repair 维修状�??
 * @Exception 1,2,3分别代表两种的异常状�?
 * @author W
 * 
 */
enum Status {
	NORMAL, REPAIR, EXCEPTION_1, EXCEPTION_2, EXCEPTION_3
}

/**
 * @TAG identify Message is Cover or Level
 * @author W
 * 
 */
public class Entity {
	String id = null;
	String name = null;
	Status status = null;
	String tag = null;
	String longtitude = null;
	String latitude = null;

	public Entity(String id, String name, Status status, String tag,
			String longtitude, String latitude) {
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

	public String getLongtitude() {
		return longtitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setLongtitude(String longtitude) {
		this.longtitude = longtitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

}

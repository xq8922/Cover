package com.cover.dbhelper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;

public class Douyatech {

	private static final String TAG = "Devicedb";
	private DouYaSqliteHelper helper;

	public Douyatech(Context context) {
		helper = new DouYaSqliteHelper(context);
	}

	/**
	 * 将List<Entity>的所有项添加到数据库--这里不用
	 * 
	 * @param EntityInfos
	 */
	public void addAll(List<Entity> EntityInfos) {
		for (Entity info : EntityInfos) {
			add(info);
		}
	}

	/**
	 * 判断是否已经存在于数据库中，即是否已经添加过
	 * 
	 * @param name
	 *            设备名称，唯一的，以此为依据
	 * @return
	 */
	public boolean exist(String tag, String id) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("entity", null, "entity_id=? and tag=?",
				new String[] { id, tag }, null, null, null);
		return cursor.moveToNext();
	}

	public void add(Entity entity) {
		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("insert into person (name) values(?)", new
		// Object[]{name});
		ContentValues contentValues = new ContentValues();
		contentValues.put("entity_id", entity.getId());
		int status = -1;
		switch (entity.getStatus()) {
		case NORMAL:
			status = 0;
			break;
		case REPAIR:
			status = 1;
			break;
		case EXCEPTION_1:
			status = 2;
			break;
		case EXCEPTION_2:
			status = 3;
			break;
		case EXCEPTION_3:
			status = 4;
			break;
		case SETTING_FINISH:
			status = 5;
			break;
		case SETTING_PARAM:
			status = 6;
			break;
		}
		contentValues.put("status", status);
		contentValues.put("tag", entity.getTag());
		contentValues.put("lonti", entity.getLongtitude());
		contentValues.put("lati", entity.getLatitude());
		contentValues.put("old_status", entity.getStatus() + "");
		db.insert("entity", null, contentValues);
	}

	/**
	 * 查询所有entity
	 * 
	 * @return List<Entity>
	 */
	public List<Entity> queryAll() {
		List<Entity> infos = new ArrayList<Entity>();
		SQLiteDatabase db = helper.getReadableDatabase();
		// Cursor cursor=db.rawQuery("select * from person where name=?",new
		// String[]{name});
		Cursor cursor = db.query("entity", null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			Entity entity = new Entity();
			entity.setId(cursor.getShort(cursor.getColumnIndex("entity_id")));
			entity.setTag(cursor.getString(cursor.getColumnIndex("tag")));
			int status = cursor.getInt(cursor.getColumnIndex("status"));
			switch (status) {
			case 0:
				entity.setStatus(Status.NORMAL);
				break;
			case 1:
				entity.setStatus(Status.REPAIR);
				break;
			case 2:
				entity.setStatus(Status.EXCEPTION_1);
				break;
			case 3:
				entity.setStatus(Status.EXCEPTION_2);
				break;
			case 4:
				entity.setStatus(Status.EXCEPTION_3);
				break;
			case 5:
				entity.setStatus(Status.SETTING_FINISH);
				break;
			case 6:
				entity.setStatus(Status.SETTING_PARAM);
				break;
			}
			entity.setLatitude(cursor.getDouble(cursor.getColumnIndex("lati")));
			entity.setLongtitude(cursor.getDouble(cursor
					.getColumnIndex("lonti")));
			infos.add(entity);
		}
		return infos;
	}

	/**
	 * 根据经纬度变化更新位置
	 * 
	 * @param tag
	 * @param id
	 * @param lonti
	 * @param lati
	 */
	public void updateLatLon(String tag, short id, double lonti, double lati) {
		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("update person set lonti=? where name=?", new
		// Object[]{newname,oldname});
		ContentValues contentValues = new ContentValues();
		contentValues.put("lonti", lonti);
		contentValues.put("lati", lati);
		db.update("entity", contentValues, "tag=? and entity_id=?",
				new String[] { tag, id + "" });
	}

	/**
	 * 根据报警信息更新状态
	 * 
	 * @param entity_id
	 * @param tag
	 * @param status
	 */
	public void updateStatus(short entity_id, String tag, Status status) {
		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("update person set lonti=? where name=?", new
		// Object[]{newname,oldname});
		ContentValues contentValues = new ContentValues();
		int statusInt = -1;
		switch (status) {
		case NORMAL:
			statusInt = 0;
			break;
		case REPAIR:
			statusInt = 1;
			break;
		case EXCEPTION_1:
			statusInt = 2;
			break;
		case EXCEPTION_2:
			statusInt = 3;
			break;
		case EXCEPTION_3:
			statusInt = 4;
			break;
		case SETTING_FINISH:
			statusInt = 5;
			break;
		case SETTING_PARAM:
			statusInt = 6;
			break;
		}
		contentValues.put("status", statusInt);
		contentValues.put("old_status", statusInt);
		db.update("entity", contentValues, "tag=? and entity_id=?",
				new String[] { tag, entity_id + "" });
	}

	/**
	 * 插入
	 * 
	 * @param tableName
	 * @param nameID
	 */
	public void add(String tableName, String nameID) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("name_id", nameID);
		db.insert(tableName, null, contentValues);
	}

	/**
	 * 查询
	 * 
	 * @return List<DeviceInfo>
	 */
	public boolean isExist(String tableName, String nameID) {
		// List<String> infos = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + tableName
				+ " where name_id=?", new String[] { nameID });
		// Cursor cursor=db.query(tableName, null, null, null, null, null,
		// null);
		return cursor.moveToNext();
	}

	public void deleteAll(String tableName) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from " + tableName);
	}

	public void delete(String tableName, String nameID) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from " + tableName + " where name_id=?",
				new Object[] { nameID });
		// db.delete(tableName, "name_id=?", new String[] { nameID });
	}

	/**
	 * 删除单条记录
	 * 
	 * @param devicename
	 */
	public void delete(String devicename) {
		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("delete from person where name=?", new Object[]{name});
		db.delete("deviceinfo", "devicename=?", new String[] { devicename });
	}

}

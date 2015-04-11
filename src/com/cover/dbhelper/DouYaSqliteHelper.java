package com.cover.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DouYaSqliteHelper extends SQLiteOpenHelper {

	public DouYaSqliteHelper(Context context) {
		super(context, "douyatech.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table if not exists leave(_id integer primary key autoincrement,name_id varchar(20))");
		db.execSQL("create table if not exists setting(_id integer primary key autoincrement,name_id varchar(20))");
		db.execSQL("create table if not exists entity(_id integer primary key autoincrement,"
				+ "entity_id varchar(10),status int,tag varchar(10),lonti double,"
				+ "lati double,old_status int)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}

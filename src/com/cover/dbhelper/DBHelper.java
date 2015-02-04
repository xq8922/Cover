package com.cover.dbhelper;

import java.util.ArrayList;
import java.util.List;

import com.cover.bean.Entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper {
	/*
public static final String _ID = "_id";
public static final String NAME = "name";
public static final String URL = "url";
public static final String DESC = "desc";
public static final String DATABASE_NAME = "webBookmarksdata";
public static final String TABLE_NAME = "webmarks";
public static final int VERSION = 1;
public static final String DATATABLE_CREATE =
"create table " + TABLE_NAME+ "(" + _ID + " integer primary key autoincrement , "
+ NAME      + " text not null," + URL + " text not null," + DESC + " text)"; 
private SQLiteDatabase sqldb; 
private MySQLHelper helper;
private Context ctx;
private ContentValues values;
private List<Entity> Link = new ArrayList<Entity>();
public DBHelper(Context context) {
	this.ctx = context;
	}
	class MySQLHelper extends SQLiteOpenHelper {
		public MySQLHelper() {
			// 创建数据库和数据库版本号
			super(ctx, DATABASE_NAME, null, VERSION);
			} 
		// 创建表
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATATABLE_CREATE);
		}
		
	   @Override 
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
		   db.execSQL("drop table if exists " + TABLE_NAME); 
		   onCreate(db);
	   } 
   }  
   // 打开数据库 
   public void open() {
	   helper = new MySQLHelper(); 
	   sqldb = helper.getWritableDatabase();
   } 
   // 关闭数据库
   public void close() {
	   helper.close(); 
   } 
   // 添加记录 
   public int insert(Link link) {
	   values = new ContentValues(); 
	   values.put(NAME, link.getName()); 
	   values.put(URL, link.getUrl()); 
	   values.put(DESC, link.getDesc());
	   return (int) sqldb.insert(TABLE_NAME, "empty", values);
	   // empty是表中没有指向的列时用empty代替
	   } 
   // 删除选中de记录
   public int delete(int id) {
	   String[] whereArgs = {
			   String.valueOf(id)
			   };
	   return sqldb.delete(TABLE_NAME, "_id=?", whereArgs); 
	   } 
   // 修改记录
   public int update(Link link) { 
	   values = new ContentValues();
	   values.put(NAME, link.getName());
	   values.put(URL, link.getUrl());
	   values.put(DESC, link.getDesc());  
	   String[] whereArgs = {
			   String.valueOf(link.getId())
			   };
	   return sqldb.update(TABLE_NAME, values, _ID + "=?", whereArgs); 
	   } 
   }
  // 查询记录
   public List<Link> query() {
	   Cursor c = sqldb.query(TABLE_NAME, null, null, null, null, null, null,     null);
	   List<Link> links = new ArrayList<Link>();
	   for (int i = 0; i < c.getCount(); i++) {  
		   c.moveToPosition(i); 
		   Link link = new Link();   
		   link.setId(c.getInt(c.getColumnIndex(_ID)));  
		   link.setName(c.getString(c.getColumnIndex(NAME)));  
		   link.setUrl(c.getString(c.getColumnIndex(URL)));  
		   link.setDesc(c.getString(c.getColumnIndex(DESC)));  
		   links.add(link);
		   }   
	   return links;
	   }  */
   } 
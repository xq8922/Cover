package com.cover.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.wxq.covers.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class CoverList extends Activity {
	private final String TAG = "cover";
	private static ListView lv_coverlist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cover_list);

		lv_coverlist = (ListView) findViewById(R.id.lv_coverlist_cover);
		// String[] strs = new String[] { "first", "second", "third", "fourth",
		// "fifth" };
		// lv_coverlist.setAdapter(new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, strs));

		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < 10; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("iv_itemstatus", R.drawable.checkbox_selected);
			map.put("tv_itemname", "testname");
			map.put("tv_itemstatus", "teststatus");
			listItem.add(map);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItem,
				R.layout.listview, new String[] {
						"iv_itemstatus", "tv_itemname", "tv_itemstatus" },
				new int[] { R.id.iv_itemstatus, R.id.tv_itemname,
						R.id.tv_itemstatus });
		lv_coverlist.setAdapter(simpleAdapter);
		lv_coverlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				setTitle("u clicked the " + arg2 + " row.");
			}

		});
	}

	public static class CoverListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

		}
	}

	public void sendMessage(String msg, String action) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(action);
		serviceIntent.putExtra("msg", msg);
		sendBroadcast(serviceIntent);
		Log.i(TAG, action + "sned broadcast " + action);
	}

}

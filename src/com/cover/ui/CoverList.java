package com.cover.ui;

import com.wxq.covers.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class CoverList extends Activity {
	private final String TAG = "cover";
	private ListView lv_coverlist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cover_list);
		
		lv_coverlist = (ListView)findViewById(R.id.lv_coverlist_cover);
	}

	public static class CoverListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
//			byte[] byte = 
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

package com.cover.ui;

import com.wxq.covers.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CoverMapList extends Activity {
	private static final String TAG = "cover";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cover_map_list);
	}

	public static class CoverMapListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

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

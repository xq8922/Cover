package com.cover.ui;

import com.cover.bean.Message;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Settings extends Activity {
	private static final String TAG = "cover";
	private final String ACTION = "com.cover.service.InternetService";
	Message askMsg = new Message();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		// ask for result of settings
		// int le+ngth = 7 + msg.length();
		// askMsg.length
		// msgAsk.data = msg.getBytes();
		// msgAsk.function = 0x0c;
		// msgAsk.length = Integer.toHexString(length).getBytes();
		// byte[] checkMsg = new byte[3 + msg.length()];
		// msgAsk.check = CoverUtils.genCRC(checkMsg, checkMsg.length);
		// sendMessage()
	}

	public static class SettingsReceiver extends BroadcastReceiver {

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

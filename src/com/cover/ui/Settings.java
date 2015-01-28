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
import android.widget.Toast;

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
	
	public void sendArgSettings(){
		
	}

	public static class SettingsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] recv = intent.getByteArrayExtra("msg");
			if(recv[0] == 0x05){
				int length = 4;
				if(recv[3] == 0x01){
					Toast.makeText(context, "set success", Toast.LENGTH_LONG).show();
				}else if(recv[3] == 0x02){
					Toast.makeText(context,"set failed",Toast.LENGTH_LONG).show();
				}
				//需要在刷新列表的时候检测是否超限
			}
		}

	}

}

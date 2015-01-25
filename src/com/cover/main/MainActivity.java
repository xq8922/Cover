package com.cover.main;

import com.cover.bean.Message;
import com.cover.service.InternetService;
import com.cover.ui.CoverList;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.os.Bundle;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	final static String TAG = "MainActivity";
	private final String ACTION = "com.cover.service.IntenetService";
	String msg = null;
	String userName;
	String password;
	TextView et_ip;
	EditText et_usr;
	EditText et_pwd;
	static String hostIp;
	Button btn_test;
	Button btn_ping;
	static byte[] recv;
	Message msgAsk = new Message();

	InternetService internetService;
	public ServiceConnection internetServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			internetService = ((InternetService.InterBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			internetService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (!CoverUtils.isNetworkAvailable(MainActivity.this)) {
			Toast.makeText(getApplicationContext(), "Network is offline",
					Toast.LENGTH_SHORT).show();
			return;
		}
		bindService(new Intent(MainActivity.this, InternetService.class),
				internetServiceConnection, Context.BIND_AUTO_CREATE);
		// myBroadcast = new MyBroadcast();
		// IntentFilter filter = new IntentFilter();
		// filter.addAction("com.cover.service.InternetService");
		// registerReceiver(myBroadcast,filter);

		hostIp = CoverUtils.getLocalIpAdress();
		et_usr = (EditText) findViewById(R.id.et_login_user_name);
		et_pwd = (EditText) findViewById(R.id.et_login_password);
		btn_test = (Button) findViewById(R.id.btn_login);
		et_usr.setText(hostIp);
		btn_test.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				userName = et_usr.getText().toString();
				password = et_pwd.getText().toString();
				String msg = "userName:" + userName + "password:" + password;
				int length = 7 + msg.length();
				msgAsk.data = msg.getBytes();
				msgAsk.function = 0x0c;
				msgAsk.length = Integer.toHexString(length).getBytes();
				byte[] checkMsg = new byte[3 + msg.length()];
				msgAsk.check = CoverUtils.genCRC(checkMsg, checkMsg.length);
				sendMessage(msgAsk, ACTION);
				System.out.println("test");

				Intent intent = new Intent();
				intent.setClass(MainActivity.this, CoverList.class);
				startActivity(intent);
			}
		});
	}

	public void sendMessage(Message msg, String action) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(action);
		int length = msg.getLength();
		byte[] totalMsg = new byte[length];
		totalMsg = CoverUtils.msg2ByteArray(msg, length);
		serviceIntent.putExtra("msg", totalMsg);
		sendBroadcast(serviceIntent);
		Log.i(TAG, action + "sned broadcast " + action);
	}

	@Override
	protected void onDestroy() {
		unbindService(internetServiceConnection);
		// unregisterReceiver(myBroadcast);
		super.onDestroy();
	}

	public static class MainActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			recv = intent.getByteArrayExtra("msg");
			if (recv[0] == 0x03) {
				switch (recv[1]) {
				case 0x01:
					Intent i = new Intent();
					i.setClass(context, CoverList.class);
					context.startActivity(i);
					break;
				case 0x02:
					Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_LONG)
							.show();
					break;
				case 0x03:
					Toast.makeText(context, "用户已登录", Toast.LENGTH_LONG).show();
					break;
				default:
					Log.w(TAG, "wrong code");
					Toast.makeText(context, "错误指令", Toast.LENGTH_LONG).show();
				}
			}
		}

	}

}

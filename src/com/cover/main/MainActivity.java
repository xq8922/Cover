package com.cover.main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

import com.cover.service.InternetService;
import com.cover.util.CoverUtils;
import com.test.covers.SocketCallback;
import com.test.covers.SocketConnect;
import com.wxq.covers.R;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	final String TAG = "testTCPClient";
	TextView et_ip;
	EditText et_usr;
	EditText et_pwd;
	static String hostIp;
	Button btn_test;
	Button btn_ping;
	static String recv;

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
			Toast.makeText(getApplicationContext(), "鏈繛鎺ョ綉缁�",
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
				// IP = et_usr.getText().toString();
				// PORT = Integer.parseInt(et_pwd.getText().toString());
				// Intent intent = new Intent();
				System.out.println("test");
			}
		});
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
			recv = intent.getStringExtra("msg");
			Log.i("test", recv);
			Toast.makeText(context, recv, Toast.LENGTH_SHORT).show();
		}

	}

}

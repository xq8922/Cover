package com.wxq.covers;

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

import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
	String IP = "219.244.118.30";
	int PORT = 10001;
	TextView et_ip;
	EditText et_usr;
	EditText et_pwd;
	static String hostIp;
	Button btn_test;
	Button btn_ping;
	Socket socket;
	boolean flag = true;
	
	InternetService internetService;
	public ServiceConnection internetServiceConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			internetService = ((InternetService.InterBinder)service).getService();			
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
		
		if(!CoverUtils.isNetworkAvailable(MainActivity.this)){
			Toast.makeText(getApplicationContext(), "未连接网络", Toast.LENGTH_SHORT).show();
			return ;
		}
		bindService(new Intent(MainActivity.this,InternetService.class),internetServiceConnection,Context.BIND_AUTO_CREATE);		
//		new Thread(internetService).start();
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
				IP = "10.175.150.180";
				PORT = 10001;
				/*
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (flag) {
							try {
								socket = new Socket(IP, PORT);
								boolean flag_long = true;
								while (flag_long) {
									if (socket.isConnected()) {
										Toast.makeText(getApplicationContext(),
												"连接成功", Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(getApplicationContext(),
												"连接超时，请重试", Toast.LENGTH_SHORT).show();
										flag = false;
									}
									String message = "Message from Android phone";
									try {
										System.out.println("Client Sending: '"
												+ message + "'");

										// 第二个参数为True则为自动flush
										PrintWriter out = new PrintWriter(
												new BufferedWriter(
														new OutputStreamWriter(
																socket.getOutputStream())),
												true);
										out.println(message);
										// out.flush();
									} catch (Exception e) {
										e.printStackTrace();
									} finally {
										// 关闭Socket
										socket.close();
										System.out
												.println("Client:Socket closed");
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					}
				}).start();
				*/
				// if (socket.isConnected()) {
				// Intent intent = new Intent();
				// intent.setClass(MainActivity.this, Success.class);
				// intent.putExtra("IP", IP);
				// intent.putExtra("PORT", PORT);
				// startActivity(intent);
				// }
				// }
				System.out.println("test");
			}
		});

	}

	@Override
	protected void onDestroy() {
		unbindService(internetServiceConnection);
		super.onDestroy();
	}

	

}

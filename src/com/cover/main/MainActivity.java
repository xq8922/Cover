package com.cover.main;

import java.io.UnsupportedEncodingException;

import com.cover.bean.Message;
import com.cover.service.InternetService;
import com.cover.ui.CoverList;
import com.cover.ui.CoverMapList;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.os.Bundle;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	final static String TAG = "MainActivity";
	private final String ACTION = "com.cover.service.IntenetService";
	String msg = null;
	static byte[] recv;
	Message msgAsk = new Message();
	private EditText et_username;
	private EditText et_password;
	static String userName;
	static String password;
	private ImageView btn_test;
	private SharedPreferences sp;
	private TextView tvChangeIP;
	private static Editor editor;
	private static CheckBox cbIsRemeber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		et_username = (EditText) findViewById(R.id.et_login_user_name);
		et_password = (EditText) findViewById(R.id.et_login_password);
		tvChangeIP = (TextView) findViewById(R.id.tv_changeip);
		cbIsRemeber = (CheckBox) findViewById(R.id.cb_is_remeber);
		sp = getSharedPreferences("douyatech", MODE_PRIVATE);
		editor = sp.edit();
		userName = sp.getString("username", "");
		password = sp.getString("password", "");
		et_username.setText(userName);
		et_password.setText(password);
		cbIsRemeber.setChecked(sp.getBoolean("isremem", false));
		
		if (!CoverUtils.isNetworkAvailable(MainActivity.this)) {
			Toast.makeText(getApplicationContext(), "Network is offline",
					Toast.LENGTH_SHORT).show();
			return;
		}

		tvChangeIP.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final EditText et_Ip = new EditText(MainActivity.this);
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("报警角度")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(et_Ip)
						.setPositiveButton(
								"确定",
								new android.content.DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 保存设置
										// 修改显示
										editor.putString("ip", et_Ip.getText()
												.toString());
										// 本地化 下次从文件读取一下 getString
									}
								}).setNegativeButton("取消", null).show();
			}
		});
		{
			Intent mainActivity = getIntent();
			String ip = mainActivity.getStringExtra("ip");
			if (ip != null) {
				Intent intent = new Intent("com.cover.service.InternetService");
				stopService(intent);
				intent.putExtra("ip", ip);
				startService(intent);
			} else {
				// bindService(new Intent(MainActivity.this,
				// InternetService.class),
				// internetServiceConnection, Context.BIND_AUTO_CREATE);
			}

		}
		btn_test = (ImageView) findViewById(R.id.btn_login);
		btn_test.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// userName = et_usr.getText().toString();
				// password = et_pwd.getText().toString();
				userName = "13468833168";
				password = "1234";
				String msg = userName + password;
				int length = 7 + msg.length();
				msgAsk.data = msg.getBytes();
				msgAsk.function = Integer.valueOf("0C", 16).byteValue();
				msgAsk.length = CoverUtils.short2ByteArray((short) length);
				byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(msgAsk);
				byte[] str_ = CRC16M.getSendBuf(CoverUtils
						.bytes2HexString(checkMsg));
				msgAsk.check[0] = str_[str_.length - 1];
				msgAsk.check[1] = str_[str_.length - 2];
				sendMessage(msgAsk, ACTION);
				System.out.println("test");
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
		// unbindService(internetServiceConnection);
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
//					 i.setClass(context, CoverMapList.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (cbIsRemeber.isChecked()) {
						editor.putString("username", userName);
						editor.putString("password", password);
						editor.putBoolean("isremem", true);
						editor.commit();						
					}
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

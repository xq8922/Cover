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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

	
	public void setNotify(){
		//创建一个NotificationManager的引用
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager)getSystemService(ns);
		// 定义Notification的各种属性
		int icon = R.drawable.icon; //通知图标
		CharSequence tickerText = "Hello"; //状态栏显示的通知文本提示
		long when = System.currentTimeMillis(); //通知产生的时间，会在通知信息里显示
		//用上面的属性初始化 Nofification
		Notification notification = new Notification(icon,tickerText,when);
		/*
		* 添加声音
		* notification.defaults |=Notification.DEFAULT_SOUND;
		* 或者使用以下几种方式
		* notification.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");
		* notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
		* 如果想要让声音持续重复直到用户对通知做出反应，则可以在notification的flags字段增加"FLAG_INSISTENT"
		* 如果notification的defaults字段包括了"DEFAULT_SOUND"属性，则这个属性将覆盖sound字段中定义的声音
		*/
		/*
		* 添加振动
		* notification.defaults |= Notification.DEFAULT_VIBRATE;
		* 或者可以定义自己的振动模式：
		* long[] vibrate = {0,100,200,300}; //0毫秒后开始振动，振动100毫秒后停止，再过200毫秒后再次振动300毫秒
		* notification.vibrate = vibrate;
		* long数组可以定义成想要的任何长度
		* 如果notification的defaults字段包括了"DEFAULT_VIBRATE",则这个属性将覆盖vibrate字段中定义的振动
		*/
		/*
		* 添加LED灯提醒
		* notification.defaults |= Notification.DEFAULT_LIGHTS;
		* 或者可以自己的LED提醒模式:
		* notification.ledARGB = 0xff00ff00;
		* notification.ledOnMS = 300; //亮的时间
		* notification.ledOffMS = 1000; //灭的时间
		* notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		*/
		/*
		* 更多的特征属性
		* notification.flags |= FLAG_AUTO_CANCEL; //在通知栏上点击此通知后自动清除此通知
		* notification.flags |= FLAG_INSISTENT; //重复发出声音，直到用户响应此通知
		* notification.flags |= FLAG_ONGOING_EVENT; //将此通知放到通知栏的"Ongoing"即"正在运行"组中
		* notification.flags |= FLAG_NO_CLEAR; //表明在点击了通知栏中的"清除通知"后，此通知不清除，
		* //经常与FLAG_ONGOING_EVENT一起使用
		* notification.number = 1; //number字段表示此通知代表的当前事件数量，它将覆盖在状态栏图标的顶部
		* //如果要使用此字段，必须从1开始
		* notification.iconLevel = ; //
		*/
		//设置通知的事件消息
		Context context = getApplicationContext(); //上下文
		CharSequence contentTitle = "My Notification"; //通知栏标题
		CharSequence contentText = "Hello World!"; //通知栏内容
		Intent notificationIntent = new Intent(this,CoverList.class); //点击该通知后要跳转的Activity
		PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		//把Notification传递给 NotificationManager
		mNotificationManager.notify(0,notification);
	}
	
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
						.setTitle("更改IP和端口")
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
										String ip_port = et_Ip.getText().toString();
										
										editor.putString("ip", ip_port.substring(0, ip_port.indexOf(":")));
										editor.putString("port", ip_port.substring(ip_port.indexOf(":"), ip_port.length()));
										// 本地化 下次从文件读取一下 getString
//										if()
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
//				setNotify();
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
	public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	//通过MenuInflater将XML 实例化为 Menu Object
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.layout.menu, menu);

	return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.item_exit_settings:
			Message msg = new Message();
			msg.data = userName.getBytes();
			msg.function = (byte)0x12;
			msg.length = CoverUtils.short2ByteArray((short)(7+msg.data.length));
			byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(msg);
			byte[] str_ = CRC16M.getSendBuf(CoverUtils
					.bytes2HexString(checkMsg));
			msg.check[0] = str_[str_.length - 1];
			msg.check[1] = str_[str_.length - 2];
			sendMessage(msg,ACTION);
			this.finish();
		}
		return super.onOptionsItemSelected(item);
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
//					i.put
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

package com.cover.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import com.cover.bean.Message;
import com.cover.ui.CoverList;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

enum functionStatus {
	WARNING, TERMINAL_CHANGED, LOGIN_VALIDATE, DEVICE_REPLAY, TERMINAL_ARGS_REPLAY, ACK_REMOVE_WARNING, ACK_SET_REPAIR_BRGIN, ACK_SET_REPAIR_END
}

public class InternetService extends Service implements Runnable {
	private final static String TAG = "COVER";

	private static final String ACTION_MainActivity = "com.cover.main.mainactivity";
	private static final String ACTION_CoverList = "com.cover.coverlist";
//	private static final String ACTION_CoverMapList = "com.cover.covermaplist";
	private static final String ACTION_Detail = "com.cover.detail";
	private static final String ACTION_Settings = "com.cover.settings";
	private static final String ACTION_SoftwareSettings = "com.cover.softwaresettings";
	// public String ip = "192.168.0.1";
	// public String ip = "219.244.118.168";
	// public String ip = "192.168.0.01";
	public String ip = "124.115.169.98";
	// public String ip = "192.168.100.201";
	// public String ip = "219.245.66.226";
	public int port = 6221;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Binder binder;
	private Thread thread;
	private String workStatus = "test";
	private String currAction;
	static boolean flag_send = false;
	static byte[] msg = null;
	ServiceReceiver myReceiver;
	boolean flagReaderThread = false;
	Message message = new Message();
	
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
		* 添加声音*/
		 notification.defaults |=Notification.DEFAULT_SOUND;		
//		 notification.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");
//		 notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
//		 如果想要让声音持续重复直到用户对通知做出反应，则可以在notification的flags字段增加"FLAG_INSISTENT"
//		* 如果notification的defaults字段包括了"DEFAULT_SOUND"属性，则这个属性将覆盖sound字段中定义的声音
		
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
		* 添加LED灯提醒*/
		notification.defaults |= Notification.DEFAULT_LIGHTS;
//		* 或者可以自己的LED提醒模式:
//		* notification.ledARGB = 0xff00ff00;
//		* notification.ledOnMS = 300; //亮的时间
//		* notification.ledOffMS = 1000; //灭的时间
//		* notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		
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

	public static class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			msg = intent.getByteArrayExtra("msg");
			
			Log.i(TAG, msg.toString());
			// sendMessage(msg);
			Toast.makeText(context, "service accept msg", Toast.LENGTH_LONG).show();
			flag_send = true;
		}

	}

	public void sendRequest(byte[] action) {
		try {
			workStatus = null;
			currAction = action.toString();
			if (flag_send && msg != null) {
				sendMessage(action);
				flag_send = false;
				Log.i(TAG, "msg sent");
			}
		} catch (Exception e) {
			workStatus = "sendmessage fail";
		}
	}

	private void sendMessage(byte[] bs) {
		if (!CoverUtils.isNetworkAvailable(this)) {
			Log.v(TAG, "workStatus is not connectted");
			workStatus = "connect failed";
			Toast.makeText(getApplicationContext(), "network is not connected",
					Toast.LENGTH_LONG).show();
			return;
		}
		if (socket == null) {
			Toast.makeText(getApplicationContext(), "socket is not connected",
					Toast.LENGTH_LONG).show();
			connectService();
		} else {
			if (bs != null) {
				try {
					writer = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!InternetService.this.thread.isAlive()) {
			Toast.makeText(getApplicationContext(), "Thread is not connected",
					Toast.LENGTH_LONG).show();
			(thread = new Thread(InternetService.this)).start();
		}
		if (!socket.isConnected() || socket.isClosed()) {
			Toast.makeText(getApplicationContext(), "socket closed",
					Toast.LENGTH_LONG).show();
			workStatus = "socket not connect";
			Log.v(TAG, "not connect");
			return;
		}
		if (!socket.isOutputShutdown()) {
			try {
				if (msg != null) {
					OutputStream socketWriter = socket.getOutputStream();
					socketWriter.write(msg);
					System.out.println("OK");
					socketWriter.flush();
					Log.i(TAG, "msg sent " + msg.toString());
					if(msg[0] == (byte)0x12){
						this.stopSelf();
						System.exit(0);
					}
					msg = null;
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "output is shutdown",
						Toast.LENGTH_LONG).show();
				Log.v(TAG, "is not connect");
				e.printStackTrace();
				workStatus = "Output err";
			}
		} else {
			workStatus = "OutputShutdown";
		}
	}

	public String getWorkStatus() {
		return workStatus;
	}

	public void closeConnection() {

	}

	public void connectService() {
		try {
			socket = new Socket();
			SocketAddress socAddress = new InetSocketAddress(ip, port);
			socket.connect(socAddress, 3000);
			Log.i(TAG, "socket is connectted");
		} catch (SocketException e) {
			Log.v(TAG, e.toString());
			e.printStackTrace();
			workStatus = e.toString();
			return;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			workStatus = e.toString();
			return;
		}
	}

	public void getMessage(byte[] msg, String action) {
		try {
			Intent serviceIntent = new Intent();
			serviceIntent.setAction(action);
			serviceIntent.putExtra("msg", msg);
			sendBroadcast(serviceIntent);
			Log.i(TAG, "send Broadcast in InternetService && action is "
					+ action);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	public class InterBinder extends Binder {
		public InternetService getService() {
			return InternetService.this;
		}

		/**
		 * this is for Activity send messages to service
		 * 
		 * @data object when send to service
		 * @reply object that service returns
		 */
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}

	}

	@Override
	public void run() {
		try {
			connectService();
			while (true) {
				Thread.sleep(1000);
				if (socket.isConnected()) {
					if (!socket.isInputShutdown()) {
						// sendMessage("hello".getBytes());
						if (!flagReaderThread)
							new Thread(new Reader()).start();
						// socket.
					}
					if (msg != null && flag_send) {
						sendMessage(msg);
						flag_send = false;
						Log.i(TAG, "message send to server .");
					}
				} else {
					connectService();
				}
			}
		} catch (Exception e) {
			try {
				socket.close();
				Log.w(TAG, e.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			workStatus = e.toString();
			e.printStackTrace();
		}
	}

	private class Reader implements Runnable {

		@Override
		public void run() {
			flagReaderThread = true;
			DataInputStream bufferedReader = null;
			try {
				bufferedReader = new DataInputStream(socket.getInputStream());
				// bufferedReader.close();
				Message msg = new Message();
				int size = 0;
				byte[] headerBuff = new byte[2];
				size = bufferedReader.read(headerBuff);

				if (!((headerBuff[0] == (byte) 0xFA) && (headerBuff[1] == (byte) 0xF5))) {
					flagReaderThread = false;
					// bufferedReader;
					return;
				}
				byte[] length = new byte[2];
				size = bufferedReader.read(length);
				byte[] length_temp = new byte[2];
				for (int i = 0; i < length.length; i++)
					length_temp[i] = length[i];
				int msgLength = CoverUtils.getShort(length,true);
				System.out.println(msgLength - 6);
				byte[] msgBuff = new byte[msgLength - 6];
				size = bufferedReader.read(msgBuff);

				byte[] checkBuf = new byte[2];
				size = bufferedReader.read(checkBuf);

				msg.check[0] = checkBuf[0];
				msg.check[1] = checkBuf[1];
				byte[] totalMsg = new byte[msgLength];
				int j = 0;
				totalMsg[j++] = (byte) 0xFA;
				totalMsg[j++] = (byte) 0xF5;
				for (int i = 0; i < length.length; i++) {
					totalMsg[j++] = length_temp[i];
					msg.length[i] = length_temp[i];
				}
				msg.data = new byte[msgBuff.length - 1];
				for (int i = 0, k = 0; i < msgBuff.length; i++) {
					totalMsg[j++] = msgBuff[i];
					if (i == 0)
						msg.function = msgBuff[i];
					else {
						msg.data[k++] = msgBuff[i];
					}
				}
				totalMsg[j++] = checkBuf[0];
				totalMsg[j++] = checkBuf[1];
				// byte[] check =
				// CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils.msg2ByteArrayExcepteCheck(msg)));
				byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(msg);
				byte[] str_ = CRC16M.getSendBuf(CoverUtils
						.bytes2HexString(checkMsg));
				byte[] check_temp = new byte[2];
				check_temp[0] = str_[str_.length - 1];
				check_temp[1] = str_[str_.length - 2];
				boolean f = CRC16M.checkBuf(CoverUtils.msg2ByteArrayExceptHeader(msg));
//				if ((check_temp[0] == msg.check[0])
//						&& (check_temp[1] == msg.check[1]))
				{
					Log.i(TAG, "check right");
					// if
					// (CRC16M.checkBuf(CoverUtils.msg2ByteArrayExceptHeader(msg)))
					// {
					switch (msgBuff[0]) {
					case 0x01: {//需要处理报警信息ack
//						getMessage(msgBuff, ACTION_CoverList);
//						getMessage(msgBuff, ACTION_CoverMapList);
						// when alarm,need alarm						
						break;
					}
					case 0x02: {
//						getMessage(msgBuff, ACTION_CoverMapList);
						NotificationManager m_NotificationManager=(NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
						Notification  m_Notification=new Notification();
						m_Notification.icon=R.drawable.icon;
						//当我们点击通知时显示的内容
						m_Notification.tickerText="Button1 通知内容.....";
						m_Notification.defaults=Notification.DEFAULT_SOUND;
						//设置通知显示的参数
						Intent   m_Intent=new Intent(InternetService.this,CoverList.class);       
						PendingIntent m_PendingIntent=PendingIntent.getActivity(InternetService.this, 0, m_Intent, 0);
						m_Notification.setLatestEventInfo(InternetService.this, "Button1", "Button1通知",m_PendingIntent );
						//这个可以理解为开始执行这个通知
						m_NotificationManager.notify(0,m_Notification);
						break;
					}
					case 0x03: {
						getMessage(msgBuff, ACTION_MainActivity);
						break;
					}
					case 0x04: {
						getMessage(msgBuff, ACTION_CoverList);
//						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					case 0x05: {
						getMessage(msgBuff, ACTION_Settings);
						break;
					}
					case 0x06: {
						getMessage(msgBuff, ACTION_CoverList);
//						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					case 0x07: {
						getMessage(msgBuff, ACTION_CoverList);
//						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					case 0x08: {// ά��״̬�������ý��ճɹ���ACK��Ϣ
						getMessage(msgBuff, ACTION_CoverList);
//						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					default:
						Toast.makeText(getApplicationContext(),
								"������������δ֪����", Toast.LENGTH_LONG).show();
					}
				}
				// }
				// bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			flagReaderThread = false;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		binder = new InterBinder();
		thread = new Thread(InternetService.this);
		thread.start();
		return binder;
	}

	@Override
	public void onCreate() {
		myReceiver = new ServiceReceiver();
		thread = new Thread(InternetService.this);
		thread.start();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
		Log.v("QLQ", "service is on destroy");
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {

		String ip = intent.getStringExtra("ip");
		if(ip!=null){
			this.ip = ip;
		}
		super.onStart(intent, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

}

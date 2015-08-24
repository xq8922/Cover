package com.cover.service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.dbhelper.Douyatech;
import com.cover.ui.Detail;
import com.cover.ui.ParamSettingActivity;
import com.cover.ui.SingleMapDetail;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class InternetService extends Service implements Runnable {
	private final static String TAG = "COVER";
	// private final int INTERVAL = 2 * 60 * 1000;
	private final int INTERVAL = 2 * 60;

	private static final String ACTION_MainActivity = "com.cover.main.mainactivity";
	private static final String ACTION_CoverList = "com.cover.coverlist";
	private static final String ACTION_Detail = "com.cover.detail";
	private static final String ACTION_Settings = "com.cover.settings";
	// private static final String ACTION_Paramsetting = "com.cover.settings";
	private SharedPreferences sp;
	public String ip;
	public int port;
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
	Douyatech douyadb;
	int count = 0;
	int count2 = 0;
	int flag_notify = 0;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x01:
				Toast.makeText(getApplicationContext(), "连接中断请重新登陆",
						Toast.LENGTH_LONG).show();
				break;
			case 0x02:
				// Toast.makeText(getApplicationContext(), "连接中断",
				// Toast.LENGTH_LONG).show();
				break;
			case 0x03:
				// Toast.makeText(getApplicationContext(),
				// "连接中断请重新登陆", Toast.LENGTH_LONG).show();
				break;
			case 0x04:
				// Toast.makeText(getApplicationContext(),
				// "network is not connected", Toast.LENGTH_LONG).show();
				break;
			case 0x05:
				// Toast.makeText(getApplicationContext(),
				// "连接中断", Toast.LENGTH_LONG).show();
				break;
			case 0x06:
				// Toast.makeText(getApplicationContext(), "连接中断",
				// Toast.LENGTH_LONG).show();
				break;
			case 0x07:
				// Toast.makeText(getApplicationContext(), "服务器连接超时",
				// Toast.LENGTH_SHORT).show();
				break;
			case 0x08:
				// Toast.makeText(getApplicationContext(), "连接中断请重新登陆",
				// Toast.LENGTH_LONG).show();
				break;
			case 0x10:
				Toast.makeText(getApplicationContext(), "未知功能码",
						Toast.LENGTH_LONG).show();
				break;
			case 0x11:
				Toast.makeText(getApplicationContext(), "撤防失败命令发送成功",
						Toast.LENGTH_LONG).show();
				break;
			case 0x12:
				Toast.makeText(getApplicationContext(), "参数设置失败命令发送成功",
						Toast.LENGTH_LONG).show();
				break;
			case 0x20:
				Toast.makeText(getApplicationContext(), "网络异常",
						Toast.LENGTH_LONG).show();
				break;
			}
		}

	};
	private int flagConnectionOutOnce = 0;
	private int countNetUnavail = 0;
	private boolean flagKillThread = false;
	private boolean flagOnCreate = false;
	private boolean flagSocketSuccess = false;

	public static class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			msg = intent.getByteArrayExtra("msg");
			Log.i(TAG, msg.toString());
			// Toast.makeText(context, "正在向服务器发送请求", Toast.LENGTH_SHORT).show();
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
		if (!socket.isOutputShutdown()) {
			try {
				if (msg != null) {
					if (msg[4] == (byte) 0x12) {
						flagKillThread = true;
						AppManager.getAppManager().AppExit(
								getApplicationContext());
						this.stopSelf();
					}
					OutputStream socketWriter = socket.getOutputStream();
					socketWriter.write(msg);
					socketWriter.flush();
					Log.i(TAG, "message send success");
					Log.i(TAG, "msg sent " + msg.toString());
					msg = null;
				}
			} catch (Exception e) {

				handler.sendEmptyMessage(0x06);
				Log.v(TAG, "is not connect");
				e.printStackTrace();
				workStatus = "Output err";
			}
		} else {
			if (msg != null) {
				if (msg[4] == (byte) 0x12) {
					flagKillThread = true;
					AppManager.getAppManager().AppExit(getApplicationContext());
					this.stopSelf();
					msg = null;
				}
			}
			workStatus = "OutputShutdown";
		}
		if (!CoverUtils.isNetworkAvailable(this)) {
			Log.v(TAG, "workStatus is not connectted");
			workStatus = "connect failed";
			handler.sendEmptyMessage(0x04);
			return;
		}
		if (socket == null) {
			handler.sendEmptyMessage(0x05);
			connectService();
		} else {
			if (bs != null) {
				//
			}
		}
		if (!InternetService.this.thread.isAlive()) {
			handler.sendEmptyMessage(0x03);
			(thread = new Thread(InternetService.this)).start();
		}
		if (!(socket.isConnected() && !socket.isClosed())) {
			handler.sendEmptyMessage(0x02);
			connectService();
			workStatus = "socket not connect";
			Log.i(TAG, "not connect");
			return;
		}
	}

	public String getWorkStatus() {
		return workStatus;
	}

	public void connectService() {
		try {
			socket = new Socket();
			SocketAddress socAddress = new InetSocketAddress(ip, port);
			socket.connect(socAddress, 3000);
			flagSocketSuccess = true;
			Log.i(TAG, "socket is connectted");
		} catch (SocketException e) {
			flagConnectionOutOnce++;
			Log.v(TAG, "time out");
			e.printStackTrace();
			workStatus = e.toString();
		} catch (IOException e) {
			Log.v(TAG, "time out");
			e.printStackTrace();
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

	int countOfUrgent = 0;

	@Override
	public void run() {
		connectService();
		int interval = 0;
		while (true) {
			if (flagKillThread == true) {
				break;
			}
			countOfUrgent++;
			if (countOfUrgent == 2) {
				try {
					socket.sendUrgentData(0x01);
				} catch (IOException e) {
					connectService();
					Log.i(TAG, "连接服务器---服务中断");
				}
				countOfUrgent = 0;
			}
			// 检测网络是否异常
			if (!CoverUtils.isNetworkAvailable(getApplication())) {
				countNetUnavail++;
				flagSocketSuccess = false;
			} else {// 有网络异常
				if (countNetUnavail != 0) {
					// connectService();
					Log.i(TAG, "连接服务器---网络恢复");
					if (flagSocketSuccess = true) {
						// 发送请求全部数据
						sendAskList();
						countNetUnavail = 0;
					}
				}

			}

			if (socket.isConnected()) {
				interval++;
				if (interval > INTERVAL) {
					msg = "appheartbeat!".getBytes();
					sendMessage(msg);
					Log.i("chaoshi", "心跳包");
					interval = 0;
				}

				if (!socket.isInputShutdown()) {
					if (!flagReaderThread) {
						new Thread(new Reader()).start();
						flagReaderThread = true;
					}
				}
				if (msg != null && flag_send) {
					sendMessage(msg);
					flag_send = false;
				}
				count = 0;
				count2 = 0;
			} else {
				count++;
				if (msg != null) {
					if (msg[4] == (byte) 0x12) {
						// flagKillThread = true;
						// msg = null;
						// AppManager.getAppManager().AppExit(
						// getApplicationContext());
						// this.stopSelf();
					}
				}
				if (flagSocketSuccess == false) {
					connectService();
					Log.i(TAG, "连接服务器---connection failed");
				}
				handler.sendEmptyMessage(0x08);
				if (count == 10) {
					handler.sendEmptyMessage(0x07);

				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class Reader implements Runnable {

		@Override
		public void run() {
			DataInputStream bufferedReader = null;
			try {
				bufferedReader = new DataInputStream(socket.getInputStream());
				int size = 0;
				byte[] total = new byte[1000];
				size = bufferedReader.read(total);
				if (size < 7) {
					flagReaderThread = false;
					return;
				}
				if (!((total[0] == (byte) 0xFA) && (total[1] == (byte) 0xF5))) {
					flagReaderThread = false;
					return;
				}// 头
				
				Log.i(TAG, "收到信息");
				Message msg = new Message();
				byte[] length = new byte[2];// 总长度
				length[0] = total[2];
				length[1] = total[3];
				msg.length[0] = total[2];
				msg.length[1] = total[3];
				int msgLength = CoverUtils.getShort(length, true);
				int num = 4;
				byte[] msgBuff = new byte[msgLength - 6];// 消息和功能码
				msg.data = new byte[msgBuff.length - 1];
				for (int i = 0; i < msgLength - 6; i++) {
					if (i == 0) {
						msg.function = total[num];
					} else {
						msg.data[i - 1] = total[num];
					}
					msgBuff[i] = total[num++];
				}
				byte[] checkBuf = new byte[2];// 校验位
				checkBuf[0] = total[num++];
				checkBuf[1] = total[num];
				msg.check[0] = checkBuf[0];
				msg.check[1] = checkBuf[1];

				byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(msg);
				byte[] str_ = CRC16M.getSendBuf(CoverUtils
						.bytes2HexString(checkMsg));
				byte[] check_temp = new byte[2];
				check_temp[0] = str_[str_.length - 1];
				check_temp[1] = str_[str_.length - 2];
				if ((check_temp[0] == msg.check[0])
						&& (check_temp[1] == msg.check[1])) {
					Log.i(TAG, "check right");
					if (msgBuff[0] == 0x03) {
						getMessage(msgBuff, ACTION_MainActivity);
						getMessage(msgBuff, ACTION_CoverList);
					} else if (sp.getString("username", "") != "") {
						switch (msgBuff[0]) {
						case 0x01: {// 需要处理报警信息ack
//							sendAskList();
							flag_notify++;
							byte[] idByte = new byte[2];
							Entity entity = new Entity();
							int i = 0;
							idByte[1] = msgBuff[i++ + 1];
							idByte[0] = msgBuff[i++ + 1];
							entity.setId(CoverUtils.getShort(idByte));
							entity.setTag(msgBuff[i++ + 1] == (byte) 0x10 ? "cover"
									: "level");
							byte[] longTi = new byte[8];
							for (int k = 0, t = i; i < t + 8; i++) {
								longTi[k++] = msgBuff[i + 1];
							}
							byte[] laTi = new byte[8];
							for (int k = 0, t = i; i < t + 8; i++) {
								laTi[k++] = msgBuff[i + 1];
							}
							entity.setLongtitude(CoverUtils.byte2Double(longTi));
							entity.setLatitude(CoverUtils.byte2Double(laTi));
							String title = "信息";
							switch (msgBuff[i++ + 1]) {
							case 0x01:
								entity.setStatus(Status.NORMAL);
								title = "正常监测信息";
								break;
							case 0x02:
								entity.setStatus(Status.EXCEPTION_1);
								title = "报警信息";
								break;
							case 0x03:
								entity.setStatus(Status.REPAIR);
								title = "维修中";
								break;
							case 0x04:
								entity.setStatus(Status.EXCEPTION_2);
								title = "欠压信息";
								break;
							case 0x05:
								entity.setStatus(Status.EXCEPTION_3);
								title = "报警欠压信息";
								break;
							case 0x06:// 处理接收到撤防或者0x07的设置中时，删除数据路相应条目
								entity.setStatus(Status.SETTING_FINISH);
								title = "报警解除";
								break;
							case 0x07:
								entity.setStatus(Status.SETTING_PARAM);
								title = "设置中";
								break;
							}
							// 处理若有从撤防中状态改变成正常状态
							if (douyadb.isExist("leave", entity.getTag() + "_"
									+ entity.getId())) {
								Detail.flagIsSetSuccess = true;
								if (douyadb.isExist("leave", entity.getTag()
										+ "_" + entity.getId()))
									douyadb.delete("leave", entity.getTag()
											+ "_" + entity.getId());
							}
							if (douyadb.isExist("setting", entity.getTag()
									+ "_" + entity.getId())) {
								ParamSettingActivity.flagIsSetSuccess = true;
								if (douyadb.isExist("setting", entity.getTag()
										+ "_" + entity.getId()))
									douyadb.delete("setting", entity.getTag()
											+ "_" + entity.getId());
							}
							setNotify(entity, title);
							if (douyadb.exist(entity.getTag(), entity.getId()
									+ "")) {
								douyadb.updateStatus(entity.getId(),
										entity.getTag(), entity.getStatus());
							}
							break;
						}
						case 0x02: {
							// sendAskList();
							flag_notify++;
							Entity entity = new Entity();
							byte[] b = new byte[2];
							b[1] = msgBuff[1];
							b[0] = msgBuff[2];
							String title = (msgBuff[3] == (byte) 0x1C ? "水位"
									: "井盖");
							entity.setId(CoverUtils.getShort(b));
							entity.setTag(title);
							byte[] lati = new byte[8];
							byte[] lonti = new byte[8];
							int j1 = 4;
							for (int i = 0; i < 8; i++) {
								lonti[i] = msgBuff[j1++];
							}
							for (int i = 0; i < 8; i++) {
								lati[i] = msgBuff[j1++];
							}
							entity.setLatitude(CoverUtils.byte2Double(lati));
							entity.setLongtitude(CoverUtils.byte2Double(lonti));
							setNotify(entity, "终端信息改变");
							if (douyadb.exist(entity.getTag(), entity.getId()
									+ "")) {
								douyadb.updateLatLon(entity.getTag(),
										entity.getId(), entity.getLongtitude(),
										entity.getLatitude());
							}
							break;
						}
						case 0x03: {
							getMessage(msgBuff, ACTION_MainActivity);
							getMessage(msgBuff, ACTION_CoverList);
							break;
						}
						case 0x04: {
							getMessage(msgBuff, ACTION_CoverList);
							break;
						}
						case 0x05: {
							// 终端参数设置回复，应该显示到通知栏
							// getMessage(msgBuff, ACTION_Paramsetting);
							// sendAskList();
							ParamSettingActivity.flagIsSetSuccess = true;
							Entity entity = new Entity();
							byte[] b = new byte[2];
							b[0] = msgBuff[1];
							b[1] = msgBuff[2];
							String title = (msgBuff[3] == (byte) 0x10 ? "井盖 "
									: "水位 ");
							entity.setId(CoverUtils.getShort(b));
							entity.setTag(title);
							if (msgBuff[3] == (byte) 0x02) {
								setNotify(
										entity.getTag() + "_" + entity.getId(),
										"设置失败");
							} else {
								setNotify(
										entity.getTag() + "_" + entity.getId(),
										"设置成功");
							}
							if (douyadb.isExist("setting", entity.getTag()
									+ "_" + entity.getId())
									&& (entity.getStatus() == Status.NORMAL)) {
								ParamSettingActivity.flagIsSetSuccess = true;
								if (douyadb.isExist("setting", entity.getTag()
										+ "_" + entity.getId()))
									douyadb.delete("setting", entity.getTag()
											+ "_" + entity.getId());
							}
							break;
						}
						case 0x06:
							getMessage(msgBuff,ACTION_Detail);
							break;
						case 0x07:
							getMessage(msgBuff, ACTION_Detail);
							break;
						case 0x09:
							getMessage(msgBuff, ACTION_Settings);
							break;
						case 0x0A:// 报警解除失败命令接收成功的ACK信息
							handler.sendEmptyMessage(0x11);
							break;
						case 0x0B:// 终端参数设置失败命令接收成功的ACK信息
							handler.sendEmptyMessage(0x12);
							break;
						case 0x16:
							getMessage(msgBuff, ACTION_Settings);
							break;
						case 0x18:
							getMessage(msgBuff, ACTION_Detail);
							break;
						default:
							handler.sendEmptyMessage(0x10);
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				flagReaderThread = false;
			}
			flagReaderThread = false;
		}
	}

	@SuppressWarnings("deprecation")
	public void setNotify(Entity entity, String title) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		// 定义Notification的各种属性
		int icon = R.drawable.icon; // 通知图标
		// CharSequence tickerText = title+"("+flag_notify+"条未读)"; //
		// 状态栏显示的通知文本提示
		CharSequence tickerText = title; // 状态栏显示的通知文本提示
		long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示
		Notification notification = null;
		if (notification == null)
			notification = new Notification(icon, tickerText, when);
		if (CoverUtils.getIntSharedP(getApplicationContext(), "setAlarmOrNot") == 1)
			notification.defaults |= Notification.DEFAULT_ALL;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// notification.number += flag_notify;
		Context context = getApplicationContext(); // 上下文
		CharSequence contentTitle = (entity.getTag().equals("level") ? "水位"
				: "井盖") + entity.getId(); // 通知栏标题
		CharSequence contentText = entity.getLatitude() + ","
				+ entity.getLongtitude(); // 通知栏内容
		Intent notificationIntent = new Intent(this, SingleMapDetail.class); // 点击该通知后要跳转的Activity
		notificationIntent.putExtra("entity", entity);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		// 把Notification传递给 NotificationManager
		mNotificationManager.notify(1, notification);
	}

	public void sendAskList() {
		Message askMsg = new Message();
		askMsg.function = (byte) 0x0D;
		askMsg.data = null;
		askMsg.length = CoverUtils.short2ByteArray((short) 7);
		byte[] checkAsk = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
		byte[] tmp_str = CRC16M
				.getSendBuf(CoverUtils.bytes2HexString(checkAsk));
		askMsg.check[0] = tmp_str[tmp_str.length - 1];
		askMsg.check[1] = tmp_str[tmp_str.length - 2];
		int lengthAsk = askMsg.getLength();
		byte[] totalAsk = new byte[lengthAsk];
		totalAsk = CoverUtils.msg2ByteArray(askMsg, lengthAsk);
		sendMessage(totalAsk);
	}

	public void setNotify(String tagID, String arg) {
		// 创建一个NotificationManager的引用
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		// 定义Notification的各种属性
		int icon = R.drawable.icon; // 通知图标
		CharSequence tickerText = "信息"; // 状态栏显示的通知文本提示
		long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示
		// 用上面的属性初始化 Nofification
		Notification notification = null;
		if (notification == null)
			notification = new Notification(icon, tickerText, when);
		// 添加声音
		if (CoverUtils.getIntSharedP(getApplicationContext(), "setAlarmOrNot") == 1)
			notification.defaults |= Notification.DEFAULT_ALL;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// notification.number += 2;
		// 设置通知的事件消息
		Context context = getApplicationContext(); // 上下文
		CharSequence contentTitle = tagID; // 通知栏标题
		CharSequence contentText = arg; // 通知栏内容
		// notification.
		notification.setLatestEventInfo(context, contentTitle, contentText,
				null);
		// 把Notification传递给 NotificationManager
		mNotificationManager.notify(0, notification);
		// mNotificationManager.cancel(0);
	}

	@Override
	public void onCreate() {
		sp = getSharedPreferences("douyatech", MODE_PRIVATE);
		if (CoverUtils.getStringSharedP(getApplicationContext(), "ip") == "")
			CoverUtils.putString2SharedP(getApplicationContext(), "ip",
					"61.185.220.74");
		if (CoverUtils.getIntSharedP(getApplicationContext(), "port") == 0)
			CoverUtils.putInt2SharedP(getApplicationContext(), "port", 9999);
		// if (CoverUtils.getStringSharedP(getApplicationContext(), "ip") ==
		// "")
		// CoverUtils.putString2SharedP(getApplicationContext(), "ip",
		// "124.115.169.98");
		// if (CoverUtils.getIntSharedP(getApplicationContext(), "port") ==
		// 0)
		// CoverUtils.putInt2SharedP(getApplicationContext(), "port", 6221);
		ip = sp.getString("ip", "");
		port = sp.getInt("port", 0);
		douyadb = new Douyatech(getApplicationContext());
		flagOnCreate = true;
		myReceiver = new ServiceReceiver();
		if (flagKillThread == true) {
			flagKillThread = false;
			// thread = new Thread(InternetService.this);
			// thread.start();
		}
		thread = new Thread(InternetService.this);
		thread.start();

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		try {
			socket.close();
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 保证退出程序时候不再开启
		flagReaderThread = true;
		super.onDestroy();
		Log.v("QLQ", "service is on destroy");
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		if (flagKillThread == true) {
			flagKillThread = false;
			thread = new Thread(InternetService.this);
			thread.start();
		}
		super.onStart(intent, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

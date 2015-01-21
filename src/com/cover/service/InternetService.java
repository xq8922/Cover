package com.cover.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import com.cover.util.CoverUtils;

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
	private final String TAG = "COVER";
	
	private static final String ACTION_MainActivity = "com.cover.main.mainactivity";
	private static final String ACTION_CoverList = "com.cover.coverlist";
	private static final String ACTION_CoverMapList = "com.cover.covermaplist";
	private static final String ACTION_Detail = "com.cover.detail";
	private static final String ACTION_Settings = "com.cover.settings";
	private static final String ACTION_SoftwareSettings = "com.cover.softwaresettings";
	// public String ip = "192.168.0.1";
	// public String ip = "219.244.118.30";
	// public String ip = "192.168.42.145";
	public String ip = "192.168.100.101";
	public int port = 10001;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Binder binder;
	private Thread thread;
	private String workStatus = "test";
	private String currAction;
	ServiceReceiver myReceiver;

	private class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

		}

	}

	public void sendRequest(String action) {
		try {
			workStatus = null;
			currAction = action;
			sendMessage(action);
		} catch (Exception e) {
			workStatus = "sendmessage fail";
		}
	}

	// public void

	private void sendMessage(String action) {
		if (!CoverUtils.isNetworkAvailable(this)) {
			Log.v(TAG, "workStatus is not connectted");
			workStatus = "connect failed";
			return;
		}
		if (socket == null) {
			connectService();
		} else {
			if (action != null) {
				try {
					writer = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!InternetService.this.thread.isAlive()) {
			(thread = new Thread(InternetService.this)).start();
		}
		if (!socket.isConnected() || socket.isClosed()) {
			workStatus = "socket not connect";
			Log.v(TAG, "not connect");
			return;
		}
		if (!socket.isOutputShutdown()) {
			try {
				writer.println(action);
				// String temp = reader.readLine().toString();
				// Log.i("reader",);
			} catch (Exception e) {
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

	public void dealUploadTask(InputStream is) {
		try {

		} catch (Exception e) {

		}
	}

	public void closeConnection() {

	}

	public void connectService() {
		try {
			socket = new Socket();
			SocketAddress socAddress = new InetSocketAddress(ip, port);
			socket.connect(socAddress, 3000);
			Log.i(TAG, "socket is connectted");
			// reader = new BufferedReader(new InputStreamReader(
			// socket.getInputStream()));
			// writer = new PrintWriter(new BufferedWriter(new
			// OutputStreamWriter(
			// socket.getOutputStream())), true);
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

	public void getMessage(String str, String action) {
		try {
			// IntentFilter filter = new IntentFilter();
			// filter.addAction("com.cover.service.InternetService");
			// registerReceiver(myReceiver,filter);
			Intent serviceIntent = new Intent();
			serviceIntent.setAction(action);
			serviceIntent.putExtra("msg", str);
			sendBroadcast(serviceIntent);
			Log.i(TAG, action + "广播发送成功");
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
						sendMessage("hello");
						new Thread(new Reader()).start();
						// readLine()方法一直等待直到socket关闭为止
						// if ((content = reader.readLine()) != null) {
						/*
						 * { DataInputStream bufferedReader = new
						 * DataInputStream( socket.getInputStream()); byte[]
						 * cbuff = new byte[5]; char[] charBuff = new char[5];
						 * int size = 0; size = bufferedReader.read(cbuff); //
						 * while ((size = bufferedReader.read(cbuff)) > 0) {
						 * convertByteToChar(cbuff, charBuff, size);
						 * System.out.println(charBuff); // } //
						 * bufferedReader.close(); }
						 */
						// getMessage("testBroadcast","com.wxq.test");
						// }
					}
				} else {
					connectService();
				}
			}
		} catch (Exception e) {
			try {
				socket.close();
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
			DataInputStream bufferedReader = null;
			try {
				bufferedReader = new DataInputStream(socket.getInputStream());
				byte[] cbuff = new byte[4];
				char[] charBuff = new char[4];
				int size = 0;
				size = bufferedReader.read(cbuff);
				// while ((size = bufferedReader.read(cbuff)) > 0) {
				convertByteToChar(cbuff, charBuff, size);
				System.out.println(cbuff);
				System.out.println(charBuff);

				// String temp = charBuff[2]+charBuff[3];
				// int msgLength_test = Integer.parseInt(temp);
				// msgLength is the length got from the msg header.
				int msgLength = Integer.parseInt(String.valueOf(charBuff[2])
						+ "" + String.valueOf(charBuff[3]));
				msgLength -= 6;
				System.out.println(msgLength);
				byte[] msgBuff = new byte[msgLength];
				char[] messageBuff = new char[msgLength];
				size = 0;
				size = bufferedReader.read(msgBuff);// set chaoshi
				convertByteToChar(msgBuff, messageBuff, size);
				System.out.println(messageBuff);
				byte[] checkBuf = new byte[2];
				char[] charCheckBuf = new char[2];
				size = 0;
				size = bufferedReader.read(checkBuf);
				convertByteToChar(checkBuf, charCheckBuf, size);

				functionStatus fs;
				if (CoverUtils.isCRCRight()) {
					if (messageBuff[0] == 0xFA && messageBuff[1] == 0xF5) {
						switch ((int) messageBuff[0]) {
						case 0x01: {
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverList);
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverMapList);
							break;
						}
						case 0x02: {
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverMapList);
							break;
						}
						case 0x03: {
							getMessage(String.valueOf(messageBuff),
									ACTION_MainActivity);
							break;
						}
						case 0x04: {
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverList);
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverMapList);
							break;
						}
						case 0x05: {
							getMessage(String.valueOf(messageBuff),
									ACTION_Settings);
							break;
						}
						case 0x06: {
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverList);
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverMapList);
							break;
						}
						case 0x07: {
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverList);
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverMapList);
							break;
						}
						case 0x08: {// 维修状态结束设置接收成功的ACK信息
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverList);
							getMessage(String.valueOf(messageBuff),
									ACTION_CoverMapList);
							break;
						}
						default:Toast.makeText(getApplicationContext(), "服务器发送了未知命令", Toast.LENGTH_LONG).show();
						}
					}
				}
				// }
				// bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void convertByteToChar(byte[] cbuff, char[] charBuff, int size) {
		for (int i = 0; i < charBuff.length; i++) {
			if (i < size) {
				charBuff[i] = (char) cbuff[i];
			} else {
				charBuff[i] = ' ';
			}
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
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v("QLQ", "service is on destroy");
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// IntentFilter filter = new IntentFilter();
		// filter.addAction("com.cover.service.InternetService");
		// registerReceiver(myReceiver, filter);
		// String msg = "hello";
		// Intent serviceIntent = new Intent();
		// serviceIntent.setAction("com.cover.main.MainActivity");
		// serviceIntent.putExtra("msg", msg);
		// sendBroadcast(serviceIntent);
		super.onStart(intent, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

}

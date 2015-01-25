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

import com.cover.bean.Message;
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
	public String ip = "192.168.100.102";
	// public String ip = "219.245.66.226";
	public int port = 10001;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Binder binder;
	private Thread thread;
	private String workStatus = "test";
	private String currAction;
	static boolean flag_send = false;
	static byte[] msg;
	ServiceReceiver myReceiver;
	Message message = new Message();

	public static class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			msg = intent.getByteArrayExtra("msg");
			// sendMessage(msg);
			Toast.makeText(context, msg.toString(), Toast.LENGTH_LONG).show();
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

	// public void

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
					// reader = new BufferedReader(new InputStreamReader(
					// socket.getInputStream()));
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
					writer.println(msg);
					Log.i(TAG, "msg sent " + msg.toString());
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

	public void getMessage(byte[] msg, String action) {
		try {
			// IntentFilter filter = new IntentFilter();
			// filter.addAction("com.cover.service.InternetService");
			// registerReceiver(myReceiver,filter);
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
						new Thread(new Reader()).start();
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
			DataInputStream bufferedReader = null;
			try {
				bufferedReader = new DataInputStream(socket.getInputStream());
				byte[] headerBuff = new byte[2];
				int size = 0;
				size = bufferedReader.read(headerBuff);
				if (!(headerBuff[0] == 0xFA || headerBuff[1] == 0xF5))
					;
				// return;
				byte[] length = new byte[2];
				int msgLength = CoverUtils.getShort(length);
				msgLength -= 6;
				System.out.println(msgLength);
				byte[] msgBuff = new byte[msgLength];
				size = bufferedReader.read(msgBuff);// set chaoshi
				byte[] checkBuf = new byte[2];
				size = bufferedReader.read(checkBuf);
				byte[] totalMsg = new byte[2 + msgLength];
				for (int i = 0; i < 2 + msgLength; i++) {
					if (i < 2)
						totalMsg[i] = length[i];
					else
						totalMsg[i] = msgBuff[i];
				}
				// functionStatus fs;
				if (CoverUtils.isCRCRight(totalMsg, checkBuf)) {
					switch (msgBuff[0]) {
					case 0x01: {
						getMessage(msgBuff, ACTION_CoverList);
						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					case 0x02: {
						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					case 0x03: {
						getMessage(msgBuff, ACTION_MainActivity);
						break;
					}
					case 0x04: {
						getMessage(msgBuff, ACTION_CoverList);
						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					case 0x05: {
						getMessage(msgBuff, ACTION_Settings);
						break;
					}
					case 0x06: {
						getMessage(msgBuff, ACTION_CoverList);
						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					case 0x07: {
						getMessage(msgBuff, ACTION_CoverList);
						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					case 0x08: {// ά��״̬�������ý��ճɹ���ACK��Ϣ
						getMessage(msgBuff, ACTION_CoverList);
						getMessage(msgBuff, ACTION_CoverMapList);
						break;
					}
					default:
						Toast.makeText(getApplicationContext(),
								"������������δ֪����", Toast.LENGTH_LONG)
								.show();
					}
				}
				// }
				// bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
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

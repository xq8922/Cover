package com.cover.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

enum Constant {
	TAG_FAILURE, TAG_SUCCESS
}

public class InternetService extends Service implements Runnable {
	public String ip = "192.168.100.103";
	public int port = 12345;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Binder binder;
	private Thread thread;
	private String workStatus;
	private String currAction;

	public void sendRequest(String action) {
		try {
			workStatus = null;
			currAction = action;
			sendMessage(action);
		} catch (Exception e) {
			workStatus = "sendmessage fail";
		}
	}

	private void sendMessage(String action) {
		// if(!CoverUtils.isNetworkAvailable(MainActivity.t)){
		// Log.v("QLQ","workStatus is not connectted");
		// workStatus = "connect failed";
		// return ;
		// }
		if (socket == null) {
			connectService();
		} else {
			if (action != null) {
				try {
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream())), true);
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
			Log.v("QLQ", "not connect");
			return;
		}
		if (!socket.isOutputShutdown()) {
			try {
				writer.println(action);
			} catch (Exception e) {
				Log.v("QLQ", "is not connect");
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
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);
		} catch (SocketException e) {
			Log.v("QLQ", "socketException");
			e.printStackTrace();
			workStatus = e.toString();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			workStatus = e.toString();
			return;
		}
	}

	public void getMessage(String str) {
		try {
			Log.i("QLQ", str);
			// get message
		} catch (Exception e) {
		}
	}

	public class InterBinder extends Binder {
		public InternetService getService() {
			return InternetService.this;
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
						String content;
						if ((content = reader.readLine()) != null) {
							getMessage(content);
						}
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

	@Override
	public IBinder onBind(Intent arg0) {
		binder = new InterBinder();
		// sendRequest("hello");
		thread = new Thread(InternetService.this);
		thread.start();
		return binder;
	}

	@Override
	public void onCreate() {
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
		super.onStart(intent, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

}

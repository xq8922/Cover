package com.cover.main;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.content.Intent;
import android.widget.Toast;

public class TCPClient {
//	public void tcpClient(final String ip, final int port) {
//		Socket socket = null;
//		try {
//			socket = new Socket(ip, port);
//			String message = "Message from Android phone";
//			try {
//				System.out.println("Client Sending: '" + message + "'");
//				// 第二个参数为True则为自动flush
//				PrintWriter out = new PrintWriter(new BufferedWriter(
//						new OutputStreamWriter(socket.getOutputStream())), true);
//				out.println(message);
//				// out.flush();
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				// 关闭Socket
//				socket.close();
//				System.out.println("Client:Socket closed");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}	
	
	public void tcpClient(String IP, int PORT,Socket socket) {
		
		try {
			socket = new Socket(IP, PORT);
			if(socket.isConnected()){
//				Toast.makeText(get++, text, duration)
			}
			String message = "Message from Android phone";
			try {
				System.out.println("Client Sending: '" + message + "'");

				// 第二个参数为True则为自动flush
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream())), true);
				out.println(message);
				// out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 关闭Socket
				socket.close();
				System.out.println("Client:Socket closed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Runnable getRunnable(final String IP, final int PORT,final Socket socket) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				tcpClient(IP, PORT,socket);
			}
		};
		return runnable;
	}
}

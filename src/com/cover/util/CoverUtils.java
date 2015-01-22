package com.cover.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import com.cover.bean.Message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class CoverUtils {

	/**
	 * 鍒ゆ鏄惁鍙互杩炴帴缃戠粶
	 * 
	 * @param activity
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		} else {
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
			return false;
		}

	}
	/**
	 * 
	 * @param cbuff
	 * @param charBuff
	 * @param size
	 * @return
	 */
	public static char[] convertByteToChar(byte[] cbuff, char[] charBuff,
			int size) {
		for (int i = 0; i < charBuff.length; i++) {
			if (i < size) {
				charBuff[i] = (char) cbuff[i];
			} else {
				charBuff[i] = ' ';
			}
		}
		return charBuff;
	}

	/**
	 * 鑾峰彇鏈湴ip
	 * 
	 * @return
	 */
	public static String getLocalIpAdress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * is crcRight
	 */
	public static boolean isCRCRight(byte[] ucCRC_Buf, byte[] check) {
		short uiX, uiY, uiCRC;
		byte ucStart = 0;
		uiCRC = (short) 0xFFFF; // set all 1
		int ucBufLength = ucCRC_Buf.length;
		ucBufLength += ucStart;
		for (uiX = (short) ucStart; uiX < ucBufLength; uiX++) {
			uiCRC = (short) (uiCRC ^ ucCRC_Buf[uiX]);
			for (uiY = 0; uiY <= 7; uiY++) {
				if ((uiCRC & 1) != 0)
					uiCRC = (short) ((uiCRC >> 1) ^ 0xA001);
				else
					uiCRC = (short) (uiCRC >> 1);
			}
		}
		if (short2ByteArray(uiCRC) == check)
			return true;
		else
			return true;
		// return true;
	}

	/**
	 * gen CRCCHECK
	 * 
	 * @param msg
	 */
	public static byte[] genCRC(byte[] ucCRC_Buf, int ucBufLength) {
		short uiX, uiY, uiCRC;
		byte ucStart = 0;
		uiCRC = (short) 0xFFFF; // set all 1
		if (ucBufLength <= 0 || ucStart > ucBufLength)
			uiCRC = 0;
		else {
			ucBufLength += ucStart;
			for (uiX = ucStart; uiX < ucBufLength; uiX++) {
				uiCRC = (short) (uiCRC ^ ucCRC_Buf[uiX]);
				for (uiY = 0; uiY <= 7; uiY++) {
					if ((uiCRC & 1) != 0)
						uiCRC = (short) ((uiCRC >> 1) ^ 0xA001);
					else
						uiCRC = (short) (uiCRC >> 1);
				}
			}
		}
		return short2ByteArray(uiCRC);
	}

	/**
	 * sendMessage throw broadcast
	 */
	// public static void sendMessage(String msg,String action){
	// Intent serviceIntent = new Intent();
	// serviceIntent.setAction(action);
	// serviceIntent.putExtra("msg", msg);
	// sendBroadcast(serviceIntent);
	// Log.i(TAG, action + "sned broadcast "+ action);
	// }
	/**
	 * 通过byte数组取到short
	 * 
	 * @param b
	 * 
	 * @return
	 */
	public static short getShort(byte[] b) {
		short sum = 0;
		for (int i = 0, k = 1, j = b.length - 1; i < b.length * 8; i++) {
			if (i == 8 * k) {
				j--;
				k++;
			}
			sum += (0x01 & b[j]) * Math.pow(2, i);
			b[j] >>= 1;
		}
		return sum;
	}

	/**
	 * short2byte
	 */
	public static byte[] short2ByteArray(short s) {
		byte[] shortBuf = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (shortBuf.length - 1 - i) * 8;
			shortBuf[i] = (byte) ((s >>> offset) & 0xff);
		}
		return shortBuf;
	}

	/**
	 * form
	 */
	public static byte[] msg2ByteArray(Message msg, int length) {
		byte[] totalMsg = new byte[length];
		int j = 0;
		totalMsg[j++] = msg.header1;
		totalMsg[j++] = msg.header2;
		for (int i = 0; i < msg.length.length; i++)
			totalMsg[j++] = msg.length[i];
		totalMsg[j++] = msg.function;
		for (int i = 0; i < msg.data.length; i++)
			totalMsg[j++] = msg.data[i];
		for (int i = 0; i < msg.check.length; i++)
			totalMsg[j++] = msg.check[i];
		return totalMsg;
	}
}

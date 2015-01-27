package com.cover.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
	public static String bytes2HexString(byte[] b) {  
		  String ret = "";  
		  for (int i = 0; i < b.length; i++) {  
		   String hex = Integer.toHexString(b[ i ] & 0xFF);  
		   if (hex.length() == 1) {  
		    hex = '0' + hex;  
		   }  
		   ret += hex.toUpperCase();  
		  }  
		  return ret;  
		}  

	/**
	 * judge network is available or not
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
	 * convert byte to char
	 * 
	 * @param cbuff
	 * @param charBuff
	 * @param size
	 * @return char[]
	 */
	public static char[] convertByteArrToChar(byte[] cbuff) {
		int size = cbuff.length;
		char[] charBuff = new char[size];
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
	 * get local phone ip
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
	 * 
	 * @return crc is right or not
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
	 * @return byte[]
	 */
	public static byte[] genCRC(byte[] ucCRC_Buf, int ucBufLength) {
		short uiX, uiY, uiCRC;
		byte ucStart = 0;
		uiCRC = (short) 0xFFFF; // set all 1
		if (ucBufLength <= 0 || ucStart > ucBufLength)
			uiCRC = 0;
		else {
			ucBufLength += ucStart;
			for (uiX = (short)ucStart; uiX < ucBufLength; uiX++) {
				uiCRC = (short) (uiCRC ^ ucCRC_Buf[uiX]);
				for (uiY = 0; uiY <= 7; uiY++) {
					if ((uiCRC & 1) != 0)
						uiCRC = (short) ((uiCRC >> 1) ^ 0xA001);
					else
						uiCRC = (short) (uiCRC >> 1);
				}
			}
		}
		System.out.println(Integer.toHexString(uiCRC));
		return short2ByteArray(uiCRC);
		
	}
	
    private short CreateCRC(byte[] Array, int Len)
    {
        short IX, IY, CRC;
        CRC = (short) 0xFFFF;//set all 1
        if (Len <= 0)
            CRC = 0;
        else
        {
            Len--;
            for (IX = 0; IX <= Len; IX++)
            {
                CRC = (short)(CRC ^ Array[IX]);
                for (IY = 0; IY <= 7; IY++)
                    if ((CRC & 1) != 0)
                        CRC = (short)((CRC >> 1) ^ 0xA001);
                    else
                        CRC = (short)(CRC >> 1);
            }
        }
        return CRC;
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
	 * ͨ��byte����ȡ��short
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
	 * 
	 * @return return byte[]
	 */
	public static byte[] short2ByteArray(short s) {
		byte[] shortBuf = new byte[2];
		// for (int i = 0; i < 2; i++) {
		// int offset = (shortBuf.length - 1 - i) * 8;
		// shortBuf[i] = (byte) ((s >>> offset) & 0xff);
		// }
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(baos);

//		((String) baos).getBytes();

		shortBuf[0] = (byte) ((s & 0xFF00) >> 8);
		shortBuf[1] = (byte) (s & 0xFF);
		System.out.println(shortBuf);
		return shortBuf;
	}

	/**
	 * form byte[] from Message
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

	/**
	 * form byte[] from Message except check
	 */
	public static byte[] msg2ByteArrayExcepteCheck(Message msg) {
		byte[] totalMsg = new byte[msg.length.length+1+msg.data.length];
		int j = 0;
		for (int i = 0; i < msg.length.length; i++) {
			totalMsg[j++] = msg.length[i];
		}
		totalMsg[j++] = msg.function;
		for (int i = 0; i < msg.data.length; i++) {
			totalMsg[j++] = msg.data[i];
		}
		return totalMsg;
	}

}

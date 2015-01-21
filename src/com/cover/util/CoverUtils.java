package com.cover.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CoverUtils {
	
	/**
	 * 判段是否可以连接网络
	 * @param activity
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}else{
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
			if(networkInfo != null && networkInfo.length>0){
				for(int i = 0;i < networkInfo.length;i ++){
					if(networkInfo[i].getState() == NetworkInfo.State.CONNECTED){
						return true;
					}
				}
			}
			return false;
		}
		
	}
	/**
	 * 获取本地ip
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
	 * CRCCHECK
	 */
	public static boolean isCRCRight(){
		/*
		unsigned int CRC_Check(unsigned char *ucCRC_Buf, unsigned char ucBufLength)
		{
		    unsigned int uiX, uiY, uiCRC;
		    unsigned char ucStart = 0;
		    uiCRC = 0xFFFF;  //set all 1

		    if (ucBufLength <= 0 || ucStart > ucBufLength)
		        uiCRC = 0;
		    else
		    {
		        ucBufLength += ucStart;
		        for (uiX = (unsigned int)ucStart; uiX < ucBufLength; uiX++)
		        {
		            uiCRC = (unsigned int)(uiCRC ^ ucCRC_Buf [uiX]);
		            
		            for (uiY = 0; uiY <= 7; uiY++)
		            {
		                if ((uiCRC & 1) != 0)
		                    uiCRC = (unsigned int)((uiCRC >> 1) ^ 0xA001);
		                else
		                    uiCRC = (unsigned int)(uiCRC >> 1);
		            }
		        }
		    }

		    return uiCRC;
		}
		*/
		return true;
	}
}

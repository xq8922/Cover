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
	public static boolean isNetworkAvailable(Activity activity){
		Context context = activity.getApplicationContext();
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
}

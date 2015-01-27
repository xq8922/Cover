package com.cover.ui;

import com.cover.bean.Entity;
import com.cover.bean.Message;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CoverMapList extends Activity {
	private static final String TAG = "cover";
	Message askMsg = new Message();
	private final String ACTION = "com.cover.service.IntenetService";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cover_map_list);

		askMsg.function = 0x0D;
		askMsg.data = null;
		int length = 7 + askMsg.data.length;
		askMsg.length = CoverUtils.short2ByteArray((short) length);
		byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
		byte[] str_ = CRC16M.getSendBuf(CoverUtils.bytes2HexString(checkMsg));
		askMsg.check[0] = str_[str_.length - 1];
		askMsg.check[1] = str_[str_.length - 2];
		sendMessage(askMsg, ACTION);
	}

	public static class CoverMapListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] recv = intent.getByteArrayExtra("msg");
			Entity[] msgRecv = null;
			if (recv[0] == 0x01) {
				int numOfEntity = (recv.length - 1) / 3;
				msgRecv = new Entity[numOfEntity];
				for (int i = 0, j = 0; j < recv.length;) {
					msgRecv[i].setId(String.valueOf(recv[j++]));
					msgRecv[i].setTag(recv[j++] == 0x51 ? "cover" : "level");
					i++;
					j++;
					// msgRecv[i].setStatu(recv[j++] == 0x51?"cover":"level");
				}
			} else if (recv[0] == 0x05) {
				int numOfEntity = (recv.length - 1) / 5;
				msgRecv = new Entity[numOfEntity];
				for (int i = 0, j = 0; j < recv.length;) {
					msgRecv[i].setId(String.valueOf(recv[j++]));
					msgRecv[i].setTag(recv[j++] == 0x51 ? "cover" : "level");
					msgRecv[i].setLongtitude(String.valueOf(recv[j++]));
					msgRecv[i].setLongtitude(String.valueOf(recv[j++]));
					switch(recv[j]){
//					case 0x01:msgRecv[i].setStatus();
					}
					i++;
					j++;
					// msgRecv[i].setStatu(recv[j++] == 0x51?"cover":"level");
				}
			}
			//msgRecv set onto map.
			int msgLength = msgRecv.length;
			for(int i = 0;i < msgLength; i++){
				
			}
		}

	}

	public void sendMessage(Message msg, String action) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(action);
		int length = msg.getLength();
		byte[] totalMsg = new byte[length];
		totalMsg = CoverUtils.msg2ByteArray(msg, length);
		serviceIntent.putExtra("msg", totalMsg);
		sendBroadcast(serviceIntent);
		Log.i(TAG, action + "send broadcast " + action);
	}
}

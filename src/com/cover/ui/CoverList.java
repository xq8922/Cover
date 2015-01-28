package com.cover.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.cover.bean.Entity;
import com.cover.bean.Message;
import com.cover.bean.Entity.Status;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class CoverList extends Activity {
	private final String TAG = "cover";
	private final String ACTION = "com.cover.service.IntenetService";
	private static ListView lv_coverlist;
	public static ArrayList<Entity> items;
	public static ArrayList<Entity> waterItems;
	public static ArrayList<Entity> coverItems;
	private Message askMsg = new Message();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cover_list);
		lv_coverlist = (ListView) findViewById(R.id.lv_coverlist_cover);

		askMsg.function = (byte)0x0D;
		askMsg.data = null;
		askMsg.length = CoverUtils.short2ByteArray((short) 7);
		
		byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
		byte[] str_ = CRC16M.getSendBuf(CoverUtils
				.bytes2HexString(checkMsg));
		askMsg.check[0] = str_[str_.length - 1];
		askMsg.check[1] = str_[str_.length - 2];				
		sendMessage(askMsg, ACTION);
		
	}

	public void sendMessage(Message msg, String action) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(action);
		int length = msg.getLength();
		byte[] totalMsg = new byte[length];
		totalMsg = CoverUtils.msg2ByteArray(msg, length);
		serviceIntent.putExtra("msg", totalMsg);
		sendBroadcast(serviceIntent);
		Log.i(TAG, action + "sned broadcast " + action);
	}

	public static class CoverListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			items = new ArrayList<Entity>();
			waterItems = new ArrayList<Entity>();
			coverItems = new ArrayList<Entity>();

			byte[] recv = intent.getByteArrayExtra("msg");
			if (recv[0] == 0x01) {
				final int dataLength = 4;
				int numOfEntity = (recv.length - 1) / dataLength;
				byte[] idByte = new byte[2];
				for (int j = 0; j < numOfEntity; j++) {
					Entity entity = new Entity();
					int i = 0;
					idByte[i] = recv[j * dataLength + i++ + 1];
					idByte[i] = recv[j * dataLength + i++ + 1];

					entity.setId(CoverUtils.getShort(idByte));
					entity.setTag(recv[j * dataLength + i++ + 1] == 0x51 ? "cover"
							: "level");
					switch (recv[j * dataLength + i++ + 1]) {
					case 0x01:
						entity.setStatus(Status.NORMAL);
					case 0x02:
						entity.setStatus(Status.EXCEPTION_1);
					case 0x03:
						entity.setStatus(Status.REPAIR);
					case 0x04:
						entity.setStatus(Status.EXCEPTION_2);
					case 0x05:
						entity.setStatus(Status.EXCEPTION_3);
					}
					if (entity.getTag() == "cover") {
						coverItems.add(entity);
						items.add(entity);
					} else {
						waterItems.add(entity);
						items.add(entity);
					}
				}
			} else if (recv[0] == 0x04) {
				final int dataLength = 20;
				int numOfEntity = (recv.length - 1) / dataLength;
				byte[] idByte = new byte[2];
				for (int j = 0; j < numOfEntity; j++) {
					Entity entity = new Entity();
					int i = 0;
					idByte[i] = recv[j * dataLength + i++ + 1];
					idByte[i] = recv[j * dataLength + i++ + 1];
					entity.setId(CoverUtils.getShort(idByte));
					entity.setTag(recv[j * dataLength + i++ + 1] == 0x51 ? "cover"
							: "level");
					byte[] longTi = new byte[8];
					for (int k = 0, t = i; i < t + 8; i++) {
						longTi[k++] = recv[j * dataLength + i + 1];
					}
					byte[] laTi = new byte[8];
					for (int k = 0, t = i; i < t + 8; i++) {
						laTi[k++] = recv[j * dataLength + i + 1];
					}
					entity.setLongtitude(CoverUtils.byte2Double(longTi));
					switch (recv[j * dataLength + i + 1]) {
					case 0x01:
						entity.setStatus(Status.NORMAL);
					case 0x02:
						entity.setStatus(Status.EXCEPTION_1);
					case 0x03:
						entity.setStatus(Status.REPAIR);
					case 0x04:
						entity.setStatus(Status.EXCEPTION_2);
					case 0x05:
						entity.setStatus(Status.EXCEPTION_3);
					}
					if (entity.getTag() == "cover") {
						coverItems.add(entity);
						items.add(entity);
					} else {
						waterItems.add(entity);
						items.add(entity);
					}
				}
			}
		}
	}
}

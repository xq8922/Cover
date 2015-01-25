package com.cover.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.cover.bean.Message;
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
	private static ListView lv_coverlist;
	private Message askMsg = new Message();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cover_list);

		askMsg.function = 0x0D;
		askMsg.data = null;
		askMsg.length = CoverUtils.short2ByteArray((short) 7);
		byte[] temp = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
		askMsg.check = CoverUtils.genCRC(CoverUtils.genCRC(temp, temp.length),
				askMsg.getLength() - 2);
		byte[] total = CoverUtils.msg2ByteArray(askMsg, askMsg.getLength());

		lv_coverlist = (ListView) findViewById(R.id.lv_coverlist_cover);
		// String[] strs = new String[] { "first", "second", "third", "fourth",
		// "fifth" };
		// lv_coverlist.setAdapter(new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, strs));
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.cover.service.InternetService");
		serviceIntent.putExtra("msg", total);
		sendBroadcast(serviceIntent);
	}

	public static class CoverListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] msg = intent.getByteArrayExtra("msg");
			if (msg[0] == 0x04) {
				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				for (int i = 0; i < (msg.length - 1) / 5; i++) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("iv_itemstatus", R.drawable.checkbox_selected);
					map.put("tv_itemname", "testname");
					map.put("tv_itemstatus", "teststatus");
					listItem.add(map);
					SimpleAdapter simpleAdapter = new SimpleAdapter(context,
							listItem, R.layout.listview, new String[] {
									"iv_itemstatus", "tv_itemname",
									"tv_itemstatus" }, new int[] {
									R.id.iv_itemstatus, R.id.tv_itemname,
									R.id.tv_itemstatus });
					lv_coverlist.setAdapter(simpleAdapter);
					lv_coverlist
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> arg0,
										View arg1, int arg2, long arg3) {
									arg1.setSelected(true);
								}

							});
				}
			}
		}
	}

	public void sendMessage(byte[] msg, String action) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(action);
		serviceIntent.putExtra("msg", msg);
		sendBroadcast(serviceIntent);
		Log.i(TAG, action + "sned broadcast " + action);
	}

}

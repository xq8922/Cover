package com.cover.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.dbhelper.Douyatech;
import com.cover.fragment.ListFragment;
import com.cover.fragment.MapFragment;
import com.cover.main.MainActivity;
import com.cover.service.InternetService;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.wxq.covers.R;

public class CoverList extends Activity implements OnClickListener {
	private final String TAG = "cover";
	private final static String ACTION = "com.cover.service.IntenetService";
	private static ListView lv_coverlist;
	private ImageView setting;
	private ImageView reload;
	private byte flag = 0x11; // 0x10 表示水位 0x01表示井盖
	public static ArrayList<Entity> items = new ArrayList<Entity>();
	public static ArrayList<Entity> waterItems = new ArrayList<Entity>();
	public static ArrayList<Entity> coverItems = new ArrayList<Entity>();
	private Message askMsg = new Message();
	public static boolean flagSend = false;
	private static CheckBox cbWater; // 水位
	private static CheckBox cbCover; // 井盖
	public static ListFragment listFragment;
	private static MapFragment mapFragment;
	private RadioGroup rgBottom;
	private RadioButton radioMap;
	public static Entity entity = null;
	static FragmentTransaction ft;
	int flagWhitchIsCurrent = 1;
	static Douyatech douyadb = null;
	private SharedPreferences sp;
	private static Editor editor;
	private static boolean flagIsReceivedMsg = false;
	public PullToRefreshListView mListView;
	public static int flagChecked = 0;

	public Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			// listFragment.update(0);
			switch (msg.what) {
			case 0x20:
				Toast.makeText(getApplicationContext(), "刷新失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 0x10:
				Toast.makeText(getApplicationContext(), "刷新成功",
						Toast.LENGTH_SHORT).show();
				break;
			case 0x11:
				Toast.makeText(getApplicationContext(), "正在刷新...",
						Toast.LENGTH_SHORT).show();
				break;
			case 0x30:
				startService(new Intent(CoverList.this, InternetService.class));
				break;
			}
		}

	};
	Handler handler2 = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x01:
				Toast.makeText(getApplicationContext(), "与服务器连接中断！",
						Toast.LENGTH_LONG).show();
				break;
			}
		}

	};

	@Override
	protected void onResume() {
		flagIsReceivedMsg = false;
		// new Thread(new ThreeSecond()).start();
		// sendAsk();
		super.onResume();

	}

	// 每个两分钟刷新一次
	public static Context myContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.cover_list);
		AppManager.getAppManager().addActivity(this);

		myContext = getApplicationContext();
		douyadb = new Douyatech(getApplicationContext());
		// lv_coverlist = (ListView) findViewById(R.id.lv_coverlist_cover);
		lv_coverlist = (ListView) findViewById(R.id.list_view);
		cbWater = (CheckBox) findViewById(R.id.cb_water);
		cbCover = (CheckBox) findViewById(R.id.cb_cover);
		rgBottom = (RadioGroup) findViewById(R.id.rg_bottom_cover);
		setting = (ImageView) findViewById(R.id.setting);
		reload = (ImageView) findViewById(R.id.iv_reload);
		radioMap = (RadioButton) findViewById(R.id.rb_map);
		setting.setOnClickListener(this);
		reload.setOnClickListener(this);

		ft = getFragmentManager().beginTransaction();
		if (listFragment == null) {
			listFragment = new ListFragment();
		}
		if (mapFragment == null) {
			mapFragment = new MapFragment();
		}
		// getDataFromDB();
		// getDatas();
		entity = (Entity) getIntent().getSerializableExtra("entity");
		if (entity != null) {
			ft.replace(R.id.contain, mapFragment).commit();
		} else {
			ft.replace(R.id.contain, listFragment).commit();
		}
		// mapFragment.firstData();listFragment.firstData();
		cbWater.setOnCheckedChangeListener(cbChangeListener);
		cbCover.setOnCheckedChangeListener(cbChangeListener);
		rgBottom.setOnCheckedChangeListener(rgChangeListener);
		//
		// mListView = (PullToRefreshListView) findViewById(R.id.list_view);
		// mListView.setMode(Mode.PULL_FROM_START);
		// mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
		//
		// @Override
		// public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// // 下拉的时候数据重置
		//
		// // mAdapter.notifyDataSetChanged();
		// //
		// // new FinishRefresh().execute();
		// }
		//
		// });
		// new Thread(new ThreeSecond()).start();
		sendAsk();
		// if (CoverUtils.getBooleanSharedP(getApplicationContext(), "isremem"))
		rgBottom.check(R.id.rb_list);
	}

	@Override
	protected void onStart() {
		super.onStart();
		getDataFromDB();
	}

	private static void getDataFromDB() {
		ArrayList<Entity> alTemp = new ArrayList<Entity>();
		alTemp = (ArrayList<Entity>) douyadb.queryAll();
		if (alTemp != null) {
			items.clear();
			waterItems.clear();
			coverItems.clear();
			for (Entity entity : alTemp) {
				if (entity.getTag().equals("cover")) {
					coverItems.add(entity);
					items.add(entity);
				} else {
					waterItems.add(entity);
					items.add(entity);
				}
			}
		}
		// sort items
		items = (ArrayList<Entity>) sortList(items);
		waterItems = (ArrayList<Entity>) sortList(waterItems);
		coverItems = (ArrayList<Entity>) sortList(coverItems);
		// getDatas();
		listFragment.firstData();
		mapFragment.firstData();
		// setAllChecked();
	}

	static List<Entity> sortList(List<Entity> items) {
		List<Entity> sortItems = new ArrayList<Entity>();
		for (Entity i : items) {
			if (i.getStatus() == Status.EXCEPTION_1
					|| i.getStatus() == Status.EXCEPTION_2
					|| i.getStatus() == Status.EXCEPTION_3) {
				sortItems.add(i);
			}
		}
		for (Entity i : items) {
			if (i.getStatus() == Status.REPAIR
					|| i.getStatus() == Status.SETTING_PARAM
					|| i.getStatus() == Status.SETTING_FINISH)
				sortItems.add(i);
		}
		for (Entity i : items) {
			if (i.getStatus() == Status.NORMAL) {
				sortItems.add(i);
			}
		}
		return sortItems;
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 存储显示哪些
	}

	/**
	 * 菜单、返回键响应
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click(); // 调用双击退出函数
		}
		return false;
	}

	/**
	 * 双击退出函数
	 */
	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			// exit
			AppManager.getAppManager().AppExit(getApplicationContext());
		}
	}

	private android.widget.CompoundButton.OnCheckedChangeListener cbChangeListener = new android.widget.CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			switch (((CheckBox) buttonView).getId()) {
			case R.id.cb_water:
				if (isChecked) {
					// 水位被选中
					flag += 0x10;
				} else {
					// 水位未被选中
					flag -= 0x10;
				}
				break;
			case R.id.cb_cover:
				if (isChecked) {
					// 井盖
					flag += 0x01;
				} else {
					// 井盖未被选中
					flag -= 0x01;
				}
				break;
			}

			switch (flag) {
			case 0x11:
				flagChecked = 0;
				// 都显示
				if (flagWhitchIsCurrent == 1)
					listFragment.update(0);
				// 地图都显示
				else
					mapFragment.update(0);
				break;
			case 0x10:
				flagChecked = 1;
				// 只显示水位
				// 让fargment来更新
				if (flagWhitchIsCurrent == 1)
					listFragment.update(1);
				else
					mapFragment.update(1);
				break;
			case 0x01:
				flagChecked = 2;
				// 只显示井盖
				if (flagWhitchIsCurrent == 1)
					listFragment.update(2);
				else
					mapFragment.update(2);
				break;
			case 0x00:
				flagChecked = 3;
				// 什么都不显示了
				if (flagWhitchIsCurrent == 1)
					listFragment.update(3);
				else
					mapFragment.update(3);
				break;
			}
		}
	};

	private OnCheckedChangeListener rgChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			switch (checkedId) {
			case R.id.rb_list:
				// 显示列表
				// 再次切换进来 仍然会再实例化 刷新
				flagWhitchIsCurrent = 1;
				getFragmentManager().beginTransaction()
						.replace(R.id.contain, listFragment).commit();
				break;
			case R.id.rb_map:
				// 显示地图
				// mapFragment.firstData();
				flagWhitchIsCurrent = 2;
				getFragmentManager().beginTransaction()
						.replace(R.id.contain, mapFragment).commit();
				break;
			}

		}

	};
	private static boolean flagReceived = false;

	public void sendAsk() {
		askMsg.function = (byte) 0x0D;
		askMsg.data = null;
		askMsg.length = CoverUtils.short2ByteArray((short) 7);
		byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
		byte[] str_ = CRC16M.getSendBuf(CoverUtils.bytes2HexString(checkMsg));
		askMsg.check[0] = str_[str_.length - 1];
		askMsg.check[1] = str_[str_.length - 2];
		sendMessage(askMsg, ACTION);
	}

	private class ThreeSecond implements Runnable {

		@Override
		public void run() {
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (flagIsReceivedMsg == false) {
				// setNotify("网络连接异常", "与服务器链接异常中断");
				handler2.sendEmptyMessage(0x01);
				Intent intent = new Intent();
				intent.setClass(CoverList.this, MainActivity.class);
				// finish();
				// startActivity(intent);
				flagIsReceivedMsg = true;
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

	public static class CoverListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			items.clear();
			waterItems.clear();
			coverItems.clear();
			byte[] recv = intent.getByteArrayExtra("msg");
			if (recv[0] == 0x04) {
				final int dataLength = 20;
				int numOfEntity = (recv.length - 1) / dataLength;
				byte[] idByte = new byte[2];
				int i = 0;
				for (int j = 0; j < numOfEntity; j++) {
					Entity entity = new Entity();
					idByte[1] = recv[i++ + 1];
					idByte[0] = recv[i++ + 1];
					entity.setId(CoverUtils.getShort(idByte));
					entity.setTag(recv[i++ + 1] == (byte) 0x10 ? "cover"
							: "level");
					byte[] longTi = new byte[8];
					for (int k = 0, t = i; i < t + 8; i++) {
						longTi[k++] = recv[i + 1];
					}
					byte[] laTi = new byte[8];
					for (int k = 0, t = i; i < t + 8; i++) {
						laTi[k++] = recv[i + 1];
					}
					entity.setLongtitude(CoverUtils.byte2Double(longTi));
					entity.setLatitude(CoverUtils.byte2Double(laTi));
					switch (recv[i++ + 1]) {
					case 0x01:
						entity.setStatus(Status.NORMAL);
						break;
					case 0x02:
						entity.setStatus(Status.EXCEPTION_1);
						break;
					case 0x03:
						entity.setStatus(Status.REPAIR);
						break;
					case 0x04:
						entity.setStatus(Status.EXCEPTION_2);
						break;
					case 0x05:
						entity.setStatus(Status.EXCEPTION_3);
						break;
					case 0x06:
						entity.setStatus(Status.SETTING_FINISH);
						break;
					case 0x07:
						entity.setStatus(Status.SETTING_PARAM);
						break;
					}
					if (entity.getTag().equals("cover")) {
						coverItems.add(entity);
						items.add(entity);
					} else {
						waterItems.add(entity);
						items.add(entity);
					}
				}
				flagIsReceivedMsg = true;
				ListFragment.flagReceived = true;
				// ///////////////////下面应该不更新而通过数据库的更新两种方法，1，oncreate，2，下拉
				// handler.sendEmptyMessage(11);
				if (items != null) {
					douyadb.deleteAll("entity");
					douyadb.addAll(items);
				}
				flagReceived = true;
				getDataFromDB();
				// listFragment.firstData();
				// mapFragment.firstData();
			} else if (recv[0] == 0x03) {
				switch (recv[1]) {
				case 0x01:

					break;
				case 0x02:
					// Toast.makeText(context, "用户名或密码错误,请重新登录",
					// Toast.LENGTH_LONG)
					// .show();
					// Intent loginIntent = new Intent();
					// loginIntent.setClass(context, MainActivity.class);
					// loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// context.startActivity(loginIntent);
					// finish();
					break;
				case 0x03:

					break;
				}
			}
		}
	}

	public static void setAllChecked() {
		cbWater.setChecked(true);
		cbCover.setChecked(true);
	}

	public static void setChecked(int checkNums) {
		switch (checkNums) {
		case 0:
			cbWater.setChecked(true);
			cbCover.setChecked(true);
			break;
		case 1:
			cbWater.setChecked(true);
			cbCover.setChecked(false);
			break;
		case 2:
			cbWater.setChecked(false);
			cbCover.setChecked(true);
			break;
		case 3:
			cbWater.setChecked(false);
			cbCover.setChecked(false);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.setting) {
			// 进入设置界面
			Intent intent = new Intent(this, SoftwareSettings.class);
			startActivity(intent);
		}
		if (v.getId() == R.id.iv_reload) {
			synchronized (this) {
				if (!CoverUtils.isServiceRunning(getApplicationContext(),
						"com.cover.service.InternetService")) {
					handler.sendEmptyMessage(0x30);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(0x11);
				sendAsk();
				flagReceived = false;
				new Refresh().execute();
			}
		}
	}

	private class Refresh extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			if (flagReceived == false) {
				handler.sendEmptyMessage(0x20);
			} else
				handler.sendEmptyMessage(0x10);
			return null;
		}
	}

	private void getDatas() {
		items.clear();
		waterItems.clear();
		coverItems.clear();
		for (int i = 0; i < (100 - 1) / 5; i++) {
			if (i <= 5) {
				Entity entity = new Entity((short) 1, Status.REPAIR, "水位",
						34.26667, 108.95000);
				waterItems.add(entity);
				items.add(entity);
			} else if (i <= 10) {
				Entity entity = new Entity((short) 2, Status.NORMAL, "井盖",
						34.26667 + 0.1 * new Random().nextFloat(),
						108.95000 + 0.1 * new Random().nextFloat());
				coverItems.add(entity);
				items.add(entity);
			} else {
				Entity entity = new Entity((short) 2, Status.EXCEPTION_1, "井盖",
						34.26667 + 0.1 * new Random().nextFloat(),
						108.95000 + 0.1 * new Random().nextFloat());
				coverItems.add(entity);
				items.add(entity);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!ft.isEmpty()) {
			if (listFragment != null) {
				ft.remove(listFragment);
			}
			if (mapFragment != null) {
				ft.remove(mapFragment);
			}
		}
		AppManager.getAppManager().finishActivity(this);
	}

	public void setNotify(String tagID, String arg) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.icon; // 通知图标
		CharSequence tickerText = "信息"; // 状态栏显示的通知文本提示
		long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示
		Notification notification = new Notification(icon, tickerText, when);
		if (CoverUtils.getIntSharedP(getApplicationContext(), "setAlarmOrNot") == 1)
			notification.defaults |= Notification.DEFAULT_ALL;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Context context = getApplicationContext(); // 上下文
		CharSequence contentTitle = tagID; // 通知栏标题
		CharSequence contentText = arg; // 通知栏内容
		notification.setLatestEventInfo(context, contentTitle, contentText,
				null);
		mNotificationManager.notify(0, notification);
	}
}

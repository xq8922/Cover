package com.cover.ui;

import java.util.ArrayList;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.cover.bean.Entity;
import com.cover.bean.Message;
import com.cover.bean.Entity.Status;
import com.cover.fragment.ListFragment;
import com.cover.fragment.MapFragment;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class CoverMapList extends Activity {
	private static final String TAG = "cover";
	Message askMsg = new Message();
	private final String ACTION = "com.cover.service.IntenetService";

	private MapView mMapView;
	private static BaiduMap mBaiduMap;
	private LocationClient mLocationClient;
	private BitmapDescriptor mBitmapDescriptor;
	private MapStatusUpdate myMapStatusUpdate;
	public static ArrayList<Entity> items;
	public static ArrayList<Entity> waterItems;
	public static ArrayList<Entity> coverItems;
	public static boolean flagSend = false;
	private CheckBox cbWater; // 水位
	private CheckBox cbCover; // 井盖
	public static ListFragment listFragment;
	private static MapFragment mapFragment;
	private byte flag = 0x11; // 0x10 表示水位 0x01表示井盖

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.cover_map_list);

		getDatas();
		Entity e = new Entity();
		e = (Entity) getIntent().getExtras().getSerializable("entity");
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();// get the map
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// normal view
//		{
//			new Thread(new sendAsk()).start();
//		}
		// 设定中心点坐标
		LatLng cenpt = new LatLng(34.26667, 108.95000);
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(12)
				.build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);
	}

	private android.widget.CompoundButton.OnCheckedChangeListener cbChangeListener = new android.widget.CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			//
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
				// 都显示
				listFragment.update(0);
				break;
			case 0x10:
				// 只显示水位
				// 让fargment来更新
				listFragment.update(1);
				break;
			case 0x01:
				// 只显示井盖
				listFragment.update(2);
				break;
			case 0x00:
				// 什么都不显示了
				listFragment.update(3);
				break;
			}
		}
	};

	private void getDatas() {
		for (int i = 0; i < (16 - 1) / 5; i++) {
			if (i <= 1) {

				Entity entity = new Entity((short) 1, "水位_65535",
						Status.REPAIR, "水位", 111, 222);
				waterItems.add(entity);
				items.add(entity);
			} else {
				Entity entity = new Entity((short) 2, "井盖_65535",
						Status.NORMAL, "井盖", 333, 444);
				coverItems.add(entity);
				items.add(entity);
			}
		}
	}

	private OnCheckedChangeListener rgChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			switch (checkedId) {
			case R.id.rb_list:
				// 显示列表
				// 再次切换进来 仍然会再实例化 刷新
				getFragmentManager().beginTransaction()
						.replace(R.id.contain, listFragment).commit();
				break;
			case R.id.rb_map:
				// 显示地图
				getFragmentManager().beginTransaction()
						.replace(R.id.contain, mapFragment).commit();
				break;
			}

		}

	};

	private class sendAsk implements Runnable {

		@Override
		public void run() {
			// ask for data
			// while(flagSend == false){
			askMsg.function = (byte) 0x0D;
			askMsg.data = null;
			askMsg.length = CoverUtils.short2ByteArray((short) 7);

			byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
			byte[] str_ = CRC16M.getSendBuf(CoverUtils
					.bytes2HexString(checkMsg));
			askMsg.check[0] = str_[str_.length - 1];
			askMsg.check[1] = str_[str_.length - 2];
			sendMessage(askMsg, ACTION);
			// try {
			// Thread.sleep(2000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// ///////////////////////////////////////////////
			flagSend = true;
			// }
		}

	}

	public void setAllChecked() {
		cbWater.setChecked(true);
		cbCover.setChecked(true);
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

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// mLocationClient.start();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mMapView.onResume();
		// if (!mLocationClient.isStarted()) mLocationClient.start();
		// mLocationClient.requestLocation();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mBaiduMap.setMyLocationEnabled(false);
		// mLocationClient.stop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mMapView.onDestroy();

	}

}

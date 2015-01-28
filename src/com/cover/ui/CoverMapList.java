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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.cover_map_list);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();// get the map
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// normal view
		{
			// ask for data
			askMsg.function = (byte) 0x0D;
			askMsg.data = null;
			askMsg.length = CoverUtils.short2ByteArray((short) 7);

			byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
			byte[] str_ = CRC16M.getSendBuf(CoverUtils
					.bytes2HexString(checkMsg));
			askMsg.check[0] = str_[str_.length - 1];
			askMsg.check[1] = str_[str_.length - 2];
			sendMessage(askMsg, ACTION);
		}

		// 定义Ground的显示地理范围
		LatLng southwest = new LatLng(39.0, 116.0);
		LatLng northeast = new LatLng(39.9, 116.9);
		LatLngBounds bounds = new LatLngBounds.Builder().include(northeast)
				.include(southwest).build();
		// 定义Ground显示的图片
		BitmapDescriptor bdGround = BitmapDescriptorFactory
				.fromResource(R.drawable.ic_launcher);
		// 定义Ground覆盖物选项
		OverlayOptions ooGround = new GroundOverlayOptions()
				.positionFromBounds(bounds).image(bdGround).transparency(0.8f);

		// 设定中心点坐标
		LatLng cenpt = new LatLng(30.663791, 104.07281);
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(12)
				.build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);

		// //在地图中添加Ground覆盖物
		// mBaiduMap.addOverlay(ooGround);
	}

	public static class CoverMapListReceiver extends BroadcastReceiver {

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

					entity.setId(String.valueOf(CoverUtils.getShort(idByte)));
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
					entity.setId(String.valueOf(CoverUtils.getShort(idByte)));
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
				// msgRecv set onto map.
				{
					// 定义Maker坐标点
					LatLng point = new LatLng(34.272121, 108.951212);
					// 构建Marker图标
					BitmapDescriptor bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.back);
					// 构建MarkerOption，用于在地图上添加Marker
					OverlayOptions option = new MarkerOptions().position(point)
							.icon(bitmap).title("cover");
					// 在地图上添加Marker，并显示
					mBaiduMap.addOverlay(option);
				}
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

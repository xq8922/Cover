package com.cover.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.cover.bean.Entity;
import com.cover.fragment.MapFragment;
import com.wxq.covers.R;

public class SingleMapDetail extends Activity {
	private ImageView back;
	private Entity entity;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private static MapFragment mapFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.map_detail);

		back = (ImageView) findViewById(R.id.back);
		entity = (Entity) getIntent().getExtras().getSerializable("entity");
		mMapView = (MapView) findViewById(R.id.map_detail_view);
		mBaiduMap = mMapView.getMap();// get the map
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// normal view
		// 设定中心点坐标
		LatLng cenpt = new LatLng(entity.getLatitude(), entity.getLongtitude());
		// 构建Marker图标
		BitmapDescriptor bitmap;
		switch (entity.getStatus()) {
		case REPAIR:
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.red_small);
			break;
		case NORMAL:
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.map_green_small);
			break;
		default:
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.map_yellow_small);
		}
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(cenpt)
				.icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);

		TextView location = new TextView(getApplicationContext());
		location.setText(entity.getTag() + "，" + entity.getId());
		location.setTextColor(Color.BLACK);
		InfoWindow info = new InfoWindow(location, new LatLng(
				entity.getLatitude(), entity.getLongtitude()), -40);
		mBaiduMap.showInfoWindow(info);

		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(12)
				.build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				TextView location = new TextView(getApplicationContext());
				// location.setBackgroundResource(R.drawable.cover);
				// location.setPadding(30, 20, 30, 50);
				location.setText(entity.getTag() + "，" + entity.getId());
				location.setTextColor(Color.BLACK);
				// location.setBackgroundColor(255);
				// location.setBackgroundDrawable(R.drawable.bg_password_normal);
				InfoWindow info = new InfoWindow(location, arg0.getPosition(),
						-16);
				mBaiduMap.showInfoWindow(info);
				return false;
			}
		});

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	/*
	 * private void Loc(){ MyLocationData myLocationOverlay = new
	 * MyLocationData(mMapView); LocationData locData = new LocationData();
	 * //手动将位置源置为天安门，在实际应用中，请使用百度定位SDK获取位置信息，要在SDK中显示一个位置，需要使用百度经纬度坐标（bd09ll）
	 * locData.latitude = 39.945; locData.longitude = 116.404; locData.direction
	 * = 2.0f; myLocationOverlay.setData(locData);
	 * mMapView.getOverlays().add(myLocationOverlay); mMapView.refresh();
	 * mMapView.getController().animateTo(new
	 * GeoPoint((int)(locData.latitude*1e6), (int)(locData.longitude* 1e6)));
	 * 
	 * }
	 */
	private void popup() {
		// pop demo
		// 创建pop对象，注册点击事件监听接口
		// PopupOverlay pop = new PopupOverlay(mMapView,new PopupClickListener()
		// {
		// @Override
		// public void onClickedPopup(int index) {
		// //在此处理pop点击事件，index为点击区域索引,点击区域最多可有三个
		// }
		// });
		// /** 准备pop弹窗资源，根据实际情况更改
		// * 弹出包含三张图片的窗口，可以传入三张图片、两张图片、一张图片。
		// * 弹出的窗口，会根据图片的传入顺序，组合成一张图片显示.
		// * 点击到不同的图片上时，回调函数会返回当前点击到的图片索引index
		// */
		// Bitmap[] bmps = new Bitmap[3];
		// try {
		// bmps[0] =
		// BitmapFactory.decodeStream(getAssets().open("marker1.png"));
		// bmps[1] =
		// BitmapFactory.decodeStream(getAssets().open("marker2.png"));
		// bmps[2] =
		// BitmapFactory.decodeStream(getAssets().open("marker3.png"));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// //弹窗弹出位置
		// GeoPoint ptTAM = new GeoPoint((int)(39.915 * 1E6), (int) (116.404 *
		// 1E6));
		// //弹出pop,隐藏pop
		// pop.showPopup(bmps, ptTAM, 32);
		// 隐藏弹窗
		// pop.hidePop();

	}

}

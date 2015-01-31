package com.cover.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.cover.adapter.CoverAdapter;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.ui.CoverList;
import com.cover.ui.CoverMapList;
import com.cover.ui.Detail;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class MapFragment extends Fragment {

	private static final String TAG = "cover";
	Message askMsg = new Message();
	private final String ACTION = "com.cover.service.IntenetService";
	private CoverAdapter adapter;
	private int flag = 0; // 0 全都显示 1显示水位 2显示井盖

	private MapView mMapView;
	private static BaiduMap mBaiduMap;
	private LocationClient mLocationClient;
	private BitmapDescriptor mBitmapDescriptor;
	private MapStatusUpdate myMapStatusUpdate;
	private static ArrayList<Entity> items = new ArrayList<Entity>();
	private static ArrayList<Entity> waterItems = new ArrayList<Entity>();
	private static ArrayList<Entity> coverItems = new ArrayList<Entity>();
	private static ArrayList<Entity> nullItems = new ArrayList<Entity>();
	private static Map<LatLng, Entity> markerEntity1 = new HashMap<LatLng, Entity>();
	private static Map<LatLng, Entity> markerEntity2 = new HashMap<LatLng, Entity>();
	private static Map<LatLng, Entity> markerEntity3 = new HashMap<LatLng, Entity>();

	public void update(int flag) {
		switch (flag) {
		case 0:
			// 在这里 修改地图上 全部显示 items
			// if(mBaiduMap != null)
			// mBaiduMap.clear();
			Iterator<Entity> it1 = items.iterator();
			while (it1.hasNext()) {
				LatLng point = new LatLng(it1.next().getLatitude(), it1.next()
						.getLongtitude());
				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.red_small);
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap).title("cover");
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(option);
				markerEntity1.put(point, it1.next());
			}
			this.flag = 0;
			break;
		case 1:
			// if(mBaiduMap != null)
			// mBaiduMap.clear();
			Iterator<Entity> it2 = waterItems.iterator();
			while (it2.hasNext()) {
				// LatLng point = new LatLng(it2.next().getLatitude(),
				// it2.next().getLongtitude());
				LatLng point = new LatLng(it2.next().getLatitude(), it2.next()
						.getLongtitude());
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.red_small);
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap).title("cover");
				mBaiduMap.addOverlay(option);
				markerEntity2.put(point, it2.next());
			}
			// 只显示水位
			this.flag = 1;
			break;
		case 2:

			// if(mBaiduMap != null)
			// mBaiduMap.clear();
			Iterator<Entity> it3 = coverItems.iterator();
			while (it3.hasNext()) {
				LatLng point = new LatLng(it3.next().getLatitude(), it3.next()
						.getLongtitude());
				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.red_small);
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap).title("cover");
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(option);
				markerEntity3.put(point, it3.next());
			}
			// adapter.update(coverItems);
			this.flag = 2;
			break;
		case 3:
			// 什么都不显示
			if (mBaiduMap != null)
				mBaiduMap.clear();
			// adapter.update(nullItems);
			this.flag = 3;
			break;
		case 4:
			// Entity entity = (Entity)
			// getArguments().getSerializable("entity");
			// System.out.println("test Map"+entity);
		}
	}

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SDKInitializer.initialize(getActivity().getApplicationContext());
		((CoverList) getActivity()).setAllChecked();
		// getDatas();
		View view = inflater.inflate(R.layout.cover_map_list, null);
		// Entity entity = (Entity) getArguments().getSerializable("entity");

		mMapView = (MapView) view.findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();// get the map
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// normal view
		//
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
		return view;
	}

	public void firstData() {
		this.items = ((CoverList) getActivity()).items;
		this.waterItems = ((CoverList) getActivity()).waterItems;
		this.coverItems = ((CoverList) getActivity()).coverItems;
		System.out.println("uuuuuuuuu---" + this.items);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((CoverList) getActivity()).setAllChecked();
		// 全部显示
		Entity e = CoverList.entity;
		// e = (Entity)
		// getActivity().getIntent().getExtras().getSerializable("entity");
		if (e == null) {
			Log.i(TAG, "test entity is null");
			Iterator<Entity> it = items.iterator();
			while (it.hasNext()) {
				LatLng point = new LatLng(34.272121 + 0.1, 108.951212 - 0.1);
				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.red_small);
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap).title("cover");
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(option);
				markerEntity1.put(point, it.next());
				mBaiduMap.setMyLocationEnabled(true);
				// mBaiduMap.
				mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

					@Override
					public boolean onMarkerClick(Marker arg0) {
						// 进入详情界面 传进对象
						Entity entity = null;

						switch (flag) {
						case 0:
							entity = markerEntity1.get(arg0.getPosition());
							break;
						case 1:
							entity = markerEntity2.get(arg0.getPosition());
							break;
						case 2:
							entity = markerEntity3.get(arg0.getPosition());
							break;
						}
						Intent intent = new Intent(getActivity(), Detail.class);

						intent.putExtra("entity", entity);
						startActivity(intent);
						return false;
					}
				});
			}
		} else {
			Log.i(TAG, "test entity is not null" + e);
			LatLng point = new LatLng(34.272121 - 0.1, 108.951212 - 0.1);
			// 构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.red_small);
			// 构建MarkerOption，用于在地图上添加Marker
			OverlayOptions option = new MarkerOptions().position(point)
					.icon(bitmap).title("cover");
			// 在地图上添加Marker，并显示
			mBaiduMap.addOverlay(option);
			CoverList.entity = null;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// mLocationClient.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		// if (!mLocationClient.isStarted()) mLocationClient.start();
		// mLocationClient.requestLocation();

	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mBaiduMap.setMyLocationEnabled(false);
		// mLocationClient.stop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();

	}

}

package com.cover.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.cover.adapter.CoverAdapter;
import com.cover.bean.Entity;
import com.cover.bean.Message;
import com.cover.service.InternetService;
import com.cover.ui.CoverList;
import com.cover.ui.Detail;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.wxq.covers.R;

public class ListFragment extends Fragment {

	private CoverAdapter adapter;
	private int flag = 0; // 0 全都显示 1显示水位 2显示井盖
	private PullToRefreshListView lv;
	private ArrayList<Entity> items = new ArrayList<Entity>();
	private ArrayList<Entity> waterItems = new ArrayList<Entity>();
	private ArrayList<Entity> coverItems = new ArrayList<Entity>();
	private ArrayList<Entity> nullItems = new ArrayList<Entity>();
	public boolean flagWhitchIsCurrent;
	public static boolean flagReceived = false;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0x20:
				Toast.makeText(CoverList.myContext, "刷新失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case 0x10:
				Toast.makeText(CoverList.myContext, "刷新成功", Toast.LENGTH_SHORT)
						.show();
				break;
			case 0x30:
				getActivity().startService(
						new Intent(getActivity(), InternetService.class));
				break;
			}
		}

	};

	public void update(int flag) {
		switch (flag) {
		case 0:
			adapter.update(items);
			this.flag = 0;
			break;
		case 1:
			adapter.update(waterItems);
			this.flag = 1;
			break;
		case 2:
			adapter.update(coverItems);
			this.flag = 2;
			break;
		case 3:
			// 什么都不显示
			adapter.update(nullItems);
			this.flag = 3;
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list_fragment, null);
		// lv = (ListView) view.findViewById(R.id.lv_coverlist_cover);
		lv = (PullToRefreshListView) view.findViewById(R.id.list_view);
		lv.setMode(Mode.PULL_FROM_START);
		lv.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// Do work to refresh the list here.
				if (!CoverUtils.isServiceRunning(getActivity(),
						"com.cover.service.InternetService")) {
					handler.sendEmptyMessage(0x30);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				flagReceived = false;
				sendAsk();
				new FinishRefresh().execute();
			}
		});
		// new listFresh().execute();

		return view;
	}

	private class FinishRefresh extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			int i = 0;
			while (i++ < 150) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (flagReceived == true) {
					handler.sendEmptyMessage(0x10);
					return null;
				}
			}
			handler.sendEmptyMessage(0x20);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// adapter.notifyDataSetChanged();
			lv.onRefreshComplete();
			super.onPostExecute(result);
		}
	}

	private class listFresh extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			adapter = new CoverAdapter(getActivity(), items);
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(itemClickListener);
			update(CoverList.flagChecked);
			CoverList.setChecked(CoverList.flagChecked);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			return null;
		}

	}

	public void sendAsk() {
		Message askMsg = new Message();
		final String ACTION = "com.cover.service.IntenetService";
		askMsg.function = (byte) 0x0D;
		askMsg.data = null;
		askMsg.length = CoverUtils.short2ByteArray((short) 7);
		byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
		byte[] str_ = CRC16M.getSendBuf(CoverUtils.bytes2HexString(checkMsg));
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
		getActivity().sendBroadcast(serviceIntent);
	}

	public void firstData() {
		this.items = CoverList.items;
		this.waterItems = CoverList.waterItems;
		this.coverItems = CoverList.coverItems;
		adapter = new CoverAdapter(getActivity(), items);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(itemClickListener);
		this.update(CoverList.flagChecked);
		CoverList.setChecked(CoverList.flagChecked);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 获取到activity里面的数据并进行显示
//		new listFresh().execute();
		// update(CoverList.flagChecked);
		// CoverList.setChecked(CoverList.flagChecked);
		adapter = new CoverAdapter(getActivity(), items);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(itemClickListener);
		update(CoverList.flagChecked);
		CoverList.setChecked(CoverList.flagChecked);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			// 进入详情界面 传进对象
			Entity entity = null;
			switch (flag) {
			case 0:
				entity = items.get(position - 1);
				break;
			case 1:
				entity = waterItems.get(position - 1);
				break;
			case 2:
				entity = coverItems.get(position - 1);
				break;
			}
			// Toast.makeText(getActivity(), entity.toString(), 1).show();
			Intent intent = new Intent(getActivity(), Detail.class);
			intent.putExtra("entity", entity);
			startActivity(intent);
		}
	};

}

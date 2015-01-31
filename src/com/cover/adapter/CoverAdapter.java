package com.cover.adapter;

import java.util.List;

import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.wxq.covers.R;

import android.content.Context;
import android.text.style.UpdateAppearance;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CoverAdapter extends BaseAdapter {

	private Context context;
	private List<Entity> items;

	public CoverAdapter(Context context, List<Entity> items) {
		this.context = context;
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void update(List<Entity> items) {
		this.items = items;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = View.inflate(context, R.layout.item_listview, null);
		ImageView ivType = (ImageView) view.findViewById(R.id.iv_type);
		TextView ivName = (TextView) view.findViewById(R.id.tv_name);
		ImageView ivState = (ImageView) view.findViewById(R.id.iv_state);
		Entity entity = items.get(position);
		if (entity.getTag().equals("level")) {
			ivType.setImageResource(R.drawable.water);
		} else {
			ivType.setImageResource(R.drawable.cover);
		}

		ivName.setText(entity.getName());

		if (Status.NORMAL == entity.getStatus()) {
			ivState.setImageResource(R.drawable.state_normal);
		} else if (Status.REPAIR == entity.getStatus()) {
			ivState.setImageResource(R.drawable.state_reparing);
		} else {
			ivState.setImageResource(R.drawable.state_alarm);
		}

		return view;
	}

}

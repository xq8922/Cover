package com.cover.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.fragment.MapFragment;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class Detail extends Activity implements OnClickListener {
	private static final String TAG = "cover";
	private Entity entity;
	private TextView tvId;
	private ImageView ivState;
	private TextView tvLocation;
	private TextView tvName;
	private ImageView ivType; 
	
	private ImageView back;

	private ImageView ivReparing;

	private ImageView ivFinish;

	private ImageView ivLeave;

	private ImageView ivParam;
	private ImageView ivEnterMap;
	private final String ACTION = "com.cover.service.IntenetService";
	Message msg = new Message();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		
		entity = (Entity) getIntent().getExtras().getSerializable("entity");
		back = (ImageView) findViewById(R.id.back);
		ivType = (ImageView) findViewById(R.id.iv_type_detail);
		tvName = (TextView) findViewById(R.id.tv_name_detail);
		tvId = (TextView) findViewById(R.id.tv_id_detail);
		ivState = (ImageView) findViewById(R.id.iv_state_detail);
		tvLocation = (TextView) findViewById(R.id.tv_location_detail);
		
		ivReparing = (ImageView) findViewById(R.id.iv_reparing);
		ivFinish = (ImageView) findViewById(R.id.iv_finish);
		ivLeave = (ImageView) findViewById(R.id.iv_leave);
		ivParam = (ImageView) findViewById(R.id.iv_param);
		ivEnterMap = (ImageView)findViewById(R.id.iv_entermap_detail);
		
		ivReparing.setOnClickListener(this);
		ivFinish.setOnClickListener(this);
		ivLeave.setOnClickListener(this);
		ivParam.setOnClickListener(this);
		if (entity.getTag().equals("level")) {			
			ivType.setImageResource(R.drawable.water);
		}else if (entity.getTag().equals("cover")) {
			ivType.setImageResource(R.drawable.cover);
		}
		tvName.setText(entity.getName());
		tvId.setText(entity.getId()+"");
		if (Status.NORMAL == entity.getStatus()) {
			ivState.setImageResource(R.drawable.state_normal);
		}else if (Status.REPAIR == entity.getStatus()) {
			ivState.setImageResource(R.drawable.state_reparing);
		}else {
			ivState.setImageResource(R.drawable.state_alarm);
		}
		tvLocation.setText(entity.getLongtitude()+"  "+entity.getLatitude());
		
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
	}

	public void setRepairBegin(Entity entity){		
		msg = CoverUtils.makeMessageExceptCheck((byte)0x0E, CoverUtils.short2ByteArray((short) 10), CoverUtils.short2ByteArray(entity.getId()));
		msg.check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils.msg2ByteArrayExcepteCheck(msg)));
		CoverUtils.sendMessage(getApplicationContext(), msg, ACTION);
	}
	public void setRepairEnd(Entity entity){		
		msg = CoverUtils.makeMessageExceptCheck((byte)0x0F, CoverUtils.short2ByteArray((short) 10), CoverUtils.short2ByteArray(entity.getId()));
		msg.check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils.msg2ByteArrayExcepteCheck(msg)));
		CoverUtils.sendMessage(getApplicationContext(), msg, ACTION);
	}
	public void setUnAlarm(Entity entity){		
		msg = CoverUtils.makeMessageExceptCheck((byte)0x10, CoverUtils.short2ByteArray((short) 10), CoverUtils.short2ByteArray(entity.getId()));
		msg.check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils.msg2ByteArrayExcepteCheck(msg)));
		CoverUtils.sendMessage(getApplicationContext(), msg, ACTION);
	}
	
	
	
	public static class DetailReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] recv = intent.getByteArrayExtra("msg");
			final int length = 1;
			switch(recv[0]){
			case 0x06://报警解除
				break;
			case 0x07://begin repair
				break;
			case 0x08://end repair
				break;
			case 0x0A://报警信息接收成功
				break;
				
				
			}
		}

	}
	
	@Override
	public void onClick(View v) {
		Toast.makeText(this, v.getId()+"", Toast.LENGTH_SHORT).show();
		switch (v.getId()) {
		case R.id.iv_reparing:
			if(entity.getStatus() != Status.NORMAL && entity.getStatus()!=Status.REPAIR)
				setRepairBegin(entity);
			else
				Toast.makeText(getApplicationContext(), "不可点击维修", Toast.LENGTH_LONG).show();
			break;
		case R.id.iv_finish:
			if(entity.getStatus() != Status.NORMAL)
				Toast.makeText(getApplicationContext(), "不可点击完成", Toast.LENGTH_LONG).show();
			else{
				setRepairEnd(entity);
			}
			break;
		case R.id.iv_leave:
//			if()
			if(entity.getStatus() != Status.NORMAL && entity.getStatus()!=Status.REPAIR){
				setUnAlarm(entity);
			}
			break;
		case R.id.iv_param:
			// 参数按钮的点击事件
			Intent intent = new Intent(Detail.this, ParamSettingActivity.class);
			intent.putExtra("entity", entity);
			startActivity(intent);
			break;
		case R.id.iv_entermap_detail:
			Toast.makeText(this, "eeeeeee", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(Detail.this, MapFragment.class);
			i.putExtra("entity", entity);
			startActivity(i);
			break;
		}
	}

}

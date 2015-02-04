package com.cover.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.fragment.MapFragment;
import com.cover.ui.ParamSettingActivity.Timer;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class Detail extends Activity implements OnClickListener {
	private static final String TAG = "cover";
	public static final int MINITE = 24 * 60;
	private Entity entity;
	private TextView tvId;
	private ImageView ivState;
	private TextView tvLocation;
	private TextView tvName;
	private ImageView ivType;
	private ImageView back;
	static ImageView ivReparing;
	static ImageView ivFinish;
	// private ImageView ivLeave;
	private ImageView ivParam;
	private ImageView ivEnterMap;
	private static MapFragment mapFragment;
	private final String ACTION = "com.cover.service.IntenetService";
	Message msg = new Message();
	private boolean flagThreadIsStart = false;
	public static boolean flagIsSetSuccess = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		// ivLeave = (ImageView) findViewById(R.id.iv_leave);
		ivParam = (ImageView) findViewById(R.id.iv_param);
		ivEnterMap = (ImageView) findViewById(R.id.iv_entermap_detail);

		ivReparing.setOnClickListener(this);
		ivFinish.setOnClickListener(this);
		// ivLeave.setOnClickListener(this);
		ivParam.setOnClickListener(this);
		ivEnterMap.setOnClickListener(this);
		if (entity.getTag().equals("level")) {
			ivType.setImageResource(R.drawable.water);
		} else {
			ivType.setImageResource(R.drawable.cover);
		}
		tvName.setText(entity.getName());
		tvId.setText(entity.getId() + "");
		if (Status.NORMAL == entity.getStatus()) {
			ivState.setImageResource(R.drawable.state_normal);
		} else if (Status.REPAIR == entity.getStatus()) {
			ivState.setImageResource(R.drawable.state_reparing);
		} else {
			ivState.setImageResource(R.drawable.state_alarm);
		}
		tvLocation.setText(new java.text.DecimalFormat("#.000000")
				.format(entity.getLatitude())
				+ " "
				+ new java.text.DecimalFormat("#.000000").format(entity
						.getLongtitude()));

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().finishActivity(this);
	}

	class Timer implements Runnable {

		@Override
		public void run() {
			flagThreadIsStart = true;
			if (flagThreadIsStart) {
				try {
					Thread.sleep(60 * 1000 * MINITE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!flagIsSetSuccess) {
					Toast.makeText(getApplicationContext(), "参数设置失败",
							Toast.LENGTH_SHORT).show();
				}
			}
			flagThreadIsStart = false;
		}
	}

	public void setRepairBegin(Entity entity) {
		byte[] b = CoverUtils.short2ByteArray(entity.getId());
		byte[] t = new byte[3];
		t[0] = b[0];
		t[1] = b[1];
		t[2] = entity.getName() == "cover" ? (byte) 0x10 : (byte) 0x2C;
		msg = CoverUtils.makeMessageExceptCheck((byte) 0x0E,
				CoverUtils.short2ByteArray((short) (7 + 3)), t);
		byte[] check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = check[check.length - 1];
		msg.check[1] = check[check.length - 2];
		sendMessage(msg, ACTION);
	}

	public void setRepairEnd(Entity entity) {
		byte[] b = CoverUtils.short2ByteArray(entity.getId());
		byte[] t = new byte[3];
		t[0] = b[0];
		t[1] = b[1];
		t[2] = entity.getName() == "cover" ? (byte) 0x10 : (byte) 0x2C;
		msg = CoverUtils.makeMessageExceptCheck((byte) 0x0F,
				CoverUtils.short2ByteArray((short) (7 + 3)), t);
		byte[] check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = check[check.length - 1];
		msg.check[1] = check[check.length - 2];
		sendMessage(msg, ACTION);
	}

	public void setUnAlarm(Entity entity) {
		byte[] b = CoverUtils.short2ByteArray(entity.getId());
		byte[] t = new byte[3];
		t[0] = b[0];
		t[1] = b[1];
		t[2] = entity.getName() == "cover" ? (byte) 0x10 : (byte) 0x2C;
		msg = CoverUtils.makeMessageExceptCheck((byte) 0x10,
				CoverUtils.short2ByteArray((short) (7 + 3)), t);
		byte[] check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = check[check.length - 1];
		msg.check[1] = check[check.length - 2];
		sendMessage(msg, ACTION);
	}

	public static class DetailReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] recv = intent.getByteArrayExtra("msg");
			final int length = 1;
			switch (recv[0]) {
			case 0x06:// 报警解除
				Toast.makeText(context, "报警解除", Toast.LENGTH_LONG).show();
				break;
			case 0x07:// begin repair
				Toast.makeText(context, "开始维修命令上传成功", Toast.LENGTH_LONG).show();
				// ivReparing.setClickable(false);
				break;
			case 0x08:// end repair
				Toast.makeText(context, "设置结束维修上传成功 ", Toast.LENGTH_LONG)
						.show();
				ivFinish.setClickable(false);
				break;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// 通过MenuInflater将XML 实例化为 Menu Object
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_exit_settings:
			Message msg = new Message();
			msg.data = "13468833168".getBytes();
			msg.function = (byte) 0x12;
			msg.length = CoverUtils
					.short2ByteArray((short) (7 + msg.data.length));
			byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(msg);
			byte[] str_ = CRC16M.getSendBuf(CoverUtils
					.bytes2HexString(checkMsg));
			msg.check[0] = str_[str_.length - 1];
			msg.check[1] = str_[str_.length - 2];
			sendMessage(msg, ACTION);
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_reparing:
			if (entity.getStatus() != Status.NORMAL
					&& entity.getStatus() != Status.REPAIR
					&& entity.getStatus() != Status.SETTING_FINISH
					&& entity.getStatus() != Status.SETTING_PARAM)
				setRepairBegin(entity);
			else
				Toast.makeText(getApplicationContext(), "当前状态下不可点击维修",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.iv_finish:
			if (entity.getStatus() == Status.REPAIR
					|| entity.getStatus() == Status.NORMAL
					|| entity.getStatus() == Status.SETTING_FINISH
					|| entity.getStatus() == Status.SETTING_PARAM)
				Toast.makeText(getApplicationContext(), "当前状态下不可点击完成",
						Toast.LENGTH_LONG).show();
			else {
				if (!flagThreadIsStart) {
					setRepairEnd(entity);
					new Thread(new Timer()).start();
				} else {
					Toast.makeText(getApplicationContext(), "已上传，请勿重复点击",
							Toast.LENGTH_SHORT).show();
				}

			}
			break;
		case R.id.iv_param:
			// 参数按钮的点击事件
			Intent intent = new Intent(Detail.this, ParamSettingActivity.class);
			intent.putExtra("entity", entity);
			startActivity(intent);
			break;
		case R.id.iv_entermap_detail:
			Intent i = new Intent(Detail.this, SingleMapDetail.class);
			// Bundle b = new Bundle();
			// b.putSerializable("entity", entity);
			i.putExtra("entity", entity);
			startActivity(i);
			break;
		}
	}

}

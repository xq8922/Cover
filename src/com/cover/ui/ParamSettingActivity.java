package com.cover.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.dbhelper.Douyatech;
import com.cover.ui.Detail.Timer;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class ParamSettingActivity extends Activity implements OnClickListener {
	private static final String TAG = "cover";
	private final static String ACTION = "com.cover.service.IntenetService";
	Message askMsg = new Message();
	private ImageView back;
	private Entity entity;

	private ImageView type;
	private ImageView ivType;
	private TextView tvName;

	private RelativeLayout rlAlarmFreq;
	private RelativeLayout rlAlarmTime;
	private RelativeLayout rlAlarmAngle;

	private TextView tvAlarmAngle;
	private TextView tvAlarmTime;
	private TextView tvAlarmFreq;
	private ImageView update;
	private short angle = 0;
	private short time = 0;
	private short alarmFrequency = 0;
	private short seconfAlarm = 0;
	final static int MINITE = 30;
	public static boolean flagIsSetSuccess = false;
	private boolean flagThreadIsStart = false;
	Douyatech douyadb = null;
	private TextView tvFrequencyAlarm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paramsetting);

		douyadb = new Douyatech(this);
		entity = (Entity) getIntent().getExtras().getSerializable("entity");
		tvFrequencyAlarm = (TextView) findViewById(R.id.tv_frequency_alarm);
		back = (ImageView) findViewById(R.id.setting_param_back);
		ivType = (ImageView) findViewById(R.id.iv_type_param);
		tvName = (TextView) findViewById(R.id.tv_name_param);
		tvAlarmAngle = (TextView) findViewById(R.id.tv_angle_param);
		tvAlarmTime = (TextView) findViewById(R.id.tv_time_param);
		tvAlarmFreq = (TextView) findViewById(R.id.tv_freq_param);
		rlAlarmAngle = (RelativeLayout) findViewById(R.id.rl_alarm_angle);
		rlAlarmTime = (RelativeLayout) findViewById(R.id.rl_alarm_time);
		rlAlarmFreq = (RelativeLayout) findViewById(R.id.rl_alarm_freq);
		update = (ImageView) findViewById(R.id.update);
		update.setOnClickListener(this);
		rlAlarmTime.setOnClickListener(this);
		rlAlarmAngle.setOnClickListener(this);
		rlAlarmFreq.setOnClickListener(this);
		if (entity.getTag().equals("level")) {
			ivType.setImageResource(R.drawable.water);
		} else {// if (entity.getTag().equals("井盖"))
			ivType.setImageResource(R.drawable.cover);
		}
		String status = "";
		if (entity.getStatus() == Status.SETTING_FINISH)
			status = "_撤防中";
		else if (entity.getStatus() == Status.SETTING_PARAM)
			status = "_参数设置中";
		tvName.setText(entity.getTag() + status);

		back.setOnClickListener(this);

		if (entity.getTag().equals("level")) {
			rlAlarmAngle.setVisibility(View.GONE);
			tvFrequencyAlarm.setText("二次报警");
		}
		// ask for result of settings
		// int le+ngth = 7 + msg.length();
		// askMsg.length
		// msgAsk.data = msg.getBytes();
		// msgAsk.function = 0x0c;
		// msgAsk.length = Integer.toHexString(length).getBytes();
		// byte[] checkMsg = new byte[3 + msg.length()];
		// msgAsk.check = CoverUtils.genCRC(checkMsg, checkMsg.length);
		// sendMessage()

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
				try {// 60 * 1000 * MINITE
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!flagIsSetSuccess) {
					Looper.prepare();
					Toast.makeText(getApplicationContext(), "参数设置失败",
							Toast.LENGTH_SHORT).show();
				}
			}
			flagThreadIsStart = false;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_param_back:
			// 1.保存所做的修改
			onBackPressed();
			break;
		case R.id.rl_alarm_angle:
			final EditText et_Ip1 = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle("报警角度")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(et_Ip1)
					.setPositiveButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 保存设置
									// 修改显示
									tvAlarmAngle.setText(et_Ip1.getText()
											.toString().trim()
											+ "度");
									angle = Short.valueOf(et_Ip1.getText()
											.toString().trim());
								}

							}).setNegativeButton("取消", null).show();
			break;
		case R.id.rl_alarm_time:
			final EditText et_Ip2 = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle("定时上报")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(et_Ip2)
					.setPositiveButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 保存设置
									// 修改显示
									tvAlarmTime.setText(et_Ip2.getText()
											.toString().trim()
											+ "小时");
									time = Short.valueOf(et_Ip2.getText()
											.toString().trim());
								}

							}).setNegativeButton("取消", null).show();
			break;
		case R.id.rl_alarm_freq:
			final EditText et_Ip3 = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle("报警频率")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(et_Ip3)
					.setPositiveButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 保存设置
									// 修改显示
									tvAlarmFreq.setText(et_Ip3.getText()
											.toString().trim()
											+ "分钟");
									alarmFrequency = Short.valueOf(et_Ip3
											.getText().toString().trim());
								}
							}).setNegativeButton("取消", null).show();
			break;
		case R.id.update:
			if (!flagThreadIsStart) {

				String nameID = entity.getTag() + "_" + entity.getId();
				if (!douyadb.isExist("setting", nameID)) {
					sendArgSettings(entity);
					new Thread(new Timer()).start();
					douyadb.add("setting", nameID);
				} else {
					Toast.makeText(getApplicationContext(), "已上传，请勿重复点击",
							Toast.LENGTH_SHORT).show();
				}

				// sendArgSettings(entity);
				// Toast.makeText(ParamSettingActivity.this, "上传中...",
				// 0).show();
				// new Thread(new Timer()).start();
			} else {
				// Toast.makeText(getApplicationContext(), "已上传，请勿重复点击",
				// Toast.LENGTH_SHORT).show();
			}
			// flagThreadIsStart = !flagThreadIsStart;
			break;
		}
	}

	public void sendArgSettings(Entity entity) {
		// 是否有小数
		Message msg = new Message();
		int j = 0;
		short id = entity.getId();
		byte[] tmp = CoverUtils.short2ByteArray(id);
		byte[] data = new byte[11];
		data[j++] = tmp[0];
		data[j++] = tmp[1];
		data[j++] = entity.getTag() == "cover" ? (byte) 0x10 : (byte) 0x2C;
		short jiaodu = 10;
		tmp = (entity.getTag() == "cover" ? CoverUtils.short2ByteArray(jiaodu)
				: new byte[] { 0, 0 });
		data[j++] = tmp[0];
		data[j++] = tmp[1];
		short time = 10;
		tmp = CoverUtils.short2ByteArray(time);
		data[j++] = tmp[0];
		data[j++] = tmp[1];
		short alarmFrequency = 10;
		tmp = (entity.getTag() == "cover" ? CoverUtils
				.short2ByteArray(alarmFrequency) : new byte[] { 0, 0 });
		data[j++] = tmp[0];
		data[j++] = tmp[1];
		short secondAlarm = 10;
		tmp = (entity.getTag() == "cover" ? new byte[] { 0, 0 } : CoverUtils
				.short2ByteArray(secondAlarm));
		data[j++] = tmp[0];
		data[j++] = tmp[1];

		msg = CoverUtils.makeMessageExceptCheck((byte) 0x11,
				CoverUtils.short2ByteArray((short) 18), data);
		byte[] tmp1 = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = tmp1[tmp1.length - 1];
		msg.check[1] = tmp1[tmp1.length - 2];
		sendMessage(msg, ACTION);

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

	public static class SettingsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] recv = intent.getByteArrayExtra("msg");
			if (recv[0] == 0x05) {
				int length = 4;
				if (recv[3] == 0x01) {
					Toast.makeText(context, "set success", Toast.LENGTH_LONG)
							.show();
					Message msg = null;
					msg.data = null;
					msg.function = (byte) 0x0B;
					msg.length = CoverUtils.short2ByteArray((short) 7);
					byte[] check = CRC16M.getSendBuf(CoverUtils
							.bytes2HexString(CoverUtils
									.msg2ByteArrayExcepteCheck(msg)));
					msg.check[0] = check[check.length - 2];
					msg.check[1] = check[check.length - 1];
					((ParamSettingActivity) context).sendMessage(msg, ACTION);

				} else if (recv[3] == 0x02) {
					Toast.makeText(context, "set failed", Toast.LENGTH_LONG)
							.show();
				}
				// 需要在刷新列表的时候检测是否超限
			}
		}

	}
}

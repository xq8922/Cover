package com.cover.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cover.bean.Message;
import com.cover.main.MainActivity;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class SoftwareSettings extends Activity implements OnClickListener{
	private static final String TAG = "cover";
	private final String ACTION = "com.cover.service.IntenetService";
	private RelativeLayout rlIp;
	private ImageView back;
	private Switch swAlarm;
	private ImageView ivSwitch;
	private ImageView exit;
	private TextView tvIP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.software_settings);
		
		rlIp = (RelativeLayout) findViewById(R.id.rl_ip);
		back = (ImageView) findViewById(R.id.setting_back);
		swAlarm = (Switch) findViewById(R.id.alarm_switch);
		ivSwitch = (ImageView) findViewById(R.id.iv_switch);
		exit = (ImageView) findViewById(R.id.iv_exit);
		tvIP = (TextView) findViewById(R.id.tv_ip);
		back.setOnClickListener(this);
		swAlarm.setOnClickListener(this);
		rlIp.setOnClickListener(this);
		ivSwitch.setOnClickListener(this);
		exit.setOnClickListener(this);
	}

	public static class SoftwareSettingsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub

		}

	}
	
public void sendChangeUser(Message msg, String action) {
		
		msg = CoverUtils.makeMessageExceptCheck((byte)0x12, CoverUtils.short2ByteArray((short) 18), ("用户名").getBytes());
		msg.check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils.msg2ByteArrayExcepteCheck(msg)));
		sendMessage( msg, ACTION);
		
	}

public void sendChangeIp(Message msg, String action) {
//		Intent intent = new Intent("com.cover.service.InternetService");
//		stopService(intent);
		Intent mainActivity = new Intent();
		mainActivity.putExtra("ip", "");
		mainActivity.setClass(getApplicationContext(), MainActivity.class);
		startActivity(mainActivity);
		msg = CoverUtils.makeMessageExceptCheck((byte)0x12, CoverUtils.short2ByteArray((short) 18), ("用户名").getBytes());
		msg.check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils.msg2ByteArrayExcepteCheck(msg)));
		sendMessage( msg, ACTION);
		
	}
/*
//首先需要接收一个Notification的参数
private void setAlarmParams(Notification notification) {
    //AudioManager provides access to volume and ringer mode control.
     AudioManager volMgr = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
     switch (volMgr.getRingerMode()) {//获取系统设置的铃声模式
        case AudioManager.RINGER_MODE_SILENT://静音模式，值为0，这时候不震动，不响铃
            notification.sound = null;
            notification.vibrate = null;
            break;
        
        case AudioManager.RINGER_MODE_NORMAL://常规模式，值为2，分两种情况：1_响铃但不震动，2_响铃+震动
            Uri ringTone = null;
            //获取软件的设置
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mAppContext);
            if(!sp.contains(SystemUtil.KEY_RING_TONE)){//如果没有生成配置文件，那么既有铃声又有震动
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_SOUND;
            }else{
                String ringFile = sp.getString(SystemUtil.KEY_RING_TONE, null);
                if(ringFile==null){//无值，为空，不播放铃声
                    ringTone=null;
                }else if(!TextUtils.isEmpty(ringFile)){//有铃声：1，默认2自定义，都返回一个uri
                    ringTone=Uri.parse(ringFile);
                }
                notification.sound = ringTone;
                 
                boolean vibrate = sp.getBoolean(SystemUtil.KEY_NEW_MAIL_VIBRATE,true);
                if(vibrate == false){//如果软件设置不震动，那么就不震动了
                    notification.vibrate = null;
                }else{//否则就是需要震动，这时候要看系统是怎么设置的：不震动=0;震动=1；仅在静音模式下震动=2；
                    if(volMgr.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER) == AudioManager.VIBRATE_SETTING_OFF){
                        //不震动
                        notification.vibrate = null;
                    }else if(volMgr.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER) == AudioManager.VIBRATE_SETTING_ONLY_SILENT){
                        //只在静音时震动
                        notification.vibrate = null;
                    }else{
                        //震动
                        notification.defaults |= Notification.DEFAULT_VIBRATE;
                    }
                }
            }
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;//都给开灯
            break;
        default:
            break;
        }
}
public void setAlarm(){
	int i = 0 ;
	switch(i){
	case 0://set jingyin
		
		break;
	case 1://set lingsheng
		break;
	}
}*/
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
public void onClick(View v) {
	switch (v.getId()) {
	case R.id.setting_back:
		// 保存所做的记录
		onBackPressed();
		break;
	case R.id.alarm_switch:
		
		break;
	case R.id.rl_ip:
		// 弹出对话框
		final EditText et_Ip = new EditText(this);
		new AlertDialog.Builder(this) 
		.setTitle("修改IP")  
		.setIcon(android.R.drawable.ic_dialog_info)  
		.setView(et_Ip)
		.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 保存设置
				// 修改显示
				tvIP.setText(et_Ip.getText().toString().trim());
			}
			
		})
		.setNegativeButton("取消", null)
		.show(); 
		break;
	case R.id.iv_switch:
		Toast.makeText(this, "切换用户", 0).show();
		break;
	case R.id.iv_exit:
		Toast.makeText(this, "退出", 0).show();
		break;
	}
}
}

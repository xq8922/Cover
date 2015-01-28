package com.cover.ui;

import com.cover.bean.Entity;
import com.cover.bean.Message;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Detail extends Activity {
	private static final String TAG = "cover";
	private final String ACTION = "com.cover.service.IntenetService";
	Message msg = new Message();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
	}

	public void setRepairBegin(Entity entity){		
		msg = CoverUtils.makeMessageExceptCheck((byte)0x0E, CoverUtils.short2ByteArray((short) 10), CoverUtils.short2ByteArray(entity.getId()));
		msg.check = CoverUtils.msg2ByteArrayExcepteCheck(msg);
		CoverUtils.sendMessage(getApplicationContext(), msg, ACTION);
	}
	public void setRepairEnd(Entity entity){		
		msg = CoverUtils.makeMessageExceptCheck((byte)0x0F, CoverUtils.short2ByteArray((short) 10), CoverUtils.short2ByteArray(entity.getId()));
		msg.check = CoverUtils.msg2ByteArrayExcepteCheck(msg);
		CoverUtils.sendMessage(getApplicationContext(), msg, ACTION);
	}
	public void seUnAlarm(Entity entity){		
		msg = CoverUtils.makeMessageExceptCheck((byte)0x10, CoverUtils.short2ByteArray((short) 10), CoverUtils.short2ByteArray(entity.getId()));
		msg.check = CoverUtils.msg2ByteArrayExcepteCheck(msg);
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

}

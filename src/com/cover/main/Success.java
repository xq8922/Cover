package com.cover.main;

import com.wxq.covers.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Success extends Activity {
	Button btn_send;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.success);
		btn_send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
//				new TCPClient().getRunnable("", PORT, socket)
			}
		});
	}

}

package com.cover.ui;

import com.cover.main.MainActivity;
import com.cover.service.InternetService;
import com.wxq.covers.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class WelcomeActivity extends Activity implements AnimationListener {

	private View welcomeView;

	// InternetService internetService;
	// public ServiceConnection internetServiceConnection = new
	// ServiceConnection() {
	//
	// @Override
	// public void onServiceConnected(ComponentName arg0, IBinder service) {
	// internetService = ((InternetService.InterBinder) service)
	// .getService();
	// }
	//
	// @Override
	// public void onServiceDisconnected(ComponentName arg0) {
	// internetService = null;
	// }
	//
	// };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		// Intent intent = new
		// Intent(WelcomeActivity.this,InternetService.class);、
		startService(new Intent(WelcomeActivity.this, InternetService.class));
		Log.i("cover", "start service");
		welcomeView = findViewById(R.id.ll);
		Animation welAnimation = AnimationUtils.loadAnimation(this,
				R.anim.welcome_animation);
		welcomeView.startAnimation(welAnimation);
		welAnimation.setAnimationListener(this);
		Intent intent = new Intent();
		intent.setClass(WelcomeActivity.this, CoverList.class);
		startActivity(intent);
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onBackPressed() {
		// 屏蔽返回键
	}
}

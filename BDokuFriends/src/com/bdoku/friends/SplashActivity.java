package com.bdoku.friends;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bdoku.controller.SplashTimerTask;
import com.facebook.android.R;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.splash);

	ImageView layout = (ImageView) findViewById(R.id.splashImage);
	layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_grow_from_top));

	ImageView friendsImage = (ImageView) findViewById(R.id.friendsSplash);
	friendsImage.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_bottom));

	@SuppressWarnings("unused")
	SplashTimerTask splashTimerTask = new SplashTimerTask(this);
    }
}
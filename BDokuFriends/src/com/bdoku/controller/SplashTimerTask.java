/**
 * 
 */
package com.bdoku.controller;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;

import com.bdoku.friends.DifficultyChooserActivity;
import com.bdoku.friends.SplashActivity;

/**
 * @author bwinters
 * 
 */
public class SplashTimerTask extends TimerTask {

    private SplashActivity splashActivity = null;
    private Timer timer = null;
    private static final long SPLASH_DELAY = 5000;

    public SplashTimerTask(SplashActivity splashActivity) {
	this.splashActivity = splashActivity;
	this.timer = new Timer();
	timer.schedule(this, SplashTimerTask.SPLASH_DELAY);
    }

    @Override
    public void run() {
	splashActivity.finish();
	Intent difficultyIntent = new Intent().setClass(splashActivity.getApplicationContext(), DifficultyChooserActivity.class);
	splashActivity.startActivity(difficultyIntent);
    }

}

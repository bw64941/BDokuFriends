/**
 * 
 */
package com.bdoku.friends;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * @author bwinters
 * 
 */
public class RestartActivity extends Activity {

    public static final String TIMER_VALUE = "gameTime";

    /*
     * Creates the resume board Alert dialog for game.
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);

	LayoutInflater layout = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	RelativeLayout alertDialog = (RelativeLayout) layout.inflate(R.layout.solved_popup, null);

	Button gameTimeButton = (Button) alertDialog.findViewById(R.id.gameTime_button);
	gameTimeButton.setText("Solve Time:" + getIntent().getStringExtra(RestartActivity.TIMER_VALUE));

	Button ok = (Button) alertDialog.findViewById(R.id.solvOkButton);
	ok.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		finish();
	    }
	});

	Button share = (Button) alertDialog.findViewById(R.id.solvePostButton);
	share.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		setResult(BDokuFriendsActivity.POST_GAME_TIME);
		finish();
	    }
	});

	setContentView(alertDialog);
    }
}

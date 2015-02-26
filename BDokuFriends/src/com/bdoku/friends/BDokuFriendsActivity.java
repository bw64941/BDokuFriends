package com.bdoku.friends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.bdoku.controller.GameController;
import com.bdoku.view.BoardView;
import com.bdoku.view.ControlListener;
import com.bdoku.view.ControlsViewFlipper;
import com.facebook.android.R;

public class BDokuFriendsActivity extends Activity implements ControlListener {

    public static final String TAG = BDokuFriendsActivity.class.getName();
    public static final String RESUME = "resume";
    public static final int POST_GAME_TIME = 200;
    private GameController gameController = null;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
	Log.d(TAG, "onDestroy called");
	super.onDestroy();
	gameController.onDestroy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Log.d(TAG, "onOptionsItemSelected called");
	if (item.getItemId() == R.id.solve) {
	    gameController.setSolvedOn();
	} else if (item.getItemId() == R.id.hint) {
	    gameController.getRandomHint();
	} else {
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	Log.d(TAG, "onCreateOptionsMenu called");
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.board_menu, menu);
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
	Log.d(TAG, "onPause called");
	super.onPause();
	gameController.onPause();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	Log.d(TAG, "onResume called");
	// For scenarios where the main activity is launched and user
	// session is not null, the session state change notification
	// may not be triggered. Trigger it if it's open/closed.
	super.onResume();
	gameController.onResume();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
	Log.d(TAG, "onSaveInstanceState called");
	super.onSaveInstanceState(outState);
	gameController.onSaveInstanceState(outState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
	Log.d(TAG, "onRetainNonConfigurationInstance called");
	return gameController.onRetainNonConfigurationInstance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	Log.d(TAG, "onActivityResult called");
	super.onActivityResult(requestCode, resultCode, data);
	gameController.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * Creates main board view and creates background board entity.
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	Log.d(TAG, "onCreate called");
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.board);

	// // Look up the AdView as a resource and load a request.
	// AdView adView = (AdView) this.findViewById(R.id.adView);
	// adView.loadAd(new AdRequest());

	ControlsViewFlipper controlsView = (ControlsViewFlipper) findViewById(R.id.viewFlipper);
	controlsView.setCustomEventListener(this);

	RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.boardLayout);
	relativeLayout.startLayoutAnimation();

	gameController = new GameController(this, (BoardView) findViewById(R.id.boardView), (ControlsViewFlipper) findViewById(R.id.viewFlipper));
	gameController.onCreate(savedInstanceState);
    }

    @Override
    public void onClickEvent(View v) {
	Log.d(TAG, "onEvent called");
	if (v.getId() == R.id.undo) {
	    gameController.undoLastMove();
	} else if (v.getId() == R.id.share) {
	    gameController.showNext();
	} else if (v.getId() == R.id.backButton) {
	    gameController.showPrevious();
	} else if (v.getId() == R.id.validate) {
	    gameController.checkWorkDoneOnBoard();
	} else if (v.getId() == R.id.postButton) {
	    gameController.postPhoto();
	} else if (v.getId() == R.id.save) {
	    gameController.saveBoard();
	} else {
	    Log.d(TAG, "Unknown Event Caught");
	}
    }

    @Override
    public void onToggleEvent(CompoundButton buttonView, boolean isChecked) {
	gameController.onCheckedChanged(buttonView, isChecked);
    }
}
/**
 * 
 */
package com.bdoku.friends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.bdoku.model.BoardOpen;

/**
 * @author bwinters
 * 
 */
public class ResumeBoardActivity extends Activity implements OnClickListener {

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
	View alertDialog = layout.inflate(R.layout.resumeboardalert, null);

	Button noButton = (Button) alertDialog.findViewById(R.id.resumeNo);
	noButton.setOnClickListener(this);

	Button yesButton = (Button) alertDialog.findViewById(R.id.resumeYes);
	yesButton.setOnClickListener(this);

	Button deleteButton = (Button) alertDialog.findViewById(R.id.delete);
	deleteButton.setOnClickListener(this);

	setContentView(alertDialog);
    }

    @Override
    public void onClick(View v) {
	if (v.getId() == R.id.resumeYes) {
	    finish();
	    Intent boardActivity = new Intent(getApplicationContext(), BDokuFriendsActivity.class);
	    boardActivity.putExtra(BDokuFriendsActivity.RESUME, true);
	    startActivity(boardActivity);
	} else if (v.getId() == R.id.resumeNo) {
	    finish();
	    Intent boardChooser = new Intent(getApplicationContext(), DifficultyChooserActivity.class);
	    startActivity(boardChooser);
	} else if (v.getId() == R.id.delete) {
	    finish();
	    BoardOpen opener = new BoardOpen(this);
	    opener.deleteExistingFile();

	    Intent difficultyChooser = new Intent(getApplicationContext(), DifficultyChooserActivity.class);
	    startActivity(difficultyChooser);
	} else {

	}
    }
}

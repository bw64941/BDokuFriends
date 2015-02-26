/**
 * 
 */
package com.bdoku.friends;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.bdoku.model.BoardOpen;

/**
 * @author bwinters
 * 
 */
public class DifficultyChooserActivity extends Activity {

    public static final String BOARD_DIFFICULTY_EASY = "Easy";
    public static final String BOARD_DIFFICULTY_MEDIUM = "Medium";
    public static final String BOARD_DIFFICULTY_HARD = "Hard";
    public static final String DIFFICULTY_CHOSEN_BUNDLE = "difficulty";

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
	View alertDialog = layout.inflate(R.layout.boardchooseralert, null);

	Button easyButton = (Button) alertDialog.findViewById(R.id.easyButton);
	easyButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		finish();
		Intent boardActivity = new Intent(getApplicationContext(), BDokuFriendsActivity.class);
		boardActivity.putExtra(DifficultyChooserActivity.DIFFICULTY_CHOSEN_BUNDLE, DifficultyChooserActivity.BOARD_DIFFICULTY_EASY);
		boardActivity.putExtra(BDokuFriendsActivity.RESUME, false);
		startActivity(boardActivity);
	    }
	});

	Button mediumButton = (Button) alertDialog.findViewById(R.id.mediumButton);
	mediumButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		finish();
		Intent boardActivity = new Intent(getApplicationContext(), BDokuFriendsActivity.class);
		boardActivity.putExtra(DifficultyChooserActivity.DIFFICULTY_CHOSEN_BUNDLE, DifficultyChooserActivity.BOARD_DIFFICULTY_MEDIUM);
		boardActivity.putExtra(BDokuFriendsActivity.RESUME, false);
		startActivity(boardActivity);
	    }
	});

	Button hardButton = (Button) alertDialog.findViewById(R.id.hardButton);
	hardButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		finish();
		Intent boardActivity = new Intent(getApplicationContext(), BDokuFriendsActivity.class);
		boardActivity.putExtra(DifficultyChooserActivity.DIFFICULTY_CHOSEN_BUNDLE, DifficultyChooserActivity.BOARD_DIFFICULTY_HARD);
		boardActivity.putExtra(BDokuFriendsActivity.RESUME, false);
		startActivity(boardActivity);
	    }
	});

	Button resumeButton = (Button) alertDialog.findViewById(R.id.resumeButton);
	resumeButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		finish();
		Intent resumeIntent = new Intent().setClass(DifficultyChooserActivity.this, ResumeBoardActivity.class);
		startActivity(resumeIntent);
	    }
	});

	if (doesInProgressBoardExist() == false) {
	    resumeButton.setVisibility(Button.GONE);
	}
	setContentView(alertDialog);

    }

    /*
     * Checks for already in progress board file
     */
    public boolean doesInProgressBoardExist() {
	boolean boardFound = true;

	try {
	    openFileInput(BoardOpen.SAVED_IN_PROGRESS_FILE_NAME);
	} catch (FileNotFoundException e) {
	    boardFound = false;
	}

	return boardFound;
    }
}

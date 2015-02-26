/**
 * 
 */
package com.bdoku.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


import android.content.Context;

/**
 * @author bwinters
 * 
 */
public class BoardOpen {

    public static final String SAVED_IN_PROGRESS_FILE_NAME = "in_progress.txt";
    public static final String FIELD_DELIMETER = "@";
    private SavedBoard savedBoard = null;
    private Context context = null;

    public BoardOpen(Context context) {
	this.context = context;
    }

    /*
     * Checks for already in progress boards with given difficulty.
     */
    public SavedBoard open() {
	String line = "";
	boolean boardFound = false;

	try {
	    FileInputStream inputStream = context.openFileInput(BoardOpen.SAVED_IN_PROGRESS_FILE_NAME);
	    BufferedReader bufferedInput = new BufferedReader(new InputStreamReader(inputStream));
	    while ((line = bufferedInput.readLine()) != null) {
		StringTokenizer tokenizer = new StringTokenizer(line, BoardOpen.FIELD_DELIMETER);
		savedBoard = new SavedBoard(tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken());
		boardFound = true;
		break;
	    }

	    if (boardFound == false) {
		savedBoard = null;
	    }
	    bufferedInput.close();
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}
	return savedBoard;
    }

    /**
     * Delete the existing board.
     */
    public void deleteExistingFile() {
	if (!context.deleteFile(BoardOpen.SAVED_IN_PROGRESS_FILE_NAME)) {
	    // Log.d("BoardOpen", "Error Deleting File");
	}
    }

    /**
     * @return the savedBoard
     */
    public SavedBoard getSavedBoard() {
	return savedBoard;
    }

    /**
     * @param savedBoard
     *            the savedBoard to set
     */
    public void setSavedBoard(SavedBoard savedBoard) {
	this.savedBoard = savedBoard;
    }

}

/**
 * 
 */
package com.bdoku.model;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import com.bdoku.friends.R;

import android.content.Context;

/**
 * @author bwinters
 * 
 */
public class PredefinedBoardList extends ArrayList<SavedBoard> {

    private static final long serialVersionUID = 1L;
    private static Context context = null;
    private static PredefinedBoardList savedBoardList = null;

    private PredefinedBoardList(Context context) {
	PredefinedBoardList.context = context;
	loadSavedBoardFromFile();
    }

    /**
     * Get all board descriptions
     */
    public void loadSavedBoardFromFile() {
	String line = "";

	try {
	    InputStream input = context.getResources().openRawResource(R.raw.preinstalledboards);
	    DataInputStream bufferedInput = new DataInputStream(input);
	    while ((line = bufferedInput.readLine()) != null) {
		StringTokenizer tokenizer = new StringTokenizer(line, BoardOpen.FIELD_DELIMETER);
		SavedBoard savedboard = new SavedBoard(tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken());
		this.add(savedboard);
	    }
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Get Boards with specified Difficulty
     */
    public SavedBoard getPredefinedBoardWithDifficulty(String difficulty) {
	ArrayList<SavedBoard> boards = new ArrayList<SavedBoard>();
	SavedBoard chosenBoard = null;

	for (SavedBoard board : this) {
	    if (board.getBoardDescriptionString().equals(difficulty)) {
		boards.add(board);
	    }
	}

	int randIndex = 0;
	if (boards.size() > 0) {
	    Random random = new Random();
	    randIndex = random.nextInt(boards.size());
	    if (randIndex == boards.size()) {
		randIndex -= 1;
	    }
	    chosenBoard = boards.get(randIndex);
	} else {
	}

	return chosenBoard;
    }

    /**
     * @return the savedBoardList
     */
    public static PredefinedBoardList getSavedBoardList(Context context) {
	if (savedBoardList == null) {
	    savedBoardList = new PredefinedBoardList(context);
	}
	return savedBoardList;
    }

    /**
     * @param savedBoardList
     *            the savedBoardList to set
     */
    public static void setSavedBoardList(PredefinedBoardList savedBoardList) {
	PredefinedBoardList.savedBoardList = savedBoardList;
    }
}

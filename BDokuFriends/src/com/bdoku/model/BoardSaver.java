/**
 * 
 */
package com.bdoku.model;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author bwinters
 * 
 */
public class BoardSaver extends AsyncTask<BoardUtils, Void, Integer> {

    private SavedBoard savedBoard = null;
    private Context context = null;
    private static final String IN_PROGRESS_SEQUENCE_ID = "0";
    private static final String IN_PROGRESS_DESCRIPTION = "IN_PROGRESS";
    private static final String SAVED_IN_PROGRESS_FILE_NAME = "in_progress.txt";
    private static final String FIELD_DELIMETER = "@";
    public static final String PENCIL_VALUES_CELL_DELIMETER = "|";
    private static final String CELL_VALUES_DELIMETER = ",";
    public static final String CELL_USER_VALUE_INDICATOR = "*";

    public BoardSaver(Context context) {
	this.context = context;
    }

    @Override
    protected Integer doInBackground(BoardUtils... boardsToSave) {
	StringBuffer valuesBuffer = new StringBuffer();
	StringBuffer pencilValuesBuffer = new StringBuffer();
	StringBuffer solvedValuesBuffer = new StringBuffer();
	Integer response = Integer.valueOf(0);

	for (BoardUtils boardUtils : boardsToSave) {
	    for (Cell cell : boardUtils.getValues()) {
		if (cell.isUserPlaced()) {
		    valuesBuffer.append(BoardSaver.CELL_USER_VALUE_INDICATOR + cell.getValue() + BoardSaver.CELL_VALUES_DELIMETER);
		} else {
		    valuesBuffer.append(cell.getValue() + BoardSaver.CELL_VALUES_DELIMETER);
		}

		if (cell.getUserPencilValues().size() == 0) {
		    pencilValuesBuffer.append(BoardSaver.IN_PROGRESS_SEQUENCE_ID + BoardSaver.CELL_VALUES_DELIMETER);
		} else {
		    for (Integer pencilValue : cell.getUserPencilValues()) {
			pencilValuesBuffer.append(pencilValue + BoardSaver.CELL_VALUES_DELIMETER);
		    }
		}

		pencilValuesBuffer.append(BoardSaver.PENCIL_VALUES_CELL_DELIMETER);
	    }

	    for (Cell cell : Board.getBoard().getSolvedValuesArray()) {
		solvedValuesBuffer.append(cell.getValue() + BoardSaver.CELL_VALUES_DELIMETER);
	    }

	    if (pencilValuesBuffer.length() > 0) {
		savedBoard = new SavedBoard(BoardSaver.IN_PROGRESS_SEQUENCE_ID, BoardSaver.IN_PROGRESS_DESCRIPTION, valuesBuffer.substring(0, valuesBuffer.length() - 1), pencilValuesBuffer.substring(0, pencilValuesBuffer.length() - 1));
	    } else {
		savedBoard = new SavedBoard(BoardSaver.IN_PROGRESS_SEQUENCE_ID, BoardSaver.IN_PROGRESS_DESCRIPTION, valuesBuffer.substring(0, valuesBuffer.length() - 1), pencilValuesBuffer.substring(0, pencilValuesBuffer.length()));
	    }

	    FileOutputStream output = null;
	    try {
		output = context.openFileOutput(BoardSaver.SAVED_IN_PROGRESS_FILE_NAME, Context.MODE_PRIVATE);
		String line = savedBoard.getBoardSequenceString() + BoardSaver.FIELD_DELIMETER + savedBoard.getBoardDescriptionString() + BoardSaver.FIELD_DELIMETER + savedBoard.getBoardValuesString() + BoardSaver.FIELD_DELIMETER
			+ savedBoard.getPencilValuesString() + BoardSaver.FIELD_DELIMETER + savedBoard.getSolvedValuesString();

		output.write(line.getBytes());
		output.close();
	    } catch (FileNotFoundException e) {
		response = -1;
	    } catch (IOException e) {
		response = -1;
	    }
	}

	return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Integer result) {
	if (result.intValue() == 0) {
	    Toast saveToast = Toast.makeText(context, "Board Save Successful", Toast.LENGTH_SHORT);
	    saveToast.setGravity(Gravity.CENTER, 0, 0);
	    saveToast.show();
	} else {
	    Toast saveToast = Toast.makeText(context, "Board Not Saved!", Toast.LENGTH_SHORT);
	    saveToast.setGravity(Gravity.CENTER, 0, 0);
	    saveToast.show();
	}
    }
}

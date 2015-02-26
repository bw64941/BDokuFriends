/**
 * 
 */
package com.bdoku.model;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * @author bwinters
 * 
 */
public class SavedBoard {

    private String boardSequenceString = "";
    private String boardDescriptionString = "";
    private String boardValuesString = "";
    private String pencilValuesString = "";
    private String solvedValuesString = "";
    private int[][] arrayValues = null;
    private ArrayList<Integer> userPlacedValues = null;
    private ArrayList<ArrayList<Integer>> userPlacedPencilValuesIndex = null;

    public SavedBoard(String boardSequenceString, String boardDescriptionString, String boardValuesString, String pencilValuesString) {
	this.setBoardSequenceString(boardSequenceString);
	this.setBoardDescriptionString(boardDescriptionString);
	this.setBoardValuesString(boardValuesString);
	this.setPencilValuesString(pencilValuesString);
	valuesToArray();
	populateUserPlacedValues();
	pencilValuesToArrayList();
    }

    /**
     * @return the boardSequenceString
     */
    public String getBoardSequenceString() {
	return boardSequenceString;
    }

    /**
     * @param boardSequenceString
     *            the boardSequenceString to set
     */
    public void setBoardSequenceString(String boardSequenceString) {
	this.boardSequenceString = boardSequenceString;
    }

    /**
     * @return the boardDescriptionString
     */
    public String getBoardDescriptionString() {
	return boardDescriptionString;
    }

    /**
     * @param boardDescriptionString
     *            the boardDescriptionString to set
     */
    public void setBoardDescriptionString(String boardDescriptionString) {
	this.boardDescriptionString = boardDescriptionString;
    }

    /**
     * @return the boardValuesString
     */
    public String getBoardValuesString() {
	return boardValuesString;
    }

    /**
     * @param boardValuesString
     *            the boardValuesString to set
     */
    public void setBoardValuesString(String boardValuesString) {
	this.boardValuesString = boardValuesString;
    }

    /**
     * @return the pencilValuesString
     */
    public String getPencilValuesString() {
	return pencilValuesString;
    }

    /**
     * @param pencilValuesString
     *            the pencilValuesString to set
     */
    public void setPencilValuesString(String pencilValuesString) {
	this.pencilValuesString = pencilValuesString;
    }

    /**
     * Get int[][] array values with specified Difficulty
     */
    private void valuesToArray() {
	arrayValues = new int[Board.ROWS][Board.COLUMNS];
	StringTokenizer tokenizer = new StringTokenizer(boardValuesString, ",");

	for (int i = 0; i < Board.ROWS; i++) {
	    for (int j = 0; j < Board.COLUMNS; j++) {
		// skip the * at the beginning for user placed values.
		String cellValueString = tokenizer.nextToken();
		int cellValue = 0;
		if (cellValueString.indexOf(BoardSaver.CELL_USER_VALUE_INDICATOR) == -1) {
		    cellValue = Integer.parseInt(cellValueString);
		} else {
		    cellValue = Integer.parseInt(cellValueString.substring(1, cellValueString.length()));
		}
		arrayValues[i][j] = cellValue;
	    }
	}
    }

    /**
     * Get int[][] array values with specified Difficulty
     */
    private void populateUserPlacedValues() {
	userPlacedValues = new ArrayList<Integer>();
	StringTokenizer tokenizer = new StringTokenizer(boardValuesString, ",");

	int cellIndex = 0;
	while (tokenizer.hasMoreTokens()) {
	    String cellValueString = tokenizer.nextToken();
	    if (cellValueString.indexOf('*') != -1) {
		userPlacedValues.add(Integer.valueOf(cellIndex));
	    }
	    cellIndex++;
	}
    }

    /**
     * Get int[][] array values with specified Difficulty
     */
    private void pencilValuesToArrayList() {
	userPlacedPencilValuesIndex = new ArrayList<ArrayList<Integer>>();
	StringTokenizer tokenizer = new StringTokenizer(pencilValuesString, BoardSaver.PENCIL_VALUES_CELL_DELIMETER);

	while (tokenizer.hasMoreTokens()) {
	    String cellPossibilitiesList = tokenizer.nextToken();

	    StringTokenizer tokenizer2 = new StringTokenizer(cellPossibilitiesList, ",");
	    ArrayList<Integer> values = new ArrayList<Integer>();

	    while (tokenizer2.hasMoreTokens()) {
		String possibilityValue = tokenizer2.nextToken();
		if (!possibilityValue.equals(" ")) {
		    values.add(Integer.valueOf(possibilityValue));
		}
	    }

	    userPlacedPencilValuesIndex.add(values);
	}
    }

    /**
     * @return the arrayValues
     */
    public int[][] getArrayValues() {
	return arrayValues;
    }

    /**
     * @param arrayValues
     *            the arrayValues to set
     */
    public void setArrayValues(int[][] arrayValues) {
	this.arrayValues = arrayValues;
    }

    /**
     * @return the userPlacedValues
     */
    public ArrayList<Integer> getUserPlacedValues() {
	return userPlacedValues;
    }

    /**
     * @param userPlacedValues
     *            the userPlacedValues to set
     */
    public void setUserPlacedValues(ArrayList<Integer> userPlacedValues) {
	this.userPlacedValues = userPlacedValues;
    }

    /**
     * @return the userPlacedPencilValuesIndex
     */
    public ArrayList<ArrayList<Integer>> getUserPlacedPencilValuesIndex() {
	return userPlacedPencilValuesIndex;
    }

    /**
     * @param userPlacedPencilValuesIndex
     *            the userPlacedPencilValuesIndex to set
     */
    public void setUserPlacedPencilValuesIndex(ArrayList<ArrayList<Integer>> userPlacedPencilValuesIndex) {
	this.userPlacedPencilValuesIndex = userPlacedPencilValuesIndex;
    }

    /**
     * @return the solvedValuesString
     */
    public String getSolvedValuesString() {
	return solvedValuesString;
    }

    /**
     * @param solvedValuesString
     *            the solvedValuesString to set
     */
    public void setSolvedValuesString(String solvedValuesString) {
	this.solvedValuesString = solvedValuesString;
    }
}

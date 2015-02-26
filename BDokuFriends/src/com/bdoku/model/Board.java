/**
 * 
 */
package com.bdoku.model;



/**
 * @author bwinters Singleton Class for Game Board
 * 
 */
public class Board {

    public static final int ROWS = 9;
    public static final int COLUMNS = 9;
    private static Board board = null;
    private static ValuesArray values = null;
    private static int numberOfRecursionsNeededToSolve = 0;
    private static int numberOfStepsToSolve = 0;
    private static ValuesArray solvedValuesArray = null;

    /**
     * Private Board Constructor
     */
    private Board() {
    }

    /**
     * @return the board after creating new values/cells
     */
    public static Board getBoard() {
	if (board == null) {
	    board = new Board();
	    values = new ValuesArray();
	    solvedValuesArray = new ValuesArray();
	}
	return board;
    }

    /**
     * @return the board given values as parameter
     */
    public Board createBoardFromArray(int[][] valuesIntArray) {
	if (board == null) {
	    board = new Board();
	}
	values = new ValuesArray(valuesIntArray);
	solvedValuesArray = new ValuesArray(valuesIntArray);
	numberOfRecursionsNeededToSolve = 0;
	numberOfStepsToSolve = 0;
	return board;
    }

    /**
     * @return reset the board to initial state.
     */
    public void clear() {
	values.clear();
	solvedValuesArray.clear();
	numberOfRecursionsNeededToSolve = 0;
	numberOfStepsToSolve = 0;
    }

    /**
     * @return the values
     */
    public ValuesArray getValues() {
	return values;
    }

    /**
     * @return the numberOfRecursionsNeededToSolve
     */
    public synchronized int getNumberOfRecursionsNeededToSolve() {
	return numberOfRecursionsNeededToSolve;
    }

    /**
     * @param numberOfRecursionsNeededToSolve
     *            the numberOfRecursionsNeededToSolve to set
     */
    public void setNumberOfRecursionsNeededToSolve(int numberOfRecursionsNeededToSolve) {
	Board.numberOfRecursionsNeededToSolve = numberOfRecursionsNeededToSolve;
    }

    /**
     * @return the numberOfStepsToSolve
     */
    public int getNumberOfStepsToSolve() {
	return numberOfStepsToSolve;
    }

    /**
     * @param numberOfStepsToSolve
     *            the numberOfStepsToSolve to set
     */
    public void setNumberOfStepsToSolve(int numberOfStepsToSolve) {
	Board.numberOfStepsToSolve = numberOfStepsToSolve;
    }

    /**
     * @return the solvedValuesArray
     */
    public ValuesArray getSolvedValuesArray() {
	return solvedValuesArray;
    }

    /**
     * @param solvedValuesArray
     *            the solvedValuesArray to set
     */
    public static void setSolvedValuesArray(ValuesArray solvedValuesArray) {
	Board.solvedValuesArray = solvedValuesArray;
    }
}

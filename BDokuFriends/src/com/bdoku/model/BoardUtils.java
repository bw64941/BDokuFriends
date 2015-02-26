/**
 * 
 */
package com.bdoku.model;

import java.util.ArrayList;

import com.bdoku.model.engine.Solver;
import com.bdoku.model.engine.SolverStep;
import com.bdoku.model.engine.SolverTechnique.CellModStatus;

/**
 * @author bwinters
 * 
 */
public class BoardUtils {

    private Board board = null;
    private Solver solver = null;

    public BoardUtils() {
	this.board = Board.getBoard();
    }

    /**
     * Get value at x,y coordinate
     */
    public ValuesArray getValues() {
	return this.board.getValues();
    }

    /**
     * Get value at x,y coordinate
     */
    public ValuesArray getSolvedValues() {
	return this.board.getSolvedValuesArray();
    }

    /**
     * Get value at x,y coordinate
     */
    public int getValueAt(int row, int col) {
	return this.board.getValues().getCellWithRowAndColumn(row, col).getValue();
    }

    /**
     * Resume already started Board
     */
    public void resumeBoard(SavedBoard savedBoard) {
	int[][] predefinedBoardValuesArray = null;

	ArrayList<ArrayList<Integer>> savedPencilValues = null;
	predefinedBoardValuesArray = savedBoard.getArrayValues();
	savedPencilValues = savedBoard.getUserPlacedPencilValuesIndex();
	board = board.createBoardFromArray(predefinedBoardValuesArray);
	ArrayList<Integer> userPlacedValuesIndexes = savedBoard.getUserPlacedValues();
	if (userPlacedValuesIndexes.size() > 0) {
	    for (Integer userPlacedValueIndex : userPlacedValuesIndexes) {
		board.getValues().get(userPlacedValueIndex).setUserPlaced(true);
	    }
	}

	if (savedPencilValues != null && !savedPencilValues.isEmpty()) {
	    int cellIndex = 0;
	    for (Cell cell : board.getValues()) {
		cell.setUserPencilValues(savedPencilValues.get(cellIndex));
		cellIndex++;
	    }
	}

	solveBackGroundBoard();
    }

    /**
     * Resume already started Board
     */
    public void openNewBoard(SavedBoard predefinedBoard) {
	int[][] predefinedBoardValuesArray = null;

	predefinedBoardValuesArray = predefinedBoard.getArrayValues();
	board = board.createBoardFromArray(predefinedBoardValuesArray);
	solveBackGroundBoard();
    }

    /**
     * Check board validity
     */
    public boolean isBoardValid() {
	boolean validBoard = true;
	for (int cellIndex = 0; cellIndex < board.getSolvedValuesArray().size(); cellIndex++) {
	    if (!board.getValues().get(cellIndex).isEmpty() && board.getSolvedValuesArray().get(cellIndex).getValue() != board.getValues().get(cellIndex).getValue()) {
		validBoard = false;
	    }
	}

	return validBoard;
    }

    /**
     * Returns solved values array
     */
    public int getValueAtSolvedCell(int row, int col) {
	int value = 0;

	Cell cell = this.board.getSolvedValuesArray().getCellWithRowAndColumn(row, col);

	if (!cell.isUserPlaced()) {
	    value = cell.getValue();
	} else {
	    value = 0;
	}

	return value;
    }

    /**
     * Returns solved values array
     */
    public int getValuesAtSystemCell(int row, int col) {
	int value = 0;

	Cell cell = this.board.getValues().getCellWithRowAndColumn(row, col);

	if (cell != null && !cell.isUserPlaced()) {
	    value = cell.getValue();
	} else {
	    value = 0;
	}

	return value;
    }

    /**
     * Returns solved values array
     */
    public int getValueAtUserCell(int row, int col) {
	int value = 0;

	Cell cell = this.board.getValues().getCellWithRowAndColumn(row, col);
	if (cell != null && cell.isUserPlaced()) {
	    value = cell.getValue();
	} else {
	    value = 0;
	}

	return value;
    }

    /**
     * Returns solved values array
     */
    public ArrayList<Integer> getPencilBoardValuesArray(int row, int col) {
	ArrayList<Integer> values = null;

	Cell cell = this.board.getValues().getCellWithRowAndColumn(row, col);
	if (cell != null && cell.isEmpty() || cell.isUserPlaced()) {
	    values = cell.getUserPencilValues();
	} else {
	    values = null;
	}

	return values;
    }

    /**
     * Returns wheter the row,col cell has pencil values or not
     */
    public boolean cellHasPencilValue(int row, int col) {
	boolean hasPencilValues = false;

	if (this.board.getValues().getCellWithRowAndColumn(row, col).getUserPencilValues().size() > 0) {
	    hasPencilValues = true;
	}

	return hasPencilValues;
    }

    /**
     * Returns a list of values from userPlaced cells on the board
     * 
     * @param row
     * @param col
     * @param value
     */
    public ArrayList<Integer> getUserPlacedValuesArray() {
	return null;
    }

    /*
     * Sets values at particular cell location
     */
    public void setValueAtCell(int row, int col, int value) {
	Cell cell = this.board.getValues().getCellWithRowAndColumn(row, col);
	if (value == 0) {
	    // Clear button pressed
	    cell.userClear();
	} else {
	    cell.setUserValue(value);
	    this.board.getValues().getHistory().push(new SolverStep(cell, CellModStatus.SET_VALUE, value, SolverStep.USER_PLACED));
	}
    }

    /*
     * Returns true if the board is solved
     */
    public boolean isBoardSolved() {
	boolean solved = true;

	ArrayList<Cell> userCells = this.board.getValues().getAllUserPlacedCells();
	ArrayList<Cell> emptyCells = this.board.getValues().getEmptyCells();

	if (emptyCells.size() > 0 || userCells.size() == 0) {
	    solved = false;
	} else {
	    for (Cell cell : userCells) {
		if (cell.getValue() != this.board.getSolvedValuesArray().getCellWithRowAndColumn(cell.getRow(), cell.getCol()).getValue()) {
		    solved = false;
		    break;
		}
	    }

	}
	return solved;
    }

    /*
     * Clear user placed values and pencil values
     */
    public void clearUserMods() {
	for (Cell cell : this.board.getValues()) {
	    if (cell.isUserPlaced() || cell.getUserPencilValues().size() > 0) {
		cell.clear();
		cell.getUserPencilValues().clear();
	    }
	}
    }

    /**
     * Solves current board
     */
    public void solve() {
	solver = new Solver(this.board.getValues());
	Thread solverThread = new Thread(solver);
	solverThread.run();
	this.board.setNumberOfRecursionsNeededToSolve(solver.getNumberOfRecursionsNeededToSolve());
	this.board.setNumberOfStepsToSolve(solver.getNumberOfStepsToSolve());
    }

    /**
     * Solves current board
     */
    public void solveBackGroundBoard() {
	solver = new Solver(this.board.getSolvedValuesArray());
	Thread solverThread = new Thread(solver);
	solverThread.run();
	this.board.setNumberOfRecursionsNeededToSolve(solver.getNumberOfRecursionsNeededToSolve());
	this.board.setNumberOfStepsToSolve(solver.getNumberOfStepsToSolve());
    }
}

/**
 * 
 */
package com.bdoku.model;

import java.util.ArrayList;

import com.bdoku.model.engine.CellChangeObserver;
import com.bdoku.model.engine.ValuesArrayHistory;

/**
 * @author bwinters
 * 
 */
public class ValuesArray extends ArrayList<Cell> {

    private static final long serialVersionUID = 1L;
    private ValuesArrayHistory history = new ValuesArrayHistory();
    private int[][] valuesArray = new int[Board.ROWS][Board.COLUMNS];

    /*
     * ValuesArray Constructor
     */
    public ValuesArray() {
	super();
	for (int i = 0; i < Math.sqrt(this.size()); i++) {
	    for (int j = 0; j < Math.sqrt(this.size()); j++) {
		Cell cell = new Cell(i, j);
		cell.addObserver(new CellChangeObserver(this));
		this.add(cell);
		valuesArray[i][j] = cell.getValue();
	    }
	}
    }

    /**
     * Allows caller to pass in int[][] to initialize Value Array
     */
    public ValuesArray(int[][] values) {
	super();
	for (int i = 0; i < values.length; i++) {
	    for (int j = 0; j < values.length; j++) {
		Cell cell = new Cell(i, j);
		cell.setValue(values[i][j]);
		cell.addObserver(new CellChangeObserver(this));
		this.add(cell);
	    }
	}
	valuesArray = values;
	evaluateInitialValues();
    }

    /**
     * Returns all cells that have a values that was placed by a user.
     */
    public ArrayList<Cell> getAllUserPlacedCells() {
	ArrayList<Cell> userPlacedCells = new ArrayList<Cell>();

	for (int i = 0; i < this.size(); i++) {
	    if (this.get(i).isUserPlaced() == true) {
		userPlacedCells.add(this.get(i));
	    }
	}

	return userPlacedCells;
    }

    /**
     * Returns all cells that have a values that was placed by a user.
     */
    public ArrayList<Cell> getEmptyCells() {
	ArrayList<Cell> emptyCells = new ArrayList<Cell>();

	for (int i = 0; i < this.size(); i++) {
	    if (this.get(i).isEmpty() == true) {
		emptyCells.add(this.get(i));
	    }
	}

	return emptyCells;
    }

    /**
     * Returns the cells in a specified quadrant.
     * 
     * @param cells
     * @param quad
     * @return
     */
    public ArrayList<Cell> getCellsInQuad(int quad) {
	ArrayList<Cell> cellsInQuad = new ArrayList<Cell>(Board.ROWS);

	for (int i = 0; i < this.size(); i++) {
	    if (this.get(i).getQuad() == quad) {
		cellsInQuad.add(this.get(i));
	    }
	}

	return cellsInQuad;
    }

    /**
     * Returns the cells in a specified row.
     * 
     * @param cells
     * @param row
     * @return
     */
    public ArrayList<Cell> getCellsInRow(int row) {
	ArrayList<Cell> cellsInRow = new ArrayList<Cell>(Board.ROWS);

	for (int i = 0; i < this.size(); i++) {
	    if (this.get(i).getRow() == row) {
		cellsInRow.add(this.get(i));
	    }
	}
	return cellsInRow;
    }

    /**
     * Returns the cells in a specified column.
     * 
     * @param cells
     * @param col
     * @return
     */
    public ArrayList<Cell> getCellsInCol(int col) {
	ArrayList<Cell> cellsInCol = new ArrayList<Cell>(9);

	for (int i = 0; i < this.size(); i++) {
	    if (this.get(i).getCol() == col) {
		cellsInCol.add(this.get(i));
	    }
	}
	return cellsInCol;
    }

    /**
     * Returns Cell with specified row and column values
     * 
     * @param row
     * @param column
     * @return
     */
    public Cell getCellWithRowAndColumn(int row, int column) {
	Cell targetCell = null;

	for (int i = 0; i < this.size(); i++) {
	    Cell cell = this.get(i);
	    if (cell.getRow() == row && cell.getCol() == column) {
		targetCell = cell;
		break;
	    }
	}
	return targetCell;
    }

    /**
     * Returns all cells in the same row, column, and quadrant as the cell
     * passed in.
     * 
     * @param cell
     * @return
     */
    public ArrayList<Cell> getCellsInSameRowORColORQuad(Cell cell) {
	ArrayList<Cell> areaCells = new ArrayList<Cell>();

	for (Cell otherCell : this) {
	    if (otherCell.getRow() == cell.getRow() || otherCell.getCol() == cell.getCol() || otherCell.getQuad() == cell.getQuad()) {
		areaCells.add(otherCell);
	    }
	}
	return areaCells;
    }

    /**
     * Wrapper for updating possibilities on board after placement of initial
     * values. Calls updatePossibilities method.
     * 
     * @param functionUpdating
     * @param cells
     * @return number of empty cells on board
     */
    public void evaluateInitialValues() {
	for (Cell lockedCell : this) {
	    if (lockedCell.isLocked() && !lockedCell.isEmpty()) {
		for (Cell areaCells : getCellsInSameRowORColORQuad(lockedCell)) {
		    if (!areaCells.equals(lockedCell) && !areaCells.isLocked() && areaCells.isEmpty()) {
			areaCells.removePossibility(lockedCell.getValue());
		    }
		}
	    }
	}
    }

    /**
     * @return the history
     */
    public ValuesArrayHistory getHistory() {
	return history;
    }

    /**
     * @param history
     *            the history to set
     */
    public void setHistory(ValuesArrayHistory history) {
	this.history = history;
    }

    /**
     * @return the valuesArray
     */
    public int[][] getValuesArray() {
	return valuesArray;
    }

    /**
     * @param valuesArray
     *            the valuesArray to set
     */
    public void setValuesArray(int[][] valuesArray) {
	this.valuesArray = valuesArray;
    }
}

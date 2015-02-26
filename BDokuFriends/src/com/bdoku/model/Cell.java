/**
 * 
 */
package com.bdoku.model;

import java.util.ArrayList;
import java.util.Observable;


/**
 * @author bwinters
 * 
 */
public class Cell extends Observable {

    private int value = 0;
    private int row = 0;
    private int col = 0;
    private int quad = 0;
    private boolean locked = false;
    private boolean empty = true;
    private boolean userPlaced = false;
    private ArrayList<Integer> remainingPossibilities = new ArrayList<Integer>();
    private ArrayList<Integer> userPlacedPencilValues = new ArrayList<Integer>();

    /**
     * Cell Constructor
     */
    public Cell(int row, int col) {
	this.row = row;
	this.col = col;
	initializeRemainingPossibilities();
	initilizeQuadrantValue();
    }

    /**
     * Initialize 1-9 in the possibility array
     */
    private void initializeRemainingPossibilities() {
	for (int i = 1; i <= Board.ROWS; i++) {
	    remainingPossibilities.add(Integer.valueOf(i));
	}
    }

    /**
     * Assigns quadrant values to the cell
     */
    private void initilizeQuadrantValue() {
	// Quadrant 0
	double squarRootOfBoard = Math.sqrt(Board.ROWS);
	if (this.row <= (int) squarRootOfBoard - 1 && this.col <= (int) squarRootOfBoard - 1) {
	    this.quad = 0;
	}
	// Quadrant 1
	else if (this.row <= (int) squarRootOfBoard - 1 && (col >= (int) squarRootOfBoard && col <= (int) squarRootOfBoard + 2)) {
	    this.quad = (int) squarRootOfBoard - 2;
	}
	// Quadrant 2
	else if (this.row <= (int) squarRootOfBoard - 1 && (col >= (int) squarRootOfBoard + 3 && col <= (int) squarRootOfBoard + 5)) {
	    this.quad = (int) squarRootOfBoard - 1;
	}
	// Quadrant 3
	else if ((this.row >= (int) squarRootOfBoard && this.row <= (int) squarRootOfBoard + 2) && (this.col <= (int) squarRootOfBoard - 1)) {
	    this.quad = (int) squarRootOfBoard;
	}
	// Quadrant 4
	else if ((this.row >= (int) squarRootOfBoard && this.row <= (int) squarRootOfBoard + 2)
		&& (this.col >= (int) squarRootOfBoard && this.col <= (int) squarRootOfBoard + 2)) {
	    this.quad = (int) squarRootOfBoard + 1;
	}
	// Quadrant 5
	else if ((this.row >= (int) squarRootOfBoard && this.row <= (int) squarRootOfBoard + 2)
		&& (this.col >= (int) squarRootOfBoard + 3 && this.col <= (int) squarRootOfBoard + 5)) {
	    this.quad = (int) squarRootOfBoard + 2;
	}
	// Quadrant 6
	else if ((this.row >= (int) squarRootOfBoard + 3 && this.col <= (int) squarRootOfBoard - 1)) {
	    this.quad = (int) squarRootOfBoard + 3;
	}
	// Quadrant 7
	else if ((this.row >= (int) squarRootOfBoard + 3) && (this.col >= (int) squarRootOfBoard && this.col <= (int) squarRootOfBoard + 2)) {
	    this.quad = (int) squarRootOfBoard + 4;
	}
	// Quadrant 8
	else if (this.row >= (int) squarRootOfBoard + 3 && this.col >= (int) squarRootOfBoard + 3) {
	    this.quad = (int) squarRootOfBoard + 5;
	}
    }

    /**
     * Returns the cell coordinates for the cell.
     * 
     * @return
     */
    public String getCoordinates() {
	String coord = "[" + this.quad + "," + this.row + "," + this.col + "]";
	return coord;
    }

    /**
     * Re-Initializes the state of the cell
     */
    public void userClear() {
	locked = false;
	empty = true;
	userPlaced = false;
	value = 0;
    }

    /**
     * Re-Initializes the state of the cell
     */
    public void clear() {
	locked = false;
	empty = true;
	userPlaced = false;
	value = 0;
	initializeRemainingPossibilities();
    }

    /**
     * Removes the specified possibility from the cells possible values.
     * 
     * @param notPosibleInt
     * @param stepSolve
     */
    public void removePossibility(int notPossibleInt) {
	remainingPossibilities.remove(Integer.valueOf(notPossibleInt));
    }

    /**
     * Returns the number of possibilities for the cell.
     * 
     * @return
     */
    public int getNum0fPossibilitiesLeft() {
	return remainingPossibilities.size();
    }

    /**
     * Removes the specified possibility from the cells pencil values.
     * 
     * @param notPosibleInt
     * @param stepSolve
     */
    public void removeUserPencilPossibility(int notPossibleInt) {
	userPlacedPencilValues.remove(Integer.valueOf(notPossibleInt));
    }

    /**
     * Returns the number of user placed pencil values for the cell.
     * 
     * @return
     */
    public int getNum0fPencilValuesPossibilitiesLeft() {
	return userPlacedPencilValues.size();
    }

    /**
     * @return the value
     */
    public int getValue() {
	return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setUserValue(int value) {
	this.value = value;

	if (value > 0) {
	    empty = false;
	    locked = true;
	    userPlaced = true;
	}
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(int value) {
	this.value = value;

	if (value > 0) {
	    empty = false;
	    locked = true;
	    remainingPossibilities.clear();
	    setChanged();
	    notifyObservers(this);
	}
    }

    /**
     * @return the row
     */
    public int getRow() {
	return row;
    }

    /**
     * @param row
     *            the row to set
     */
    public void setRow(int row) {
	this.row = row;
    }

    /**
     * @return the col
     */
    public int getCol() {
	return col;
    }

    /**
     * @param col
     *            the col to set
     */
    public void setCol(int col) {
	this.col = col;
    }

    /**
     * @return the quad
     */
    public int getQuad() {
	return quad;
    }

    /**
     * @param quad
     *            the quad to set
     */
    public void setQuad(int quad) {
	this.quad = quad;
    }

    /**
     * @return the locked
     */
    public boolean isLocked() {
	return locked;
    }

    /**
     * @param locked
     *            the locked to set
     */
    public void setLocked(boolean locked) {
	this.locked = locked;
    }

    /**
     * @return the empty
     */
    public boolean isEmpty() {
	return empty;
    }

    /**
     * @param empty
     *            the empty to set
     */
    public void setEmpty(boolean empty) {
	this.empty = empty;
    }

    /**
     * @return the remainingPossibilities
     */
    public ArrayList<Integer> getRemainingPossibilities() {
	return remainingPossibilities;
    }

    /**
     * @param remainingPossibilities
     *            the remainingPossibilities to set
     */
    public void setRemainingPossibilities(ArrayList<Integer> remainingPossibilities) {
	this.remainingPossibilities = remainingPossibilities;
    }

    /**
     * @return the userPencilValues
     */
    public ArrayList<Integer> getUserPencilValues() {
	return userPlacedPencilValues;
    }

    /**
     * @param userPencilValues
     *            the userPlacedPencilValues to set
     */
    public void setUserPencilValues(ArrayList<Integer> userPencilValues) {
	this.userPlacedPencilValues = userPencilValues;
    }

    /**
     * @return the userPlaced
     */
    public boolean isUserPlaced() {
	return userPlaced;
    }

    /**
     * @param userPlaced
     *            the userPlaced to set
     */
    public void setUserPlaced(boolean userPlaced) {
	this.userPlaced = userPlaced;
    }
}

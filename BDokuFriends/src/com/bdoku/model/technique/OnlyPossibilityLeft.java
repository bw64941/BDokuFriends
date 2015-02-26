/**
 * 
 */
package com.bdoku.model.technique;

import java.util.ArrayList;

import com.bdoku.model.Cell;
import com.bdoku.model.ValuesArray;
import com.bdoku.model.engine.Rules;
import com.bdoku.model.engine.SolverStep;
import com.bdoku.model.engine.SolverTechnique;

/**
 * @author bwinters
 * 
 */
public class OnlyPossibilityLeft implements SolverTechnique, Rules {

    public static final String TECHNIQUE = "ONLY POSSIBILITY LEFT";
    private ValuesArray values = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.model.SolverTechnique#executeTechnique(com.model.engine.SolverStack,
     * com.model.ValuesArray)
     */
    @Override
    public void executeTechnique(ValuesArray values) {
	this.values = values;
	
	/*
	 * Process entire ValueArray List.
	 */
	processCellGrouping(values);
    }

    /*
     * (non-Javadoc)
     * @see com.model.SolverTechnique#processCellGrouping(java.util.ArrayList)
     */
    @Override
    public void processCellGrouping(ArrayList<Cell> cells) {
	int valuePlaced = 0;
	
	/*
	 * Loop through all cells and if the cell only has 1 possibility,
	 * set the cells value to the remaining possibility.
	 */
	for (Cell cell : cells) {
	    if (cell.getNum0fPossibilitiesLeft() == 1 && cell.isEmpty() && !(cell.isLocked())) {
		valuePlaced = cell.getRemainingPossibilities().get(0);
		if (runRules(cell.getRow(), cell.getCol(), cell.getQuad(), valuePlaced)) {
		    cell.setValue(valuePlaced);
		    values.getHistory().push(new SolverStep(cell, CellModStatus.SET_VALUE, valuePlaced, OnlyPossibilityLeft.TECHNIQUE));
		}
	    }
	}
    }

    @Override
    public boolean runRules(int row, int column, int quadrant, int valueToCheck) {
	if (isRowOK(row, valueToCheck) && isColOK(column, valueToCheck) && isQuadOK(quadrant, valueToCheck)) {
	    return true;
	}
	return false;
    }

    @Override
    public boolean isRowOK(int row, int tempValue) {
	for (Cell tempCell : values.getCellsInRow(row)) {
	    if (tempCell.getValue() == tempValue) {
		// Already a cell in the row with the value you are trying to
		// place.
		return false;
	    }
	}
	return true;
    }

    @Override
    public boolean isColOK(int col, int tempValue) {
	for (Cell tempCell : values.getCellsInCol(col)) {
	    if (tempCell.getValue() == tempValue) {
		// Already a cell in the column with the value you are trying to
		// place.
		return false;
	    }
	}
	return true;
    }

    @Override
    public boolean isQuadOK(int quad, int tempValue) {
	for (Cell tempCell : values.getCellsInQuad(quad)) {
	    if (tempCell.getValue() == tempValue) {
		// Already a cell in the quadrant with the value you are trying
		// to place.
		return false;
	    }
	}
	return true;
    }
}

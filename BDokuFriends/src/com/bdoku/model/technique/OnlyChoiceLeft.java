/**
 * 
 */
package com.bdoku.model.technique;

import java.util.ArrayList;

import com.bdoku.model.Board;
import com.bdoku.model.Cell;
import com.bdoku.model.ValuesArray;
import com.bdoku.model.engine.Rules;
import com.bdoku.model.engine.SolverStep;
import com.bdoku.model.engine.SolverTechnique;

/**
 * @author bwinters
 * 
 */
public class OnlyChoiceLeft implements SolverTechnique, Rules {
    
    public static final String TECHNIQUE = "ONLY CHOICE LEFT";
    private ValuesArray values = null;

    /*
     * (non-Javadoc)
     * 
     * @see com.model.SolverTechnique#executeTechnique(com.model.ValuesArrays)
     */
    @Override
    public void executeTechnique(ValuesArray values) {
	this.values = values;

	/*
	 * Process all rows, columns, and quadrants.
	 */
	for (int groupIndex = 0; groupIndex < Board.ROWS; groupIndex++) {
	    processCellGrouping(values.getCellsInRow(groupIndex));
	    processCellGrouping(values.getCellsInCol(groupIndex));
	    processCellGrouping(values.getCellsInQuad(groupIndex));
	}
    }

    /**
     * 
     * Execute technique on given cell grouping.
     * 
     * @param cells
     */
    @Override 
    public void processCellGrouping(ArrayList<Cell> cells) {
	int[] allPossibleValues = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	/*
	 * Obtain all cells in area that already have a value. Remove the locked
	 * cells value from the int[] array of 1-9. Capture the cells that are
	 * still empty, but if emptyList becomes > than 1 then condition is not
	 * met, break out.
	 */
	ArrayList<Cell> lockedCells = new ArrayList<Cell>();
	ArrayList<Cell> emptyCells = new ArrayList<Cell>();
	for (Cell cell : cells) {
	    if (cell.isLocked() && !cell.isEmpty() && cell.getValue() != 0) {
		lockedCells.add(cell);
		allPossibleValues[cell.getValue() - 1] = 0;
	    } else {
		emptyCells.add(cell);
		if (emptyCells.size() > 1) {
		    break;
		}
	    }
	}

	/*
	 * If there are 8 cells in area that have a value placed, then there is
	 * only 1 choice for remaining empty cell in area.
	 */
	if (emptyCells.size() == 1) {
	    for (int i = 0; i < allPossibleValues.length; i++) {
		if (allPossibleValues[i] != 0) {
		    if (runRules(emptyCells.get(0).getRow(), emptyCells.get(0).getCol(), emptyCells.get(0).getQuad(), allPossibleValues[i])) {
			emptyCells.get(0).setValue(allPossibleValues[i]);
			values.getHistory().push(new SolverStep(emptyCells.get(0), CellModStatus.SET_VALUE, allPossibleValues[i], OnlyChoiceLeft.TECHNIQUE));
		    }
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

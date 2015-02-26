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
public class LoneValues implements SolverTechnique, Rules {

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
	int[][] possibilityCounts = new int[9][1];
	
	/*
	 * Loop through cells in area and count number of times each possibility
	 * occurs.
	 */
	for (Cell cell : cells) {
	    for (Integer posibility : cell.getRemainingPossibilities()) {
		possibilityCounts[posibility - 1][0]++;
	    }
	}

	/*
	 * Any possibility that occurs only once in the row can be removed from
	 * the cell that contains that possibility.
	 */
	for (int i = 0; i < possibilityCounts.length; i++) {
	    if (possibilityCounts[i][0] == 1) {
		for (Cell cell : cells) {
		    if (cell.getRemainingPossibilities().contains(i + 1)) {
			if (runRules(cell.getRow(), cell.getCol(), cell.getQuad(), (i + 1))) {
			    cell.setValue(i + 1);
			    values.getHistory().push(new SolverStep(cell, CellModStatus.SET_VALUE, i + 1, LoneValues.TECHNIQUE));
			}
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

/**
 * 
 */
package com.bdoku.model.technique;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
public class Reduction implements SolverTechnique, Rules {

    public static final String TECHNIQUE = "REDUCTION";
    private ValuesArray values = null;

    /*
     * (non-Javadoc)
     * 
     * @see com.model.engine.SolverTechnique#executeTechnique(com.model.engine.
     * SolverStack, com.model.ValuesArray)
     */
    @Override
    public void executeTechnique(ValuesArray values) {
	this.values = values;

	for (int groupIndex = 0; groupIndex < Board.ROWS; groupIndex++) {
	    processCellGrouping(values.getCellsInRow(groupIndex));
	    processCellGrouping(values.getCellsInCol(groupIndex));
	}

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.model.engine.SolverTechnique#processCellGrouping(java.util.ArrayList)
     */
    @Override
    public void processCellGrouping(ArrayList<Cell> cells) {
	Set<Integer> uniques = new HashSet<Integer>();
	Set<Integer> uniqueQuads = new HashSet<Integer>();
	Set<Integer> dups = new HashSet<Integer>();

	/*
	 * Obtain all possibilities that occur more than once in the cell
	 * grouping.
	 */
	for (Cell cell : cells) {
	    ArrayList<Integer> possibleValues = cell.getRemainingPossibilities();
	    for (Integer posibility : possibleValues) {
		if (!uniques.add(posibility)) {
		    dups.add(posibility);
		}
	    }
	}

	/*
	 * Gather the quadrant values for all cells in the duplicates list.
	 */
	for (Integer possibility : dups) {
	    ArrayList<Cell> cellsContainingPossibility = getCellsContainingPossibility(cells, possibility);
	    for (Cell cell : cellsContainingPossibility) {
		uniqueQuads.add(cell.getQuad());
	    }

	    // For any value possibility that is in the array, remove that
	    // possibility from any cell in the quad, excluding the area in
	    // question.
	    if (uniqueQuads.size() == 1) {
		for (Integer quad : uniqueQuads) {
		    ArrayList<Cell> quadCells = values.getCellsInQuad(quad);
		    for (Cell cell : quadCells) {
			/*
			 * If cell in quadrant is not one of the cells in the
			 * cell area being passed in.
			 */
			if (!cells.contains(cell) && cell.isEmpty() && cell.getRemainingPossibilities().contains(possibility)) {
			    cell.removePossibility(possibility);
			    values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, possibility, SolverStep.REDUCE_ROW));

			}
		    }
		}

	    }
	    uniqueQuads.clear();
	}

    }

    /**
     * Returns the cells within the given list of cells that contain the
     * specified possibility.
     * 
     * @param cells
     * @param possibility
     * @return
     */
    public ArrayList<Cell> getCellsContainingPossibility(ArrayList<Cell> cells, Integer possibility) {
	ArrayList<Cell> cellsContainingPossibility = new ArrayList<Cell>();

	for (Cell cell : cells) {
	    if (cell.getRemainingPossibilities().contains(possibility)) {
		cellsContainingPossibility.add(cell);
	    }
	}
	return cellsContainingPossibility;
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

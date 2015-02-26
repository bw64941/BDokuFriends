/**
/**
 * 
 */
package com.bdoku.model.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.bdoku.model.Board;
import com.bdoku.model.Cell;
import com.bdoku.model.ValuesArray;
import com.bdoku.model.engine.SolverTechnique.CellModStatus;
import com.bdoku.model.technique.LoneValues;
import com.bdoku.model.technique.OnlyChoiceLeft;
import com.bdoku.model.technique.OnlyPossibilityLeft;
import com.bdoku.model.technique.Reduction;

/**
 * @author BWinters
 * 
 */
public class Solver implements Runnable {

    public static final int ANALYZE_ENTIRE_ARRAY = 0;
    public static final int ANALYZE_ROW = 1;
    public static final int ANALYZE_COL = 2;
    public static final int ANALYZE_QUADRANT = 3;
    private ValuesArray values = null;
    private boolean solveInProgress = false;
    private int numberOfRecursionsNeededToSolve = 0;
    private int numberOfStepsToSolve = 0;
    private SolverContext solverContext = null;

    /**
     * Solver Constructor
     * 
     * @param board
     */
    public Solver(ValuesArray values) {
	solveInProgress = true;
	this.values = values;
    }

    /**
     * Recursive function to solve sudoku board. Places obvious values in cells
     * with only 1 possibility. Then cross checks each row, column, and quadrant
     * in which a possibility only occurs once in that row, column, or quadrant.
     * 
     * @return
     */
    public boolean solve() {

	if (solved()) {
	    numberOfStepsToSolve = values.getHistory().size();
	    return true;
	} else {

	    for (int areaNumber = 0; areaNumber < Board.ROWS; areaNumber++) {
		solverContext = new SolverContext(new Reduction());
		solverContext.executeTechnique(values);
		solverContext = new SolverContext(new OnlyPossibilityLeft());
		solverContext.executeTechnique(values);
		solverContext = new SolverContext(new OnlyChoiceLeft());
		solverContext.executeTechnique(values);
		solverContext = new SolverContext(new LoneValues());
		solverContext.executeTechnique(values);
		nakedPairsInRow(areaNumber);
		nakedPairsInCol(areaNumber);
		nakedPairsInQuad(areaNumber);
		nakedTripleInRow(areaNumber);
		nakedTripleInCol(areaNumber);
		nakedTripleInQuad(areaNumber);
	    }
	}
	numberOfRecursionsNeededToSolve++;
	return solve();
    }

    /**
     * Returns if the board is solved by checking if there are any cells that
     * are still empty.
     * 
     * @return
     */
    private boolean solved() {
	for (Cell cell : values) {
	    if (cell.isEmpty()) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Identify any 2 cells in a quad that have the same 2 identical
     * possibilities left. Remove those possibilities from the quad excluding
     * the 2 cells identified
     * 
     * @param cells
     * @param quadInQuestion
     */
    private void nakedPairsInQuad(int quadInQuestion) {
	ArrayList<Cell> cellsWith2Pos = new ArrayList<Cell>();
	Set<ArrayList<Integer>> uniquePosSets = new HashSet<ArrayList<Integer>>();
	ArrayList<ArrayList<Integer>> duplicatePosSets = new ArrayList<ArrayList<Integer>>();
	HashMap<Cell, ArrayList<Integer>> nakedPairs = new HashMap<Cell, ArrayList<Integer>>();
	ArrayList<Integer> nakedPossibilities = new ArrayList<Integer>();

	/*
	 * Obtain all cells in area containing only 2 possibilities.
	 */
	for (Cell cell : values.getCellsInQuad(quadInQuestion)) {
	    if (cell.getRemainingPossibilities().size() == 2) {
		cellsWith2Pos.add(cell);
	    }
	}

	/*
	 * If there are not 2 cells in area with 2 identical possibilities then
	 * return.
	 */
	if (cellsWith2Pos.isEmpty() || cellsWith2Pos.size() < 2) {
	    return;
	}

	for (Cell cell : cellsWith2Pos) {
	    if (!uniquePosSets.add(cell.getRemainingPossibilities())) {
		duplicatePosSets.add(cell.getRemainingPossibilities());
	    }
	}

	// logger.debug(duplicatePosSets.size() + " = DUPLICATE POS SETS SIZE");

	if (!duplicatePosSets.isEmpty()) {
	    for (Cell cell : cellsWith2Pos) {
		if (cell.getRemainingPossibilities().equals(duplicatePosSets.get(0))) {
		    nakedPairs.put(cell, cell.getRemainingPossibilities());
		}
	    }

	    for (Cell cell : nakedPairs.keySet()) {
		for (Integer possibility : nakedPairs.get(cell)) {
		    nakedPossibilities.add(possibility);
		}
	    }

	    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quadInQuestion);
	    for (Cell cell : cellsInQuad) {
		for (Integer possibility : nakedPossibilities) {
		    if (!(nakedPairs.keySet().contains(cell)) && (cell.getRemainingPossibilities().contains(possibility))) {
			cell.removePossibility(possibility);
			values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, possibility, SolverStep.NAKED_PAIRS_IN_QUAD));
		    }
		}
	    }
	}
    }

    /**
     * Identify any 2 cells in a column that have the same 2 identical
     * possibilities left. Remove those possibilities from the column excluding
     * the 2 cells identified.
     * 
     * @param cells
     * @param colInQuestion
     */
    private void nakedPairsInCol(int colInQuestion) {
	ArrayList<Cell> cellsWith2Pos = new ArrayList<Cell>();
	Set<ArrayList<Integer>> uniquePosSets = new HashSet<ArrayList<Integer>>();
	ArrayList<ArrayList<Integer>> duplicatePosSets = new ArrayList<ArrayList<Integer>>();
	HashMap<Cell, ArrayList<Integer>> nakedPairs = new HashMap<Cell, ArrayList<Integer>>();
	ArrayList<Integer> nakedPossibilities = new ArrayList<Integer>();

	for (Cell cell : values.getCellsInCol(colInQuestion)) {
	    if (cell.getRemainingPossibilities().size() == 2) {
		cellsWith2Pos.add(cell);
	    }
	}
	// Gather all cells in the colInQuestion that have 2 possibilities
	if (cellsWith2Pos.isEmpty() || cellsWith2Pos.size() < 2) {
	    return;
	}

	for (Cell cell : cellsWith2Pos) {
	    if (!uniquePosSets.add(cell.getRemainingPossibilities())) {
		duplicatePosSets.add(cell.getRemainingPossibilities());
	    }
	}

	// logger.debug(duplicatePosSets.size() + " = DUPLICATE POS SETS SIZE");

	if (!duplicatePosSets.isEmpty()) {
	    for (Cell cell : cellsWith2Pos) {
		if (cell.getRemainingPossibilities().equals(duplicatePosSets.get(0))) {
		    nakedPairs.put(cell, cell.getRemainingPossibilities());
		}
	    }

	    for (Cell cell : nakedPairs.keySet()) {
		for (Integer possibility : nakedPairs.get(cell)) {
		    nakedPossibilities.add(possibility);
		}
	    }

	    ArrayList<Cell> cellsInCol = values.getCellsInCol(colInQuestion);
	    for (Cell cell : cellsInCol) {
		for (Integer possibility : nakedPossibilities) {
		    if (!(nakedPairs.keySet().contains(cell)) && (cell.getRemainingPossibilities().contains(possibility))) {
			cell.removePossibility(possibility);
			values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, possibility, SolverStep.NAKED_PAIRS_IN_COL));
		    }
		}
	    }
	}
    }

    /**
     * Identify any 2 cells in a row that have the same 2 identical
     * possibilities left. Remove those possibilities from the row excluding the
     * 2 cells identified.
     * 
     * @param cells
     * @param rowInQuestion
     */
    private void nakedPairsInRow(int rowInQuestion) {
	ArrayList<Cell> cellsWith2Pos = new ArrayList<Cell>();
	Set<ArrayList<Integer>> uniquePosSets = new HashSet<ArrayList<Integer>>();
	ArrayList<ArrayList<Integer>> duplicatePosSets = new ArrayList<ArrayList<Integer>>();
	HashMap<Cell, ArrayList<Integer>> nakedPairs = new HashMap<Cell, ArrayList<Integer>>();
	ArrayList<Integer> nakedPossibilities = new ArrayList<Integer>();

	for (Cell cell : values.getCellsInRow(rowInQuestion)) {
	    if (cell.getRemainingPossibilities().size() == 2) {
		cellsWith2Pos.add(cell);
	    }
	}
	// Gather all cells in the rowInQuestion that have 2 possibilities
	if (cellsWith2Pos.isEmpty() || cellsWith2Pos.size() < 2) {
	    return;
	}

	for (Cell cell : cellsWith2Pos) {
	    if (!uniquePosSets.add(cell.getRemainingPossibilities())) {
		duplicatePosSets.add(cell.getRemainingPossibilities());
	    }
	}

	// logger.debug(duplicatePosSets.size() + " = DUPLICATE POS SETS SIZE");

	if (!duplicatePosSets.isEmpty()) {
	    for (Cell cell : cellsWith2Pos) {
		if (cell.getRemainingPossibilities().equals(duplicatePosSets.get(0))) {
		    nakedPairs.put(cell, cell.getRemainingPossibilities());
		}
	    }

	    for (Cell cell : nakedPairs.keySet()) {
		for (Integer possibility : nakedPairs.get(cell)) {
		    nakedPossibilities.add(possibility);
		}
	    }

	    ArrayList<Cell> cellsInRow = values.getCellsInRow(rowInQuestion);
	    for (Cell cell : cellsInRow) {
		for (Integer possibility : nakedPossibilities) {
		    if (!(nakedPairs.keySet().contains(cell)) && (cell.getRemainingPossibilities().contains(possibility))) {
			cell.removePossibility(possibility);
			values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, possibility, SolverStep.NAKED_PAIRS_IN_ROW));
		    }
		}
	    }
	}
    }

    /**
     * Identify any 3 cells in a quad that have the same 3 identical
     * possibilities left. Remove those possibilities from the cells in the qaud
     * excluding the 3 cells identified. If the 3 cells.
     * 
     * @param cells
     * @param rowInQuestion
     */
    private void nakedTripleInQuad(int quadInQuestion) {
	ArrayList<Cell> nakedTriples = new ArrayList<Cell>();
	ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quadInQuestion);
	ArrayList<Cell> cellsWith3Pos = new ArrayList<Cell>();
	Set<Integer> uniquePos = new HashSet<Integer>();
	Set<Integer> rowList = new HashSet<Integer>();
	Set<Integer> colList = new HashSet<Integer>();

	// 1. Gather all cells in the quadInQuestion with 3 possibilities and
	// search for pattern:
	// A,B,C | A,B,C | A,B,C
	// System.out.println("1. COL - ["+quadInQuestion+"]");
	for (Cell cell : cellsInQuad) {
	    if (cell.getRemainingPossibilities().size() == 3) {
		cellsWith3Pos.add(cell);
	    }
	}

	int cellsTheSame = 0;
	for (Cell cell : cellsWith3Pos) {
	    ArrayList<Integer> pos3Set = cell.getRemainingPossibilities();
	    for (Cell cell2 : cellsWith3Pos) {
		if (cell2.getRemainingPossibilities().equals(pos3Set)) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 3 && !nakedTriples.contains(cell)) {
		// System.out.println("1. COL - ["+quadInQuestion+"] FOUND CELL "+cell.getCoordinates());
		nakedTriples.add(cell);
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("1. Triples FOUND - COL ["+quadInQuestion+"] RIGHT NUMBER OF CELLS");
	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		rowList.add(cell.getRow());
		colList.add(cell.getCol());
		for (Integer pos : cell.getRemainingPossibilities()) {
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("1. Triples FOUND - COL ["+quadInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");
		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell : cellsInQuad) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("1. COL ["+quadInQuestion+"] - Removing "
			    // + pos + " from " + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory()
					.push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (rowList.size() == 1) {
		    int row = rowList.iterator().next();
		    ArrayList<Cell> cellsInRow = values.getCellsInRow(row);
		    for (Cell cell : cellsInRow) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("1. ROW ["+row+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

				}
			    }
			}

		    }
		}

		if (colList.size() == 1) {
		    int col = colList.iterator().next();
		    ArrayList<Cell> cellsInCol = values.getCellsInCol(col);
		    for (Cell cell : cellsInCol) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("4. COL ["+col+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	rowList.clear();
	colList.clear();

	// 2. Gather all cells in the rowInQuestion with 2 or 3 possibilities
	// and search for pattern:
	// A,B | A,B,C | A,B,C
	// System.out.println("2. COL - ["+quadInQuestion+"]");
	cellsTheSame = 0;

	ArrayList<Cell> cellsWith2Pos = new ArrayList<Cell>();
	for (Cell cell : cellsInQuad) {
	    if (cell.getRemainingPossibilities().size() == 3) {
		cellsWith2Pos.add(cell);
	    }
	}

	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsWith3Pos) {
		if (cell2.getRemainingPossibilities().containsAll(cell.getRemainingPossibilities())) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 2) {
		ArrayList<Cell> cellsContaining = getCellsContainingPossibilityList(cellsWith3Pos, cell.getRemainingPossibilities());
		if (cellsContaining.get(0).getRemainingPossibilities().equals(cellsContaining.get(1).getRemainingPossibilities())) {
		    // System.out.println("2. COL - ["+quadInQuestion+"] FOUND CELL "+cell.getCoordinates()+"PLUS "+cellsContaining.size());
		    nakedTriples.add(cell);
		    nakedTriples.addAll(cellsContaining);
		}
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("2. Triples FOUND - COL ["+quadInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		rowList.add(cell.getRow());
		colList.add(cell.getCol());
		for (Integer pos : cell.getRemainingPossibilities()) {
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("2. Triples FOUND - COL [" +
		// quadInQuestion
		// + "] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list, then remove the unique
		// possibilities
		// found above from that cell.
		for (Cell cell : cellsInQuad) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("2. COL [" + quadInQuestion
			    // + "] - Removing " + pos + " from "
			    // + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory()
					.push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));
			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (rowList.size() == 1) {
		    int row = rowList.iterator().next();
		    ArrayList<Cell> cellsInRow = values.getCellsInRow(row);
		    for (Cell cell : cellsInRow) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("2. ROW ["+row+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

				}
			    }
			}

		    }
		}

		if (colList.size() == 1) {
		    int col = colList.iterator().next();
		    ArrayList<Cell> cellsInCol = values.getCellsInCol(col);
		    for (Cell cell : cellsInCol) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("4. COL ["+col+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	rowList.clear();
	colList.clear();

	// 3. Gather all cells in the quadInQuestion with 2 or 3 possibilities
	// and search for pattern:
	// A,B | A,B | A,B,C
	// System.out.println("3. COL - ["+quadInQuestion+"]");
	cellsTheSame = 0;
	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsInQuad) {
		if (!cell.equals(cell2) && cell2.getRemainingPossibilities().equals(cell.getRemainingPossibilities())) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 1) {
		int cellCount = 0;
		for (Cell cell3 : cellsWith3Pos) {
		    if (cell3.getRemainingPossibilities().containsAll(cell.getRemainingPossibilities())) {
			cellCount++;
		    }
		}
		if (cellCount == 1) {
		    ArrayList<Cell> cellsContaining = getCellsContainingPossibilityList(cellsWith3Pos, cell.getRemainingPossibilities());
		    // System.out.println("3. COL - ["+quadInQuestion+"] FOUND CELL "+cell.getCoordinates());
		    nakedTriples.add(cell);
		    nakedTriples.addAll(cellsContaining);
		}
		cellCount = 0;
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("3. Triples FOUND - COL ["+quadInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		for (Integer pos : cell.getRemainingPossibilities()) {
		    rowList.add(cell.getRow());
		    colList.add(cell.getCol());
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("3. Triples FOUND - COL ["+quadInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell : cellsInQuad) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("3. COL ["+quadInQuestion+"] - Removing "
			    // + pos + " from " + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory()
					.push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (rowList.size() == 1) {
		    int row = rowList.iterator().next();
		    ArrayList<Cell> cellsInRow = values.getCellsInRow(row);
		    for (Cell cell : cellsInRow) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("3. ROW ["+row+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

				}
			    }
			}

		    }
		}

		if (colList.size() == 1) {
		    int col = colList.iterator().next();
		    ArrayList<Cell> cellsInCol = values.getCellsInCol(col);
		    for (Cell cell : cellsInCol) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("4. COL ["+col+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	rowList.clear();
	colList.clear();

	// 4. Gather all cells in the quadInQuestion with 2 possibilities and
	// search for pattern:
	// A,B | B,C | A,C
	// System.out.println("4. COL - ["+quadInQuestion+"]");
	if (cellsWith2Pos.size() < 3) {
	    return;
	}

	cellsTheSame = 0;
	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsWith2Pos) {
		if (!cell.equals(cell2) && cell2.getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(0))) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 1) {
		ArrayList<Cell> cellsContaining = getCellsContainingPossibility(cellsWith2Pos, cell.getRemainingPossibilities().get(0));
		cellsTheSame = 0;
		for (Cell cell3 : cellsContaining) {
		    if (!cell3.equals(cell)
			    && (cell3.getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(0)) || cell3
				    .getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(1))
				    && (cell3.getRemainingPossibilities().contains(cellsContaining.get(1)) || cell3.getRemainingPossibilities()
					    .contains(cellsContaining.get(0))))) {
			// System.out.println("4. COL - ["+quadInQuestion+"] FOUND CELL "+cell.getCoordinates()+" PLUS "
			// + cellsContaining.size());
			nakedTriples.add(cell);
			// nakedTriples.addAll(cellsContaining);
		    }
		}
	    }
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("4. Triples FOUND - COL ["+quadInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell5 : nakedTriples) {
		for (Integer pos : cell5.getRemainingPossibilities()) {
		    rowList.add(cell5.getRow());
		    colList.add(cell5.getCol());
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("4. Triples FOUND - COL ["+quadInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell6 : cellsInQuad) {
		    if (!nakedTriples.contains(cell6)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("4. COL ["+quadInQuestion+"] - Removing "
			    // + pos + " from " + cell6.getCoordinates());
			    if (cell6.isEmpty()) {
				cell6.removePossibility(pos);
				values.getHistory().push(
					new SolverStep(cell6, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (rowList.size() == 1) {
		    int row = rowList.iterator().next();
		    ArrayList<Cell> cellsInRow = values.getCellsInRow(row);
		    for (Cell cell : cellsInRow) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("4. ROW ["+row+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

				}
			    }
			}

		    }
		}

		if (colList.size() == 1) {
		    int col = colList.iterator().next();
		    ArrayList<Cell> cellsInCol = values.getCellsInCol(col);
		    for (Cell cell : cellsInCol) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("4. COL ["+col+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_QUAD));

				}
			    }
			}

		    }
		}
	    }
	}
    }

    /**
     * Identify any 3 cells in a column that have the same 3 identical
     * possibilities left. Remove those possibilities from the cells in the
     * column excluding the 3 cells identified. If the 3 cells identified all
     * fall inside the same quadrant. Remove the possibilities from the cells in
     * the quadrant, excluding the 3 cells identified.
     * 
     * @param cells
     * @param colInQuestion
     */
    private void nakedTripleInCol(int colInQuestion) {
	ArrayList<Cell> nakedTriples = new ArrayList<Cell>();
	ArrayList<Cell> cellsInCol = values.getCellsInCol(colInQuestion);
	Set<Integer> uniquePos = new HashSet<Integer>();
	Set<Integer> quadList = new HashSet<Integer>();

	// 1. Gather all cells in the colInQuestion with 3 possibilities and
	// search for pattern:
	// A,B,C | A,B,C | A,B,C
	// System.out.println("1. COL - ["+colInQuestion+"]");
	ArrayList<Cell> cellsWith3Pos = new ArrayList<Cell>();
	for (Cell cell : cellsInCol) {
	    if (cell.getRemainingPossibilities().size() == 3) {
		cellsWith3Pos.add(cell);
	    }
	}

	int cellsTheSame = 0;
	for (Cell cell : cellsWith3Pos) {
	    ArrayList<Integer> pos3Set = cell.getRemainingPossibilities();
	    for (Cell cell2 : cellsWith3Pos) {
		if (cell2.getRemainingPossibilities().equals(pos3Set)) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 3 && !nakedTriples.contains(cell)) {
		// System.out.println("1. COL - ["+colInQuestion+"] FOUND CELL "+cell.getCoordinates());
		nakedTriples.add(cell);
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("1. Triples FOUND - COL ["+colInQuestion+"] RIGHT NUMBER OF CELLS");
	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		quadList.add(cell.getQuad());
		for (Integer pos : cell.getRemainingPossibilities()) {
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("1. Triples FOUND - COL ["+colInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");
		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell : cellsInCol) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("1. COL ["+colInQuestion+"] - Removing "
			    // + pos + " from " + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_COL));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (quadList.size() == 1) {
		    int quad = quadList.iterator().next();
		    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quad);
		    for (Cell cell : cellsInQuad) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("1. QUAD ["+quad+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_COL));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	quadList.clear();

	// 2. Gather all cells in the rowInQuestion with 2 or 3 possibilities
	// and search for pattern:
	// A,B | A,B,C | A,B,C
	// System.out.println("2. COL - ["+colInQuestion+"]");
	ArrayList<Cell> cellsWith2Pos = new ArrayList<Cell>();
	for (Cell cell : cellsInCol) {
	    if (cell.getRemainingPossibilities().size() == 2) {
		cellsWith2Pos.add(cell);
	    }
	}

	cellsTheSame = 0;
	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsWith3Pos) {
		if (cell2.getRemainingPossibilities().containsAll(cell.getRemainingPossibilities())) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 2) {
		ArrayList<Cell> cellsContaining = getCellsContainingPossibilityList(cellsWith3Pos, cell.getRemainingPossibilities());
		if (cellsContaining.get(0).getRemainingPossibilities().equals(cellsContaining.get(1).getRemainingPossibilities())) {
		    // System.out.println("2. COL - ["+colInQuestion+"] FOUND CELL "+cell.getCoordinates()+"PLUS "+cellsContaining.size());
		    nakedTriples.add(cell);
		    nakedTriples.addAll(cellsContaining);
		}
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("2. Triples FOUND - COL ["+colInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		quadList.add(cell.getQuad());
		for (Integer pos : cell.getRemainingPossibilities()) {
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("2. Triples FOUND - COL [" + colInQuestion
		// + "] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list, then remove the unique
		// possibilities
		// found above from that cell.
		for (Cell cell : cellsInCol) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("2. COL [" + colInQuestion
			    // + "] - Removing " + pos + " from "
			    // + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_COL));
			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (quadList.size() == 1) {
		    int quad = quadList.iterator().next();
		    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quad);
		    for (Cell cell : cellsInQuad) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("2. QUAD ["+quad+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_COL));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	quadList.clear();

	// 3. Gather all cells in the colInQuestion with 2 or 3 possibilities
	// and search for pattern:
	// A,B | A,B | A,B,C
	// System.out.println("3. COL - ["+colInQuestion+"]");
	cellsTheSame = 0;
	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsInCol) {
		if (!cell.equals(cell2) && cell2.getRemainingPossibilities().equals(cell.getRemainingPossibilities())) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 1) {
		int cellCount = 0;
		for (Cell cell3 : cellsWith3Pos) {
		    if (cell3.getRemainingPossibilities().containsAll(cell.getRemainingPossibilities())) {
			cellCount++;
		    }
		}
		if (cellCount == 1) {
		    ArrayList<Cell> cellsContaining = getCellsContainingPossibilityList(cellsWith3Pos, cell.getRemainingPossibilities());
		    // System.out.println("3. COL - ["+colInQuestion+"] FOUND CELL "+cell.getCoordinates());
		    nakedTriples.add(cell);
		    nakedTriples.addAll(cellsContaining);
		}
		cellCount = 0;
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("3. Triples FOUND - COL ["+colInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		for (Integer pos : cell.getRemainingPossibilities()) {
		    quadList.add(cell.getQuad());
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("3. Triples FOUND - COL ["+colInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell : cellsInCol) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("3. COL ["+colInQuestion+"] - Removing "
			    // + pos + " from " + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_COL));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (quadList.size() == 1) {
		    int quad = quadList.iterator().next();
		    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quad);
		    for (Cell cell : cellsInQuad) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("3. QUAD ["+quad+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_COL));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	quadList.clear();

	// 4. Gather all cells in the colInQuestion with 2 possibilities and
	// search for pattern:
	// A,B | B,C | A,C
	// System.out.println("4. COL - ["+colInQuestion+"]");
	if (cellsWith2Pos.size() < 3) {
	    return;
	}

	cellsTheSame = 0;
	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsWith2Pos) {
		if (!cell.equals(cell2) && cell2.getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(0))) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 1) {
		ArrayList<Cell> cellsContaining = getCellsContainingPossibility(cellsWith2Pos, cell.getRemainingPossibilities().get(0));
		cellsTheSame = 0;
		for (Cell cell3 : cellsContaining) {
		    if (!cell3.equals(cell)
			    && (cell3.getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(0)) || cell3
				    .getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(1))
				    && (cell3.getRemainingPossibilities().contains(cellsContaining.get(1)) || cell3.getRemainingPossibilities()
					    .contains(cellsContaining.get(0))))) {
			// System.out.println("4. COL - ["+colInQuestion+"] FOUND CELL "+cell.getCoordinates()+" PLUS "
			// + cellsContaining.size());
			nakedTriples.add(cell);
			// nakedTriples.addAll(cellsContaining);
		    }
		}
	    }
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("4. Triples FOUND - COL ["+colInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell5 : nakedTriples) {
		for (Integer pos : cell5.getRemainingPossibilities()) {
		    quadList.add(cell5.getQuad());
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("4. Triples FOUND - COL ["+colInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell6 : cellsInCol) {
		    if (!nakedTriples.contains(cell6)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("4. COL ["+colInQuestion+"] - Removing "
			    // + pos + " from " + cell6.getCoordinates());
			    if (cell6.isEmpty()) {
				cell6.removePossibility(pos);
				values.getHistory()
					.push(new SolverStep(cell6, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_COL));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (quadList.size() == 1) {
		    int quad = quadList.iterator().next();
		    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quad);
		    for (Cell cell : cellsInQuad) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("4. QUAD ["+quad+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_COL));

				}
			    }
			}

		    }
		}
	    }
	}
    }

    /**
     * Identify any 3 cells in a row that have the same 3 identical
     * possibilities left. Remove those possibilities from the cells in the row
     * excluding the 3 cells identified. If the 3 cells identified all fall
     * inside the same quadrant. Remove the possibilities from the cells in the
     * quadrant, excluding the 3 cells identified.
     * 
     * @param cells
     * @param rowInQuestion
     */
    private void nakedTripleInRow(int rowInQuestion) {
	ArrayList<Cell> nakedTriples = new ArrayList<Cell>();
	ArrayList<Cell> cellsInRow = values.getCellsInRow(rowInQuestion);
	Set<Integer> uniquePos = new HashSet<Integer>();
	Set<Integer> quadList = new HashSet<Integer>();

	// 1. Gather all cells in the rowInQuestion with 3 possibilities and
	// search for pattern:
	// A,B,C | A,B,C | A,B,C
	// System.out.println("1. ROW - ["+rowInQuestion+"]");
	ArrayList<Cell> cellsWith3Pos = new ArrayList<Cell>();
	for (Cell cell : cellsInRow) {
	    if (cell.getRemainingPossibilities().size() == 3) {
		cellsWith3Pos.add(cell);
	    }
	}

	int cellsTheSame = 0;
	for (Cell cell : cellsWith3Pos) {
	    ArrayList<Integer> pos3Set = cell.getRemainingPossibilities();
	    for (Cell cell2 : cellsWith3Pos) {
		if (cell2.getRemainingPossibilities().equals(pos3Set)) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 3 && !nakedTriples.contains(cell)) {
		// System.out.println("1. ROW - ["+rowInQuestion+"] FOUND CELL "+cell.getCoordinates());
		nakedTriples.add(cell);
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("1. Triples FOUND - ROW ["+rowInQuestion+"] RIGHT NUMBER OF CELLS");
	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		quadList.add(cell.getQuad());
		for (Integer pos : cell.getRemainingPossibilities()) {
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("1. Triples FOUND - ROW ["+rowInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");
		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell : cellsInRow) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("1. ROW ["+rowInQuestion+"] - Removing "
			    // + pos + " from " + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_ROW));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (quadList.size() == 1) {
		    int quad = quadList.iterator().next();
		    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quad);
		    for (Cell cell : cellsInQuad) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("1. QUAD ["+quad+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_ROW));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	quadList.clear();

	// 2. Gather all cells in the rowInQuestion with 2 or 3 possibilities
	// and search for pattern:
	// A,B | A,B,C | A,B,C
	// System.out.println("2. ROW - ["+rowInQuestion+"]");
	ArrayList<Cell> cellsWith2Pos = new ArrayList<Cell>();
	for (Cell cell : cellsInRow) {
	    if (cell.getRemainingPossibilities().size() == 2) {
		cellsWith2Pos.add(cell);
	    }
	}

	cellsTheSame = 0;
	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsWith3Pos) {
		if (cell2.getRemainingPossibilities().containsAll(cell.getRemainingPossibilities())) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 2) {
		ArrayList<Cell> cellsContaining = getCellsContainingPossibilityList(cellsWith3Pos, cell.getRemainingPossibilities());
		if (cellsContaining.get(0).getRemainingPossibilities().equals(cellsContaining.get(1).getRemainingPossibilities())) {
		    // System.out.println("2. ROW - ["+rowInQuestion+"] FOUND CELL "+cell.getCoordinates()+"PLUS "+cellsContaining.size());
		    nakedTriples.add(cell);
		    nakedTriples.addAll(cellsContaining);
		}
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("2. Triples FOUND - ROW ["+rowInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		quadList.add(cell.getQuad());
		for (Integer pos : cell.getRemainingPossibilities()) {
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("2. Triples FOUND - ROW [" + rowInQuestion
		// + "] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the row, and if the cell in the row is
		// not in the nakedTriples list, then remove the unique
		// possibilities
		// found above from that cell.
		for (Cell cell : cellsInRow) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("2. ROW [" + rowInQuestion
			    // + "] - Removing " + pos + " from "
			    // + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_ROW));
			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (quadList.size() == 1) {
		    int quad = quadList.iterator().next();
		    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quad);
		    for (Cell cell : cellsInQuad) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("2. QUAD ["+quad+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_ROW));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	quadList.clear();

	// 3. Gather all cells in the rowInQuestion with 2 or 3 possibilities
	// and search for pattern:
	// A,B | A,B | A,B,C
	// System.out.println("3. ROW - ["+rowInQuestion+"]");
	cellsTheSame = 0;
	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsInRow) {
		if (!cell.equals(cell2) && cell2.getRemainingPossibilities().equals(cell.getRemainingPossibilities())) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 1) {
		int cellCount = 0;
		for (Cell cell3 : cellsWith3Pos) {
		    if (cell3.getRemainingPossibilities().containsAll(cell.getRemainingPossibilities())) {
			cellCount++;
		    }
		}
		if (cellCount == 1) {
		    ArrayList<Cell> cellsContaining = getCellsContainingPossibilityList(cellsWith3Pos, cell.getRemainingPossibilities());
		    // System.out.println("3. ROW - ["+rowInQuestion+"] FOUND CELL "+cell.getCoordinates());
		    nakedTriples.add(cell);
		    nakedTriples.addAll(cellsContaining);
		}
		cellCount = 0;
	    }
	    cellsTheSame = 0;
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("3. Triples FOUND - ROW ["+rowInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell : nakedTriples) {
		for (Integer pos : cell.getRemainingPossibilities()) {
		    quadList.add(cell.getQuad());
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("3. Triples FOUND - ROW ["+rowInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the row, and if the cell in the row is not
		// in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell : cellsInRow) {
		    if (!nakedTriples.contains(cell)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("3. ROW ["+rowInQuestion+"] - Removing "
			    // + pos + " from " + cell.getCoordinates());
			    if (cell.isEmpty()) {
				cell.removePossibility(pos);
				values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_ROW));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (quadList.size() == 1) {
		    int quad = quadList.iterator().next();
		    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quad);
		    for (Cell cell : cellsInQuad) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("3. QUAD ["+quad+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_ROW));

				}
			    }
			}

		    }
		}
	    }
	}

	uniquePos.clear();
	nakedTriples.clear();
	quadList.clear();

	// 4. Gather all cells in the rowInQuestion with 2 possibilities and
	// search for pattern:
	// A,B | B,C | A,C
	// System.out.println("4. ROW - ["+rowInQuestion+"]");
	if (cellsWith2Pos.size() < 3) {
	    return;
	}

	cellsTheSame = 0;
	for (Cell cell : cellsWith2Pos) {
	    for (Cell cell2 : cellsWith2Pos) {
		if (!cell.equals(cell2) && cell2.getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(0))) {
		    cellsTheSame++;
		}
	    }
	    if (cellsTheSame == 1) {
		ArrayList<Cell> cellsContaining = getCellsContainingPossibility(cellsWith2Pos, cell.getRemainingPossibilities().get(0));
		cellsTheSame = 0;
		for (Cell cell3 : cellsContaining) {
		    if (!cell3.equals(cell)
			    && (cell3.getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(0)) || cell3
				    .getRemainingPossibilities().contains(cell.getRemainingPossibilities().get(1))
				    && (cell3.getRemainingPossibilities().contains(cellsContaining.get(1)) || cell3.getRemainingPossibilities()
					    .contains(cellsContaining.get(0))))) {
			// System.out.println("4. ROW - ["+rowInQuestion+"] FOUND CELL "+cell.getCoordinates()+" PLUS "
			// + cellsContaining.size());
			nakedTriples.add(cell);
			// nakedTriples.addAll(cellsContaining);
		    }
		}
	    }
	}

	// Make sure there are only 3 cells found.
	if (nakedTriples.size() == 3) {
	    // System.out.println("4. Triples FOUND - ROW ["+rowInQuestion+"] RIGHT NUMBER OF CELLS");

	    // For each cell save its quadrant and possibilities.
	    for (Cell cell5 : nakedTriples) {
		for (Integer pos : cell5.getRemainingPossibilities()) {
		    quadList.add(cell5.getQuad());
		    uniquePos.add(pos);
		}
	    }

	    // Make sure there are only 3 unique possibilities in the 3 cells
	    // found.
	    if (uniquePos.size() == 3) {
		// System.out.println("4. Triples FOUND - ROW ["+rowInQuestion+"] RIGHT NUMBER OF UNIQUE NUMBERS");

		// For each cell in the column, and if the cell in the column is
		// not in the nakedTriples list,
		// then remove the unique possibilities found above from that
		// cell.
		for (Cell cell6 : cellsInRow) {
		    if (!nakedTriples.contains(cell6)) {
			for (Integer pos : uniquePos) {
			    // System.out.println("4. ROW ["+rowInQuestion+"] - Removing "
			    // + pos + " from " + cell6.getCoordinates());
			    if (cell6.isEmpty()) {
				cell6.removePossibility(pos);
				values.getHistory()
					.push(new SolverStep(cell6, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_ROW));

			    }
			}

		    }
		}

		// If all 3 cells are within the same quadrant, remove the 3
		// unique possibilities from the quadrant
		// as well.
		if (quadList.size() == 1) {
		    int quad = quadList.iterator().next();
		    ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quad);
		    for (Cell cell : cellsInQuad) {
			if (!nakedTriples.contains(cell)) {
			    for (Integer pos : uniquePos) {
				// System.out.println("4. QUAD ["+quad+"]- Removing "
				// + pos + " from " + cell.getCoordinates());
				if (cell.isEmpty()) {
				    cell.removePossibility(pos);
				    values.getHistory().push(
					    new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, pos, SolverStep.NAKED_TRIPLE_IN_ROW));

				}
			    }
			}

		    }
		}
	    }
	}
    }

    /**
     * Identify any unique possibilities that occur only 2 times in a quadrant,
     * If the 2 unique possibilities occur in the same cell, then remove any
     * other possibility from those 2 cells.
     * 
     * @param cells
     * @param quadInQuestion
     */
    @SuppressWarnings("unused")
    private void hiddenPairsInQuad(int quadInQuestion) {
	ArrayList<Cell> cellsInQuad = values.getCellsInQuad(quadInQuestion);
	ArrayList<Integer> possibilitiesInQuad = new ArrayList<Integer>();
	Set<Integer> possibilitiesThatOccurTwice = new HashSet<Integer>();
	Set<Cell> map = new HashSet<Cell>();
	ArrayList<Integer> hiddenPair = new ArrayList<Integer>();
	Set<Integer> posToRemove = new HashSet<Integer>();

	for (Cell cell : cellsInQuad) {
	    if (cell.isEmpty()) {
		possibilitiesInQuad.addAll(cell.getRemainingPossibilities());
	    }
	}

	for (Integer possibility : possibilitiesInQuad) {
	    int occurrances = getNumOfOccurancesOfPossibilityInCellList(cellsInQuad, possibility);
	    if (occurrances == 2) {
		possibilitiesThatOccurTwice.add(possibility);
	    }
	}

	for (Integer pos1 : possibilitiesThatOccurTwice) {
	    for (Integer pos2 : possibilitiesThatOccurTwice) {
		if (pos1 != pos2) {
		    for (Cell cell : cellsInQuad) {
			if (cell.getRemainingPossibilities().contains(pos1) && cell.getRemainingPossibilities().contains(pos2)) {
			    // System.out.println(pos1 + " " + pos2 +
			    // " occurs in cell " + cell.getCellCoordinates());
			    map.add(cell);
			}
		    }
		}
	    }
	}

	hiddenPair.addAll(possibilitiesThatOccurTwice);
	for (Cell cell : map) {
	    // System.out.println("Cell " + cell.getCellCoordinates() +
	    // " has a hidden pair");
	    for (Integer in : possibilitiesThatOccurTwice) {
		if (!cell.getRemainingPossibilities().contains(in)) {
		    // System.out.println("Removing "+in);
		    hiddenPair.remove(in);
		}
	    }

	}

	for (Cell cell : cellsInQuad) {
	    for (Integer in : hiddenPair) {
		if (!map.contains(cell) && cell.getRemainingPossibilities().contains(in)) {
		    return;
		}
	    }
	}

	if (hiddenPair.size() == 2) {
	    // System.out.println(hiddenPair.size());
	    // System.out.println(hiddenPair.get(0) + " and " +
	    // hiddenPair.get(1) + " is one of the hidden pair candidates");
	    for (Cell cell : map) {
		for (Integer possible : cell.getRemainingPossibilities()) {
		    if (possible != hiddenPair.get(0) && possible != hiddenPair.get(1)) {
			posToRemove.add(possible);
		    }
		}
	    }

	    for (Cell cell : map) {
		for (Integer in : posToRemove) {
		    // System.out.println("Removing " + in + " from " +
		    // cell.getCellCoordinates());
		    cell.removePossibility(in);
		    values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, in, SolverStep.HIDDEN_PAIRS_IN_QUAD));

		}
	    }
	}
    }

    /**
     * Identify any unique possibilities that occur only 2 times in a column, If
     * the 2 unique possibilities occur in the same cell, then remove any other
     * possibility from those 2 cells.
     * 
     * @param cells
     * @param colInQuestion
     */
    @SuppressWarnings("unused")
    private void hiddenPairsInCol(int colInQuestion) {
	ArrayList<Cell> cellsInCol = values.getCellsInCol(colInQuestion);
	Set<Integer> possibilitiesThatOccurTwice = new HashSet<Integer>();
	Set<Cell> map = new HashSet<Cell>();
	ArrayList<Integer> hiddenPair = new ArrayList<Integer>();
	Set<Integer> posToRemove = new HashSet<Integer>();

	for (Cell cell : cellsInCol) {
	    for (Integer possibility : cell.getRemainingPossibilities()) {
		int count = getNumOfOccurancesOfPossibilityInCellList(cellsInCol, possibility);
		if (count == 2) {
		    possibilitiesThatOccurTwice.add(possibility);
		}
	    }
	}

	for (Integer pos1 : possibilitiesThatOccurTwice) {
	    for (Integer pos2 : possibilitiesThatOccurTwice) {
		for (Cell cell : cellsInCol) {
		    if (pos1 != pos2) {
			if (cell.getRemainingPossibilities().contains(pos1) && cell.getRemainingPossibilities().contains(pos2)) {
			    // System.out.println(pos1 + " " + pos2 +
			    // " occurs in cell " + cell.getCellCoordinates());
			    map.add(cell);
			}
		    }
		}
	    }
	}

	hiddenPair.addAll(possibilitiesThatOccurTwice);
	for (Cell cell : map) {
	    // System.out.println("Cell " + cell.getCellCoordinates() +
	    // " has a hidden pair");
	    for (Integer in : possibilitiesThatOccurTwice) {
		if (!cell.getRemainingPossibilities().contains(in)) {
		    // System.out.println("Removing "+in);
		    hiddenPair.remove(in);
		}
	    }

	}

	for (Cell cell : cellsInCol) {
	    for (Integer in : hiddenPair) {
		if (!map.contains(cell) && cell.getRemainingPossibilities().contains(in)) {
		    return;
		}
	    }
	}

	if (hiddenPair.size() == 2) {
	    // System.out.println(hiddenPair.size());
	    // System.out.println(hiddenPair.get(0) + " and " +
	    // hiddenPair.get(1) + " is one of the hidden pair candidates");
	    for (Cell cell : map) {
		for (Integer possible : cell.getRemainingPossibilities()) {
		    if (possible != hiddenPair.get(0) && possible != hiddenPair.get(1)) {
			posToRemove.add(possible);
		    }
		}
	    }

	    for (Cell cell : map) {
		for (Integer in : posToRemove) {
		    // System.out.println("Removing " + in + " from " +
		    // cell.getCellCoordinates());
		    cell.removePossibility(in);
		    values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, in, SolverStep.HIDDEN_PAIRS_IN_COL));

		}
	    }
	}
    }

    /**
     * Identify any unique possibilities that occur only 2 times in a row, If
     * the 2 unique possibilities occur in the same cell, then remove any other
     * possibility from those 2 cells.
     * 
     * @param cells
     * @param colInQuestion
     */
    @SuppressWarnings("unused")
    private void hiddenPairsInRow(int rowInQuestion) {
	ArrayList<Cell> cellsInRow = values.getCellsInRow(rowInQuestion);
	ArrayList<Integer> rowPossibilities = new ArrayList<Integer>();
	Set<Integer> possibilitiesThatOccurTwice = new HashSet<Integer>();
	Set<Cell> map = new HashSet<Cell>();
	ArrayList<Integer> hiddenPair = new ArrayList<Integer>();
	Set<Integer> posToRemove = new HashSet<Integer>();

	for (Cell cell : cellsInRow) {
	    if (cell.isEmpty()) {
		rowPossibilities.addAll(cell.getRemainingPossibilities());
	    }
	}

	for (Integer possibility : rowPossibilities) {
	    int occurrances = getNumOfOccurancesOfPossibilityInCellList(cellsInRow, possibility);
	    if (occurrances == 2) {
		possibilitiesThatOccurTwice.add(possibility);
	    }
	}

	for (Integer pos1 : possibilitiesThatOccurTwice) {
	    for (Integer pos2 : possibilitiesThatOccurTwice) {
		if (pos1 != pos2) {
		    for (Cell cell : cellsInRow) {
			if (cell.getRemainingPossibilities().contains(pos1) && cell.getRemainingPossibilities().contains(pos2)) {
			    // System.out.println(pos1 + " " + pos2 +
			    // " occurs in cell " + cell.getCellCoordinates());
			    map.add(cell);
			}
		    }
		}
	    }
	}

	hiddenPair.addAll(possibilitiesThatOccurTwice);
	for (Cell cell : map) {
	    // System.out.println("Cell " + cell.getCellCoordinates() +
	    // " has a hidden pair");
	    for (Integer in : possibilitiesThatOccurTwice) {
		if (!cell.getRemainingPossibilities().contains(in)) {
		    // System.out.println("Removing "+in);
		    hiddenPair.remove(in);
		}
	    }

	}

	for (Cell cell : cellsInRow) {
	    for (Integer in : hiddenPair) {
		if (!map.contains(cell) && cell.getRemainingPossibilities().contains(in)) {
		    return;
		}
	    }
	}

	if (hiddenPair.size() == 2) {
	    // System.out.println(hiddenPair.size());
	    // System.out.println(hiddenPair.get(0) + " and " +
	    // hiddenPair.get(1) + " is one of the hidden pair candidates");
	    for (Cell cell : map) {
		for (Integer possible : cell.getRemainingPossibilities()) {
		    if (possible != hiddenPair.get(0) && possible != hiddenPair.get(1)) {
			posToRemove.add(possible);
		    }
		}
	    }

	    for (Cell cell : map) {
		for (Integer in : posToRemove) {
		    // System.out.println("Removing " + in + " from " +
		    // cell.getCellCoordinates());
		    cell.removePossibility(in);
		    values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, in, SolverStep.HIDDEN_PAIRS_IN_ROW));

		}
	    }
	}
    }

    /**
     * Identify any unique possibilities that occur only 3 times in a row, If
     * the 3 unique possibilities occur in the same cell, then remove any other
     * possibility from those 3 cells.
     * 
     * @param cells
     * @param rowInQuestion
     */
    @SuppressWarnings("unused")
    private void hiddenTriplesInRow(int rowInQuestion) {
	ArrayList<Cell> cellsInRow = values.getCellsInRow(rowInQuestion);
	Set<Integer> possibilitiesThatOccurThreeTimes = new HashSet<Integer>();
	Set<Cell> map = new HashSet<Cell>();
	ArrayList<Integer> hiddenTriple = new ArrayList<Integer>();
	Set<Integer> posToRemove = new HashSet<Integer>();

	for (Cell cell : cellsInRow) {
	    for (Integer possibility : cell.getRemainingPossibilities()) {
		int count = getNumOfOccurancesOfPossibilityInCellList(cellsInRow, possibility);
		if (count == 3) {
		    possibilitiesThatOccurThreeTimes.add(possibility);
		}
	    }
	}

	for (Integer pos1 : possibilitiesThatOccurThreeTimes) {
	    for (Integer pos2 : possibilitiesThatOccurThreeTimes) {
		for (Integer pos3 : possibilitiesThatOccurThreeTimes) {
		    for (Cell cell : cellsInRow) {
			if (pos1 != pos2 && pos1 != pos3) {
			    if (cell.getRemainingPossibilities().contains(pos1) && cell.getRemainingPossibilities().contains(pos2)
				    && cell.getRemainingPossibilities().contains(pos3)) {
				System.out.println(pos1 + " " + pos2 + " " + pos3 + " " + "occurs in cell " + cell.getCoordinates());
				map.add(cell);
			    }
			}
		    }
		}
	    }
	}

	hiddenTriple.addAll(possibilitiesThatOccurThreeTimes);
	for (Cell cell : map) {
	    System.out.println("Cell " + cell.getCoordinates() + " has a hidden triple");
	    for (Integer in : possibilitiesThatOccurThreeTimes) {
		if (!cell.getRemainingPossibilities().contains(in)) {
		    System.out.println("Removing " + in);
		    hiddenTriple.remove(in);
		}
	    }

	}

	for (Cell cell : cellsInRow) {
	    for (Integer in : hiddenTriple) {
		if (!map.contains(cell) && cell.getRemainingPossibilities().contains(in)) {
		    return;
		}
	    }
	}

	if (hiddenTriple.size() == 3) {
	    System.out.println(hiddenTriple.size());
	    System.out.println(hiddenTriple.get(0) + " and " + hiddenTriple.get(1) + " is one of the hidden triple candidates");
	    for (Cell cell : map) {
		for (Integer possible : cell.getRemainingPossibilities()) {
		    if (possible != hiddenTriple.get(0) && possible != hiddenTriple.get(1) && possible != hiddenTriple.get(2)) {
			posToRemove.add(possible);
		    }
		}
	    }

	    for (Cell cell : map) {
		for (Integer in : posToRemove) {
		    System.out.println("Removing " + in + " from " + cell.getCoordinates());
		    cell.removePossibility(in);
		    values.getHistory().push(new SolverStep(cell, CellModStatus.REMOVE_POSSIBILITY, in, SolverStep.HIDDEN_TRIPLE_IN_ROW));

		}
	    }
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

    /**
     * Returns the cells within the given list of cells that contain the
     * specified list of possibilities.
     * 
     * @param cells
     * @param possibility
     * @return
     */
    public ArrayList<Cell> getCellsContainingPossibilityList(ArrayList<Cell> cells, ArrayList<Integer> possibility) {
	ArrayList<Cell> cellsContainingPossibility = new ArrayList<Cell>();

	for (Cell cell : cells) {
	    if (cell.getRemainingPossibilities().containsAll(possibility)) {
		cellsContainingPossibility.add(cell);
	    }
	}
	return cellsContainingPossibility;
    }

    /**
     * Returns the number of times the specified possibility occurs in the given
     * cell list.
     * 
     * @param cells
     * @param possibility
     * @return
     */
    public int getNumOfOccurancesOfPossibilityInCellList(ArrayList<Cell> cells, Integer possibility) {
	int numOfOccurrances = 0;

	for (Cell cell : cells) {
	    for (Integer poss : cell.getRemainingPossibilities()) {
		if (poss.equals(possibility)) {
		    numOfOccurrances++;
		}
	    }
	}
	return numOfOccurrances;
    }

    /**
     * @param solveInProgress
     *            the solveInProgress to set
     */
    public void setSolveInProgress(boolean solveInProgress) {
	this.solveInProgress = solveInProgress;
    }

    /**
     * @return the solveInProgress
     */
    public boolean isSolveInProgress() {
	return solveInProgress;
    }

    /**
     * Runs the solver routine
     */
    public void run() {
	while (solveInProgress) {
	    if (solve()) {
		solveInProgress = false;
	    }
	}
    }

    /**
     * @return the numberOfRecursionsNeededToSolve
     */
    public int getNumberOfRecursionsNeededToSolve() {
	return numberOfRecursionsNeededToSolve;
    }

    /**
     * @param numberOfRecursionsNeededToSolve
     *            the numberOfRecursionsNeededToSolve to set
     */
    public void setNumberOfRecursionsNeededToSolve(int numberOfRecursionsNeededToSolve) {
	this.numberOfRecursionsNeededToSolve = numberOfRecursionsNeededToSolve;
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
	this.numberOfStepsToSolve = numberOfStepsToSolve;
    }
}

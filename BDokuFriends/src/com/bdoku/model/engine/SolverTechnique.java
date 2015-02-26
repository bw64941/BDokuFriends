/**
 * 
 */
package com.bdoku.model.engine;

import java.util.ArrayList;

import com.bdoku.model.Cell;
import com.bdoku.model.ValuesArray;

/**
 * @author bwinters
 *
 */
public interface SolverTechnique {
    
    public enum CellModStatus { 
	SET_VALUE, REMOVE_POSSIBILITY;
    };
    public void executeTechnique(ValuesArray values);
    public void processCellGrouping(ArrayList<Cell> cells);
}

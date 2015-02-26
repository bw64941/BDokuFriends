/**
 * 
 */
package com.bdoku.model.engine;

import com.bdoku.model.ValuesArray;

/**
 * @author bwinters
 *
 */
public class SolverContext {
    
    private SolverTechnique technique;
    
    /**
     * Solver Context Constructor
     * @param technique
     */
    public SolverContext(SolverTechnique technique) {
	this.technique = technique;
    }
    
    /**
     * Call execution interface to SolverTechniques
     * @param values
     * @param area
     */
    public void executeTechnique(ValuesArray values) {
	this.technique.executeTechnique(values);
    }
}

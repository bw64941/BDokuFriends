/**
 * 
 */
package com.bdoku.model.engine;

import java.util.Stack;

/**
 * @author bwinters
 * 
 */
public class ValuesArrayHistory extends Stack<SolverStep> {

    private static final long serialVersionUID = 1L;

    public ValuesArrayHistory() {
	super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Stack#peek()
     */
    @Override
    public synchronized SolverStep peek() {
	return super.peek();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Stack#pop()
     */
    @Override
    public synchronized SolverStep pop() {
	return super.pop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Stack#push(java.lang.Object)
     */
    @Override
    public SolverStep push(SolverStep step) {
	return super.push(step);
    }
}

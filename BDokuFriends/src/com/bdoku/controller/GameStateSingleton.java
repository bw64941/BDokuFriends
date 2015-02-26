/**
 * 
 */
package com.bdoku.controller;

/**
 * @author bwinters
 * 
 */
public class GameStateSingleton {

    private static GameStateSingleton gameState = null;
    private boolean solved = false;

    private GameStateSingleton() {
    }

    /**
     * @return the gameState
     */
    public static GameStateSingleton getGameState() {
	if (gameState == null) {
	    gameState = new GameStateSingleton();
	}
	return gameState;
    }

    /**
     * @param gameState
     *            the gameState to set
     */
    public static void setGameState(GameStateSingleton gameState) {
	GameStateSingleton.gameState = gameState;
    }

    /**
     * @return the solved
     */
    public boolean isSolved() {
	return solved;
    }

    /**
     * @param solved
     *            the solved to set
     */
    public void setSolved(boolean solved) {
	this.solved = solved;
    }

}

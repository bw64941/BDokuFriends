/**
 * 
 */
package com.bdoku.controller;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.bdoku.view.BoardView;

/**
 * @author bwinters
 * 
 */
public class UIThread extends Thread {

    private SurfaceHolder surfaceHolder;
    private BoardView boardView;
    private boolean isRunning = false;

    public UIThread(SurfaceHolder surfaceHolder, BoardView boardView) {
	this.surfaceHolder = surfaceHolder;
	this.boardView = boardView;
    }

    public boolean isRunning() {
	return this.isRunning;
    }

    public void setRunning(boolean isRunning) {
	this.isRunning = isRunning;
    }

    @Override
    public void run() {
	Canvas canvas;
	while (isRunning) {
	    canvas = null;
	    try {
		canvas = surfaceHolder.lockCanvas(null);
		synchronized (surfaceHolder) {
		    if (boardView != null && canvas != null) {
			boardView.doDraw(canvas, 0, 0);
			if (boardView.isSolved()) {
			    isRunning = false;
			}
		    }
		}

	    } finally {
		if (canvas != null) {
		    surfaceHolder.unlockCanvasAndPost(canvas);
		}
	    }
	}
    }

    /**
     * @return the surfaceHolder
     */
    public SurfaceHolder getSurfaceHolder() {
	return surfaceHolder;
    }

    /**
     * @param surfaceHolder
     *            the surfaceHolder to set
     */
    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
	this.surfaceHolder = surfaceHolder;
    }

}

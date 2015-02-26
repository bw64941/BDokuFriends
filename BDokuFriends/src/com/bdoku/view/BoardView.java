/**
 * 
 */
package com.bdoku.view;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.bdoku.controller.UIThread;
import com.bdoku.friends.R;
import com.bdoku.model.BoardUtils;

/**
 * @author bwinters
 * 
 */
public class BoardView extends SurfaceView implements SurfaceHolder.Callback {

    private float cellWidth;
    private float cellHeight;
    private int selectedXCoordinate;
    private int selectedYCoordinate;
    private final Rect selectedRow = new Rect();
    private final Rect selectedCol = new Rect();
    private final Rect selectedCell = new Rect();
    private final Rect hintCell = new Rect();
    private String hintValue = "";
    private final Rect errorCell = new Rect();
    public static final int HIGHLIGHT = 0;
    public static final int HIGHLIGHT_HINT = 1;
    public static final int HIGHLIGHT_ERROR = 2;
    private UIThread thread = null;

    private String selectedCellValue = "";
    private ArrayList<Integer> selectedCellPencilValues = new ArrayList<Integer>();
    private boolean pencilModeOn = false;
    private BoardUtils boardUtils = null;
    private boolean solved = false;
    private Bitmap bitmap = null;
    private byte[] originalStateOfBoard = null;

    /**
     * BoardView Constructor
     * 
     * @param context
     */
    public BoardView(Context context, AttributeSet attributes) {
	super(context, attributes);
	setFocusable(true);
	setFocusableInTouchMode(true);
	getHolder().addCallback(this);
	thread = new UIThread(getHolder(), this);
	boardUtils = new BoardUtils();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	// Log.d("BoardView", "Surface Changed");
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
	// Log.d("BoardView", "Surface Created");
	if (!thread.isAlive()) {
	    thread = new UIThread(getHolder(), this);
	    thread.setRunning(true);
	    thread.start();
	    boardUtils = new BoardUtils();
	    solved = boardUtils.isBoardSolved();

	    // mHandler.removeCallbacks(mUpdateTimeTask);
	    // mHandler.postDelayed(mUpdateTimeTask, 100);

	    originalStateOfBoard = captureCanvas();
	}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
	if (thread.isAlive()) {
	    // Log.d("BoardView", "Surface Destroyed");
	    thread.setRunning(false);
	}
    }

    /*
     * Captures the bitmap drawn on the screen for Facebook share
     */
    public byte[] captureCanvas() {
	bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
	Canvas imageCanvas = new Canvas(bitmap);
	doDraw(imageCanvas, 0, 0);

	Bitmap backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fb_snapshot_flat);
	int width = backgroundBitmap.getWidth();
	int height = backgroundBitmap.getHeight();
	int newWidth = bitmap.getWidth() + 80;
	int newHeight = bitmap.getHeight() + 80;

	// calculate the scale - in this case = 0.4f
	float scaleWidth = ((float) newWidth) / width;
	float scaleHeight = ((float) newHeight) / height;
	// createa matrix for the manipulation
	Matrix matrix = new Matrix();
	matrix.postScale(scaleWidth, scaleHeight);
	Bitmap resizedBitmap = Bitmap.createBitmap(backgroundBitmap, 0, 0, width, height, matrix, true);

	// createa matrix for the manipulation
	Matrix matrix2 = new Matrix();
	matrix2.postTranslate(80, 0);

	Canvas canvas2 = new Canvas(resizedBitmap);
	canvas2.drawBitmap(bitmap, matrix2, null);

	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	return (bytes.toByteArray());
    }

    /**
     * On touch event for board....determine cell pressed
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
	if (event.getAction() != MotionEvent.ACTION_DOWN) {
	    return super.onTouchEvent(event);
	}

	clearBoardRectangles();

	if (solved == true) {
	    return super.onTouchEvent(event);
	}

	int xPosTouched = (int) (event.getX() / cellWidth);
	int yPosTouched = (int) (event.getY() / cellHeight);

	highlightSelectedArea(xPosTouched, yPosTouched, BoardView.HIGHLIGHT);
	showTouchDialog(selectedXCoordinate, selectedYCoordinate);
	return true;
    }

    /**
     * Select Cell at x,y coordinates
     * 
     * @param x
     * @param y
     */
    public void highlightSelectedArea(int x, int y, int type) {
	selectedXCoordinate = Math.min(Math.max(x, 0), 8);
	selectedYCoordinate = Math.min(Math.max(y, 0), 8);
	selectedCell.set((int) (x * cellWidth), (int) (y * cellHeight), (int) (x * cellWidth + (cellWidth)), (int) (y * cellHeight + cellHeight));
	switch (type) {
	case BoardView.HIGHLIGHT:
	    // left,top,right,bottom
	    selectedRow.set(0, (int) (y * cellHeight), (int) (x * cellWidth + (9 * cellWidth)), (int) (y * cellHeight + (cellHeight)));
	    selectedCol.set((int) (x * cellWidth), 0, (int) (x * cellWidth + (cellWidth)), (int) (y * cellHeight + (9 * cellHeight)));
	    selectedCellValue = String.valueOf(boardUtils.getValueAt(selectedYCoordinate, selectedXCoordinate));
	    selectedCellPencilValues = boardUtils.getPencilBoardValuesArray(selectedYCoordinate, selectedXCoordinate);
	    break;
	case BoardView.HIGHLIGHT_HINT:
	    hintCell.set((int) (x * cellWidth), (int) (y * cellHeight), (int) (x * cellWidth + (cellWidth)), (int) (y * cellHeight + cellHeight));
	    hintValue = String.valueOf(boardUtils.getValueAtSolvedCell(y, x));
	    break;
	case BoardView.HIGHLIGHT_ERROR:
	    errorCell.set((int) (x * cellWidth), (int) (y * cellHeight), (int) (x * cellWidth + cellWidth), (int) (y * cellHeight + cellHeight));
	    break;
	default:
	    break;
	}

    }

    /**
     * Show appropriate game dialog.
     */
    protected void showTouchDialog(int x, int y) {
	// Flip X and Y when indexing values array.
	if (pencilModeOn && selectedCellPencilValues != null) {
	    PencilQuickActionBar valueBar = new PencilQuickActionBar(this, selectedCellPencilValues);
	    valueBar.show();
	} else {
	    if (boardUtils.getValuesAtSystemCell(y, x) == 0) {
		ValueQuickActionBar valueBar = new ValueQuickActionBar(this, selectedCellPencilValues);
		valueBar.show();
	    }
	}

    }

    /**
     * Determine if chosen value can be placed in location.
     * 
     * @param possibilityChosen
     */
    public void setValueInSelectedCell(int possibilityChosen) {
	boardUtils.setValueAtCell(selectedYCoordinate, selectedXCoordinate, possibilityChosen);
	if (boardUtils.isBoardSolved() == true) {
	    solved = true;
	}

    }

    /*
     * Re-draw the board.
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    public void doDraw(Canvas canvas, float xOffset, float yOffset) {

	// Always draw system placed values.
	drawBackground(canvas, xOffset, yOffset);
	drawSystemPlacedValues(canvas);
	drawUserPlacedValues(canvas);

	// draw pencil values or user placed values.
	if (pencilModeOn) {
	    drawPencilValues(canvas);
	}

	if (!hintCell.isEmpty()) {
	    Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
	    foreground.setColor(getResources().getColor(R.color.errorHighlightColor));
	    foreground.setStyle(Style.FILL);
	    foreground.setTextSize(cellHeight * 0.75f);
	    foreground.setTextScaleX(cellWidth / cellHeight);
	    foreground.setTextAlign(Paint.Align.CENTER);

	    // Draw the number in the center of the tile
	    FontMetrics fm = foreground.getFontMetrics();
	    // Centering in X: use alignment (and X at midpoint)
	    float x = cellWidth / 2;
	    // Centering in Y: measure ascent/descent first
	    float y = cellHeight / 2 - (fm.ascent + fm.descent) / 2;

	    canvas.drawText(hintValue, hintCell.left + x, hintCell.top + y, foreground);
	    // imageCanvas.drawText(hintValue, hintCell.left + x, hintCell.top +
	    // y, foreground);
	    foreground.setAlpha(foreground.getAlpha() - 1);
	}

	// Draw the highlighted cell selection...
	Paint selected = new Paint();
	selected.setColor(getResources().getColor(R.color.highlightColor));
	canvas.drawRect(selectedRow, selected);
	// imageCanvas.drawRect(selectedRow, selected);
	canvas.drawRect(selectedCol, selected);
	// imageCanvas.drawRect(selectedCol, selected);

	// Draw the error Cell, if it exists...
	Paint error = new Paint();
	error.setColor(getResources().getColor(R.color.errorHighlightColor));
	canvas.drawRect(errorCell, error);
	// imageCanvas.drawRect(errorCell, error);
    }

    /*
     * Re-draw the board background.
     */
    private void drawBackground(Canvas canvas, float xOffset, float yOffset) {
	cellWidth = this.getWidth() / (float) 9;
	cellHeight = this.getHeight() / (float) 9;
	// Draw the background...
	Paint background = new Paint();
	background.setColor(getResources().getColor(R.color.white));
	background.setAntiAlias(true);
	background.setShadowLayer(cellWidth / 30, cellWidth / 30, cellWidth / 30, Color.BLACK);
	canvas.drawRect(xOffset, yOffset, getWidth(), getHeight(), background);
	// imageCanvas.drawRect(0, 0, getWidth(), getHeight(), background);

	// Define colors for the grid lines
	Paint dark = new Paint();
	dark.setColor(getResources().getColor(R.color.majorBoardGridLinesColor));
	dark.setStyle(Style.FILL_AND_STROKE);
	dark.setStrokeWidth(cellWidth / 10);
	dark.setShadowLayer(cellWidth / 30, cellWidth / 30, cellWidth / 30, R.color.highlightColor);

	Paint light = new Paint();
	light.setColor(getResources().getColor(R.color.majorBoardGridLinesColor));
	light.setStyle(Style.FILL_AND_STROKE);
	light.setStrokeWidth(cellWidth / 20);
	light.setShadowLayer(cellWidth / 30, cellWidth / 30, cellWidth / 30, R.color.highlightColor);

	// Draw the minor grid lines
	for (int i = 0; i <= 10; i++) {
	    if (i % 3 == 0) {
		canvas.drawLine(0, i * cellHeight, getWidth(), i * cellHeight, dark);
		// imageCanvas.drawLine(0, i * cellHeight, getWidth(), i *
		// cellHeight, dark);
		canvas.drawLine(i * cellWidth, 0, i * cellWidth, getHeight(), dark);
		// imageCanvas.drawLine(i * cellWidth, 0, i * cellWidth,
		// getHeight(), dark);
	    } else {
		canvas.drawLine(0, i * cellHeight, getWidth(), i * cellHeight, light);
		// imageCanvas.drawLine(0, i * cellHeight, getWidth(), i *
		// cellHeight, light);
		canvas.drawLine(i * cellWidth, 0, i * cellWidth, getHeight(), light);
		// imageCanvas.drawLine(i * cellWidth, 0, i * cellWidth,
		// getHeight(), light);
	    }
	}
    }

    /**
     * Draws system placed values placed on the board.
     * 
     * @param canvas
     * @param foreground
     */
    private void drawSystemPlacedValues(Canvas canvas) {
	Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
	foreground.setColor(getResources().getColor(R.color.systemValueColor));
	foreground.setStyle(Style.FILL);
	foreground.setTextSize(cellHeight * 0.75f);
	foreground.setTextScaleX(cellWidth / cellHeight);
	foreground.setTextAlign(Paint.Align.CENTER);

	// Draw the number in the center of the tile
	FontMetrics fm = foreground.getFontMetrics();
	// Centering in X: use alignment (and X at midpoint)
	float x = cellWidth / 2;
	// Centering in Y: measure ascent/descent first
	float y = cellHeight / 2 - (fm.ascent + fm.descent) / 2;

	if (solved == true) {
	    for (int row = 0; row < 9; row++) {
		for (int col = 0; col < 9; col++) {
		    int value = boardUtils.getValueAtSolvedCell(row, col);
		    if (value > 0) {
			canvas.drawText(String.valueOf(value), col * cellWidth + x, row * cellHeight + y, foreground);
			// imageCanvas.drawText(String.valueOf(value), col *
			// cellWidth + x, row * cellHeight + y, foreground);
		    }
		}
	    }
	} else {
	    Paint selectedCellPaint = new Paint();
	    selectedCellPaint.setColor(getResources().getColor(R.color.highlightColor));

	    for (int row = 0; row < 9; row++) {
		for (int col = 0; col < 9; col++) {
		    int value = boardUtils.getValuesAtSystemCell(row, col);
		    if (value > 0) {
			if (String.valueOf(value).equals(selectedCellValue)) {
			    canvas.drawRect(new Rect((int) (col * cellWidth), (int) (row * cellHeight), (int) (col * cellWidth + (cellWidth)), (int) (row * cellHeight + cellHeight)), selectedCellPaint);
			    // imageCanvas.drawRect(new Rect((int) (col *
			    // cellWidth), (int) (row * cellHeight), (int) (col
			    // * cellWidth + (cellWidth)),
			    // (int) (row * cellHeight + cellHeight)),
			    // selectedCellPaint);
			}
			canvas.drawText(String.valueOf(value), col * cellWidth + x, row * cellHeight + y, foreground);
			// imageCanvas.drawText(String.valueOf(value), col *
			// cellWidth + x, row * cellHeight + y, foreground);
		    }
		}
	    }
	}
    }

    /**
     * Draws User Placed values placed on the board.
     * 
     * @param canvas
     * @param foreground
     */
    private void drawUserPlacedValues(Canvas canvas) {
	Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
	foreground.setColor(getResources().getColor(R.color.userPlacedValueColor));
	foreground.setStyle(Style.FILL);
	foreground.setTextSize(cellHeight * 0.75f);
	foreground.setTextScaleX(cellWidth / cellHeight);
	foreground.setTextAlign(Paint.Align.CENTER);

	// Draw the number in the center of the tile
	FontMetrics fm = foreground.getFontMetrics();
	// Centering in X: use alignment (and X at midpoint)
	float x = cellWidth / 2;
	// Centering in Y: measure ascent/descent first
	float y = cellHeight / 2 - (fm.ascent + fm.descent) / 2;

	Paint selectedCellPaint = new Paint();
	selectedCellPaint.setColor(getResources().getColor(R.color.highlightColor));

	for (int row = 0; row < 9; row++) {
	    for (int col = 0; col < 9; col++) {
		int value = boardUtils.getValueAtUserCell(row, col);
		if (value > 0) {
		    if (pencilModeOn && boardUtils.cellHasPencilValue(row, col)) {
			foreground.setAlpha(128);
		    }
		    if (String.valueOf(value).equals(selectedCellValue)) {
			canvas.drawRect(new Rect((int) (col * cellWidth), (int) (row * cellHeight), (int) (col * cellWidth + (cellWidth)), (int) (row * cellHeight + cellHeight)), selectedCellPaint);
			// imageCanvas.drawRect(new Rect((int) (col *
			// cellWidth), (int) (row * cellHeight), (int) (col *
			// cellWidth + (cellWidth)), (int) (row
			// * cellHeight + cellHeight)), selectedCellPaint);
		    }
		    canvas.drawText(String.valueOf(value), col * cellWidth + x, row * cellHeight + y, foreground);
		    // imageCanvas.drawText(String.valueOf(value), col *
		    // cellWidth + x, row * cellHeight + y, foreground);
		}
	    }
	}
    }

    /**
     * Draws only the possibilities that remain in each cell.
     * 
     * @param canvas
     * @param foreground
     */
    private void drawPencilValues(Canvas canvas) {
	// Draw the possbilities for empty cells...
	// Define color and style for numbers
	Paint foreground_poss = new Paint(Paint.ANTI_ALIAS_FLAG);
	foreground_poss.setColor(getResources().getColor(R.color.pencilValueColor));
	foreground_poss.setStyle(Style.FILL_AND_STROKE);
	foreground_poss.setStrokeWidth(cellWidth / 20);
	foreground_poss.setTextSize(cellHeight * 0.25f);
	foreground_poss.setTextScaleX(cellWidth / cellHeight);
	foreground_poss.setTextAlign(Paint.Align.CENTER);
	foreground_poss.setStrikeThruText(false);

	// Centering in X: use alignment (and X at midpoint)
	float x_poss = cellWidth / 3.5f;
	// Centering in Y: measure ascent/descent first
	float y_poss = cellHeight / 3f;
	float centerText = foreground_poss.getTextSize() / 2.65f;
	// Draw values on board.

	for (int row = 0; row < 9; row++) {
	    for (int col = 0; col < 9; col++) {
		ArrayList<Integer> valuesToDraw = boardUtils.getPencilBoardValuesArray(row, col);
		if (valuesToDraw != null) {
		    for (Integer value : valuesToDraw) {
			float xStart = 0;
			float yStart = 0;
			switch (value) {
			case 1:
			    xStart = (col * cellWidth + (x_poss * 1) - centerText);
			    yStart = (row * cellHeight + (y_poss * 1) - centerText);
			    break;
			case 2:
			    xStart = (col * cellWidth + (x_poss * 2) - centerText);
			    yStart = (row * cellHeight + (y_poss * 1) - centerText);
			    break;
			case 3:
			    xStart = (col * cellWidth + (x_poss * 3) - centerText);
			    yStart = (row * cellHeight + (y_poss * 1) - centerText);
			    break;
			case 4:
			    xStart = (col * cellWidth + (x_poss * 1) - centerText);
			    yStart = (row * cellHeight + (y_poss * 2) - centerText);
			    break;
			case 5:
			    xStart = (col * cellWidth + (x_poss * 2) - centerText);
			    yStart = (row * cellHeight + (y_poss * 2) - centerText);
			    break;
			case 6:
			    xStart = (col * cellWidth + (x_poss * 3) - centerText);
			    yStart = (row * cellHeight + (y_poss * 2) - centerText);
			    break;
			case 7:
			    xStart = (col * cellWidth + (x_poss * 1) - centerText);
			    yStart = (row * cellHeight + (y_poss * 3) - centerText);
			    break;
			case 8:
			    xStart = (col * cellWidth + (x_poss * 2) - centerText);
			    yStart = (row * cellHeight + (y_poss * 3) - centerText);
			    break;
			case 9:
			    xStart = (col * cellWidth + (x_poss * 3) - centerText);
			    yStart = (row * cellHeight + (y_poss * 3) - centerText);
			    break;
			default:
			    break;
			}

			canvas.drawText(String.valueOf(value), xStart, yStart, foreground_poss);
			// imageCanvas.drawText(String.valueOf(value), xStart,
			// yStart, foreground_poss);
		    }
		}
	    }
	}
    }

    /**
     * Clear view rectangle
     */
    public void clearBoardRectangles() {
	hintCell.setEmpty();
	errorCell.setEmpty();
	selectedCell.setEmpty();
	selectedRow.setEmpty();
	selectedCol.setEmpty();
	selectedCellValue = "";
    }

    /**
     * @return the pencilModeOn
     */
    public boolean isPencilModeOn() {
	return pencilModeOn;
    }

    /**
     * @param pencilModeOn
     *            the pencilModeOn to set
     */
    public void setPencilModeOn(boolean pencilModeOn) {
	this.pencilModeOn = pencilModeOn;
    }

    /**
     * @return the thread
     */
    public UIThread getThread() {
	return thread;
    }

    /**
     * @param thread
     *            the thread to set
     */
    public void setThread(UIThread thread) {
	this.thread = thread;
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
	if (solved == true) {
	    clearBoardRectangles();
	    boardUtils.clearUserMods();
	}
	this.solved = solved;
    }

    /**
     * @return the bitmap
     */
    public Bitmap getBitmap() {
	return bitmap;
    }

    /**
     * @param bitmap
     *            the bitmap to set
     */
    public void setBitmap(Bitmap bitmap) {
	this.bitmap = bitmap;
    }

    /**
     * @return the originalStateOfBoard
     */
    public byte[] getOriginalStateOfBoard() {
	return originalStateOfBoard;
    }

    /**
     * @param originalStateOfBoard
     *            the originalStateOfBoard to set
     */
    public void setOriginalStateOfBoard(byte[] originalStateOfBoard) {
	this.originalStateOfBoard = originalStateOfBoard;
    }

}

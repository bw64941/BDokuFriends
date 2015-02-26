/**
 * 
 */
package com.bdoku.controller;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bdoku.friends.BDokuFriendsActivity;
import com.bdoku.friends.DifficultyChooserActivity;
import com.bdoku.friends.RestartActivity;
import com.bdoku.model.Board;
import com.bdoku.model.BoardOpen;
import com.bdoku.model.BoardPostObject;
import com.bdoku.model.BoardSaver;
import com.bdoku.model.BoardUtils;
import com.bdoku.model.Cell;
import com.bdoku.model.PredefinedBoardList;
import com.bdoku.model.SavedBoard;
import com.bdoku.model.engine.SolverStep;
import com.bdoku.view.BoardView;
import com.bdoku.view.ControlsViewFlipper;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.R;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

/**
 * @author bwinters
 * 
 */
public class GameController {

    private static final String TAG = GameController.class.getName();
    private static final int SOLVED_TIME_DISPLAY_POST = 100;
    private BDokuFriendsActivity activity = null;
    private BoardView boardView = null;
    private ControlsViewFlipper controlsViewFlipper = null;
    private static BoardUtils boardUtilities = new BoardUtils();
    private GameTimer gameTimer = null;
    private long time = 0L;
    private static Handler fbCommentPollThread = new Handler();
    private boolean responseLoopStarted = false;
    private static String commentDataFromPost = "";
    private PendingAction pendingAction = PendingAction.NONE;
    private final String PENDING_ACTION_BUNDLE_KEY = "com.bdoku.friends.BDokuFriendsActivity:PendingAction";
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");
    private static String picturePostId = "";
    private UiLifecycleHelper uiHelper;

    private enum PendingAction {
	NONE, POST_PHOTO
    }

    public GameController(BDokuFriendsActivity activity, BoardView boardView, ControlsViewFlipper controlsViewFlipper) {
	this.activity = activity;
	this.boardView = boardView;
	this.boardView.setDrawingCacheEnabled(true);
	this.controlsViewFlipper = controlsViewFlipper;
	this.gameTimer = new GameTimer(1000, mUpdateTimeTask);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    public void onDestroy() {
	// Log.d(TAG, "Destroy called");
	if (boardView.getThread().isAlive()) {
	    boardView.getThread().setRunning(false);
	    // boardView.getmHandler().removeCallbacks(boardView.getmUpdateTimeTask());
	}
	gameTimer.stop();
	fbCommentPollThread.removeCallbacks(fbResponseTask);
	responseLoopStarted = false;
	uiHelper.onDestroy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    public boolean onOptionsItemSelected(MenuItem item) {
	if (item.getItemId() == R.id.solve) {
	    setSolvedOn();
	} else if (item.getItemId() == R.id.hint) {
	    getRandomHint();
	} else {
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = activity.getMenuInflater();
	inflater.inflate(R.menu.board_menu, menu);
	return true;
    }

    /*
     * 
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(Bundle)
     */
    public void onCreate(Bundle savedInstanceState) {
	if (savedInstanceState != null) {
	    String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
	    pendingAction = PendingAction.valueOf(name);
	}

	Board board = (Board) activity.getLastNonConfigurationInstance();
	if (board == null) {
	    GameStateSingleton.getGameState().setSolved(false);
	    /*
	     * Initialize the board chosen from parent activity and solve
	     * background board.
	     */
	    showBoard();
	}
	commentDataFromPost = activity.getResources().getString(R.string.waitingForFBResponseText);
	uiHelper = new UiLifecycleHelper(activity, callback);
	uiHelper.onCreate(savedInstanceState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    public void onPause() {
	uiHelper.onPause();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    public void onResume() {
	// Log.d(TAG, "Resume called");
	uiHelper.onResume();
	// For scenarios where the main activity is launched and user
	// session is not null, the session state change notification
	// may not be triggered. Trigger it if it's open/closed.
	Session session = Session.getActiveSession();
	if (session != null && (session.isOpened() || session.isClosed())) {
	    onSessionStateChange(session, session.getState(), null);
	}

	if (boardUtilities != null) {
	    GameStateSingleton.getGameState().setSolved(boardUtilities.isBoardSolved());
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    public void onSaveInstanceState(Bundle outState) {
	outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	uiHelper.onSaveInstanceState(outState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    public Object onRetainNonConfigurationInstance() {
	return Board.getBoard();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	uiHelper.onActivityResult(requestCode, resultCode, data);
	if (requestCode == GameController.SOLVED_TIME_DISPLAY_POST) {
	    TextView timerView = (TextView) activity.findViewById(R.id.timerText);
	    // Log.d(TAG, "SOLVED_TIME_DISPLAY_POST returned");
	    if (resultCode == BDokuFriendsActivity.POST_GAME_TIME) {
		// Log.d(TAG, "POST_GAME_TIME returned");
		BoardPostObject boardPostObject = new BoardPostObject(activity.getResources().getString(R.string.applicationName), activity.getResources().getString(R.string.playingPostString), activity.getResources().getString(
			R.string.solvedPostString), activity.getResources().getString(R.string.appLink), activity.getResources().getString(R.string.appLogoLink));
		// imageCaptured = boardView.getOriginalStateOfBoard();
		if (Session.getActiveSession() != null) {
		    // Set the dialog parameters
		    Bundle params = new Bundle();
		    params.putString("name", boardPostObject.getTitle());
		    params.putString("caption", boardPostObject.getCaption());
		    params.putString("description", boardPostObject.getDescription() + "\nSolve Time: " + timerView.getText());
		    params.putString("link", boardPostObject.getLink());
		    params.putString("picture", boardPostObject.getImageLink());

		    // Invoke the dialog
		    WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(activity, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(Bundle values, FacebookException error) {
			    if (error == null) {
				// When the story is posted, echo the success
				// and the post Id.
				String solvePostId = values.getString("post_id");
				if (picturePostId != null) {
				    Toast toast = Toast.makeText(activity.getApplicationContext(), "Solved Post Published: [" + solvePostId + "]", Toast.LENGTH_SHORT);
				    toast.setGravity(Gravity.CENTER, 0, 0);
				    toast.show();
				}
			    }
			}

		    }).build();
		    feedDialog.show();
		} else {
		    Toast toast = Toast.makeText(activity.getApplicationContext(), "Not connected to Facebook", Toast.LENGTH_SHORT);
		    toast.setGravity(Gravity.CENTER, 0, 0);
		    toast.show();
		}
	    } else {
		activity.finish();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCheckedChanged(android.widget.CompoundButton,
     * boolean)
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	if (buttonView.getId() == R.id.pencilToggle) {
	    if (GameStateSingleton.getGameState().isSolved() == false) {
		boardView.setPencilModeOn(isChecked);
	    }
	} else {
	}
    }

    private Runnable mUpdateTimeTask = new Runnable() {
	public void run() {
	    final long start = time;
	    long millis = System.currentTimeMillis() - start;
	    int seconds = (int) (millis / 1000);
	    int minutes = seconds / 60;
	    seconds = seconds % 60;

	    String time = "";
	    if (seconds < 10) {
		time = "" + minutes + ":0" + seconds;
	    } else {
		time = "" + minutes + ":" + seconds;
	    }
	    updateTimer(time);
	}
    };

    /*
     * Returns the predefined board to play given chosen board from dialog box
     * on splash screen.
     */
    private void showBoard() {
	BoardOpen opener = new BoardOpen(activity);
	boolean resumeInProgressGame = activity.getIntent().getBooleanExtra(BDokuFriendsActivity.RESUME, false);

	if (resumeInProgressGame == true) {
	    SavedBoard savedInProgressBoard = opener.open();
	    if (savedInProgressBoard != null) {
		boardUtilities.resumeBoard(savedInProgressBoard);
		boardView.requestFocus();
	    } else {
		showToast("Error opening saved board!");
	    }
	    time = System.currentTimeMillis();
	} else {
	    PredefinedBoardList listOfPredefinedBoards = PredefinedBoardList.getSavedBoardList(activity);
	    String difficultyChosen = activity.getIntent().getStringExtra(DifficultyChooserActivity.DIFFICULTY_CHOSEN_BUNDLE);
	    SavedBoard predefinedBoard = listOfPredefinedBoards.getPredefinedBoardWithDifficulty(difficultyChosen);
	    if (predefinedBoard != null) {
		boardUtilities.openNewBoard(predefinedBoard);
		boardView.requestFocus();
	    } else {
		showToast("Error opening new board!");
	    }
	    time = System.currentTimeMillis();
	}
	gameTimer.start();
    }

    /**
     * Checks all of the user placed values on the board.
     */
    public void checkWorkDoneOnBoard() {
	if (GameStateSingleton.getGameState().isSolved() == false) {
	    if (boardUtilities.isBoardValid() == false) {
		showToast(activity.getResources().getString(R.string.mistakeText));
	    } else {
		showToast(activity.getResources().getString(R.string.doingGreatText));
	    }
	}
    }

    /**
     * Save Board
     * 
     * @return
     */
    public void saveBoard() {
	if (GameStateSingleton.getGameState().isSolved() == false) {
	    if (boardUtilities.isBoardValid() == true) {
		new BoardSaver(activity).execute(boardUtilities);
	    } else {
		showToast(activity.getResources().getString(R.string.correctMistakesText));
		// TODO Should I show where the mistake is?
		// boardView.highlightSelectedArea(boardUtilities.getValues().get(cellIndex).getCol(),
		// boardUtilities.getValues().get(cellIndex).getRow(),
		// BoardView.HIGHLIGHT_ERROR);
	    }
	}
    }

    /*
     * Get next Solved Value not figure out by player for Hint.
     */
    public void getRandomHint() {
	if (GameStateSingleton.getGameState().isSolved() == false) {
	    Cell hintCell = null;

	    /*
	     * Pick a random cell on the board that is either empty or been
	     * incorrectly solved by user. Give correct hint for that cell.
	     */
	    while (hintCell == null) {
		Random randon = new Random();
		int randomInex = randon.nextInt(boardUtilities.getValues().size());
		if ((boardUtilities.getValues().get(randomInex).isUserPlaced() || boardUtilities.getValues().get(randomInex).isEmpty())
			&& boardUtilities.getSolvedValues().get(randomInex).getValue() != boardUtilities.getValues().get(randomInex).getValue()) {
		    hintCell = boardUtilities.getSolvedValues().get(randomInex);
		}
	    }

	    boardView.highlightSelectedArea(hintCell.getCol(), hintCell.getRow(), BoardView.HIGHLIGHT_HINT);
	}
    }

    /*
     * Clears a cell on the board after clear button has been pressed.
     */
    public void undoLastMove() {
	if (boardUtilities.getValues().getHistory().size() > 0 && GameStateSingleton.getGameState().isSolved() == false) {
	    SolverStep setValueStep = null;

	    if (boardUtilities.getValues().getHistory().peek().getAlgorithm().equals(SolverStep.USER_PLACED)) {
		setValueStep = boardUtilities.getValues().getHistory().pop();
		setValueStep.getCell().clear();
		boardView.highlightSelectedArea(setValueStep.getCell().getCol(), setValueStep.getCell().getRow(), BoardView.HIGHLIGHT);
		boardUtilities.getValues().evaluateInitialValues();
	    }

	    while (!boardUtilities.getValues().getHistory().isEmpty() && boardUtilities.getValues().getHistory().peek().getAlgorithm().equals(SolverStep.SYSTEM_POSSIBILITY_REMOVED)) {
		SolverStep removePossibilitiesStep = boardUtilities.getValues().getHistory().pop();
		removePossibilitiesStep.getCell().getRemainingPossibilities().add(removePossibilitiesStep.getValue());
		if (boardUtilities.getValues().getHistory().empty()) {
		    break;
		}
	    }
	}
    }

    /**
     * @param showSolved
     *            the solved to set
     */
    public void setSolvedOn() {
	GameStateSingleton.getGameState().setSolved(true);
	boardView.setSolved(true);
	gameTimer.stop();
	Intent restartActivity = new Intent(activity.getApplicationContext(), RestartActivity.class);
	TextView timerView = (TextView) activity.findViewById(R.id.timerText);
	restartActivity.putExtra("gameTime", timerView.getText());
	activity.startActivityForResult(restartActivity, GameController.SOLVED_TIME_DISPLAY_POST);
    }

    /*
     * Show a message to the user
     */
    private void showToast(final String message) {
	activity.runOnUiThread(new Runnable() {
	    @Override
	    public void run() {
		Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	    }
	});
    }

    /*
     * Moves the view flipper to next view.
     */
    public void showNext() {
	controlsViewFlipper.showNext();
    }

    /*
     * Moves the view flipper to previous view.
     */
    public void showPrevious() {
	controlsViewFlipper.showPrevious();
    }

    /*
     * Updated timer on main activity
     */
    private void updateTimer(String time) {
	// Log.d(TAG, "updating timer");
	TextView timerView = (TextView) activity.findViewById(R.id.timerText);
	timerView.setText(time);
    }

    /*
     * Session Callback Handler for Facebook Session
     */
    private Session.StatusCallback callback = new Session.StatusCallback() {
	@Override
	public void call(Session session, SessionState state, Exception exception) {
	    onSessionStateChange(session, state, exception);
	}
    };

    /*
     * Logic for when the Facebook Session state changes
     */
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	LoginButton authButton = (LoginButton) activity.findViewById(R.id.authButton);
	Button shareButton = (Button) activity.findViewById(R.id.share);

	if (pendingAction != PendingAction.NONE && (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
	    new AlertDialog.Builder(activity).setTitle(R.string.cancelled).setMessage(R.string.permission_not_granted).setPositiveButton(R.string.ok, null).show();
	    pendingAction = PendingAction.NONE;
	    hasPublishPermission();
	} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
	    if (pendingAction != PendingAction.NONE) {
		handleAnnounce();
	    }
	}

	if (state.isOpened()) {
	    Log.i(TAG, "Logged in...");
	    authButton.setVisibility(View.GONE);
	    shareButton.setVisibility(View.VISIBLE);
	} else if (state.isClosed()) {
	    Log.i(TAG, "Logged out...");
	    authButton.setVisibility(View.VISIBLE);
	    shareButton.setVisibility(View.GONE);
	}
    }

    public void postPhoto() {
	if (hasPublishPermission()) {
	    byte[] boardByteArray = captureCanvas();
	    Bitmap bitmap = BitmapFactory.decodeByteArray(boardByteArray, 0, boardByteArray.length);
	    Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), bitmap, new Request.Callback() {
		@Override
		public void onCompleted(Response response) {
		    Log.d("Photo Post Result: [", response.toString() + "]");
		    showPublishResult(response.getGraphObject(), response.getError());
		}
	    });
	    request.executeAsync();
	} else {
	    pendingAction = PendingAction.POST_PHOTO;
	    Session session = Session.getActiveSession();
	    requestPublishPermissions(session);
	}
    }

    private boolean hasPublishPermission() {
	Session session = Session.getActiveSession();
	List<String> permissions = session.getPermissions();
	if (!permissions.containsAll(PERMISSIONS)) {
	    return false;
	}
	return true;
    }

    private void requestPublishPermissions(Session session) {
	if (session != null) {
	    Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(activity, PERMISSIONS);
	    // // demonstrate how to set an audience for the publish
	    // permissions,
	    // // if none are set, this defaults to FRIENDS
	    // .setDefaultAudience(SessionDefaultAudience.FRIENDS).setRequestCode(REAUTH_ACTIVITY_CODE);
	    session.requestNewPublishPermissions(newPermissionsRequest);
	}
    }

    /*
     * Handle facebook error after requests
     */
    private void handleError(FacebookRequestError error) {
	DialogInterface.OnClickListener listener = null;
	String dialogBody = null;

	if (error == null) {
	    dialogBody = activity.getString(R.string.error_dialog_default_text);
	} else {
	    switch (error.getCategory()) {
	    case AUTHENTICATION_RETRY:
		// tell the user what happened by getting the message id, and
		// retry the operation later
		String userAction = (error.shouldNotifyUser()) ? "" : activity.getString(error.getUserActionMessageId());
		dialogBody = activity.getString(R.string.error_authentication_retry, userAction);
		listener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialogInterface, int i) {
			Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
			activity.startActivity(intent);
		    }
		};
		break;

	    case AUTHENTICATION_REOPEN_SESSION:
		// close the session and reopen it.
		dialogBody = activity.getString(R.string.error_authentication_reopen);
		listener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialogInterface, int i) {
			Session session = Session.getActiveSession();
			if (session != null && !session.isClosed()) {
			    session.closeAndClearTokenInformation();
			}
		    }
		};
		break;

	    case PERMISSION:
		// request the publish permission
		dialogBody = activity.getString(R.string.error_permission);
		listener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialogInterface, int i) {
			pendingAction = PendingAction.POST_PHOTO;
			requestPublishPermissions(Session.getActiveSession());
		    }
		};
		break;

	    case SERVER:
	    case THROTTLING:
		// this is usually temporary, don't clear the fields, and
		// ask the user to try again
		dialogBody = activity.getString(R.string.error_server);
		break;

	    case BAD_REQUEST:
		// this is likely a coding error, ask the user to file a bug
		dialogBody = activity.getString(R.string.error_bad_request, error.getErrorMessage());
		break;

	    case OTHER:
	    case CLIENT:
	    default:
		// an unknown issue occurred, this could be a code error, or
		// a server side issue, log the issue, and either ask the
		// user to retry, or file a bug
		dialogBody = activity.getString(R.string.error_unknown, error.getErrorMessage());
		break;
	    }
	}

	new AlertDialog.Builder(activity).setPositiveButton(R.string.ok, listener).setTitle(R.string.error_dialog_title).setMessage(dialogBody).show();
    }

    private void handleAnnounce() {
	pendingAction = PendingAction.NONE;
	Session session = Session.getActiveSession();

	if (session == null || !session.isOpened()) {
	    return;
	}

	List<String> permissions = session.getPermissions();
	if (!permissions.containsAll(PERMISSIONS)) {
	    pendingAction = PendingAction.POST_PHOTO;
	    requestPublishPermissions(session);
	    return;
	}

	// byte[] boardByteArray = gameController.captureCanvas();
	// Bitmap bitmap = BitmapFactory.decodeByteArray(boardByteArray, 0,
	// boardByteArray.length);
	// Request request =
	// Request.newUploadPhotoRequest(Session.getActiveSession(), bitmap, new
	// Request.Callback() {
	// @Override
	// public void onCompleted(Response response) {
	// Log.d("Photo Post Result: [", response.toString() + "]");
	// showPublishResult(getString(R.string.photo_post),
	// response.getGraphObject(), response.getError());
	// }
	// });
	// request.executeAndWait();
    }

    /*
     * Captures the bitmap drawn on the screen for Facebook share
     */
    private byte[] captureCanvas() {
	Bitmap bitmap = Bitmap.createBitmap(boardView.getWidth(), boardView.getHeight(), Config.ARGB_8888);
	Canvas imageCanvas = new Canvas(bitmap);
	boardView.doDraw(imageCanvas, 0, 0);

	Bitmap backgroundBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.fb_snapshot_flat);
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

    private Runnable fbResponseTask = new Runnable() {
	public void run() {
	    updateFbResponseText(commentDataFromPost);
	    fbCommentPollThread.postDelayed(this, 3000);
	    requestCommentsFromPost();
	}
    };

    /**
     * Start the response thread listener after connecting to facebook
     */
    private void startResponseTask() {
	if (!responseLoopStarted) {
	    fbCommentPollThread.removeCallbacks(fbResponseTask);
	    fbCommentPollThread.postDelayed(fbResponseTask, 100);
	}
    }

    /*
     * Update facebook response text
     */
    private void updateFbResponseText(String text) {
	TextView fbCommentTextArea = (TextView) activity.findViewById(R.id.facebookFeedView);
	fbCommentTextArea.setText(text);
    }

    private void showQueryResult(GraphObject response, FacebookRequestError error) {
	if (error == null) {
	    if (response != null) {
		try {
		    JSONObject innerJSONObject = response.getInnerJSONObject();
		    JSONArray array = innerJSONObject.getJSONArray("data");
		    for (int i = 0; i < array.length(); i++) {
			JSONObject object = (JSONObject) array.get(i);
			String comment = object.getString("message");
			String time = object.getString("created_time");
			time = time.substring(time.indexOf('T') + 1, time.indexOf('+'));
			JSONObject fromObject = object.getJSONObject("from");
			String person = fromObject.getString("name");
			if (time != null && person != null && comment != null) {
			    // Log.d(TAG, "Does not equal NULL");
			    if (!commentDataFromPost.equals(activity.getResources().getString(R.string.waitingForFBResponseText)) && !commentDataFromPost.contains("" + time)) {
				commentDataFromPost = commentDataFromPost + "\n" + time + ":" + person + " says: [" + comment + "]";
			    } else {
				commentDataFromPost = time + ":" + person + " says: [" + comment + "]";
			    }
			    Log.d(TAG, commentDataFromPost);
			}
		    }
		} catch (JSONException e) {
		    e.printStackTrace();
		    fbCommentPollThread.removeCallbacks(fbResponseTask);
		}
	    }

	} else {
	    fbCommentPollThread.removeCallbacks(fbResponseTask);
	    handleError(error);
	}
    }

    private void showPublishResult(GraphObject response, FacebookRequestError error) {
	String title = null;
	String alertMessage = null;
	if (error == null) {
	    title = activity.getString(R.string.success);
	    if (response != null) {
		JSONObject jsonObject = response.getInnerJSONObject();
		try {
		    picturePostId = jsonObject.getString("id");
		    alertMessage = activity.getString(R.string.successfully_posted_post, picturePostId);
		    Log.d(TAG, alertMessage);
		    startResponseTask();
		} catch (JSONException e) {
		    e.printStackTrace();
		}
	    }
	    new AlertDialog.Builder(activity).setTitle(title).setMessage(alertMessage).setPositiveButton(R.string.ok, null).show();
	} else {
	    // title = getString(R.string.error);
	    // alertMessage = error.getErrorMessage();
	    // Log.d(TAG, alertMessage);
	    handleError(error);
	}
    }

    private void requestCommentsFromPost() {
	// TEST picturePostId = "548827525137873";
	Request request = Request.newGraphPathRequest(Session.getActiveSession(), picturePostId + "/comments", new Request.Callback() {
	    @Override
	    public void onCompleted(Response response) {
		showQueryResult(response.getGraphObject(), response.getError());
	    }
	});

	Bundle params = request.getParameters();
	params.putString("fields", "message, created_time, from.fields(name)");
	request.setGraphPath(picturePostId + "/comments");
	request.setParameters(params);
	request.executeAsync();
    }

}

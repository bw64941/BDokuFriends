package com.bdoku.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bdoku.friends.R;

/**
 * create QuickAction window
 * 
 * @author Yuki Anzai
 */
public class QuickAction extends PopupWindowForQuickAction {
    private View rootView;
    private Animation mTrackAnim;
    private final LayoutInflater inflater;
    private final Context context;

    public static final int STYLE_BUTTON = 1;
    public static final int STYLE_TOGGLE = 3;

    public static final int ANIM_GROW_FROM_LEFT = 1;
    public static final int ANIM_GROW_FROM_RIGHT = 2;
    public static final int ANIM_GROW_FROM_CENTER = 3;
    public static final int ANIM_REFLECT = 4;
    public static final int ANIM_AUTO = 5;

    private int itemLayoutId;
    private int layoutStyle;
    private int animStyle;

    private boolean animateTrack;
    private ViewGroup mTrack;

    private ArrayList<ActionItem> actionList;
    private ArrayList<ToggleItem> toggleList;

    /**
     * Constructor
     * 
     * @param anchor
     *            {@link View} on where the popup should be displayed
     */
    public QuickAction(View anchor, int layoutId, int layoutStyle, int itemLayoutId) {
	super(anchor);

	actionList = new ArrayList<ActionItem>();
	toggleList = new ArrayList<ToggleItem>();

	context = anchor.getContext();
	inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	if (itemLayoutId == -1) {
	    switch (layoutStyle) {
	    case STYLE_BUTTON:
		itemLayoutId = R.layout.action_item_btn;
		break;
	    case STYLE_TOGGLE:
		itemLayoutId = R.layout.action_item_tgl;
		break;
	    default:
		itemLayoutId = R.layout.action_item_btn;
		break;
	    }
	}

	this.itemLayoutId = itemLayoutId;

	setLayoutId(layoutId, layoutStyle);

	animStyle = ANIM_AUTO;

	// for button style
	setAnimTrack(R.anim.rail, new Interpolator() {
	    public float getInterpolation(float t) {
		// Pushes past the target area, then snaps back into place.
		// Equation for graphing: 1.2 - ((x * 1.6) - 1.1)^2
		final float inner = (t * 1.55f) - 1.1f;

		return 1.2f - inner * inner;
	    }
	});
    }

    /**
     * Constructor
     * 
     * @param anchor
     *            {@link View} on where the popup should be displayed
     */
    public QuickAction(View anchor, int layoutId, int layoutStyle) {
	this(anchor, layoutId, layoutStyle, -1);
    }

    /**
     * Constructor
     * 
     * @param anchor
     *            {@link View} on where the popup should be displayed
     */
    public QuickAction(View anchor) {
	this(anchor, R.layout.quickaction, STYLE_BUTTON, R.layout.action_item_btn);
    }

    /**
     * Animate track
     * 
     * @param animateTrack
     *            flag to animate track
     */
    public void setAnimTrackEnabled(boolean animateTrack) {
	this.animateTrack = animateTrack;
    }

    /**
     * Animate track
     * 
     * @param animId
     *            resource id of animation
     * @param interpolator
     *            interpolator of animation
     */
    public void setAnimTrack(int animId, Interpolator interpolator) {
	mTrackAnim = AnimationUtils.loadAnimation(anchor.getContext(), animId);
	if (interpolator != null)
	    mTrackAnim.setInterpolator(interpolator);
    }

    /**
     * Set animation style
     * 
     * @param animStyle
     *            animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(int animStyle) {
	this.animStyle = animStyle;
    }

    /**
     * Set layout style
     * 
     * @param layoutStyle
     *            layout style, default is set to STYLE_BUTTON
     */
    public void setLayoutStyle(int layoutStyle) {
	switch (layoutStyle) {
	case STYLE_TOGGLE:
	    setLayoutId(R.layout.quickaction, layoutStyle);
	    break;
	case STYLE_BUTTON:
	default:
	    setLayoutId(R.layout.quickaction, layoutStyle);
	    break;
	}
    }

    /**
     * Set layout style
     * 
     * @param layoutStyle
     *            layout style, default is set to STYLE_BUTTON
     */
    public void setLayoutId(int layoutId, int layoutStyle) {
	this.layoutStyle = layoutStyle;

	rootView = (ViewGroup) inflater.inflate(layoutId, null);

	setContentView(rootView);

	mTrack = (ViewGroup) rootView.findViewById(R.id.tracks);
	switch (layoutStyle) {
	case STYLE_TOGGLE:
	    setAnimTrackEnabled(true);
	    break;
	case STYLE_BUTTON:
	default:
	    setAnimTrackEnabled(true);
	    break;
	}
    }

    public void setItemLayoutId(int itemLayoutId) {
	this.itemLayoutId = itemLayoutId;
    }

    /**
     * Add action item
     * 
     * @param action
     *            {@link ActionItem}
     */
    public void addActionItem(ActionItem action) {
	actionList.add(action);
    }

    /**
     * Add action item
     * 
     * @param action
     *            {@link ActionItem}
     */
    public void addToggleItem(ToggleItem action) {
	toggleList.add(action);
    }

    /**
     * Show popup window
     */
    public void show() {
	switch (layoutStyle) {
	case STYLE_TOGGLE:
	    showToggleStyle();
	    break;
	case STYLE_BUTTON:
	default:
	    showButtonStyle();
	    break;
	}
    }

    /**
     * Show popup window
     */
    private void showToggleStyle() {
	preShow();

	int[] location = new int[2];

	anchor.getLocationOnScreen(location);

	Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

	rootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	rootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	int rootWidth = rootView.getMeasuredWidth();
	int rootHeight = rootView.getMeasuredHeight();

	int screenWidth = windowManager.getDefaultDisplay().getWidth();
	// int screenHeight = windowManager.getDefaultDisplay().getHeight();

	int xPos = (screenWidth - rootWidth) / 2;
	int yPos = anchorRect.bottom;

	// display on bottom
	if (rootHeight > anchorRect.top) {
	    yPos = anchorRect.bottom;
	}

	setAnimationStyle(screenWidth, anchorRect.centerX(), false);

	createToggleList();

	window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);

	if (animateTrack)
	    mTrack.startAnimation(mTrackAnim);
    }

    /**
     * Show popup window
     */
    private void showButtonStyle() {
	preShow();

	int[] location = new int[2];

	anchor.getLocationOnScreen(location);

	Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

	rootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	rootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	int rootWidth = rootView.getMeasuredWidth();
	int rootHeight = rootView.getMeasuredHeight();

	int screenWidth = windowManager.getDefaultDisplay().getWidth();

	int xPos = (screenWidth - rootWidth) / 2;
	int yPos = anchorRect.bottom;

	// display on bottom
	if (rootHeight > anchorRect.top) {
	    yPos = anchorRect.bottom;
	}

	setAnimationStyle(screenWidth, anchorRect.centerX(), false);

	createActionList();

	window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);

	if (animateTrack)
	    mTrack.startAnimation(mTrackAnim);
    }

    /**
     * Set animation style
     * 
     * @param screenWidth
     *            Screen width
     * @param requestedX
     *            distance from left screen
     * @param isOnTop
     *            flag to indicate where the popup should be displayed. Set TRUE
     *            if displayed on top of anchor and vice versa
     */
    private void setAnimationStyle(int screenWidth, int requestedX, boolean isOnTop) {
	switch (animStyle) {
	case ANIM_GROW_FROM_LEFT:
	    window.setAnimationStyle((isOnTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
	    break;

	case ANIM_GROW_FROM_RIGHT:
	    window.setAnimationStyle((isOnTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
	    break;

	case ANIM_GROW_FROM_CENTER:
	    window.setAnimationStyle((isOnTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
	    break;

	case ANIM_AUTO:
	    window.setAnimationStyle((isOnTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
	    break;
	}
    }

    /**
     * Create action list
     */
    private void createActionList() {
	View view;
	String title;
	Drawable icon;
	OnClickListener listener;
	int index = 0;

	for (ActionItem actionItem : actionList) {
	    title = actionItem.getTitle();
	    icon = actionItem.getIcon();
	    listener = actionItem.getListener();

	    view = getActionItem(title, icon, listener);

	    view.setFocusable(true);
	    view.setClickable(true);

	    mTrack.addView(view, index);
	    index++;
	}
    }

    /**
     * Create action list
     */
    private void createToggleList() {
	View view;
	String title;
	Drawable icon;
	boolean checked;
	OnCheckedChangeListener listener;
	int index = 0;

	for (ToggleItem actionItem : toggleList) {
	    title = actionItem.getTitle();
	    icon = actionItem.getIcon();
	    listener = actionItem.getListener();
	    checked = actionItem.isChecked();

	    view = getToggleItem(title, icon, checked, listener);

	    view.setFocusable(true);
	    view.setClickable(true);

	    mTrack.addView(view, index);
	    index++;
	}
    }

    /**
     * Get action item {@link View}
     * 
     * @param title
     *            action item title
     * @param icon
     *            {@link Drawable} action item icon
     * @param listener
     *            {@link View.OnClickListener} action item listener
     * @return action item {@link View}
     */
    private View getActionItem(String title, Drawable icon, OnClickListener listener) {

	LinearLayout container = (LinearLayout) inflater.inflate(itemLayoutId, null);

	TextView text = (TextView) container.findViewById(R.id.title);
	if (title != null) {
	    text.setText(title);
	} else {
	    text.setVisibility(View.GONE);
	}

	if (listener != null) {
	    container.setOnClickListener(listener);
	}

	return container;
    }

    /**
     * Get toggle item {@link View}
     * 
     * @param title
     *            action item title
     * @param icon
     *            {@link Drawable} action item icon
     * @param listener
     *            {@link View.OnCheckedChangeListener} action item listener
     * @return action item {@link View}
     */
    private View getToggleItem(String title, Drawable icon, boolean checked, OnCheckedChangeListener listener) {

	LinearLayout container = (LinearLayout) inflater.inflate(itemLayoutId, null);
	ToggleButton toggle = (ToggleButton) container.findViewById(R.id.toggle);

	if (title != null) {
	    toggle.setTextOn(title);
	    toggle.setTextOff(title);
	    toggle.setChecked(checked);
	    if (checked == true) {
		toggle.setTextColor(Color.BLACK);
	    }
	} else {
	    toggle.setVisibility(View.GONE);
	}

	if (listener != null) {
	    toggle.setOnCheckedChangeListener(listener);
	}

	return container;
    }
}
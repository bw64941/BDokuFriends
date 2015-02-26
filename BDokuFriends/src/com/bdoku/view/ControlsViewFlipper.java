/**
 * 
 */
package com.bdoku.view;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.facebook.android.R;

/**
 * @author bwinters
 * 
 */
public class ControlsViewFlipper extends LinearLayout implements OnClickListener, OnCheckedChangeListener {

    private ViewFlipper flipper = null;
    private ControlListener controlListener = null;

    public ControlsViewFlipper(Context context) {
	super(context);
    }

    public ControlsViewFlipper(Context context, AttributeSet attrs) {
	super(context, attrs);

	LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View view = layoutInflater.inflate(R.layout.viewflipper, this);

	flipper = (ViewFlipper) view.findViewById(R.id.flipper);

	Button back = (Button) findViewById(R.id.backButton);
	back.setOnClickListener(this);

	Button post = (Button) findViewById(R.id.postButton);
	post.setOnClickListener(this);

	Button undo = (Button) findViewById(R.id.undo);
	undo.setOnClickListener(this);

	Button checkWork = (Button) findViewById(R.id.validate);
	checkWork.setOnClickListener(this);

	ToggleButton pencilToggle = (ToggleButton) findViewById(R.id.pencilToggle);
	pencilToggle.setOnCheckedChangeListener(this);

	Button shareButton = (Button) findViewById(R.id.share);
	shareButton.setOnClickListener(this);

	Button save = (Button) findViewById(R.id.save);
	save.setOnClickListener(this);

	TextView fbCommentTextArea = (TextView) findViewById(R.id.facebookFeedView);
	fbCommentTextArea.setMovementMethod(new ScrollingMovementMethod());

    }

    public void setCustomEventListener(ControlListener controlListener) {
	this.controlListener = controlListener;
    }

    /*
     * Moves the view flipper to next view.
     */
    public void showNext() {
	flipper.showNext();
    }

    /*
     * Moves the view flipper to previous view.
     */
    public void showPrevious() {
	flipper.showPrevious();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	controlListener.onToggleEvent(buttonView, isChecked);
    }

    @Override
    public void onClick(View v) {
	controlListener.onClickEvent(v);
    }

}

package com.bdoku.friends;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class AboutActivity extends Activity {

    /*
     * Creates the about dialog for game.
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);

	LayoutInflater layout = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View aboutDialog = layout.inflate(R.layout.about, null);

	TextView textView = (TextView) aboutDialog.findViewById(R.id.about_content);
	textView.setMovementMethod(new ScrollingMovementMethod());
	textView.setText(getResources().getString(R.string.aboutDialogText));

	setContentView(aboutDialog);

    }
}

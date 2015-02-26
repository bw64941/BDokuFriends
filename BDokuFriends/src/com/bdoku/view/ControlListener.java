/**
 * 
 */
package com.bdoku.view;

import android.view.View;
import android.widget.CompoundButton;

/**
 * @author bwinters
 * 
 */
public interface ControlListener {

    public void onClickEvent(View v);

    public void onToggleEvent(CompoundButton buttonView, boolean isChecked);

}

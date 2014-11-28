/*
 * Copyright (c) 2014 Simon Robinson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package qr.cloud.util;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

/**
 * This layout intercepts back presses to hide the keyboard as well as exiting the activity - see:
 * http://stackoverflow.com/a/5811630/1993220
 */
public class BackDetectorRelativeLayout extends RelativeLayout {

	private static Activity mSearchActivity;

	public BackDetectorRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BackDetectorRelativeLayout(Context context) {
		super(context);
	}

	public static void setSearchActivity(Activity searchActivity) {
		mSearchActivity = searchActivity;
	}

	/**
	 * Overrides the handling of the back key to move back to the previous sources or dismiss the search dialog, instead
	 * of dismissing the input method.
	 */
	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (mSearchActivity != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			KeyEvent.DispatcherState state = getKeyDispatcherState();
			if (state != null) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
					state.startTracking(event, this);
					return true;
				} else if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled() && state.isTracking(event)) {
					mSearchActivity.onBackPressed();
					return true;
				}
			}
		}

		return super.dispatchKeyEventPreIme(event);
	}
}

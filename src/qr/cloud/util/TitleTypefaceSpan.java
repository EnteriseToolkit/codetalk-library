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

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

// see: http://stackoverflow.com/a/15181195
public class TitleTypefaceSpan extends MetricAffectingSpan {
	private Typeface mTypeface;

	// setting a custom title font (for pre-Honeycomb):
	// SpannableString s = new SpannableString("My Title");
	// s.setSpan(new TitleTypefaceSpan(this, getString(R.string.default_font)), 0, s.length(),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	// getSupportActionBar().setTitle(s);
	public TitleTypefaceSpan(Context context, String typefaceName) {
		mTypeface = Typefaces.get(context, typefaceName);
	}

	@Override
	public void updateMeasureState(TextPaint p) {
		p.setTypeface(mTypeface);
		p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG); // note: flag required for proper typeface rendering
	}

	@Override
	public void updateDrawState(TextPaint tp) {
		tp.setTypeface(mTypeface);
		tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG); // note: flag required for proper typeface rendering
	}
}
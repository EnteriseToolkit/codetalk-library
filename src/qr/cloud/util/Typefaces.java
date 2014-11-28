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
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.util.Log;

public class Typefaces {
	private static final String TAG = "Typefaces";

	private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);

	// see: http://stackoverflow.com/a/13087128/1993220 and http://stackoverflow.com/a/15181195
	public static Typeface get(Context c, String typefaceName) {
		Typeface typeface = sTypefaceCache.get(typefaceName);
		if (typeface == null) {
			try {
				typeface = Typeface.createFromAsset(c.getApplicationContext().getAssets(), typefaceName);
				sTypefaceCache.put(typefaceName, typeface);
			} catch (Exception e) {
				if (QRCloudUtils.DEBUG) {
					Log.e(TAG, "Could not get typeface '" + typefaceName + "' - " + e.getMessage());
				}
			}
		}
		return typeface;
	}
}

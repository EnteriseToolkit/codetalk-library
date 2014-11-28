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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import qr.cloud.library.R;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.internal.ResourcesCompat;

public class QRCloudUtils {
	public static final boolean DEBUG = false; // TODO: ensure this is false for Play Store release

	public static final int ITEMS_TO_LOAD = 10; // TODO: make this screen-size dependent
	public static final String FRAGMENT_SORT_TYPE = "sort_type";
	public static final String FRAGMENT_EXTRA_FILTER_OPERATOR = "extra_filter_operator";
	public static final String FRAGMENT_EXTRA_FILTER_PROPERTY = "extra_filter_property";
	public static final String FRAGMENT_EXTRA_FILTER_VALUE = "extra_filter_value";

	// from SecurityChecker.java in backend - *must* match code there and also in CloudEntity.java
	public static final String KIND_PREFIX_PUBLIC = "[public]"; // any user can edit any record
	public static final String KIND_PREFIX_PRIVATE = "[private]"; // not currently used (doesn't work properly)

	public static final String DATABASE_KIND_CODES = KIND_PREFIX_PUBLIC + "_QArghCodes";
	public static final String DATABASE_KIND_MESSAGES = KIND_PREFIX_PUBLIC + "_QArghMessages";
	public static final String DATABASE_KIND_QUESTIONNAIRE = "QRpediaQuestionnaire";
	public static final String DATABASE_KIND_BANS = "QArghBans";
	public static final String DATABASE_KIND_VERSIONS = "QArghVersions";

	public static final String DATABASE_PROP_VERSION = "version";
	public static final String DATABASE_PROP_USER = "user";

	public static final String DATABASE_PROP_CONTENTS = "contents"; // the original barcode's contents
	public static final String DATABASE_PROP_TYPE = "type"; // the original barcode's type (e.g., TEXT or URL)
	public static final String DATABASE_PROP_FORMAT = "format"; // the original barcode's format (e.g., QR or UPC)
	public static final String DATABASE_PROP_SCANS = "scans"; // the number of times this code has been scanned
	public static final String DATABASE_PROP_SOURCE = "source"; // the original app that scanned the code (qar or qrp)

	public static final String DATABASE_PROP_HASH = "hash"; // the SHA1 hash of the code's contents (for filtering)
	public static final String DATABASE_PROP_MESSAGE = "message"; // the user's message - a link or text
	public static final String DATABASE_PROP_RATING = "rating"; // rating for this item; currently just +1 or accessed
	public static final String DATABASE_PROP_COUNTRY = "country"; // the user's country, according to the SIM card
	public static final String DATABASE_PROP_LATITUDE = "latitude"; // the latitude of this item's location, or null
	public static final String DATABASE_PROP_LONGITUDE = "longitude"; // the longitude of this item's location, or null
	public static final String DATABASE_PROP_GEOCELL = "geocell"; // the GeoCell hash of this item's location, or null
	public static final String DATABASE_PROP_REPORTED = "reported"; // whether this item's been reported as bad/spam

	public static final String DATABASE_PROP_Q1 = "q1";
	public static final String DATABASE_PROP_Q2 = "q2";
	public static final String DATABASE_PROP_Q2_INVALID = "q2nomsg";
	public static final String DATABASE_PROP_OTHER_COMMENTS = "comments";

	// note: the precision defines a cell around the current location, but not necessarily centred within it
	// (see: https://developers.google.com/appengine/articles/geosearch)
	// example values - 8: ~100m either side of the current position; 9: ~30m either side - 9 is the default
	public static final int GEOCELL_STORED_PRECISION = 12; // 12 default, must be more than GEOCELL_QUERY_PRECISION
	public static final int GEOCELL_QUERY_PRECISION = 8; // must be less than GEOCELL_STORED_PRECISION
	public static final String GEOCELL_LOADING_MAGIC_VALUE = "***LOADING***";

	private static final String Utf8 = "utf-8";
	private static final String Sha1 = "sha-1";

	public static String sha1Hash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance(Sha1);
			md.update(input.getBytes(Utf8));
			byte[] digest = md.digest();
			BigInteger bi = new BigInteger(1, digest);
			return String.format((Locale) null, "%0" + (digest.length * 2) + "x", bi).toLowerCase(Locale.ENGLISH);
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	public static boolean internetAvailable(Context context) {
		if (context == null) {
			return true; // giving the wrong answer is better than crashing
		}
		boolean wifiConnected = false;
		boolean mobileNetworkConnected = false;
		boolean ethernetConnected = false;

		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
		for (NetworkInfo info : networkInfo) {
			if (info != null) {
				if (info.getType() == ConnectivityManager.TYPE_WIFI) {
					if (info.isConnectedOrConnecting()) {
						wifiConnected = true;
						break;
					}
				} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
					if (info.isConnectedOrConnecting()) {
						mobileNetworkConnected = true;
						break;
					}
				} else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
					if (info.isConnectedOrConnecting()) {
						ethernetConnected = true;
						break;
					}
				}
			}
		}
		return wifiConnected || mobileNetworkConnected || ethernetConnected;
	}

	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
	private static final long MILLISECONDS_PER_DAY = 86400000;
	private static final long MILLISECONDS_PER_HOUR = 3600000;
	private static final long MILLISECONDS_PER_MINUTE = 60000;
	private static final long MILLISECONDS_PER_SECOND = 1000;

	public static String getElapsedTime(Date dateCreated) {
		if (dateCreated == null) {
			return 0 + " secs"; // time hasn't yet been set
		}
		long duration = System.currentTimeMillis() - dateCreated.getTime();
		if (duration < 0) {
			return 0 + " secs"; // time is in the future (i.e., our clock is slow)
		}

		long days = duration / MILLISECONDS_PER_DAY;
		if (days > 20) {
			return dateFormatter.format(dateCreated); // our clock is wrong or date long ago - just return the date
		}

		if (days > 0) {
			return days + " day" + (days != 1 ? "s" : "");
		}

		long hours = duration / MILLISECONDS_PER_HOUR;
		if (hours > 0) {
			return hours + " hr" + (hours != 1 ? "s" : "");
		}

		long minutes = duration / MILLISECONDS_PER_MINUTE;
		if (minutes > 0) {
			return minutes + " min" + (minutes != 1 ? "s" : "");
		}

		long seconds = duration / MILLISECONDS_PER_SECOND;
		return seconds + " sec" + (seconds != 1 ? "s" : "");
	}

	public static String getFormattedDate(Date dateCreated) {
		if (dateCreated == null) {
			return ""; // time hasn't yet been set
		}
		return dateFormatter.format(dateCreated);
	}

	public static String getFormattedDistance(float distance) {
		return (((int) distance + 1) / 2 * 2) + " metres"; // round up to 2m
	}

	public static String toDisplayCase(String s) {
		if (s == null) {
			return "";
		}
		// see: http://stackoverflow.com/a/15738441/1993220
		final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following to be capitalized
		StringBuilder sb = new StringBuilder();
		boolean capNext = true;
		for (char c : s.toCharArray()) {
			c = (capNext) ? Character.toUpperCase(c) : Character.toLowerCase(c);
			sb.append(c);
			capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
		}
		return sb.toString();
	}

	public static boolean actionBarIsSplit(Context context) {
		return ResourcesCompat.getResources_getBoolean(context, R.bool.abs__split_action_bar_is_narrow);
	}

	public static void setListViewEmptyView(ListView listView, String text, int textFont, int paddingHorizontal,
			int paddingVertical) {
		if (listView == null) {
			return;
		}

		Context context = listView.getContext();
		ViewGroup listParent = (ViewGroup) listView.getParent();
		View existingView = listParent.findViewById(R.id.custom_empty_view);

		TextView emptyView;
		if (existingView != null) {
			emptyView = (TextView) existingView;
		} else {
			emptyView = new TextView(context);
			emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			emptyView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
			emptyView.setId(R.id.custom_empty_view);
			emptyView.setVisibility(View.GONE);

			listParent.addView(emptyView);
			listView.setEmptyView(emptyView);
		}

		emptyView.setText(text);
		emptyView.setTypeface(Typefaces.get(context, context.getString(textFont)));

		Resources resources = context.getResources();
		int horizontalPadding = resources.getDimensionPixelSize(paddingHorizontal);
		int verticalPadding = resources.getDimensionPixelSize(paddingVertical);
		emptyView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
	}
}

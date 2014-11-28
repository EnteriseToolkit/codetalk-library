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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.Browser;

//see: http://mobile.tutsplus.com/tutorials/android/android-fundamentals-downloading-data-with-services/
public class QRCloudDatabase extends SQLiteOpenHelper {

	private static final String DB_NAME = "saved_messages";
	private static final int DB_VERSION = 2;

	public static final String TABLE_MESSAGES = "messages";
	public static final String TABLE_TAGS = "tags";

	public static final String COL_ID = "_id";
	public static final String COL_TYPE = "type";
	public static final String COL_FORMAT = "format";
	public static final String COL_HASH = "hash";
	public static final String COL_MESSAGE = "message";
	public static final String COL_DATE = "date_added";

	public static final String[] PROJECTION_MESSAGE = { COL_MESSAGE };
	public static final String[] PROJECTION_ID_MESSAGE = { COL_ID, COL_MESSAGE };

	private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + " (" + COL_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_MESSAGE + " TEXT NOT NULL, " + COL_DATE
			+ " INTEGER NOT NULL);";
	private static final String CREATE_TABLE_TAGS = "CREATE TABLE " + TABLE_TAGS + " (" + COL_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_HASH + " TEXT, " + COL_TYPE + " TEXT, " + COL_FORMAT
			+ " TEXT, " + COL_MESSAGE + " TEXT NOT NULL, " + COL_DATE + " INTEGER NOT NULL);";

	public QRCloudDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_MESSAGES);
		db.execSQL(CREATE_TABLE_TAGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			db.execSQL(CREATE_TABLE_TAGS); // upgrade to support saving all personal tags
		}
	}

	public static String getMessageById(ContentResolver contentResolver, long itemId) {
		Cursor messageCursor = null;
		try {
			Uri queryUri = Uri.withAppendedPath(QRCloudProvider.CONTENT_URI_MESSAGES, String.valueOf(itemId));
			messageCursor = contentResolver.query(queryUri, PROJECTION_MESSAGE, null, null, null);
			if (messageCursor.moveToFirst()) {
				return messageCursor.getString(messageCursor.getColumnIndexOrThrow(COL_MESSAGE));
			}
		} finally {
			if (messageCursor != null) {
				messageCursor.close();
			}
		}
		return null;
	}

	public static int getSavedMessageCount(ContentResolver contentResolver) {
		Cursor messageCursor = null;
		try {
			messageCursor = contentResolver.query(QRCloudProvider.CONTENT_URI_MESSAGES, PROJECTION_MESSAGE, null, null,
					null);
			return messageCursor.getCount();
		} finally {
			if (messageCursor != null) {
				messageCursor.close();
			}
		}
	}

	public static int getPostedTagsCount(ContentResolver contentResolver) {
		Cursor tagsCursor = null;
		try {
			tagsCursor = contentResolver.query(QRCloudProvider.CONTENT_URI_TAGS, PROJECTION_MESSAGE, null, null, null);
			return tagsCursor.getCount();
		} finally {
			if (tagsCursor != null) {
				tagsCursor.close();
			}
		}
	}

	public static String getURLByBookmarkId(ContentResolver contentResolver, long itemId) {
		Cursor bookmarksCursor = null;
		try {
			Uri queryUri = Uri.withAppendedPath(android.provider.Browser.BOOKMARKS_URI, String.valueOf(itemId));
			bookmarksCursor = contentResolver.query(queryUri, new String[] { Browser.BookmarkColumns.URL }, null, null,
					null);
			if (bookmarksCursor.moveToFirst()) {
				return bookmarksCursor.getString(bookmarksCursor.getColumnIndexOrThrow(Browser.BookmarkColumns.URL));
			}
		} finally {
			if (bookmarksCursor != null) {
				bookmarksCursor.close();
			}
		}
		return null;
	}

	public static int getBookmarksCount(ContentResolver contentResolver) {
		Cursor messageCursor = null;
		try {
			messageCursor = contentResolver.query(android.provider.Browser.BOOKMARKS_URI,
					new String[] { Browser.BookmarkColumns.URL }, android.provider.Browser.BookmarkColumns.BOOKMARK,
					null, null);
			return messageCursor.getCount();
		} finally {
			if (messageCursor != null) {
				messageCursor.close();
			}
		}
	}
}

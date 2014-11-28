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

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.cloud.backend.android.Consts;

@SuppressLint("Registered")
public class QRCloudProvider extends ContentProvider {

	public static final String CONTENT_AUTHORITY = initAuthority();

	// see: http://stackoverflow.com/a/14592121
	private static String initAuthority() {
		String authority = "qr.cloud.fallback";
		try {
			ClassLoader loader = QRCloudProvider.class.getClassLoader();
			Class<?> clz = loader.loadClass("qr.cloud.db.ContentProviderAuthority");
			Field declaredField = clz.getDeclaredField("CONTENT_AUTHORITY");
			authority = declaredField.get(null).toString();
		} catch (ClassNotFoundException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		return authority;
	}

	private static final String TAG = CONTENT_AUTHORITY;

	public static final int MESSAGES = 100;
	public static final int MESSAGES_ID = 110;
	public static final int TAGS = 200;
	public static final int TAGS_ID = 210;

	private static final String MESSAGES_BASE_PATH = "saved-messages";
	private static final String TAGS_BASE_PATH = "my-tags";

	public static final Uri CONTENT_URI_MESSAGES = Uri.parse("content://" + CONTENT_AUTHORITY + "/"
			+ MESSAGES_BASE_PATH);
	public static final Uri CONTENT_URI_TAGS = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + TAGS_BASE_PATH);

	// not currently used
	// public static final String CONTENT_ITEM_TYPE_MESSAGES = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/mt-" +
	// MESSAGES_BASE_PATH;
	// public static final String CONTENT_TYPE_MESSAGES = ContentResolver.CURSOR_DIR_BASE_TYPE + "/mt-" +
	// MESSAGES_BASE_PATH;
	// public static final String CONTENT_ITEM_TYPE_TAGS = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/mt-" +
	// TAGS_BASE_PATH;
	// public static final String CONTENT_TYPE_TAGS = ContentResolver.CURSOR_DIR_BASE_TYPE + "/mt-" + TAGS_BASE_PATH;

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(CONTENT_AUTHORITY, MESSAGES_BASE_PATH, MESSAGES);
		sURIMatcher.addURI(CONTENT_AUTHORITY, MESSAGES_BASE_PATH + "/#", MESSAGES_ID);
		sURIMatcher.addURI(CONTENT_AUTHORITY, TAGS_BASE_PATH, TAGS);
		sURIMatcher.addURI(CONTENT_AUTHORITY, TAGS_BASE_PATH + "/#", TAGS_ID);
	}

	private QRCloudDatabase mSavedTextDB;

	@Override
	public boolean onCreate() {
		mSavedTextDB = new QRCloudDatabase(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case MESSAGES_ID:
				queryBuilder.setTables(QRCloudDatabase.TABLE_MESSAGES);
				queryBuilder.appendWhere(QRCloudDatabase.COL_ID + "=" + uri.getLastPathSegment());
				break;
			case MESSAGES:
				queryBuilder.setTables(QRCloudDatabase.TABLE_MESSAGES);
				// no filter
				break;
			case TAGS_ID:
				queryBuilder.setTables(QRCloudDatabase.TABLE_TAGS);
				queryBuilder.appendWhere(QRCloudDatabase.COL_ID + "=" + uri.getLastPathSegment());
				break;
			case TAGS:
				queryBuilder.setTables(QRCloudDatabase.TABLE_TAGS);
				// no filter
				break;
			default:
				throw new IllegalArgumentException("Unknown URI");
		}
		Cursor cursor = queryBuilder.query(mSavedTextDB.getReadableDatabase(), projection, selection, selectionArgs,
				null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null; // no need - we're not exported
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = mSavedTextDB.getWritableDatabase();
		try {
			switch (uriType) {
				case MESSAGES:
					long newMessageID = sqlDB.insertOrThrow(QRCloudDatabase.TABLE_MESSAGES, null, values);
					if (newMessageID > 0) {
						Uri newUri = ContentUris.withAppendedId(uri, newMessageID);
						getContext().getContentResolver().notifyChange(uri, null);
						return newUri;
					} else {
						throw new SQLException("Failed to insert row into " + uri);
					}
				case TAGS:
					long newTagID = sqlDB.insertOrThrow(QRCloudDatabase.TABLE_TAGS, null, values);
					if (newTagID > 0) {
						Uri newUri = ContentUris.withAppendedId(uri, newTagID);
						getContext().getContentResolver().notifyChange(uri, null);
						return newUri;
					} else {
						throw new SQLException("Failed to insert row into " + uri);
					}
				default:
					throw new IllegalArgumentException("Invalid URI for insert");
			}

		} catch (SQLiteConstraintException e) {
			if (Consts.DEBUG) {
				Log.i(TAG, "Ignoring constraint failure.");
			}
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = mSavedTextDB.getWritableDatabase();
		int rowsAffected = 0;
		switch (uriType) {
			case MESSAGES:
				rowsAffected = sqlDB.delete(QRCloudDatabase.TABLE_MESSAGES, selection, selectionArgs);
				break;
			case MESSAGES_ID:
				String messageId = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					rowsAffected = sqlDB.delete(QRCloudDatabase.TABLE_MESSAGES, QRCloudDatabase.COL_ID + "=?",
							new String[] { messageId });
				} else {
					rowsAffected = sqlDB.delete(QRCloudDatabase.TABLE_MESSAGES, selection + " AND "
							+ QRCloudDatabase.COL_ID + "=" + messageId, selectionArgs); // TODO: improve this
				}
				break;
			case TAGS:
				rowsAffected = sqlDB.delete(QRCloudDatabase.TABLE_TAGS, selection, selectionArgs);
				break;
			case TAGS_ID:
				String tagId = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					rowsAffected = sqlDB.delete(QRCloudDatabase.TABLE_TAGS, QRCloudDatabase.COL_ID + "=?",
							new String[] { tagId });
				} else {
					rowsAffected = sqlDB.delete(QRCloudDatabase.TABLE_TAGS, selection + " AND "
							+ QRCloudDatabase.COL_ID + "=" + tagId, selectionArgs); // TODO: improve this
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0; // no need to update messages for now
	}
}

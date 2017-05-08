/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.lucasbaiao.minharotina.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.SparseArray;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.lucasbaiao.minharotina.persistence.model.Category;
import br.com.lucasbaiao.minharotina.persistence.model.Event;
import br.com.lucasbaiao.minharotina.persistence.model.Theme;

/**
 * Database for storing and retrieving info for categories and quizzes
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "AppDatabaseHelper";
    private static final String DB_NAME = "myDatabase";
    private static final String DB_SUFFIX = ".db";
    private static final int DB_VERSION = 2;
    private static List<Category> mCategories;
    private static AppDatabaseHelper mInstance;
    private final Resources mResources;

    private AppDatabaseHelper(Context context) {
        super(context, DB_NAME + DB_SUFFIX, null, DB_VERSION);
        mResources = context.getResources();
    }

    private static AppDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppDatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    public synchronized static List<Category> getCategories(Context context, boolean fromDatabase) {
        if (mCategories == null || fromDatabase) {
            mCategories = loadCategories(context);
        }
        return mCategories;
    }

    public synchronized static List<Category> loadCategories(Context context) {
        Cursor data = AppDatabaseHelper.getCategoryCursor(context);
        List<Category> tmpCategories = new ArrayList<>(data.getCount());
        final SQLiteDatabase readableDatabase = AppDatabaseHelper.getReadableDatabase(context);
        while (data.moveToNext()) {
            final Category category = getCategory(data, readableDatabase);
            tmpCategories.add(category);
        }
        return tmpCategories;
    }

    public synchronized static boolean createNewCategory(Context context, Category category) {
        if (mCategories != null && mCategories.contains(category)) {
            final int location = mCategories.indexOf(category);
            mCategories.remove(location);
            mCategories.add(location, category);
        } else if (mCategories != null) {
            mCategories.add(category);
        }

        SQLiteDatabase db = getWritableDatabase(context);

        ContentValues values = new ContentValues();
        values.put(CategoryTable.COLUMN_ID, category.getId());
        values.put(CategoryTable.COLUMN_NAME, category.getName());
        values.put(CategoryTable.COLUMN_THEME, category.getTheme().name());

        long id = db.insertWithOnConflict(CategoryTable.NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        // id == -1 -> already exists
        return id == -1;
    }

    public static void updateCategory(Context context, Category category) {
        if (mCategories != null && mCategories.contains(category)) {
            final int location = mCategories.indexOf(category);
            mCategories.remove(location);
            mCategories.add(location, category);
        }
        SQLiteDatabase writableDatabase = getWritableDatabase(context);
        ContentValues categoryValues = createContentValuesFor(category);

        writableDatabase.update(CategoryTable.NAME, categoryValues, CategoryTable.COLUMN_ID + "=?",
                new String[]{category.getId()});
        final SparseArray<Event> events = category.getEvents();
        updateEvents(writableDatabase, events, category.getId());
    }

    private static void updateEvents(SQLiteDatabase writableDatabase, SparseArray<Event> events, String categoryId) {
        ContentValues eventValues = new ContentValues();
        for (int i = 0; i < events.size(); i++) {
            Event event = events.valueAt(i);
            eventValues.clear();
            if (event.getId() > 0) {
                eventValues.put(EventTable.COLUMN_ID, event.getId());
            }
            eventValues.put(EventTable.FK_CATEGORY, categoryId);
            eventValues.put(EventTable.COLUMN_START, event.getStart());
            eventValues.put(EventTable.COLUMN_STOP, event.getStop());

            long id = writableDatabase.insertWithOnConflict(EventTable.NAME, null,
                    eventValues, SQLiteDatabase.CONFLICT_REPLACE);
            event.setId((int) id);
        }

    }

    private static ContentValues createContentValuesFor(Category category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CategoryTable.COLUMN_NAME, category.getName());
        contentValues.put(CategoryTable.COLUMN_THEME, category.getTheme().name());
        return contentValues;
    }

    private static Cursor getCategoryCursor(Context context) {
        SQLiteDatabase readableDatabase = getReadableDatabase(context);
        return readableDatabase
                .query(CategoryTable.NAME, CategoryTable.PROJECTION, null, null, null, null, null);
    }

    private static Category getCategory(Cursor cursor, SQLiteDatabase readableDatabase) {
        final String id = cursor.getString(0);
        final String name = cursor.getString(1);
        final Theme theme = Theme.from(cursor.getString(2));
        Category category = new Category(id, name, theme);
        category.addEvent(getEvents(id, readableDatabase));
        return category;
    }

    private static SparseArray<Event> getEvents(final String categoryId, SQLiteDatabase database) {

        SparseArray<Event> events = new SparseArray<>();
        final Cursor cursor = database.query(EventTable.NAME, EventTable.PROJECTION,
                EventTable.FK_CATEGORY + " LIKE ?", new String[]{categoryId}, null, null, null);
        while (cursor.moveToNext()) {
            final int id = cursor.getInt(0);
            final String fk = cursor.getString(1);
            final String start = cursor.getString(2);
            final String stop = cursor.getString(3);

            Event event = new Event(id, start, stop);
            events.put(id, event);
        }
        cursor.close();
        return events;
    }

    public static void reset(Context context) {
        SQLiteDatabase writableDatabase = getWritableDatabase(context);
        writableDatabase.delete(CategoryTable.NAME, null, null);
        writableDatabase.delete(EventTable.NAME, null, null);
    }

    private static SQLiteDatabase getReadableDatabase(Context context) {
        return getInstance(context).getReadableDatabase();
    }

    private static SQLiteDatabase getWritableDatabase(Context context) {
        return getInstance(context).getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CategoryTable.CREATE);
        db.execSQL(EventTable.CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.delete(CategoryTable.NAME, null, null);
        }

        onCreate(db);
    }

    /**
     * Puts a non-empty string to ContentValues provided.
     *
     * @param values The place where the data should be put.
     * @param quiz The quiz potentially containing the data.
     * @param jsonKey The key to look for.
     * @param contentKey The key use for placing the data in the database.
     */
    private static void putNonEmptyString(ContentValues values, JSONObject quiz, String jsonKey,
                                          String contentKey) {
        final String stringToPut = quiz.optString(jsonKey, null);
        if (!TextUtils.isEmpty(stringToPut)) {
            values.put(contentKey, stringToPut);
        }
    }

}
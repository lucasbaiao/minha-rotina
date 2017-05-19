package br.com.lucasbaiao.minharotina.persistence;

import android.provider.BaseColumns;

/**
 * Structure of the events table.
 */
interface EventTable {

    String NAME = "event";

    String COLUMN_ID = BaseColumns._ID;
    String FK_CATEGORY = "fk_category";
    String COLUMN_START = "start";
    String COLUMN_STOP = "stop";

    String[] PROJECTION = new String[]{COLUMN_ID, FK_CATEGORY, COLUMN_START, COLUMN_STOP};

    String CREATE = "CREATE TABLE IF NOT EXISTS " + NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY, "
            + FK_CATEGORY + " REFERENCES "
            + CategoryTable.NAME + "(" + CategoryTable.COLUMN_ID + "), "
            + COLUMN_START + " TEXT, "
            + COLUMN_STOP + " TEXT);";
}

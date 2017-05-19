package br.com.lucasbaiao.minharotina.persistence;

import android.provider.BaseColumns;

/**
 * Structure of the foreground app table.
 */
public interface ForegroundAppTable {

    String TimeStampFormat = "ddMMyyyyHHmmss";
    String NAME = "foreground_table";
    String COLUMN_ID = BaseColumns._ID;
    String COLUMN_APP = "app";
    String COLUMN_TIMESTAMP = "timestamp";

    String[] PROJECTION = new String[]{COLUMN_ID, COLUMN_TIMESTAMP, COLUMN_APP};

    String CREATE = "CREATE TABLE IF NOT EXISTS " + NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY, "
            + COLUMN_TIMESTAMP + " TEXT, "
            + COLUMN_APP + " TEXT);";
}

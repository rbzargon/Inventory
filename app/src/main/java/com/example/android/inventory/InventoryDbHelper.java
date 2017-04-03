package com.example.android.inventory;

/**
 * Created by cking10 on 2/27/2017.
 */

//adapted from
//https://github.com/udacity/ud845-Pets/tree/lesson-four
//http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventory.InventoryContract.InventoryEntry;


class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;


    InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_ITEM_QUANTITY + " INT NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_ITEM_IMG_BYTE + " BLOB);";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       //TODO: implement dropping tables on upgrade
    }

}

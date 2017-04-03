package com.example.android.inventory;

/**
 * Created by cking10 on 2/27/2017.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventory.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    final static String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int ITEMS = 1;
    private static final int ITEMS_ID = 2;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        //matches for all inventory items
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);

        //matches for a single inventory item
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS_ID, ITEMS_ID);

    }

    private InventoryDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        final int match = sUriMatcher.match(uri);

        switch(match) {
        case ITEMS:
            cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder);
            break;
        case ITEMS_ID:
            selection = InventoryEntry._ID + "=?";
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder);
            break;
        default:
            throw new IllegalArgumentException("Unknown query URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        boolean insertIsValid = false;
        Uri insertedRowURI = null;

        //Only process if there are values to insert
        if (contentValues.size() != 0) {
            //Check for input validity and alert user if invalid
            insertIsValid = validateValues(contentValues);
        }

        if(insertIsValid) {

            final int match = sUriMatcher.match(uri);

            if (match != ITEMS) {
                throw new IllegalArgumentException("Insert not supported for " + uri);
            } else {
                SQLiteDatabase database = mDbHelper.getWritableDatabase();

                long id = database.insert(InventoryEntry.TABLE_NAME, null, contentValues);

                //if insertion fails
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                } else {
                    getContext().getContentResolver().notifyChange(uri, null);
                    insertedRowURI = ContentUris.withAppendedId(uri, id);
                }
            }
        }
        return insertedRowURI;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        int numItemsUpdated = 0;
        boolean updateIsValid = false;

        //Only process if there are values to update
        if (contentValues.size() != 0) {
            //Check for input validity and alert user if invalid
            updateIsValid = validateValues(contentValues);
        }

        if(updateIsValid) {
            final int match = sUriMatcher.match(uri);

            if (match == ITEMS_ID) {
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            } else if (match != ITEMS) {
                throw new IllegalArgumentException("Update not supported for " + uri);
            }


            SQLiteDatabase database = mDbHelper.getWritableDatabase();

            numItemsUpdated = database.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);

            if(numItemsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return numItemsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int numItemsDeleted = 0;

        final int match = sUriMatcher.match(uri);

        if(match == ITEMS) {
            numItemsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
        } else if (match == ITEMS_ID) {
            selection = InventoryEntry._ID + "=?";
            selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

            numItemsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);

        } else {
            throw new IllegalArgumentException("Delete not supported for " + uri);
        }

        if (numItemsDeleted !=  0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numItemsDeleted;
    }



    //Input validation function
    private boolean validateValues(ContentValues values) {
        boolean validity = true;

        try {
            if (values.containsKey(InventoryEntry.COLUMN_ITEM_NAME)) {
                String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
                if (name == null) {
                    throw new IllegalArgumentException("Name Required");
                }
            }

            if (values.containsKey(InventoryEntry.COLUMN_ITEM_PRICE)) {
                Double price = values.getAsDouble(InventoryEntry.COLUMN_ITEM_PRICE);
                if(price < 0) {
                    throw new IllegalArgumentException("Invalid Price");
                }
            }

            if (values.containsKey(InventoryEntry.COLUMN_ITEM_QUANTITY)) {
                Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
                if(quantity < 0) {
                    throw new IllegalArgumentException("Invalid Quantity");
                }
            }


            if (values.containsKey(InventoryEntry.COLUMN_ITEM_IMG_BYTE)) {
                //TO-DO: check if valid path for file
            }
        } catch (IllegalArgumentException e) {
            //Alert user of invalid input
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            validity = false;
        }

        return validity;

    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        if (match == ITEMS) {
            return InventoryEntry.CONTENT_LIST_TYPE;
        } else if (match == ITEMS_ID) {
            return InventoryEntry.CONTENT_ITEM_TYPE;
        } else {
            throw new IllegalStateException("Unknown URI: " + uri + "Match: " + match);
        }

    }

}
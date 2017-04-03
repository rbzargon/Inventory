package com.example.android.inventory;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by cking10 on 2/27/2017.
 */

//adapted from
//https://github.com/udacity/ud845-Pets/tree/lesson-four


final class InventoryContract {

    //prevent instantiation of class
    private InventoryContract() {
        //TODO: Assert non-instantiation
    }


    static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String PATH_ITEMS = "items";

   static final String PATH_ITEMS_ID = "items/#";


    static final class InventoryEntry implements BaseColumns {

        static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        static final String TABLE_NAME = "items";

        static final String _ID = BaseColumns._ID;

        static final String COLUMN_ITEM_NAME = "name";

        static final String COLUMN_ITEM_PRICE = "price";

        static final String COLUMN_ITEM_QUANTITY = "quantity";

        static final String COLUMN_ITEM_IMG_BYTE = "imgByte";



    }
}



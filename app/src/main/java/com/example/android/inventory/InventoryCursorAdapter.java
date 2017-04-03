package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

import static com.example.android.inventory.InventoryContract.InventoryEntry;
import static com.example.android.inventory.InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY;
import static com.example.android.inventory.InventoryContract.InventoryEntry.CONTENT_URI;

/**
 * Created by cking10 on 3/1/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {

        TextView nameTV = (TextView) view.findViewById(R.id.product_name);
        TextView quantityTV = (TextView) view.findViewById(R.id.quantity_value);
        TextView priceTV = (TextView) view.findViewById(R.id.price_value);
        Button sellOneButton = (Button) view.findViewById(R.id.sell_one_button);


        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);

        String itemName = cursor.getString(nameColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);
        double itemPrice = cursor.getDouble(priceColumnIndex);

        //http://stackoverflow.com/questions/7131922/how-to-format-a-float-value-with-the-device-currency-format
        NumberFormat localCurrencyFormat = NumberFormat.getCurrencyInstance();

        nameTV.setText(itemName);
        quantityTV.setText(String.valueOf(itemQuantity));
        priceTV.setText(localCurrencyFormat.format(itemPrice));
        //http://stackoverflow.com/questions/16878933/get-a-contentresolver-within-a-cursoradapter
        sellOneButton.setTag(cursor.getLong(0));

        //adapted from eyal-lezmy
        //http://stackoverflow.com/questions/11591501/how-to-use-onclicklistener-method-of-button-on-a-listview
        sellOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = (Long) v.getTag();
                if (itemQuantity > 0) {
                    int newItemQuantity = itemQuantity - 1;
                    Uri currentItemUri = ContentUris.withAppendedId(CONTENT_URI, id);

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_ITEM_QUANTITY, newItemQuantity);
                    context.getContentResolver().update(currentItemUri, values, null, null);

                } else {
                    Toast.makeText(context, R.string.no_quantity, Toast.LENGTH_SHORT).show();
                }
                }

        });

    }
}

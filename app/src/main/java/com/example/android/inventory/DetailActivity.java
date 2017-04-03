package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.inventory.InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_BYTE;
import static com.example.android.inventory.InventoryContract.InventoryEntry.COLUMN_ITEM_NAME;
import static com.example.android.inventory.InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY;
import static com.example.android.inventory.InventoryContract.InventoryEntry._ID;

/**
 * Created by cking10 on 3/1/2017.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final int EXISTING_INVENTORY_LOADER = 0;

    private Uri mCurrentItemUri;

    private int quantity;
    private String name;

    private ImageView mProductImageView;
    private TextView mNameTextView;
    private TextView mQuantityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_detail_activity);

       getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);

        mProductImageView = (ImageView) findViewById(R.id.product_image);
        mNameTextView = (TextView) findViewById(R.id.product_name_detail);
        mQuantityTextView = (TextView) findViewById(R.id.quantity_value_detail);

        Button mSalesButton = (Button) findViewById(R.id.sales_tracking_button);
        Button mShipmentButton = (Button) findViewById(R.id.shipment_received_button);
        Button mOrderButton = (Button) findViewById(R.id.order_product);
        Button mDeleteButton = (Button) findViewById(R.id.delete_product);

        //implemented in OnClick method
        mSalesButton.setOnClickListener(this);
        mShipmentButton.setOnClickListener(this);
        mOrderButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                _ID,
                COLUMN_ITEM_NAME,
                COLUMN_ITEM_QUANTITY,
                COLUMN_ITEM_IMG_BYTE
        };
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() == 1) {
            cursor.moveToFirst();
            int nameIdx = cursor.getColumnIndex(COLUMN_ITEM_NAME);
            int quantityIdx = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
            int imgIdx = cursor.getColumnIndex(COLUMN_ITEM_IMG_BYTE);

            name = cursor.getString(nameIdx);
            quantity = cursor.getInt(quantityIdx);
            byte[] imgBytes = cursor.getBlob(imgIdx);

            mNameTextView.setText(name);
            mQuantityTextView.setText(String.valueOf(quantity));
            if(imgBytes != null) {
                Bitmap imageBitmap = DbBitmapUtility.getBitmap(imgBytes);
                mProductImageView.setImageBitmap(imageBitmap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameTextView.setText("");
        mQuantityTextView.setText("");
        //Mario Lenci and bonnyz
        //http://stackoverflow.com/questions/2859212/how-to-clear-an-imageview-in-android
        mProductImageView.setImageResource(android.R.color.transparent);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.sales_tracking_button:
                trackSales();
                break;
            case R.id.shipment_received_button:
                trackShipment();
                break;
            case R.id.order_product:
                orderProduct();
                break;
            case R.id.delete_product:
                deleteProduct();
                break;
            default:
                break;
        }
    }

    private void orderProduct() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);

        final EditText editText = (EditText) dialogView.findViewById(R.id.edit_text);

        dialog.setTitle("How many to order?");
        dialog.setView(dialogView);
        dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int quantityOrdered = 0;
                try {
                    quantityOrdered = Integer.parseInt(editText.getText().toString());
                } catch (Exception e) {
                    Log.v("DetailActivity", "Invalid quantity ordered");
                }
                if (quantityOrdered < 1) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.invalid_quantity_received),
                            Toast.LENGTH_SHORT).show();
                } else {
                    //https://developer.android.com/training/sharing/send.html
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT,  name + " " + getString(R.string.order));
                    sendIntent.putExtra(Intent.EXTRA_TITLE, name + getString(R.string.order) );
                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                            getString(R.string.order_request) + "\n" + name + getString(R.string.quant)
                            + " " + String.valueOf(quantityOrdered));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.create().show();
    }

    private void deleteProduct() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailActivity.this);
        dialog.setTitle(R.string.delete_product);
        dialog.setMessage(R.string.deletion_confirmation);
        dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(mCurrentItemUri, null, null);
                Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
                startActivity(intent);
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.create().show();
    }

    private void trackShipment() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);

        final EditText editText = (EditText) dialogView.findViewById(R.id.edit_text);

        dialog.setTitle(R.string.how_many_received);
        dialog.setView(dialogView);
        dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int quantityReceived = 0;
                try {
                    quantityReceived = Integer.parseInt(editText.getText().toString());
                } catch (Exception e) {
                    Log.v("DetailActivity", "Invalid quantity received");
                }
                if (quantityReceived < 1) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.invalid_quantity_received),
                            Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues values = new ContentValues();
                    int newQuantity = quantity + quantityReceived;
                    values.put(COLUMN_ITEM_QUANTITY, newQuantity);
                    getContentResolver().update(mCurrentItemUri, values, null, null);
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.create().show();
    }

    private void trackSales() {
        if (quantity <= 0) {
            Toast.makeText(DetailActivity.this, R.string.no_quantity_toast, Toast.LENGTH_SHORT).show();
        } else {

            //adapted from
            //http://stackoverflow.com/questions/17805040/how-to-create-a-number-picker-dialog
            AlertDialog.Builder dialog = new AlertDialog.Builder(DetailActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.number_picker, null);
            dialog.setTitle(R.string.quantity_sold);
            dialog.setView(dialogView);

            final NumberPicker nPicker = (NumberPicker) dialogView.findViewById(R.id.number_picker);
            nPicker.setMaxValue(quantity);
            nPicker.setMinValue(1);
            nPicker.setWrapSelectorWheel(true);

            dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int quantitySold = nPicker.getValue();
                    int newQuantity = quantity - quantitySold;

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_ITEM_QUANTITY, newQuantity);

                    getContentResolver().update(mCurrentItemUri, values, null, null);
                }
            });
            dialog.setNegativeButton(R.string.cancel, null);
            dialog.create().show();
        }
    }
}

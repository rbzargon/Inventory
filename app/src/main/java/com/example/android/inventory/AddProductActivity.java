package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.android.inventory.InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_BYTE;
import static com.example.android.inventory.InventoryContract.InventoryEntry.COLUMN_ITEM_NAME;
import static com.example.android.inventory.InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE;
import static com.example.android.inventory.InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY;
import static com.example.android.inventory.InventoryContract.InventoryEntry.CONTENT_URI;

/**
 * Created by cking10 on 3/1/2017.
 */

public class AddProductActivity extends AppCompatActivity{

    private static int SELECT_IMAGE = 1;
    private String productName;
    private int quantity;
    private double price;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product_activity);

        final EditText productET = (EditText) findViewById(R.id.product_name);
        final EditText quantityET = (EditText) findViewById(R.id.quantity);
        final EditText priceET = (EditText) findViewById(R.id.price);

        Button add_img_button = (Button) findViewById(R.id.add_img_button);
        Button cancel_button = (Button) findViewById(R.id.cancel_button);
        Button submit_button = (Button) findViewById(R.id.submit_button);

        add_img_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adapted from aerrow
                //http://stackoverflow.com/questions/10838138/how-can-i-retrieve-the-path-from-an-image-in-the-android-gallery
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_IMAGE);
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddProductActivity.this, InventoryActivity.class);
                startActivity(intent);
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                boolean inputIsValid = true;
                productName = productET.getText().toString().trim();
                if (productName.isEmpty()) {
                    inputIsValid = false;
                    Toast.makeText(context, R.string.invalid_product_name, Toast.LENGTH_SHORT).show();
                }

                String quantityText = quantityET.getText().toString().trim();
                if (quantityText.isEmpty()) {
                    inputIsValid = false;
                    Toast.makeText(context, R.string.invalid_quantity, Toast.LENGTH_SHORT).show();
                } else {
                    try{
                        quantity = Integer.parseInt(quantityText);
                    } catch (NumberFormatException e){
                        inputIsValid = false;
                        Toast.makeText(context, R.string.invalid_quantity_format, Toast.LENGTH_SHORT).show();
                    }
                }

                String priceText = priceET.getText().toString().trim();
                if(priceText.isEmpty()) {
                    inputIsValid = false;
                    Toast.makeText(context, R.string.invalid_price, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        price = Double.parseDouble(priceText);
                    } catch (NumberFormatException e)  {
                        inputIsValid = false;
                        Toast.makeText(context, R.string.invalid_price_format, Toast.LENGTH_SHORT).show();
                    }
                }
                byte[] img_byte_array = null;

                if(imageBitmap != null) {
                    img_byte_array = DbBitmapUtility.getBytes(imageBitmap);
                }


                Uri newItemUri = null;

                if(inputIsValid) {
                    ContentValues values = new ContentValues();

                    values.put(COLUMN_ITEM_NAME, productName);
                    values.put(COLUMN_ITEM_QUANTITY, quantity);
                    values.put(COLUMN_ITEM_PRICE, price);
                    if (img_byte_array != null) {
                        values.put(COLUMN_ITEM_IMG_BYTE, img_byte_array);
                    }


                    newItemUri = getContentResolver().insert(CONTENT_URI, values);
                }
                //insert was successful if uri is not null
                //alert and return to list
                if (newItemUri != null) {
                    Toast.makeText(context,
                            productName + getString(R.string.success_message),
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddProductActivity.this, InventoryActivity.class);
                    startActivity(intent);
                } else {
                    //otherwise remain in AddProductActivity
                    Toast.makeText(context,
                            productName + getString(R.string.failure_message),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    //adapted from siamii example in
    //http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == SELECT_IMAGE) {
            Uri selectedImage = data.getData();

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (imageStream != null) {
                imageBitmap = BitmapFactory.decodeStream(imageStream);
            }

            ImageView image_preview = (ImageView) findViewById(R.id.image_preview);
            image_preview.setImageBitmap(imageBitmap);
        }
    }


}

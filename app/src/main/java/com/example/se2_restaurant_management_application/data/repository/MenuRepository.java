package com.example.se2_restaurant_management_application.data.repository;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.local.DatabaseHelper;
import com.example.se2_restaurant_management_application.data.models.Menu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuRepository {

    private final DatabaseHelper dbHelper;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public MenuRepository(Application application) {
        dbHelper = new DatabaseHelper(application);
        // Populate database with initial data if it's empty
        initializeData();
    }

    public void getAllMenuItems(final OnDataReadyCallback callback) {
        databaseWriteExecutor.execute(() -> {
            List<Menu> menuList = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_MENU_ITEMS,
                    null, null, null, null, null, DatabaseHelper.COLUMN_MENU_CATEGORY + " ASC");

            if (cursor.moveToFirst()) {
                do {
                    Menu menu = new Menu();
                    menu.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_ID)));
                    menu.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_NAME)));
                    menu.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_DESCRIPTION)));
                    menu.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_PRICE)));
                    menu.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_CATEGORY)));
                    menu.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_IMAGE_URI)));
                    menu.setImageDrawableId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_IMAGE_DRAWABLE_ID)));
                    menuList.add(menu);
                } while (cursor.moveToNext());
            }
            cursor.close();
            // Pass the result back on the main thread or via the callback
            callback.onDataReady(menuList);
        });
    }

    public void insert(Menu menu) {
        databaseWriteExecutor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_MENU_NAME, menu.getName());
            values.put(DatabaseHelper.COLUMN_MENU_DESCRIPTION, menu.getDescription());
            values.put(DatabaseHelper.COLUMN_MENU_PRICE, menu.getPrice());
            values.put(DatabaseHelper.COLUMN_MENU_CATEGORY, menu.getCategory());
            values.put(DatabaseHelper.COLUMN_MENU_IMAGE_URI, menu.getImageUri());
            values.put(DatabaseHelper.COLUMN_MENU_IMAGE_DRAWABLE_ID, menu.getImageDrawableId());

            db.insert(DatabaseHelper.TABLE_MENU_ITEMS, null, values);
        });
    }

    public void update(Menu menu) {
        databaseWriteExecutor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_MENU_NAME, menu.getName());
            values.put(DatabaseHelper.COLUMN_MENU_DESCRIPTION, menu.getDescription());
            values.put(DatabaseHelper.COLUMN_MENU_PRICE, menu.getPrice());
            values.put(DatabaseHelper.COLUMN_MENU_CATEGORY, menu.getCategory());
            values.put(DatabaseHelper.COLUMN_MENU_IMAGE_URI, menu.getImageUri());
            values.put(DatabaseHelper.COLUMN_MENU_IMAGE_DRAWABLE_ID, menu.getImageDrawableId());

            String selection = DatabaseHelper.COLUMN_MENU_ID + " = ?";
            String[] selectionArgs = { String.valueOf(menu.getId()) };

            db.update(DatabaseHelper.TABLE_MENU_ITEMS, values, selection, selectionArgs);
        });
    }

    public void delete(Menu menu, Runnable onFinished) {
        databaseWriteExecutor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String selection = DatabaseHelper.COLUMN_MENU_ID + " = ?";
            String[] selectionArgs = { String.valueOf(menu.getId()) };
            db.delete(DatabaseHelper.TABLE_MENU_ITEMS, selection, selectionArgs);

            // After the delete operation is complete, run the onFinished action
            // on the main UI thread.
            if (onFinished != null) {
                mainThreadHandler.post(onFinished);
            }
        });
    }

    private void insertAll(List<Menu> menuItems) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Menu menu : menuItems) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_MENU_NAME, menu.getName());
                values.put(DatabaseHelper.COLUMN_MENU_DESCRIPTION, menu.getDescription());
                values.put(DatabaseHelper.COLUMN_MENU_PRICE, menu.getPrice());
                values.put(DatabaseHelper.COLUMN_MENU_CATEGORY, menu.getCategory());
                values.put(DatabaseHelper.COLUMN_MENU_IMAGE_URI, menu.getImageUri());
                values.put(DatabaseHelper.COLUMN_MENU_IMAGE_DRAWABLE_ID, menu.getImageDrawableId());
                db.insert(DatabaseHelper.TABLE_MENU_ITEMS, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private int getCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_MENU_ITEMS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private void initializeData() {
        databaseWriteExecutor.execute(() -> {
            if (getCount() == 0) {
                insertAll(createDummyData());
            }
        });
    }

    private List<Menu> createDummyData() {
        List<Menu> items = new ArrayList<>();
        // Use the new drawable resources for each item
        items.add(new Menu("Spring Rolls", "Crispy fried spring rolls with a vegetable filling.", 5.99, R.drawable.food_spring_rolls, "Appetizers"));
        items.add(new Menu("Garlic Bread", "Toasted bread with garlic, butter, and herbs.", 4.50, R.drawable.food_garlic_bread, "Appetizers"));
        items.add(new Menu("Classic Cheeseburger", "A juicy beef patty with melted cheese, lettuce, and tomato.", 12.99, R.drawable.food_cheeseburger, "Burgers"));
        items.add(new Menu("Bacon Avocado Burger", "Beef patty topped with crispy bacon and fresh avocado.", 14.50, R.drawable.food_bacon_burger, "Burgers"));
        items.add(new Menu("Chicken Caesar Salad", "Classic salad with grilled chicken.", 10.50, R.drawable.food_caesar_salad, "Salads"));
        items.add(new Menu("Chocolate Lava Cake", "Warm chocolate cake with a gooey molten center.", 7.00, R.drawable.food_lava_cake, "Desserts"));
        return items;
    }

    // Callback interface to pass data from background thread to UI thread
    public interface OnDataReadyCallback {
        void onDataReady(List<Menu> menuList);
    }
}

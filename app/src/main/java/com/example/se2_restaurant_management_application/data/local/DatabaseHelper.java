package com.example.se2_restaurant_management_application.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "restaurant.db";
    private static final int DATABASE_VERSION = 5;

    // --- User Table Columns ---
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_IMAGE_URI = "image_uri";


    // --- Menu Table Columns ---
    public static final String TABLE_MENU_ITEMS = "menu_items";
    public static final String COLUMN_MENU_ID = "id";
    public static final String COLUMN_MENU_NAME = "name";
    public static final String COLUMN_MENU_DESCRIPTION = "description";
    public static final String COLUMN_MENU_PRICE = "price";
    public static final String COLUMN_MENU_CATEGORY = "category";
    public static final String COLUMN_MENU_IMAGE_URI = "imageUri";
    public static final String COLUMN_MENU_IMAGE_DRAWABLE_ID = "imageDrawableId";

    // --- Reservation Table Columns ---
    public static final String TABLE_RESERVATIONS = "reservations";
    public static final String COLUMN_RESERVATION_ID = "id";
    public static final String COLUMN_RESERVATION_STATUS = "status";
    public static final String COLUMN_RESERVATION_DATETIME = "dateTime";
    public static final String COLUMN_RESERVATION_GUESTS = "numberOfGuests";
    public static final String COLUMN_RESERVATION_TABLE_NUM = "tableNumber";

    // --- Notification Table Column ---
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_NOTIFICATION_ID = "id";
    public static final String COLUMN_NOTIFICATION_USER_ID = "user_id";
    public static final String COLUMN_NOTIFICATION_TITLE = "title";
    public static final String COLUMN_NOTIFICATION_BODY = "body";
    public static final String COLUMN_NOTIFICATION_STATUS = "status";
    public static final String COLUMN_NOTIFICATION_IS_READ = "is_read";


    // --- SQL Create Statements ---
    private static final String TABLE_CREATE_MENU_ITEMS =
            "CREATE TABLE " + TABLE_MENU_ITEMS + " (" +
                    COLUMN_MENU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MENU_NAME + " TEXT, " +
                    COLUMN_MENU_DESCRIPTION + " TEXT, " +
                    COLUMN_MENU_PRICE + " REAL, " +
                    COLUMN_MENU_CATEGORY + " TEXT, " +
                    COLUMN_MENU_IMAGE_URI + " TEXT, " +
                    COLUMN_MENU_IMAGE_DRAWABLE_ID + " INTEGER" +
                    ");";

    private static final String TABLE_CREATE_RESERVATIONS =
            "CREATE TABLE " + TABLE_RESERVATIONS + " (" +
                    COLUMN_RESERVATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RESERVATION_STATUS + " TEXT, " +
                    COLUMN_RESERVATION_DATETIME + " TEXT, " +
                    COLUMN_RESERVATION_GUESTS + " INTEGER, " +
                    COLUMN_RESERVATION_TABLE_NUM + " INTEGER, " +
                    COLUMN_USER_ID + " INTEGER" +
                    ")";


    private static final String TABLE_CREATE_NOTIFICATIONS =
            "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                    COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOTIFICATION_USER_ID + " INTEGER, " +
                    COLUMN_NOTIFICATION_TITLE + " TEXT, " +
                    COLUMN_NOTIFICATION_BODY + " TEXT, " +
                    COLUMN_NOTIFICATION_STATUS + " TEXT, " +
                    COLUMN_NOTIFICATION_IS_READ + " INTEGER" +
                    ")";

    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_USER_IMAGE_URI + " TEXT" +
                    ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_MENU_ITEMS);
        db.execSQL(TABLE_CREATE_RESERVATIONS);
        db.execSQL(TABLE_CREATE_NOTIFICATIONS);
        db.execSQL(TABLE_CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}

package com.example.se2_restaurant_management_application.data.local;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.se2_restaurant_management_application.data.models.Reservation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReservationRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public ReservationRepository(Application application) {
        dbHelper = new DatabaseHelper(application);
        initializeData();
    }

    public void getAllReservations(final OnReservationsReadyCallback callback) {
        databaseWriteExecutor.execute(() -> {
            List<Reservation> reservationList = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(DatabaseHelper.TABLE_RESERVATIONS,
                    null, null, null, null, null, DatabaseHelper.COLUMN_RESERVATION_DATETIME + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVATION_ID));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVATION_STATUS));
                    String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVATION_DATETIME));
                    int guests = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVATION_GUESTS));
                    int tableNum = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVATION_TABLE_NUM));
                    String userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));

                    Reservation reservation = new Reservation(id, status, dateTime, guests, tableNum, userId);
                    reservationList.add(reservation);
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onDataReady(reservationList);
        });
    }

    public void insert(Reservation reservation) {
        final String userId = reservation.getUserId();
        insert(reservation, userId);
    }

    public void insert(Reservation reservation, final String userId) {
        databaseWriteExecutor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_RESERVATION_STATUS, reservation.getStatus());
            values.put(DatabaseHelper.COLUMN_RESERVATION_DATETIME, reservation.getDateTime());
            values.put(DatabaseHelper.COLUMN_RESERVATION_GUESTS, reservation.getNumberOfGuests());
            values.put(DatabaseHelper.COLUMN_RESERVATION_TABLE_NUM, reservation.getTableNumber());

            values.put(DatabaseHelper.COLUMN_USER_ID, userId);

            db.insert(DatabaseHelper.TABLE_RESERVATIONS, null, values);
        });
    }

    public void update(Reservation reservation) {
        databaseWriteExecutor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_RESERVATION_STATUS, reservation.getStatus());
            values.put(DatabaseHelper.COLUMN_RESERVATION_DATETIME, reservation.getDateTime());
            values.put(DatabaseHelper.COLUMN_RESERVATION_GUESTS, reservation.getNumberOfGuests());
            values.put(DatabaseHelper.COLUMN_RESERVATION_TABLE_NUM, reservation.getTableNumber());
            values.put(DatabaseHelper.COLUMN_USER_ID, reservation.getUserId());

            String selection = DatabaseHelper.COLUMN_RESERVATION_ID + " = ?";
            String[] selectionArgs = { String.valueOf(reservation.getId()) };

            db.update(DatabaseHelper.TABLE_RESERVATIONS, values, selection, selectionArgs);
        });
    }

    private void initializeData() {
        databaseWriteExecutor.execute(() -> {
        });
    }

    private int getCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RESERVATIONS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public interface OnReservationsReadyCallback {
        void onDataReady(List<Reservation> reservationList);
    }
}

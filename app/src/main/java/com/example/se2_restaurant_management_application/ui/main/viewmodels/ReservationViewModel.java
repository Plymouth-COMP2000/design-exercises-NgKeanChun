package com.example.se2_restaurant_management_application.ui.main.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.se2_restaurant_management_application.data.repository.ReservationRepository;
import com.example.se2_restaurant_management_application.data.models.Notification;
import com.example.se2_restaurant_management_application.data.models.Reservation;
import com.example.se2_restaurant_management_application.util.NotificationHelper;
import com.example.se2_restaurant_management_application.util.SettingsManager;
import com.example.se2_restaurant_management_application.data.models.User;

import java.util.List;

public class ReservationViewModel extends AndroidViewModel {

    private final ReservationRepository reservationRepository;
    private final MutableLiveData<List<Reservation>> allReservations = new MutableLiveData<>();
    private final NotificationViewModel notificationViewModel;
    private final SettingsManager settingsManager;
    private final MutableLiveData<Reservation> _newReservationEvent = new MutableLiveData<>();
    public LiveData<Reservation> getNewReservationEvent() { return _newReservationEvent; }

    private final MutableLiveData<Reservation> _cancelReservationEvent = new MutableLiveData<>();
    public LiveData<Reservation> getCancelReservationEvent() { return _cancelReservationEvent; }

    public ReservationViewModel(@NonNull Application application) {
        super(application);
        reservationRepository = new ReservationRepository(application);
        notificationViewModel = new NotificationViewModel(application);
        settingsManager = new SettingsManager(application);
        loadAllReservations();
    }

    public LiveData<List<Reservation>> getAllReservations() {
        return allReservations;
    }

    private void loadAllReservations() {
        reservationRepository.getAllReservations(reservations -> allReservations.postValue(reservations));
    }

    public void insert(Reservation reservation, User currentUser) {
        if (currentUser == null) {
            Log.e("DataTrace", "ReservationViewModel: FAILED to get current user! Cannot insert.");
            return;
        }
        final String correctUserId = currentUser.getId();
        reservation.setUserId(correctUserId);

        Log.d("DataTrace", "ReservationViewModel: Calling repository insert with CORRECT User ID: " + correctUserId);
        reservationRepository.insert(reservation);
        _newReservationEvent.postValue(reservation);

        // --- Staff Notification Logic ---
        String staffUserId = "staff";
        String title = "New Reservation Request";
        String body = "A new booking for " + reservation.getNumberOfGuests() + " guests is pending for " + reservation.getDateTime();

        // The in-app notification is ALWAYS created, regardless of settings.
        Notification staffNotification = new Notification(staffUserId, title, body, "pending");
        notificationViewModel.createNotification(staffNotification);

        // The pop-up notification is ONLY shown if the setting is enabled.
        if (settingsManager.getBoolean("staff_new_reservations", true)) {
            NotificationHelper notificationHelper = new NotificationHelper(getApplication().getApplicationContext());
            notificationHelper.showNotification(title, body, (int) System.currentTimeMillis(), "Staff");
        }

        loadAllReservations();
    }

    /**
     * This method is for GUEST-initiated updates (editing or cancelling their own reservation).
     * It correctly creates notifications for the STAFF.
     */
    public void update(Reservation reservation) {
        Reservation originalReservation = findOriginalReservation(reservation.getId());
        reservationRepository.update(reservation);

        // --- STAFF NOTIFICATION LOGIC (For Edits/Cancellations by Guest) ---
        String staffUserId = "staff";
        String newStatus = reservation.getStatus().toLowerCase();
        String originalStatus = (originalReservation != null) ? originalReservation.getStatus().toLowerCase() : "";
        String staffTitle = "";
        String staffBody = "";
        String staffNotifStatus = "";

        // Scenario 1: A guest cancels a reservation
        if (newStatus.equals("cancelled") && (originalStatus.equals("pending") || originalStatus.equals("confirmed"))) {
            staffTitle = "Reservation Cancelled by Guest";
            staffBody = "The reservation for " + reservation.getDateTime() + " has been cancelled by the guest.";
            staffNotifStatus = "cancelled";

            if (settingsManager.getBoolean("staff_cancelled_reservations", true)) {
                NotificationHelper helper = new NotificationHelper(getApplication().getApplicationContext());
                helper.showNotification(staffTitle, staffBody, reservation.getId() * -1, "Staff");
            }
        }
        // Scenario 2: A guest edits a reservation
        else if (newStatus.equals("pending") && (originalStatus.equals("pending") || originalStatus.equals("confirmed"))) {
            staffTitle = "Reservation Changed by Guest";
            staffBody = "A reservation for " + reservation.getDateTime() + " has been modified and is now pending approval.";
            staffNotifStatus = "pending";

            if (settingsManager.getBoolean("staff_reservation_changes", true)) {
                NotificationHelper helper = new NotificationHelper(getApplication().getApplicationContext());
                helper.showNotification(staffTitle, staffBody, reservation.getId() * -1, "Staff");
            }
        }

        // If a staff notification was generated, create the in-app version
        if (!staffTitle.isEmpty()) {
            Notification staffNotification = new Notification(staffUserId, staffTitle, staffBody, staffNotifStatus);
            notificationViewModel.createNotification(staffNotification);
        }

        // Also create a "cancelled" notification for the guest themselves.
        if (newStatus.equals("cancelled")) {
            _cancelReservationEvent.postValue(reservation);
            Notification userNotification = new Notification(reservation.getUserId(), "Reservation Cancelled", "Your reservation for " + reservation.getDateTime() + " has been cancelled.", "cancelled");
            notificationViewModel.createNotification(userNotification);

            if (settingsManager.getBoolean("guest_notifications_allowed", true)) {
                NotificationHelper notificationHelper = new NotificationHelper(getApplication().getApplicationContext());
                notificationHelper.showNotification("Reservation Cancelled", "Your reservation for " + reservation.getDateTime() + " has been cancelled.", reservation.getId(), "Guest");
            }
        }

        loadAllReservations();
    }

    /**
     * This method is for STAFF-initiated updates (confirming or cancelling).
     */
    public void updateReservationFromStaff(Reservation reservation) {
        // Step 1: Update the reservation in the database.
        reservationRepository.update(reservation);

        // Step 2: Prepare the notification for the GUEST.
        String newStatus = reservation.getStatus().toLowerCase();
        String guestTitle = "";
        String guestBody = "";

        if (newStatus.equals("confirmed")) {
            guestTitle = "Reservation Confirmed!";
            guestBody = "Your reservation for " + reservation.getDateTime() + " has been confirmed.";
        } else if (newStatus.equals("cancelled")) {
            guestTitle = "Reservation Cancelled";
            guestBody = "Your reservation for " + reservation.getDateTime() + " has been cancelled by the restaurant.";
        }

        // Step 3: If there's a notification to send, create it for the GUEST.
        if (!guestTitle.isEmpty()) {
            // Create the in-app notification for the guest's list
            Notification guestNotification = new Notification(reservation.getUserId(), guestTitle, guestBody, newStatus);
            notificationViewModel.createNotification(guestNotification);

            // Show the pop-up notification if the guest's settings allow it
            if (settingsManager.getBoolean("guest_notifications_allowed", true)) {
                NotificationHelper helper = new NotificationHelper(getApplication().getApplicationContext());
                helper.showNotification(guestTitle, guestBody, reservation.getId(), "Guest");
            }
        }

        // Step 4: Refresh the list of all reservations.
        loadAllReservations();
    }

    private Reservation findOriginalReservation(int reservationId) {
        List<Reservation> currentList = allReservations.getValue();
        if (currentList != null) {
            for (Reservation res : currentList) {
                if (res.getId() == reservationId) {
                    return res;
                }
            }
        }
        return null;
    }

    public void consumeNewReservationEvent() {
        _newReservationEvent.setValue(null);
    }

    public void consumeCancelReservationEvent() {
        _cancelReservationEvent.setValue(null);
    }
}

package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.CategoryHeader;
import com.example.se2_restaurant_management_application.data.models.DisplayableItem;
import com.example.se2_restaurant_management_application.data.models.Notification;
import com.example.se2_restaurant_management_application.data.models.Reservation;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.ui.main.Adapters.NotificationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class StaffNotificationsFragment extends Fragment implements NotificationsAdapter.OnNotificationClickListener {

    private NotificationViewModel notificationViewModel;
    private ReservationViewModel reservationViewModel;
    private AccountViewModel accountViewModel;

    private RecyclerView notificationsRecyclerView;
    private NotificationsAdapter adapter;
    private TextView emptyStateTextView;
    private List<Reservation> allReservations = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViewModels();
        initializeViews(view);
        setupRecyclerView();
        observeData();

        notificationViewModel.fetchNotifications(-1); // Staff ID
    }

    private void initializeViewModels() {
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        reservationViewModel = new ViewModelProvider(requireActivity()).get(ReservationViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
    }

    private void initializeViews(View view) {
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
        ImageButton backButton = view.findViewById(R.id.BackButton);
        backButton.setOnClickListener(v -> {
            if (isAdded()) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationsAdapter(new ArrayList<>(), getContext(), this);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRecyclerView.setAdapter(adapter);
    }

    private void observeData() {
        reservationViewModel.getAllReservations().observe(getViewLifecycleOwner(), reservations -> {
            if (reservations != null) {
                this.allReservations = reservations;
            }
        });

        accountViewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                this.allUsers = users;
            }
        });

        notificationViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                emptyStateTextView.setVisibility(View.VISIBLE);
                notificationsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateTextView.setVisibility(View.GONE);
                notificationsRecyclerView.setVisibility(View.VISIBLE);
                updateAdapterWithHeaders(notifications);
            }
        });

        notificationViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                notificationViewModel.clearError();
            }
        });
    }

    private void updateAdapterWithHeaders(List<Notification> notifications) {
        List<DisplayableItem> itemsWithHeaders = new ArrayList<>();
        List<Notification> unreadNotifications = new ArrayList<>();
        List<Notification> readNotifications = new ArrayList<>();

        for (Notification n : notifications) {
            if (n.isRead()) {
                readNotifications.add(n);
            } else {
                unreadNotifications.add(n);
            }
        }

        if (!unreadNotifications.isEmpty()) {
            itemsWithHeaders.add(new CategoryHeader("New (" + unreadNotifications.size() + ")"));
            itemsWithHeaders.addAll(unreadNotifications);
        }

        if (!readNotifications.isEmpty()) {
            itemsWithHeaders.add(new CategoryHeader("Earlier"));
            itemsWithHeaders.addAll(readNotifications);
        }

        adapter.updateList(itemsWithHeaders);
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (!notification.isRead()) {
            notification.markAsRead();
            notificationViewModel.markNotificationAsRead(notification);
            notificationViewModel.fetchNotifications(-1);
        }

        // --- FIX 1: Find the target reservation for ANY notification type ---
        Reservation targetReservation = findReservationForNotification(notification);

        if (targetReservation != null) {
            navigateToReservationStatus(targetReservation);
        } else {
            Toast.makeText(getContext(), "Could not find the original reservation.", Toast.LENGTH_SHORT).show();
        }
    }

    private Reservation findReservationForNotification(Notification notification) {
        String body = notification.getBody();
        if (body == null || allReservations.isEmpty()) {
            return null;
        }

        // This is a more robust way to find a matching reservation.
        // It iterates through all known reservations and checks if its unique dateTime string
        // is present anywhere inside the notification body.
        for (Reservation res : allReservations) {
            if (body.contains(res.getDateTime())) {
                // As soon as we find a reservation whose date/time is in the body, we have our match.
                return res;
            }
        }

        // If no reservation's date/time string was found in the body, return null.
        Log.e("NavDebug", "No reservation found for notification body: " + body);
        return null;
    }


    private void navigateToReservationStatus(Reservation reservation) {
        Bundle args = new Bundle();

        args.putInt("reservationId", reservation.getId());
        args.putString("reservationStatus", reservation.getStatus());
        args.putString("reservationDateTime", reservation.getDateTime());
        args.putInt("reservationPax", reservation.getNumberOfGuests());
        args.putInt("reservationTable", reservation.getTableNumber());
        args.putInt("reservationUserId", reservation.getUserId());

        User guestUser = findUserById(reservation.getUserId());
        if (guestUser != null) {
            args.putString("guestName", guestUser.getFullName());
            args.putString("guestPhone", guestUser.getContact());
        } else {
            args.putString("guestName", "Unknown Guest");
            args.putString("guestPhone", "N/A");
        }

        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_staff_notifications_to_staff_reservation_status, args);
        } catch (IllegalArgumentException e) {
            Log.e("NavError", "Navigation failed. Action not found on current destination.", e);
            Toast.makeText(getContext(), "Navigation Error: Action not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private User findUserById(int userId) {
        for (User user : allUsers) {
            if (user.getId() == userId) {
                return user;
            }
        }
        return null;
    }
}

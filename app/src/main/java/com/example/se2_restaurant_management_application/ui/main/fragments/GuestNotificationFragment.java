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
import com.example.se2_restaurant_management_application.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class GuestNotificationFragment extends Fragment implements NotificationsAdapter.OnNotificationClickListener {

    private NotificationViewModel notificationViewModel;
    private ReservationViewModel reservationViewModel;
    private AccountViewModel accountViewModel;
    private SessionManager sessionManager;

    private RecyclerView notificationsRecyclerView;
    private NotificationsAdapter adapter;
    private TextView emptyStateTextView;
    private List<Reservation> allReservations = new ArrayList<>();
    private int currentUserId = -1;

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

        User currentUser = accountViewModel.getLoggedInUser().getValue();
        if (currentUser != null) {
            currentUserId = currentUser.getId();
            notificationViewModel.fetchNotifications(currentUserId);
        } else {
            Toast.makeText(getContext(), "User session expired. Please log in again.", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViewModels() {
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        reservationViewModel = new ViewModelProvider(requireActivity()).get(ReservationViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        sessionManager = new SessionManager(requireContext());
    }

    private void initializeViews(View view) {
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
        ImageButton backButton = view.findViewById(R.id.BackButton);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
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
            notificationViewModel.fetchNotifications(currentUserId);
        }

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

        for (Reservation res : allReservations) {
            if (res.getUserId() == currentUserId && body.contains(res.getDateTime())) {
                return res;
            }
        }

        Log.e("NavDebug", "No reservation found for GUEST notification body: " + body);
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

        User currentUser = accountViewModel.getLoggedInUser().getValue();
        if (currentUser != null) {
            args.putString("guestName", currentUser.getFullName());
            args.putString("guestPhone", currentUser.getContact());
        }

        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_guest_notifications_to_reservation_status, args);
        } catch (IllegalArgumentException e) {
            Log.e("NavError", "Guest Navigation failed. Action not found on current destination.", e);
            Toast.makeText(getContext(), "Navigation Error: Action not found.", Toast.LENGTH_SHORT).show();
        }
    }
}

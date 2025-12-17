package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.Reservation;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.ui.auth.LoginActivity;
import com.example.se2_restaurant_management_application.ui.auth.LoginViewModel;
import com.example.se2_restaurant_management_application.ui.main.Adapters.ReservationsAdapter;
import com.example.se2_restaurant_management_application.util.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StaffReservationsFragment extends Fragment implements ReservationsAdapter.OnItemClickListener {

    // UI Components
    private RecyclerView reservationsRecyclerView;
    private ReservationsAdapter adapter;
    private TabLayout tabLayout;
    private ImageButton logoutButton;

    // ViewModels and Managers
    private ReservationViewModel reservationViewModel;
    private LoginViewModel loginViewModel;
    private AccountViewModel accountViewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViewModelsAndManagers();
        initializeViews(view);
        setupUI();
        observeViewModel();

        ImageButton notificationButton = view.findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_staff_reservations_to_notifications)
        );
    }

    private void initializeViewModelsAndManagers() {
        reservationViewModel = new ViewModelProvider(requireActivity()).get(ReservationViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        sessionManager = new SessionManager(requireContext());
    }

    private void initializeViews(@NonNull View view) {
        reservationsRecyclerView = view.findViewById(R.id.reservationsRecyclerView);
        tabLayout = view.findViewById(R.id.tabLayout);
        logoutButton = view.findViewById(R.id.logoutButton);
    }

    private void setupUI() {
        setupRecyclerView();
        setupTabLayout();
        setupButtonClickListeners();
    }

    private void observeViewModel() {
        reservationViewModel.getAllReservations().observe(getViewLifecycleOwner(), reservations -> {
            if (reservations != null) {
                filterAndDisplayReservations(reservations);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ReservationsAdapter(new ArrayList<>(), getContext(), this);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reservationsRecyclerView.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                List<Reservation> currentReservations = reservationViewModel.getAllReservations().getValue();
                if (currentReservations != null) {
                    filterAndDisplayReservations(currentReservations);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupButtonClickListeners() {
        logoutButton.setOnClickListener(v -> performLogout());
    }

    private void filterAndDisplayReservations(List<Reservation> reservations) {
        // Tab 0 is "Pending", Tab 1 is "All"
        if (tabLayout.getSelectedTabPosition() == 0) {
            List<Reservation> pending = reservations.stream()
                    .filter(r -> "pending".equalsIgnoreCase(r.getStatus()))
                    .collect(Collectors.toList());
            adapter.updateList(pending);
        } else {
            adapter.updateList(reservations);
        }
    }

    private void performLogout() {
        sessionManager.logoutUser();
        loginViewModel.onLogout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onItemClick(Reservation reservation) {
        Log.d("DataTrace", "StaffReservationsFragment: Clicked on a reservation. The guest's User ID is: " + reservation.getUserId());

        Bundle args = new Bundle();
        args.putInt("reservationId", reservation.getId());
        args.putString("reservationStatus", reservation.getStatus());
        args.putString("reservationDateTime", reservation.getDateTime());
        args.putInt("reservationPax", reservation.getNumberOfGuests());
        args.putInt("reservationTable", reservation.getTableNumber());
        args.putString("reservationUserId", reservation.getUserId());
        Log.d("DataTrace", "StaffReservationsFragment: Putting reservationUserId into bundle: " + args.getString("reservationUserId"));
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_staff_reservations_to_staff_status, args);
    }
}


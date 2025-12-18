package com.example.se2_restaurant_management_application.ui.main.guest;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.Reservation;
import com.example.se2_restaurant_management_application.ui.auth.LoginActivity;
import com.example.se2_restaurant_management_application.ui.auth.LoginViewModel;
import com.example.se2_restaurant_management_application.ui.main.adapters.ReservationsAdapter;
import com.example.se2_restaurant_management_application.ui.main.viewmodels.AccountViewModel;
import com.example.se2_restaurant_management_application.ui.main.viewmodels.ReservationViewModel;
import com.example.se2_restaurant_management_application.util.SessionManager;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class GuestReservationsFragment extends Fragment implements ReservationsAdapter.OnItemClickListener {

    // UI Components
    private RecyclerView reservationsRecyclerView;
    private ReservationsAdapter adapter;
    private TabLayout tabLayout;
    private Button bookTableButton;
    private ImageButton logoutButton;

    // --- ViewModels and Managers ---
    private ReservationViewModel reservationViewModel;
    private AccountViewModel accountViewModel;
    private LoginViewModel loginViewModel;
    private SessionManager sessionManager;

    // --- Lifecycle Methods ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guest_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViewModelsAndManagers();
        initializeViews(view);
        setupUI();
        observeViewModel();

        NavHostFragment.findNavController(this).getCurrentBackStackEntry()
                .getSavedStateHandle().getLiveData("show_history", false)
                .observe(getViewLifecycleOwner(), showHistory -> {
                    if (showHistory) {
                        // Select the "History" tab.
                        tabLayout.selectTab(tabLayout.getTabAt(1));
                        // Consume the event by resetting the value.
                        NavHostFragment.findNavController(this).getCurrentBackStackEntry()
                                .getSavedStateHandle().set("show_history", false);
                    }
                });
    }


    // --- Initialization and Setup ---
    private void initializeViewModelsAndManagers() {
        // Use requireActivity() to scope ViewModels to the Activity's lifecycle
        reservationViewModel = new ViewModelProvider(requireActivity()).get(ReservationViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        sessionManager = new SessionManager(requireContext());
    }

    private void initializeViews(@NonNull View view) {
        reservationsRecyclerView = view.findViewById(R.id.reservationsRecyclerView);
        tabLayout = view.findViewById(R.id.tabLayout);
        bookTableButton = view.findViewById(R.id.bookTableButton);
        logoutButton = view.findViewById(R.id.logoutButton);
    }

    private void setupUI() {
        setupRecyclerView();
        setupTabLayout();
        setupButtonClickListeners();
    }

    // --- ViewModel Observation ---
    private void observeViewModel() {
        // Observer for the master list of reservations from the database
        reservationViewModel.getAllReservations().observe(getViewLifecycleOwner(), reservations -> {
            if (reservations != null) {
                // When data changes, re-filter the list for the currently selected tab
                filterAndDisplayReservations(reservations);
            }
        });

        // Observer for the "cancellation" event
        reservationViewModel.getCancelReservationEvent().observe(getViewLifecycleOwner(), reservation -> {
            if (reservation != null) {
                Toast.makeText(getContext(), "Reservation Cancelled", Toast.LENGTH_SHORT).show();
                reservationViewModel.consumeCancelReservationEvent(); // Consume event
            }
        });

        // Observer for the "new reservation" event
        reservationViewModel.getNewReservationEvent().observe(getViewLifecycleOwner(), reservation -> {
            if (reservation != null) {
                Toast.makeText(getContext(), "New reservation added!", Toast.LENGTH_SHORT).show();
                reservationViewModel.consumeNewReservationEvent(); // Consume event
            }
        });
    }

    // --- UI Setup Methods ---

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

        // Ensure the first tab is selected on initial setup
        if (tabLayout.getTabCount() > 0) {
            tabLayout.getTabAt(0).select();
        }
    }

    private void setupButtonClickListeners() {
        bookTableButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_reservations_to_bookingDetails));
        logoutButton.setOnClickListener(v -> performLogout());
        ImageButton notificationButton = requireView().findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_guest_reservations_to_notifications)
        );
    }

    // --- Data and Logic Methods ---

    private void filterAndDisplayReservations(List<Reservation> reservations) {
        List<Reservation> upcoming = new ArrayList<>();
        List<Reservation> history = new ArrayList<>();

        for (Reservation res : reservations) {
            String status = res.getStatus().toLowerCase();
            if ("pending".equals(status) || "confirmed".equals(status)) {
                upcoming.add(res);
            } else {
                history.add(res);
            }
        }

        if (tabLayout.getSelectedTabPosition() == 0) {
            adapter.updateList(upcoming);
        } else {
            adapter.updateList(history);
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

    // --- Interface Implementation (OnItemClickListener) ---
    @Override
    public void onItemClick(Reservation reservation) {
        Bundle args = new Bundle();

        // Pass ONLY the reservation details. The next screen will fetch the user info.
        args.putInt("reservationId", reservation.getId());
        args.putString("reservationStatus", reservation.getStatus());
        args.putString("reservationDateTime", reservation.getDateTime());
        args.putInt("reservationPax", reservation.getNumberOfGuests());
        args.putInt("reservationTable", reservation.getTableNumber());
        args.putString("reservationUserId", reservation.getUserId());

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_to_reservation_status, args);
    }
}

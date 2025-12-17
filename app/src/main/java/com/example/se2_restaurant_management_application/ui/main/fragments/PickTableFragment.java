package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.Reservation;
import com.example.se2_restaurant_management_application.data.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickTableFragment extends Fragment implements View.OnClickListener {

    // --- ViewModels and Data ---
    private String selectedDateTime = "";
    private ReservationViewModel reservationViewModel;
    private AccountViewModel accountViewModel;
    private List<Reservation> allReservations = new ArrayList<>();
    private int reservationIdToEdit = -1;

    // --- UI Components ---
    private Map<Integer, TextView> tableViewsMap = new HashMap<>();
    private TextView selectedTable = null;
    private Button reserveTableButton;

    // --- Lifecycle Methods ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pick_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        reservationViewModel = new ViewModelProvider(requireActivity()).get(ReservationViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class); // <-- INITIALIZE THIS

        // Find and map all UI components
        initializeUiComponents(view);

        // Get arguments passed to this fragment (e.g., for editing a reservation)
        retrieveArguments();

        // Start observing the list of reservations for any changes
        observeReservations();

        // Set up the listener for the main action button
        setupReserveButtonListener();
    }

    // --- Initialization ---
    private void initializeUiComponents(View view) {
        // Map all table TextViews by their ID for easy access
        tableViewsMap.put(R.id.table_1, view.findViewById(R.id.table_1));
        tableViewsMap.put(R.id.table_2, view.findViewById(R.id.table_2));
        tableViewsMap.put(R.id.table_3, view.findViewById(R.id.table_3));
        tableViewsMap.put(R.id.table_4, view.findViewById(R.id.table_4));
        tableViewsMap.put(R.id.table_5, view.findViewById(R.id.table_5));
        tableViewsMap.put(R.id.table_6, view.findViewById(R.id.table_6));
        tableViewsMap.put(R.id.table_7, view.findViewById(R.id.table_7));

        // Find buttons
        reserveTableButton = view.findViewById(R.id.reserveTableButton);
        ImageButton backButton = view.findViewById(R.id.BackButton);

        // Set listeners
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Hide the reserve button until a table is selected
        reserveTableButton.setVisibility(View.GONE);
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            reservationIdToEdit = getArguments().getInt("reservationIdToEdit", -1);
            selectedDateTime = getArguments().getString("selectedDateTime", "");
            if (reservationIdToEdit != -1) {
                Log.d("EditReservationFlow", "PickTableFragment: Received ID " + reservationIdToEdit);
            }
        }
    }

    // --- ViewModel Observation ---

    private void observeReservations() {
        reservationViewModel.getAllReservations().observe(getViewLifecycleOwner(), reservations -> {
            if (reservations != null) {
                this.allReservations = reservations;
                // Whenever the reservation list changes, update the table states
                updateAllTableStates();
            }
        });
    }

    // --- Core Logic for Table Availability ---
    private void updateAllTableStates() {
        List<Integer> occupiedTableNumbers = new ArrayList<>();
        String selectedDatePart = "";
        if (selectedDateTime != null && selectedDateTime.contains(",")) {
            selectedDatePart = selectedDateTime.substring(0, selectedDateTime.lastIndexOf(',')).trim();
        }


        for (Reservation reservation : allReservations) {
            String status = reservation.getStatus();
            String reservationDatePart = "";
            if (reservation.getDateTime() != null && reservation.getDateTime().contains(",")) {
                reservationDatePart = reservation.getDateTime().substring(0, reservation.getDateTime().lastIndexOf(',')).trim();
            }

            // A table is occupied if it's Pending/Confirmed AND on the same day
            if (("Pending".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status))
                    && selectedDatePart.equalsIgnoreCase(reservationDatePart)) {

                if (reservation.getId() != reservationIdToEdit) {
                    occupiedTableNumbers.add(reservation.getTableNumber());
                }
            }
        }

        for (Map.Entry<Integer, TextView> entry : tableViewsMap.entrySet()) {
            TextView table = entry.getValue();
            if(table == null) continue; // Safety check in case a layout doesn't have all tables
            int tableNumber = Integer.parseInt(table.getText().toString());
            if (occupiedTableNumbers.contains(tableNumber)) {
                setTableState(table, false);
            } else {
                setTableState(table, true);
            }
        }
    }

    /**
     * Sets the visual state of a table (background, text color, and clickability).
     * @param table The TextView representing the table.
     * @param isAvailable True if the table is available, false if occupied.
     */
    private void setTableState(TextView table, boolean isAvailable) {
        int backgroundResId;
        int textColor;

        // Determine background drawable based on shape (circle or rectangle)
        boolean isCircle = (table.getId() == R.id.table_1 || table.getId() == R.id.table_5);

        if (isAvailable) {
            backgroundResId = isCircle ? R.drawable.table_background_circle_available : R.drawable.table_background_available;
            textColor = ContextCompat.getColor(requireContext(), R.color.text_color_primary);
            table.setOnClickListener(this); // Make available tables clickable
        } else {
            backgroundResId = isCircle ? R.drawable.table_background_circle_occupied : R.drawable.table_background_occupied;
            textColor = ContextCompat.getColor(requireContext(), R.color.text_color_secondary);
            table.setOnClickListener(null); // Make occupied tables not clickable
        }

        table.setBackgroundResource(backgroundResId);
        table.setTextColor(textColor);
    }

    // --- OnClickListener Implementation ---
    @Override
    public void onClick(View v) {

        // If a table was already selected, reset its appearance to "available"
        if (selectedTable != null) {
            setTableState(selectedTable, true);
        }

        // Check if the user is re-clicking the same table to deselect it
        if (selectedTable == v) {
            selectedTable = null; // Deselect
        } else {
            selectedTable = (TextView) v; // Select the new table

            // Set the new table's appearance to "selected"
            boolean isCircle = (selectedTable.getId() == R.id.table_1 || selectedTable.getId() == R.id.table_5);
            int selectedBg = isCircle ? R.drawable.table_background_circle_selected : R.drawable.table_background_selected;
            selectedTable.setBackgroundResource(selectedBg);
            selectedTable.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }

        // Show or hide the "Reserve Table" button based on whether a table is selected
        reserveTableButton.setVisibility(selectedTable != null ? View.VISIBLE : View.GONE);
    }

    // --- Button Listener Setup ---
    private void setupReserveButtonListener() {
        reserveTableButton.setOnClickListener(v -> {
            if (selectedTable == null) {
                Toast.makeText(getContext(), "Please select a table first.", Toast.LENGTH_SHORT).show();
                return;
            }

            User currentUser = null;
            if (getArguments() != null) {
                currentUser = (User) getArguments().getSerializable("currentUser");
            }

            if (currentUser == null) {
                Toast.makeText(getContext(), "Error: User not logged in.", Toast.LENGTH_SHORT).show();
                Log.e("DataTrace", "PickTableFragment: CRITICAL - currentUser from bundle is null!");
                return;
            }
            String userId = currentUser.getId();
            Log.d("DataTrace", "PickTableFragment: Preparing to save reservation with User ID: " + userId);


            // Get the selected table number
            int tableNumber = Integer.parseInt(selectedTable.getText().toString());
            // Get the pax count that was passed from the previous screen
            int pax = getArguments() != null ? getArguments().getInt("selectedPax", 1) : 1;

            if (reservationIdToEdit != -1) {
                // Add the userId to the constructor call
                Reservation updatedReservation = new Reservation(
                        reservationIdToEdit,
                        "Pending",
                        selectedDateTime,
                        pax,
                        tableNumber,
                        userId
                );
                // Call the UPDATE method in the ViewModel
                reservationViewModel.update(updatedReservation);
                Toast.makeText(getContext(), "Reservation Updated!", Toast.LENGTH_SHORT).show();

            } else {
                // Add the userId to the constructor call
                Reservation newBooking = new Reservation(
                        "Pending",
                        selectedDateTime,
                        pax,
                        tableNumber,
                        userId // <-- Pass the user ID
                );
                // Call the INSERT method in the ViewModel
                reservationViewModel.insert(newBooking, currentUser);
            }

            // After inserting or updating, navigate ALL THE WAY BACK to the reservations list.
            NavHostFragment.findNavController(this)
                    .popBackStack(R.id.navigation_reservations, false);
        });
    }
}

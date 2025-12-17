package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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


public class StaffReservationStatusFragment extends Fragment {

    // UI Components
    private TextView guestNameTextView, guestPhoneTextView;
    private TextView dateTextView, timeTextView, paxTextView, tableTextView;
    private AccountViewModel accountViewModel;
    private ImageView statusIcon;
    private TextView statusText;
    private Button denyButton, confirmButton;

    // ViewModel and Data
    private ReservationViewModel reservationViewModel;
    private Reservation currentReservation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_reservation_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the ViewModel
        reservationViewModel = new ViewModelProvider(requireActivity()).get(ReservationViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class); // <-- ADD THIS

        initializeViews(view);
        populateDataFromArgs();
        setupButtonListeners();
    }

    private void initializeViews(View view) {
        guestNameTextView = view.findViewById(R.id.guestNameTextView);
        guestPhoneTextView = view.findViewById(R.id.guestPhoneTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        paxTextView = view.findViewById(R.id.paxTextView);
        tableTextView = view.findViewById(R.id.tableTextView);
        statusIcon = view.findViewById(R.id.statusIcon);
        statusText = view.findViewById(R.id.statusText);
        denyButton = view.findViewById(R.id.denyButton);
        confirmButton = view.findViewById(R.id.confirmButton);

        ImageButton backButton = view.findViewById(R.id.BackButton);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void populateDataFromArgs() {
        Bundle args = getArguments();
        if (args == null) return;

        // Reconstruct the reservation object from the arguments passed to this fragment
        currentReservation = new Reservation(
                args.getInt("reservationId"),
                args.getString("reservationStatus"),
                args.getString("reservationDateTime"),
                args.getInt("reservationPax"),
                args.getInt("reservationTable"),
                args.getString("reservationUserId")
        );

        String guestId = currentReservation.getUserId();
        Log.d("DataTrace", "StaffReservationStatusFragment: Received guestId from bundle: " + guestId);

        if (guestId != null && !guestId.isEmpty()) { // Check if the String is valid
            Log.d("DataTrace", "StaffReservationStatusFragment: Calling getGuestUserById(" + guestId + ") now.");

            accountViewModel.getGuestUserById(guestId).observe(getViewLifecycleOwner(), guestUser -> {
                if (guestUser != null) {
                    // SUCCESS: Display the fetched guest's details
                    Log.d("DataTrace", "StaffReservationStatusFragment: Observer received SUCCESS. User is: " + guestUser.getFullName());
                    guestNameTextView.setText(guestUser.getFullName());
                    guestPhoneTextView.setText(guestUser.getContact());
                } else {
                    // FAILURE: The user could not be found on the server
                    Log.e("DataTrace", "StaffReservationStatusFragment: Observer received FAILURE. guestUser is null.");
                    guestNameTextView.setText("Unknown Guest");
                    guestPhoneTextView.setText("ID: " + guestId);
                }

                String dateTime = currentReservation.getDateTime();
                String dateToDisplay = "Date: N/A";
                String timeToDisplay = "Time: N/A";

                if (dateTime != null && dateTime.contains(",")) {
                    int lastComma = dateTime.lastIndexOf(',');
                    dateToDisplay = "Date: " + dateTime.substring(0, lastComma).trim();
                    timeToDisplay = "Time: " + dateTime.substring(lastComma + 1).trim();
                }

                dateTextView.setText(dateToDisplay);
                timeTextView.setText(timeToDisplay);
                paxTextView.setText("Pax: " + currentReservation.getNumberOfGuests());
                tableTextView.setText("Table: " + currentReservation.getTableNumber());

                updateStatusAppearance(currentReservation.getStatus());
            });
        } else {

            guestNameTextView.setText("Invalid Guest ID");
            guestPhoneTextView.setText("N/A");
        }
    }

    private void setupButtonListeners() {
        denyButton.setOnClickListener(v -> {
            if (currentReservation == null) return;

            // Determine the title and message based on the button's current text ("Deny" or "Cancel")
            String titleText = "Deny Reservation";
            String messageText = "Are you sure you want to deny this reservation?";
            String positiveButtonText = "Deny it";
            if ("Cancel".equalsIgnoreCase(denyButton.getText().toString())) {
                titleText = "Cancel Reservation";
                messageText = "Are you sure you want to cancel this confirmed reservation?";
                positiveButtonText = "Cancel it";
            }

            TextView customTitle = new TextView(requireContext());
            customTitle.setText(titleText);
            customTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            customTitle.setGravity(Gravity.CENTER);
            customTitle.setPadding(10, 40, 10, 10);
            customTitle.setTextSize(22f);
            customTitle.setTypeface(null, Typeface.BOLD);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setCustomTitle(customTitle)
                    .setMessage(messageText)
                    .setPositiveButton(positiveButtonText, (dialog, which) -> {
                        handleReservationUpdate("Cancelled", "Reservation Cancelled");
                    })
                    .setNegativeButton("No", null)
                    .setIcon(R.drawable.ic_cancel);

            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();

            Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));

            TextView messageTextView = dialog.findViewById(android.R.id.message);
            if (messageTextView != null) {
                negativeButton.setTextColor(messageTextView.getCurrentTextColor());
            }

            final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            params.gravity = Gravity.CENTER;
            positiveButton.setLayoutParams(params);
            negativeButton.setLayoutParams(params);
        });

        confirmButton.setOnClickListener(v -> {
            TextView confirmTitle = new TextView(requireContext());
            confirmTitle.setText("Confirm Reservation?");
            confirmTitle.setTextColor(getResources().getColor(R.color.status_confirmed_bg, null)); // Using your green color
            confirmTitle.setGravity(Gravity.CENTER);
            confirmTitle.setPadding(10, 40, 10, 10);
            confirmTitle.setTextSize(22f);
            confirmTitle.setTypeface(null, Typeface.BOLD);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setCustomTitle(confirmTitle)
                    .setMessage("Do you want to confirm this reservation?")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        handleReservationUpdate("Confirmed", "Reservation Confirmed");
                    })
                    .setNegativeButton("No", null)
                    .setIcon(R.drawable.ic_confirmed);

            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();

            Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(getResources().getColor(R.color.status_confirmed_bg, null));
            TextView messageTextView = dialog.findViewById(android.R.id.message);
            if (messageTextView != null) {
                negativeButton.setTextColor(messageTextView.getCurrentTextColor());
            }

            // Center the buttons
            final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            params.gravity = Gravity.CENTER;
            positiveButton.setLayoutParams(params);
            negativeButton.setLayoutParams(params);
        });
    }


    private void handleReservationUpdate(String newStatus, String toastMessage) {
        if (currentReservation != null) {
            currentReservation.setStatus(newStatus);
            reservationViewModel.updateReservationFromStaff(currentReservation);
            Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        }
    }


    private void updateStatusAppearance(String status) {
        int colorRes;
        int iconRes;

        // By default, hide all action buttons.
        denyButton.setVisibility(View.GONE);
        confirmButton.setVisibility(View.GONE);

        switch (status.toLowerCase()) {
            case "pending":
                colorRes = ContextCompat.getColor(requireContext(), R.color.status_pending_bg);
                iconRes = R.drawable.ic_pending;
                denyButton.setVisibility(View.VISIBLE);
                confirmButton.setVisibility(View.VISIBLE);
                denyButton.setText("Deny"); // Set text for clarity
                break;

            case "confirmed":
                colorRes = ContextCompat.getColor(requireContext(), R.color.status_confirmed_bg);
                iconRes = R.drawable.ic_confirmed;
                denyButton.setVisibility(View.VISIBLE);
                denyButton.setText("Cancel");
                confirmButton.setVisibility(View.GONE);
                break;

            case "cancelled":
                colorRes = ContextCompat.getColor(requireContext(), R.color.status_cancelled_bg);
                iconRes = R.drawable.ic_cancel;
                // No actions can be taken on a cancelled reservation.
                break;

            case "completed":
                colorRes = ContextCompat.getColor(requireContext(), R.color.status_completed_bg);
                iconRes = R.drawable.ic_confirmed; // Same icon as confirmed
                // No actions can be taken on a completed reservation.
                break;

            default:
                colorRes = ContextCompat.getColor(requireContext(), R.color.text_color_secondary);
                iconRes = R.drawable.ic_pending;
                break;
        }

        statusText.setText(status);
        statusText.setTextColor(colorRes);
        statusIcon.setImageResource(iconRes);
        statusIcon.setImageTintList(ColorStateList.valueOf(colorRes));
    }

}

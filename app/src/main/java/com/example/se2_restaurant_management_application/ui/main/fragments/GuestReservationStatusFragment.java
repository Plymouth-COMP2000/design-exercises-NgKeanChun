package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.graphics.Typeface;
import android.util.Log;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.Reservation;

public class GuestReservationStatusFragment extends Fragment {

    // UI Components
    private TextView guestNameTextView, guestPhoneTextView;
    private TextView dateTextView, timeTextView, paxTextView, tableTextView;
    private ImageView statusIcon;
    private TextView statusText;
    private Button cancelReservationButton, editButton;

    //ViewModel
    private ReservationViewModel reservationViewModel;
    private AccountViewModel accountViewModel;
    private Reservation currentReservation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guest_reservation_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reservationViewModel = new ViewModelProvider(requireActivity()).get(ReservationViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        initializeViews(view);

        ImageButton backButton = view.findViewById(R.id.BackButton);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        populateData();
        setupButtonListeners();
    }

    private void initializeViews(View view) {
        // Guest Info
        guestNameTextView = view.findViewById(R.id.guestNameTextView);
        guestPhoneTextView = view.findViewById(R.id.guestPhoneTextView);

        // Details
        dateTextView = view.findViewById(R.id.dateTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        paxTextView = view.findViewById(R.id.paxTextView);
        tableTextView = view.findViewById(R.id.tableTextView);

        // Status
        statusIcon = view.findViewById(R.id.statusIcon);
        statusText = view.findViewById(R.id.statusText);

        // Buttons
        cancelReservationButton = view.findViewById(R.id.cancelReservationButton);
        editButton = view.findViewById(R.id.editButton);
        cancelReservationButton.setText("Cancel");
    }

    private void populateData() {
        Bundle args = getArguments();
        if (args == null) {
            return; // No data to show
        }

        currentReservation = new Reservation(
                args.getInt("reservationId", -1),
                args.getString("reservationStatus", "N/A"),
                args.getString("reservationDateTime", "N/A, N/A"),
                args.getInt("reservationPax", 0),
                args.getInt("reservationTable", 0),
                args.getString("reservationUserId", null)
        );

        accountViewModel.getLoggedInUser().observe(getViewLifecycleOwner(), loggedInUser -> {
            if (loggedInUser != null) {
                guestNameTextView.setText(loggedInUser.getFullName());
                guestPhoneTextView.setText(loggedInUser.getContact());
            } else {
                // This is a fallback in case the user data isn't ready yet.
                guestNameTextView.setText("Loading...");
                guestPhoneTextView.setText("Loading...");
            }
        });


        // Retrieve all data from the bundle
        String status = args.getString("reservationStatus", "N/A");
        String dateTime = args.getString("reservationDateTime", "N/A, N/A");
        int pax = args.getInt("reservationPax", 0);
        int table = args.getInt("reservationTable", 0);
        String guestName = args.getString("guestName", "Guest");
        String guestPhone = args.getString("guestPhone", "N/A");

        // parse the date and time from the combined string
        String dateToDisplay;
        String timeToDisplay;

        // Find the last comma in the string, which separates the date part from the time part
        int lastCommaIndex = dateTime.lastIndexOf(',');
        if (lastCommaIndex != -1) {
            String datePart = dateTime.substring(0, lastCommaIndex).trim(); // e.g., "Sun, December 25, 2025"
            timeToDisplay = dateTime.substring(lastCommaIndex + 1).trim();  // e.g., "8:00 PM"

            // format the date part to add the ordinal suffix
            String[] dateComponents = datePart.split(" ");
            if (dateComponents.length >= 4) {
                String dayOfMonthStr = dateComponents[2].replace(",", "");
                try {
                    int day = Integer.parseInt(dayOfMonthStr);
                    // Build the final formatted date string
                    dateToDisplay = String.format("Date: %s %s%s, %s",
                            dateComponents[1],
                            day,
                            getDayOfMonthSuffix(day),
                            dateComponents[3]
                    );
                } catch (NumberFormatException e) {
                    dateToDisplay = "Date: " + datePart;
                }
            } else {
                dateToDisplay = "Date: " + datePart; // Fallback for unexpected format
            }
        } else {
            // Fallback if the dateTime string has no comma
            dateToDisplay = "Date: N/A";
            timeToDisplay = "Time: N/A";
        }
        // Set the text for all views
        guestNameTextView.setText(guestName);
        guestPhoneTextView.setText(guestPhone);
        dateTextView.setText(dateToDisplay);
        timeTextView.setText(String.format("Time: %s", timeToDisplay));
        paxTextView.setText(String.format("Pax: %d", pax));
        tableTextView.setText(String.format("Table: %d", table));
        statusText.setText(status);

        // Update status icon and color based on the status string
        updateStatusAppearance(currentReservation.getStatus());
    }

    // This function returns the correct ordinal suffix (st, nd, rd, th) for a given day.
    private String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }


    //Method to set up the button listeners
    private void setupButtonListeners() {
        cancelReservationButton.setOnClickListener(v -> {
            if (currentReservation == null) return;

            TextView customTitle = new TextView(requireContext());
            customTitle.setText("Cancel Reservation?");
            customTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            customTitle.setGravity(Gravity.CENTER);
            customTitle.setPadding(10, 40, 10, 10);
            customTitle.setTextSize(22f);
            customTitle.setTypeface(null, Typeface.BOLD);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setCustomTitle(customTitle)
                    .setMessage("Are you sure you want to cancel this reservation?")
                    .setPositiveButton("Cancel it", (dialog, which) -> {
                        // User confirmed, proceed with cancellation
                        currentReservation.setStatus("Cancelled");
                        reservationViewModel.update(currentReservation);
                        NavHostFragment.findNavController(this).getPreviousBackStackEntry()
                                .getSavedStateHandle().set("show_history", true);
                        NavHostFragment.findNavController(this).popBackStack();
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

            final android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            params.gravity = Gravity.CENTER;
            positiveButton.setLayoutParams(params);
            negativeButton.setLayoutParams(params);
        });

        // --- Edit Button Listener ---
        editButton.setOnClickListener(v -> {
            if (currentReservation == null) return;
            TextView customTitle = new TextView(requireContext());
            customTitle.setText("Edit Reservation?");
            customTitle.setTextColor(getResources().getColor(R.color.button_dark_green, null));
            customTitle.setGravity(Gravity.CENTER);
            customTitle.setPadding(10, 40, 10, 10);
            customTitle.setTextSize(22f);
            customTitle.setTypeface(null, Typeface.BOLD);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setCustomTitle(customTitle)
                    .setMessage("Do you want to edit this reservation?")
                    .setPositiveButton("Edit it", (dialog, which) -> {
                        Bundle args = new Bundle();
                        Log.d("EditReservationFlow", "StatusFragment: Putting reservationIdToEdit into bundle: " + currentReservation.getId());
                        args.putInt("reservationIdToEdit", currentReservation.getId());
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_status_to_bookingDetails, args);
                    })
                    .setNegativeButton("No", null)
                    .setIcon(R.drawable.ic_edit); // Use an edit icon

            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();

            Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(getResources().getColor(R.color.button_dark_green, null));

            TextView messageTextView = dialog.findViewById(android.R.id.message);
            if (messageTextView != null) {
                negativeButton.setTextColor(messageTextView.getCurrentTextColor());
            }

            final android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            params.gravity = Gravity.CENTER;
            positiveButton.setLayoutParams(params);
            negativeButton.setLayoutParams(params);
        });
    }


    private void updateStatusAppearance(String status) {
        int colorRes;
        int iconRes;

        //set the default visibility for the buttons
        cancelReservationButton.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.VISIBLE);

        switch (status.toLowerCase()) {
            case "completed":
                colorRes = ContextCompat.getColor(requireContext(), R.color.status_completed_bg);
                iconRes = R.drawable.ic_confirmed;
                // A completed reservation cannot be edited or cancelled
                cancelReservationButton.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                break;
            case "cancelled":
                colorRes = ContextCompat.getColor(requireContext(), R.color.status_cancelled_bg);
                iconRes = R.drawable.ic_cancel;
                // An already cancelled reservation cannot be edited or cancelled again
                cancelReservationButton.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                break;
            case "pending":
                colorRes = ContextCompat.getColor(requireContext(), R.color.status_pending_bg);
                iconRes = R.drawable.ic_pending;
                break;
            case "confirmed":
                colorRes = ContextCompat.getColor(requireContext(), R.color.status_confirmed_bg);
                iconRes = R.drawable.ic_confirmed;
                break;
            default:
                // Fallback for any unexpected status
                colorRes = ContextCompat.getColor(requireContext(), R.color.text_color_secondary);
                iconRes = R.drawable.ic_pending;
                break;
        }

        statusText.setTextColor(colorRes);
        statusIcon.setImageResource(iconRes);
        statusIcon.setImageTintList(ColorStateList.valueOf(colorRes));
    }
}

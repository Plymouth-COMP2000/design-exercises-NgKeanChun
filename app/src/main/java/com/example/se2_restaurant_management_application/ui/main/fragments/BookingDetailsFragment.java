package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.se2_restaurant_management_application.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit; // <-- IMPORT THIS

public class BookingDetailsFragment extends Fragment {

    // --- UI Components ---
    private CalendarView calendarView;
    private TextView timeTextView;
    private TextView paxTextView;
    private ImageButton minusButton;
    private ImageButton plusButton;
    private Button proceedToTableButton;
    private TextView tableNumberTextView;

    // --- Data Holders ---
    private String selectedDate;
    private String selectedTime;
    private int currentPax = 1;
    private int reservationIdToEdit = -1;
    private long lastSelectedDateMillis; // <-- FIX 1: Variable to hold the last valid date

    // Define Restaurant Operating Hours
    private static final int OPENING_HOUR = 10; // 10:00 AM
    private static final int CLOSING_HOUR = 22; // 10:00 PM

    // --- Lifecycle Methods ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guest_booking_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        retrieveArguments();
        initializeDate(); // Moved up to set dates before listeners
        setupListeners();
    }

    // --- Initialization ---

    private void initializeViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        timeTextView = view.findViewById(R.id.timeTextView);
        paxTextView = view.findViewById(R.id.paxTextView);
        minusButton = view.findViewById(R.id.minusButton);
        plusButton = view.findViewById(R.id.plusButton);
        proceedToTableButton = view.findViewById(R.id.proceedToTableButton);
        tableNumberTextView = view.findViewById(R.id.tableNumberTextView);

        tableNumberTextView.setVisibility(View.GONE);
        view.findViewById(R.id.tableLabel).setVisibility(View.GONE);

        paxTextView.setText(String.valueOf(currentPax));

        ImageButton backButtonHeader = view.findViewById(R.id.BackButton);
        Button backButtonFooter = view.findViewById(R.id.formBackButton);
        View.OnClickListener goBackListener = v -> NavHostFragment.findNavController(this).popBackStack();
        backButtonHeader.setOnClickListener(goBackListener);
        backButtonFooter.setOnClickListener(goBackListener);
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            reservationIdToEdit = getArguments().getInt("reservationIdToEdit", -1);
            if (reservationIdToEdit != -1) {
                Log.d("EditReservationFlow", "BookingDetailsFragment: Starting in EDIT mode for ID: " + reservationIdToEdit);
            }
        }
    }

    private void initializeDate() {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        // Set the minimum selectable date to today.
        calendarView.setMinDate(calendar.getTimeInMillis());
        lastSelectedDateMillis = calendar.getTimeInMillis(); // Initialize with today's date

        // --- FIX 2: REMOVE THE setMaxDate() CALL ---
        // calendarView.setMaxDate(maxDate); // This line is now removed.

        // Set the initially selected date string to today
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        selectedDate = sdf.format(calendar.getTime());
    }


    // --- Listeners Setup ---

    private void setupListeners() {
        // --- FIX 3: UPDATED LISTENER LOGIC ---
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar today = Calendar.getInstance(TimeZone.getDefault());
            Calendar selected = Calendar.getInstance(TimeZone.getDefault());
            selected.set(year, month, dayOfMonth);

            // Calculate the difference in days between today and the selected date
            long diffInMillis = selected.getTimeInMillis() - today.getTimeInMillis();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            // Check if the selected date is within the 7-day window (0 to 7 days from now)
            if (diffInDays >= 0 && diffInDays <= 7) {
                // VALID DATE: Update the date and reset the time
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
                selectedDate = sdf.format(selected.getTime());
                lastSelectedDateMillis = selected.getTimeInMillis(); // Remember this valid date

                selectedTime = null;
                timeTextView.setText("");
                timeTextView.setHint("Choose time");
            } else {
                // INVALID DATE: Show a toast and revert the calendar to the last valid date
                Toast.makeText(getContext(), "You can only book up to 7 days in advance.", Toast.LENGTH_SHORT).show();
                view.setDate(lastSelectedDateMillis, true, true);
            }
        });


        timeTextView.setOnClickListener(v -> showTimePickerDialog());

        minusButton.setOnClickListener(v -> {
            if (currentPax > 1) {
                currentPax--;
                paxTextView.setText(String.valueOf(currentPax));
            }
        });

        plusButton.setOnClickListener(v -> {
            if (currentPax < 10) {
                currentPax++;
                paxTextView.setText(String.valueOf(currentPax));
            }
        });

        proceedToTableButton.setOnClickListener(v -> {
            if (selectedTime == null || selectedTime.isEmpty()) {
                Toast.makeText(getContext(), "Please choose a time", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle args = new Bundle();
            String dateTime = selectedDate + ", " + selectedTime;
            args.putString("selectedDateTime", dateTime);
            args.putInt("selectedPax", currentPax);

            if (reservationIdToEdit != -1) {
                args.putInt("reservationIdToEdit", reservationIdToEdit);
            }

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_bookingDetails_to_pickTable, args);
        });
    }

    private void showTimePickerDialog() {
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, hourOfDay, minuteOfHour) -> {
                    if (hourOfDay < OPENING_HOUR || hourOfDay >= CLOSING_HOUR) {
                        Toast.makeText(getContext(), "Please select a time between 10:00 AM and 10:00 PM.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (isToday(calendarView.getDate()) && (hourOfDay < currentHour || (hourOfDay == currentHour && minuteOfHour < currentMinute))) {
                        Toast.makeText(getContext(), "You cannot book a reservation in the past.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCalendar.set(Calendar.MINUTE, minuteOfHour);
                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    selectedTime = sdf.format(selectedCalendar.getTime());
                    timeTextView.setText(selectedTime);

                }, currentHour, currentMinute, false);

        if (timePickerDialog.getWindow() != null) {
            timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        timePickerDialog.show();
    }

    private boolean isToday(long selectedDateMillis) {
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDateMillis);

        Calendar todayCal = Calendar.getInstance();

        return selectedCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                selectedCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR);
    }
}

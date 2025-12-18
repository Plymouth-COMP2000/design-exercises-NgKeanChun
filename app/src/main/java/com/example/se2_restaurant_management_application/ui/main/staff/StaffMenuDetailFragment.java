package com.example.se2_restaurant_management_application.ui.main.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.se2_restaurant_management_application.R;

import java.util.Locale;

public class StaffMenuDetailFragment extends Fragment {

    // Declare UI components from the layout
    private ImageView detailFoodImageView;
    private TextView detailFoodNameTextView;
    private TextView detailFoodPriceTextView;
    private TextView detailDescriptionTextView;
    private TextView categoryChip;
    private Button returnButton;
    private Button editButton; // Staff-specific button

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the staff-specific detail layout
        return inflater.inflate(R.layout.fragment_staff_menu_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find all the UI components by their ID
        detailFoodImageView = view.findViewById(R.id.detailFoodImageView);
        detailFoodNameTextView = view.findViewById(R.id.detailFoodNameTextView);
        detailFoodPriceTextView = view.findViewById(R.id.detailFoodPriceTextView);
        detailDescriptionTextView = view.findViewById(R.id.detailDescriptionTextView);
        categoryChip = view.findViewById(R.id.categoryChip);
        returnButton = view.findViewById(R.id.returnButton);
        editButton = view.findViewById(R.id.editButton);

        // Populate the UI with data passed from the previous fragment
        populateUiWithArguments();

        // Setup listeners for the buttons
        setupButtonListeners();
    }

    private void populateUiWithArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String name = args.getString("itemName", "Not Found");
            double price = args.getDouble("itemPrice", 0.0);
            String description = args.getString("itemDescription", "No description available.");
            String category = args.getString("itemCategory", "Uncategorized");
            String imageUriString = args.getString("imageUri");
            if (imageUriString != null) {
                // If a URI string exists, use it to load the image.
                detailFoodImageView.setImageURI(android.net.Uri.parse(imageUriString));
            } else {
                // Otherwise, fall back to the old drawable ID.
                int imageId = args.getInt("itemImageId", R.drawable.ic_launcher_foreground);
                detailFoodImageView.setImageResource(imageId);
            }

            detailFoodNameTextView.setText(name);
            detailFoodPriceTextView.setText(String.format(Locale.US, "RM%.2f", price));
            detailDescriptionTextView.setText(description);
            categoryChip.setText(category);
        }
    }


    private void setupButtonListeners() {
        // Set listener for the "Back" button to navigate back
        returnButton.setOnClickListener(v -> NavHostFragment.findNavController(StaffMenuDetailFragment.this).popBackStack());

        // Set listener for the "Edit" button
        editButton.setOnClickListener(v -> {
            Bundle currentItemBundle = getArguments();

            if (currentItemBundle != null) {
                // Navigate to the edit screen, passing the item's data bundle.
                NavHostFragment.findNavController(StaffMenuDetailFragment.this)
                        .navigate(R.id.action_staff_detail_to_edit, currentItemBundle);
            } else {
                Toast.makeText(getContext(), "Error: No item data found to edit.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

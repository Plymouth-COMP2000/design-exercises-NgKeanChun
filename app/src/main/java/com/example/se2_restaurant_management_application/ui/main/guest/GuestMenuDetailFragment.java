package com.example.se2_restaurant_management_application.ui.main.guest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.se2_restaurant_management_application.R;

import java.util.Locale;

public class GuestMenuDetailFragment extends Fragment {

    // --- Declare UI components ---
    private ImageView detailFoodImageView;
    private TextView detailFoodNameTextView;
    private TextView detailFoodPriceTextView;
    private TextView detailDescriptionTextView;
    private TextView categoryChip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guest_menu_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the back button
        Button backButton = view.findViewById(R.id.BackButton);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(GuestMenuDetailFragment.this).popBackStack());

        // --- Find all the other UI components ---
        detailFoodImageView = view.findViewById(R.id.detailFoodImageView);
        detailFoodNameTextView = view.findViewById(R.id.detailFoodNameTextView);
        detailFoodPriceTextView = view.findViewById(R.id.detailFoodPriceTextView);
        detailDescriptionTextView = view.findViewById(R.id.detailDescriptionTextView);
        categoryChip = view.findViewById(R.id.categoryChip);

        // --- Get the arguments passed from MenuFragment ---
        Bundle args = getArguments();
        if (args != null) {
            String name = args.getString("itemName", "Not Found");
            double price = args.getDouble("itemPrice", 0.0);
            String description = args.getString("itemDescription", "No description available.");
            String category = args.getString("itemCategory", "Uncategorized");
            int imageId = args.getInt("itemImageId", R.drawable.ic_launcher_foreground);

            // Populate the UI with the received data
            detailFoodNameTextView.setText(name);
            detailFoodPriceTextView.setText(String.format(Locale.US, "$%.2f", price));
            detailDescriptionTextView.setText(description);
            categoryChip.setText(category);
            detailFoodImageView.setImageResource(imageId);
        }
    }
}

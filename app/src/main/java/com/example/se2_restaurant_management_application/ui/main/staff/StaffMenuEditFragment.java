package com.example.se2_restaurant_management_application.ui.main.staff;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.Menu;
import com.example.se2_restaurant_management_application.ui.main.viewmodels.MenuViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class StaffMenuEditFragment extends Fragment {

    // Declare ImageView and ActivityResultLauncher
    private ImageView foodImageView;
    private ActivityResultLauncher<String> galleryLauncher;
    private Uri selectedImageUri = null;

    private EditText itemNameEditText;
    private EditText pricingEditText;
    private EditText descriptionEditText;
    private ImageButton backButton;
    private Button deleteButton;
    private Button saveButton;
    private TextView titleTextView;
    private ChipGroup categoryChipGroup;

    private MenuViewModel menuViewModel;
    private int currentItemId = -1;
    private Menu currentMenuItem = null;

    private final List<String> categories = Arrays.asList("Appetizers", "Burgers", "Salads", "Desserts", "Drinks", "Pizza");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // The user has successfully picked an image.
                        selectedImageUri = uri;
                        // Display the selected image in our ImageView.
                        foodImageView.setImageURI(selectedImageUri);

                        // Remove any background tint to show the full-color image.
                        foodImageView.setImageTintList(null);
                        // Persist permission to access this URI across device reboots
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_menu_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        menuViewModel = new ViewModelProvider(requireActivity()).get(MenuViewModel.class);

        // Find all views, including the new ImageView
        foodImageView = view.findViewById(R.id.foodImageView);
        itemNameEditText = view.findViewById(R.id.foodNameEditText);
        pricingEditText = view.findViewById(R.id.pricingEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        backButton = view.findViewById(R.id.backButton);
        deleteButton = view.findViewById(R.id.deleteButton);
        saveButton = view.findViewById(R.id.saveButton);
        titleTextView = view.findViewById(R.id.editMenuTitle);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);

        // Set a click listener on the ImageView to open the gallery
        foodImageView.setOnClickListener(v -> {
            // Launch the gallery to pick an image.
            galleryLauncher.launch("image/*");
        });

        menuViewModel.getMenuItemDeletedEvent().observe(getViewLifecycleOwner(), isDeleted -> {
            if (isDeleted != null && isDeleted) {
                Toast.makeText(getContext(), "Item Deleted!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigate(R.id.action_edit_to_staff_menu);
                menuViewModel.consumeMenuItemDeletedEvent();
            }
        });

        setupCategoryChips();
        populateFields();
        setupButtonListeners();
    }

    private void setupCategoryChips() {
        if (getContext() == null) return;
        for (String category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setClickable(true);
            categoryChipGroup.addView(chip);
        }
    }

    private void populateFields() {
        Bundle args = getArguments();
        if (args != null) {
            currentItemId = args.getInt("itemId", -1);
        }

        if (currentItemId != -1 && args != null) {
            // EDITING MODE
            titleTextView.setText("Edit Menu Item");
            deleteButton.setVisibility(View.VISIBLE);

            currentMenuItem = new Menu();
            currentMenuItem.setId(currentItemId);
            currentMenuItem.setName(args.getString("itemName"));
            currentMenuItem.setPrice(args.getDouble("itemPrice"));
            currentMenuItem.setDescription(args.getString("itemDescription"));
            currentMenuItem.setCategory(args.getString("itemCategory"));
            currentMenuItem.setImageDrawableId(args.getInt("itemImageId"));
            currentMenuItem.setImageUri(args.getString("imageUri"));

            itemNameEditText.setText(currentMenuItem.getName());
            pricingEditText.setText(String.format(Locale.US, "%.2f", currentMenuItem.getPrice()));
            descriptionEditText.setText(currentMenuItem.getDescription());

            // Load the saved image if it exists
            if (currentMenuItem.getImageUri() != null && !currentMenuItem.getImageUri().isEmpty()) {
                selectedImageUri = Uri.parse(currentMenuItem.getImageUri());
                foodImageView.setImageURI(selectedImageUri);
                foodImageView.setImageTintList(null);

            } else {
                foodImageView.setImageResource(R.drawable.ic_launcher_foreground); // Placeholder
            }

            for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                View child = categoryChipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.getText().toString().equalsIgnoreCase(currentMenuItem.getCategory())) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        } else {
            // ADDING MODE
            titleTextView.setText("Add New Item");
            deleteButton.setVisibility(View.GONE);
            foodImageView.setImageResource(R.drawable.ic_launcher_foreground); // Placeholder
        }
    }

    private void setupButtonListeners() {
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        deleteButton.setOnClickListener(v -> deleteItem());
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        if (itemNameEditText == null || pricingEditText == null) {
            Toast.makeText(getContext(), "Error: Layout components not found.", Toast.LENGTH_LONG).show();
            return;
        }

        String name = itemNameEditText.getText().toString().trim();
        String priceStr = pricingEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Name and Price cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedChipId = categoryChipGroup.getCheckedChipId();
        if (selectedChipId == View.NO_ID) {
            Toast.makeText(getContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
            return;
        }
        Chip selectedChip = categoryChipGroup.findViewById(selectedChipId);
        String category = selectedChip.getText().toString();

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price format.", Toast.LENGTH_SHORT).show();
            return;
        }

        int imageId = R.drawable.ic_launcher_foreground;

        if (currentMenuItem != null) {
            currentMenuItem.setName(name);
            currentMenuItem.setPrice(price);
            currentMenuItem.setDescription(description);
            currentMenuItem.setCategory(category);
            if (selectedImageUri != null) {
                currentMenuItem.setImageUri(selectedImageUri.toString());
            }
            menuViewModel.update(currentMenuItem);
            Toast.makeText(getContext(), "Changes Saved!", Toast.LENGTH_SHORT).show();
        } else {
            // INSERT NEW ITEM
            Menu newItem = new Menu(name, description, price, imageId, category);
            if (selectedImageUri != null) {
                newItem.setImageUri(selectedImageUri.toString());
            }
            menuViewModel.insert(newItem);
            Toast.makeText(getContext(), "Item Added!", Toast.LENGTH_SHORT).show();
        }
        NavHostFragment.findNavController(this).navigate(R.id.action_edit_to_staff_menu);
    }

    private void deleteItem() {
        if (currentMenuItem != null) {
            TextView customTitle = new TextView(requireContext());
            customTitle.setText("Delete Item?");
            customTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            customTitle.setGravity(Gravity.CENTER);
            customTitle.setPadding(10, 40, 10, 10);
            customTitle.setTextSize(22f);
            customTitle.setTypeface(null, Typeface.BOLD);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setCustomTitle(customTitle)
                    .setMessage("Are you sure you want to delete this item? This action cannot be undone.")
                    .setPositiveButton("Delete it", (dialog, which) -> {
                        menuViewModel.delete(currentMenuItem);
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

        } else {
            Toast.makeText(getContext(), "Error: Cannot delete item.", Toast.LENGTH_SHORT).show();
        }
    }
}

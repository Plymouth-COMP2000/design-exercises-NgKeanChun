package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.CategoryHeader;
import com.example.se2_restaurant_management_application.data.models.DisplayableItem;
import com.example.se2_restaurant_management_application.data.models.Menu;
import com.example.se2_restaurant_management_application.ui.auth.LoginActivity;
import com.example.se2_restaurant_management_application.ui.auth.LoginViewModel;
import com.example.se2_restaurant_management_application.ui.main.Adapters.MenuAdapter;
import com.example.se2_restaurant_management_application.util.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MenuFragment extends Fragment {


    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private MenuViewModel menuViewModel;
    private SearchView searchView;
    private ChipGroup categoryChipGroup;
    private ImageButton logoutButton;
    private SessionManager sessionManager;
    private LoginViewModel loginViewModel;
    private String currentSearchQuery = "";
    private String currentCategoryFilter = "All";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guest_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        menuRecyclerView = view.findViewById(R.id.menuRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        logoutButton = view.findViewById(R.id.logoutButton);

        ImageButton notificationButton = view.findViewById(R.id.notificationButton);
        if (notificationButton != null) {
            notificationButton.setOnClickListener(v -> {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_global_to_notifications);
            });
        }

        setupRecyclerView();
        observeViewModel();
        setupSearchListener();
        setupLogoutButton();
    }

    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        sessionManager.logoutUser();
        if (loginViewModel != null) {
            loginViewModel.onLogout();
        }
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void setupRecyclerView() {
        menuAdapter = new MenuAdapter(new ArrayList<>(), menu -> {
            Bundle bundle = new Bundle();
            bundle.putString("itemName", menu.getName());
            bundle.putDouble("itemPrice", menu.getPrice());
            bundle.putString("itemDescription", menu.getDescription());
            bundle.putString("itemCategory", menu.getCategory());
            bundle.putInt("itemImageId", menu.getImageDrawableId());
            NavHostFragment.findNavController(MenuFragment.this)
                    .navigate(R.id.action_menu_to_menu_detail, bundle);
        });
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuRecyclerView.setAdapter(menuAdapter);
    }

    private void observeViewModel() {
        menuViewModel.getAllMenuItems().observe(getViewLifecycleOwner(), menuItems -> {
            if (menuItems != null) {
                updateCategoryChips(menuItems);
                filterList();
            }
        });
    }

    private void updateCategoryChips(List<Menu> menuItems) {
        categoryChipGroup.clearCheck();
        categoryChipGroup.removeAllViews();

        // Use a Set to get unique category names while preserving insertion order
        Set<String> categories = new LinkedHashSet<>();
        for (Menu item : menuItems) {
            categories.add(item.getCategory());
        }

        // --- Create "All" Chip ---
        Chip allChip = new Chip(getContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setId(View.generateViewId());
        categoryChipGroup.addView(allChip);

        // --- Create a chip for each unique category ---
        for (String category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            categoryChipGroup.addView(chip);
        }

        // Set the previously selected filter (or default to "All")
        boolean filterSet = false;
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoryChipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(currentCategoryFilter)) {
                chip.setChecked(true);
                filterSet = true;
                break;
            }
        }
        // If the previously selected category no longer exists, default to "All"
        if (!filterSet) {
            allChip.setChecked(true);
            currentCategoryFilter = "All";
        }

        // --- Set up the listener AFTER creating the chips ---
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                allChip.setChecked(true); // Prevent having no selection
                return;
            }
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                currentCategoryFilter = selectedChip.getText().toString();
                filterList(); // Re-filter when a new chip is selected
            }
        });
    }

    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filterList();
                return true;
            }
        });
    }

    private void filterList() {
        List<Menu> originalList = menuViewModel.getAllMenuItems().getValue();
        if (originalList == null) {
            return;
        }
        List<DisplayableItem> filteredList = new ArrayList<>();
        for (Menu menuItem : originalList) {
            boolean categoryMatch = currentCategoryFilter.equals("All") || menuItem.getCategory().equalsIgnoreCase(currentCategoryFilter);
            boolean nameMatch = menuItem.getName().toLowerCase(Locale.ROOT).contains(currentSearchQuery.toLowerCase(Locale.ROOT));

            if (categoryMatch && nameMatch) {
                filteredList.add(menuItem);
            }
        }
        List<DisplayableItem> finalListWithHeaders = addHeadersToMenuList(filteredList);
        menuAdapter.filterList(finalListWithHeaders);
    }

    private List<DisplayableItem> addHeadersToMenuList(List<DisplayableItem> menuList) {
        List<DisplayableItem> listWithHeaders = new ArrayList<>();
        String lastHeader = "";
        for (DisplayableItem item : menuList) {
            if (item instanceof Menu) {
                Menu menuItem = (Menu) item;
                if (!menuItem.getCategory().equals(lastHeader)) {
                    listWithHeaders.add(new CategoryHeader(menuItem.getCategory()));
                    lastHeader = menuItem.getCategory();
                }
                listWithHeaders.add(menuItem);
            }
        }
        return listWithHeaders;
    }
}

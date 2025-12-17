package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class StaffMenuFragment extends Fragment {

    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private SearchView searchView;
    private ChipGroup categoryChipGroup;
    private ImageButton logoutButton;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton addNewItemButton;
    private SessionManager sessionManager;
    private LoginViewModel loginViewModel;
    private MenuViewModel menuViewModel;

    private List<Menu> originalMenuItems = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentCategoryFilter = "All";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        menuViewModel = new ViewModelProvider(requireActivity()).get(MenuViewModel.class);

        menuRecyclerView = view.findViewById(R.id.menuRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        logoutButton = view.findViewById(R.id.logoutButton);
        addNewItemButton = view.findViewById(R.id.addNewItemButton);

        ImageButton notificationButton = view.findViewById(R.id.notificationButton);
        if (notificationButton != null) {
            notificationButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.action_global_to_notifications));
        }

        setupRecyclerView();
        setupSearchListener();
        setupButtonListeners();
        observeMenuItems();
    }

    private void observeMenuItems() {
        menuViewModel.getAllMenuItems().observe(getViewLifecycleOwner(), menus -> {
            if (menus != null) {
                this.originalMenuItems = new ArrayList<>(menus);
                updateCategoryChips(menus);
                filterList();
            }
        });
    }

    private void setupButtonListeners() {
        logoutButton.setOnClickListener(v -> performLogout());
        addNewItemButton.setOnClickListener(v ->
                NavHostFragment.findNavController(StaffMenuFragment.this)
                        .navigate(R.id.action_staff_menu_to_add_item));
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
            bundle.putInt("itemId", menu.getId());
            bundle.putString("itemName", menu.getName());
            bundle.putDouble("itemPrice", menu.getPrice());
            bundle.putString("itemDescription", menu.getDescription());
            bundle.putString("itemCategory", menu.getCategory());
            bundle.putInt("itemImageId", menu.getImageDrawableId());
            bundle.putString("imageUri", menu.getImageUri());
            NavHostFragment.findNavController(StaffMenuFragment.this)
                    .navigate(R.id.action_staff_menu_to_staff_detail, bundle);
        });
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuRecyclerView.setAdapter(menuAdapter);
    }

    private void updateCategoryChips(List<Menu> menuItems) {
        categoryChipGroup.clearCheck();
        categoryChipGroup.removeAllViews();

        Set<String> categories = new LinkedHashSet<>();
        for (Menu item : menuItems) {
            categories.add(item.getCategory());
        }

        Chip allChip = new Chip(getContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setId(View.generateViewId());
        categoryChipGroup.addView(allChip);

        for (String category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            categoryChipGroup.addView(chip);
        }

        boolean filterSet = false;
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoryChipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(currentCategoryFilter)) {
                chip.setChecked(true);
                filterSet = true;
                break;
            }
        }
        if (!filterSet) {
            allChip.setChecked(true);
            currentCategoryFilter = "All";
        }

        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                allChip.setChecked(true);
                return;
            }
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                currentCategoryFilter = selectedChip.getText().toString();
                filterList();
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
        List<DisplayableItem> filteredList = new ArrayList<>();
        for (Menu menuItem : originalMenuItems) {
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

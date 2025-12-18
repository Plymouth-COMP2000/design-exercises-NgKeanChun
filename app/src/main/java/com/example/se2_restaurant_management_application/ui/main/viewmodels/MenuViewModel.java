package com.example.se2_restaurant_management_application.ui.main.viewmodels;import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.se2_restaurant_management_application.data.repository.MenuRepository;
import com.example.se2_restaurant_management_application.data.models.Menu;

import java.util.List;

public class MenuViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Menu>> allMenuItems = new MutableLiveData<>();
    private final MenuRepository menuRepository;
    private final MutableLiveData<Boolean> _menuItemDeletedEvent = new MutableLiveData<>();
    public LiveData<Boolean> getMenuItemDeletedEvent() {
        return _menuItemDeletedEvent;
    }
    public MenuViewModel(@NonNull Application application) {
        super(application);
        menuRepository = new MenuRepository(application);
        loadAllMenuItems(); // Initial load
    }

    // Method to load or refresh data from the repository
    public void loadAllMenuItems() {
        menuRepository.getAllMenuItems(menuList -> allMenuItems.postValue(menuList));
    }

    public LiveData<List<Menu>> getAllMenuItems() {
        return allMenuItems;
    }

    public void insert(Menu menu) {
        menuRepository.insert(menu);
        // Refresh the list after inserting
        loadAllMenuItems();
    }

    public void update(Menu menu) {
        menuRepository.update(menu);
        // Refresh the list after updating
        loadAllMenuItems();
    }

    public void delete(Menu menu) {
        menuRepository.delete(menu, () -> {
            _menuItemDeletedEvent.postValue(true);
            loadAllMenuItems();
        });
    }

    public void consumeMenuItemDeletedEvent() {
        _menuItemDeletedEvent.setValue(null);
    }
}

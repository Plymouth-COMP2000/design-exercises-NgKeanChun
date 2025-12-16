package com.example.se2_restaurant_management_application.ui.main.Adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.Menu;
import com.example.se2_restaurant_management_application.data.models.CategoryHeader;
import com.example.se2_restaurant_management_application.data.models.DisplayableItem;
import java.util.List;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Menu menu);
    }

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_MENU_ITEM = 1;

    private final List<DisplayableItem> items;
    private final OnItemClickListener listener;

    public MenuAdapter(List<DisplayableItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof CategoryHeader) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_MENU_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(R.layout.list_header_category, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.list_item_menu, parent, false);
            // Pass the listener to the ViewHolder
            return new MenuViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MenuViewHolder) {
            MenuViewHolder menuHolder = (MenuViewHolder) holder;
            Menu menuItem = (Menu) items.get(position);

            menuHolder.itemName.setText(menuItem.getName());
            menuHolder.itemPrice.setText(String.format(Locale.US, "RM%.2f", menuItem.getPrice()));

            // --- THE FIX IS HERE ---
            // This is the new, crucial logic for loading the image.

            // 1. Check if an image URI string exists for this menu item.
            String imageUriString = menuItem.getImageUri();

            if (imageUriString != null && !imageUriString.isEmpty()) {
                // If it exists, parse it into a Uri and set it to the ImageView.
                menuHolder.itemImage.setImageURI(Uri.parse(imageUriString));
                // Also, remove any tint to ensure the full-color image is shown.
                menuHolder.itemImage.setImageTintList(null);
            } else {
                // 2. If NO URI exists, fall back to the old placeholder drawable ID.
                menuHolder.itemImage.setImageResource(menuItem.getImageDrawableId());
                // Since this is a placeholder, we might want to re-apply the tint if needed,
                // but for now, setting it to null is safer to avoid green placeholders.
                menuHolder.itemImage.setImageTintList(null);
            }

            // The click listener remains the same.
            menuHolder.itemView.setOnClickListener(v -> listener.onItemClick(menuItem));

        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            CategoryHeader header = (CategoryHeader) items.get(position);
            headerHolder.headerTextView.setText(header.getCategoryName());
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
    public void filterList(List<DisplayableItem> filteredList) {
        items.clear();
        items.addAll(filteredList);
        notifyDataSetChanged(); // Notify the adapter to refresh the view
    }
    static class MenuViewHolder extends RecyclerView.ViewHolder {
        private final ImageView itemImage;
        private final TextView itemName;
        private final TextView itemPrice;

        // The listener is no longer needed in the constructor
        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.foodImageView);
            itemName = itemView.findViewById(R.id.foodNameTextView);
            itemPrice = itemView.findViewById(R.id.foodPriceTextView);
            // The redundant listener that was here has been REMOVED.
        }

        // The listener is passed into bind() where it has access to the specific menuItem
        public void bind(final Menu menuItem, final OnItemClickListener listener) {
            itemName.setText(menuItem.getName());
            itemPrice.setText(String.format(Locale.US, "$%.2f", menuItem.getPrice()));

            if (menuItem.getImageDrawableId() != 0) {
                itemImage.setImageResource(menuItem.getImageDrawableId());
            } else {
                itemImage.setImageResource(R.drawable.ic_launcher_foreground); // A default image
            }

            // This is the correct place to set the listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(menuItem);
                }
            });
        }
    }

    // ViewHolder for the category header (list_header_category.xml)
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView headerTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.categoryHeaderTextView);
        }

        public void bind(CategoryHeader header) {
            headerTextView.setText(header.getCategoryName());
        }
    }
}

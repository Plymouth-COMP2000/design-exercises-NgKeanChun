package com.example.se2_restaurant_management_application.ui.main.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.CategoryHeader;
import com.example.se2_restaurant_management_application.data.models.DisplayableItem;
import com.example.se2_restaurant_management_application.data.models.Notification;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // --- OnItemClickListener Interface ---
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTIFICATION = 1;

    private final List<DisplayableItem> items;
    private final Context context;
    private final OnNotificationClickListener clickListener;

    public NotificationsAdapter(List<DisplayableItem> items, Context context, OnNotificationClickListener listener) {
        this.items = items;
        this.context = context;
        this.clickListener = listener; // Initialize listener
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof CategoryHeader) {
            return TYPE_HEADER;
        }
        return TYPE_NOTIFICATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header_notification, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            CategoryHeader header = (CategoryHeader) items.get(position);
            headerHolder.headerTextView.setText(header.getCategoryName());
        } else {
            NotificationViewHolder notificationHolder = (NotificationViewHolder) holder;
            Notification notification = (Notification) items.get(position);
            notificationHolder.bind(notification, context, clickListener);
        }
    }

    public void updateList(List<DisplayableItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.headerTextView);
        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView notificationIcon;
        TextView notificationTitle;
        TextView notificationBody;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationIcon = itemView.findViewById(R.id.notificationIcon);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationBody = itemView.findViewById(R.id.notificationBody);
        }

        void bind(Notification notification, Context context, OnNotificationClickListener listener) {
            notificationTitle.setText(notification.getTitle());
            notificationBody.setText(notification.getBody());

            int iconRes = R.drawable.ic_notification_active;
            int colorRes;

            switch (notification.getStatus().toLowerCase()) {
                case "pending":
                    iconRes = R.drawable.ic_pending;
                    colorRes = R.color.status_pending_bg;
                    break;
                case "confirmed":
                    iconRes = R.drawable.ic_confirmed;
                    colorRes = R.color.status_confirmed_bg;
                    break;
                case "cancelled":
                    iconRes = R.drawable.ic_cancel;
                    colorRes = R.color.status_cancelled_bg;
                    break;
                default:
                    colorRes = R.color.text_color_secondary;
                    break;
            }

            notificationIcon.setImageResource(iconRes);
            notificationIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));

            // --- READ/UNREAD VISUALS ---
            if (notification.isRead()) {
                itemView.setAlpha(0.6f);
                notificationTitle.setTypeface(null, Typeface.NORMAL);
            } else {
                itemView.setAlpha(1.0f);
                notificationTitle.setTypeface(null, Typeface.BOLD);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }
}

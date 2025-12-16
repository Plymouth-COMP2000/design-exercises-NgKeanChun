package com.example.se2_restaurant_management_application.ui.main.Adapters;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.Reservation;
import java.util.List;
import java.util.Locale;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Reservation reservation);
    }
    private List<Reservation> reservations;
    private final Context context;
    private final OnItemClickListener onItemClickListener;




    public ReservationsAdapter(List<Reservation> reservations, Context context, OnItemClickListener listener) {
        this.reservations = reservations;
        this.context = context;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.bind(reservation, context, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public void updateList(List<Reservation> newList) {
        this.reservations = newList;
        notifyDataSetChanged();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        private final TextView statusTextView;
        private final TextView dateTimeTextView;
        private final TextView paxTextView;
        private final TextView tableTextView;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            paxTextView = itemView.findViewById(R.id.paxTextView);
            tableTextView = itemView.findViewById(R.id.tableTextView);
        }

        public void bind(Reservation reservation, Context context, final OnItemClickListener listener) {
            statusTextView.setText(reservation.getStatus());
            dateTimeTextView.setText(reservation.getDateTime());
            paxTextView.setText(String.format(Locale.getDefault(), "Pax: %d", reservation.getNumberOfGuests()));
            tableTextView.setText(String.format(Locale.getDefault(), "Table: %d", reservation.getTableNumber()));

            // Set the background tint color for the status tag based on the status text
            int backgroundColor;
            int textColor;

            switch (reservation.getStatus().toLowerCase()) {
                case "completed":
                    backgroundColor = ContextCompat.getColor(context, R.color.status_completed_bg);
                    textColor = ContextCompat.getColor(context, R.color.white);
                    break;
                case "cancelled":
                    backgroundColor = ContextCompat.getColor(context, R.color.status_cancelled_bg);
                    textColor = ContextCompat.getColor(context, R.color.white);
                    break;
                case "confirmed":
                    backgroundColor = ContextCompat.getColor(context, R.color.status_confirmed_bg);
                    textColor = ContextCompat.getColor(context, R.color.white);
                    break;
                case "pending":
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.status_pending_bg);
                    textColor = ContextCompat.getColor(context, R.color.white);
                    break;
            }

            statusTextView.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
            statusTextView.setTextColor(textColor);

            // Set the click listener on the item view
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(reservation);
                }
            });
        }
    }
    }

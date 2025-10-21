package com.vasrask.boubou;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vasrask.boubou.entities.BabyActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class BabyActivityAdapter extends RecyclerView.Adapter<BabyActivityAdapter.BabyActivityViewHolder> {

    public interface OnDeleteListener {
        void onDeleteClick(BabyActivity babyActivity);
    }

    private List<BabyActivity> babyActivities;

    private OnDeleteListener onDeleteListener;

    public BabyActivityAdapter() {
        this.babyActivities = new ArrayList<>();
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.onDeleteListener = listener;
    }

    public static class BabyActivityViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView amountView;
        private final TextView dateTextView;
        private final TextView notesTextView;
        private Button deleteButton;
        private final String TAG = "Element";

        public BabyActivityViewHolder(View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element is " + getAdapterPosition() + " clicked");
                }
            });

            textView = (TextView) view.findViewById(R.id.babyActivityTitleText);
            amountView = (TextView) view.findViewById(R.id.babyActivityAmountText);
            dateTextView = (TextView) view.findViewById(R.id.dateTextView);
            notesTextView = (TextView) view.findViewById(R.id.babyActivityNotesText);
            deleteButton = (Button) view.findViewById(R.id.deleteButton);
        }

        public View getTextView() {
            return textView;
        }

        public View getAmountView() {
            return amountView;
        }

        public View getDateView() {
            return dateTextView;
        }
        public View getNotesView() {
            return notesTextView;
        }
    }
    private void deleteBabyActivityFromFirestore(BabyActivity babyActivity, int position) {
        String babyActivityId = babyActivity.getId();

        if (babyActivityId == null) {
            Log.e("BabyActivityAdapter", "BabyActivity id is null");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(user.getUid()).collection("babyActivities").document(babyActivityId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("BabyActivityAdapter", "BabyActivity deleted successfully");


                })
                .addOnFailureListener(e -> {
                    Log.e("BabyActivityAdapter", "Failed to delete babyActivities ", e);
                });


    }


    public void setBabyActivities(List<BabyActivity> babyActivities) {
        this.babyActivities = babyActivities;
        notifyDataSetChanged();
    }

    public BabyActivity getBabyActivityAt(int position) {
        return babyActivities.get(position);
    }

    public void removeBabyActivity(int position) {
        babyActivities.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, babyActivities.size());
    }

    @NonNull
    @Override
    public BabyActivityAdapter.BabyActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.baby_activity_item, parent, false);

        return new BabyActivityViewHolder(view);
    }

    private String formatDuration(double totalMinutes) {
        @SuppressLint("DefaultLocale") String formattedDuration;
        int hours = (int) totalMinutes / 60;
        int minutes = (int) totalMinutes % 60;
        if (hours > 0 && minutes > 0) {
            formattedDuration = String.format("%dh%02dm", hours, minutes);
        } else if (hours > 0) {
            formattedDuration = String.format("%dh", hours);
        } else {
            formattedDuration = String.format("%dm", minutes);
        }
                return formattedDuration;
    }
    @Override
    public void onBindViewHolder(@NonNull BabyActivityAdapter.BabyActivityViewHolder viewHolder, int position) {

        String TAG = "BabyActivityAdapter";

        BabyActivity babyActivity = babyActivities.get(position);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewHolder.itemView.getLayoutParams();

        if (position == getItemCount() - 1) {
            params.bottomMargin = 32;
        } else {
            params.bottomMargin = 0;
        }

        viewHolder.itemView.setLayoutParams(params);

        Log.d(TAG, "BabyActivity is at " + babyActivity + " and " + position);

        Timestamp timestamp = babyActivity.getTimestamp();

        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());
            viewHolder.dateTextView.setText(sdf.format(date));
        } else {
            viewHolder.dateTextView.setText("");
        }

        viewHolder.deleteButton.setOnClickListener(v -> {
                if (onDeleteListener != null) {
                    onDeleteListener.onDeleteClick(babyActivity);
                }
        });
        String category = babyActivity.getCategory().trim();

        viewHolder.notesTextView.setText(babyActivity.getNotes());

        int color = Color.MAGENTA;

        if (category.equals("SLEEP") || category.equals("PLAYTIME")) {
            viewHolder.textView.setText(category);
            viewHolder.amountView.setText(formatDuration(babyActivity.getDuration()));
            viewHolder.amountView.setTextColor(color);
        } else if (category.equals("FEEDING")) {
            String feedingCategory = babyActivity.getFeeding_category().trim();
            viewHolder.textView.setText(category + ": " + feedingCategory);
            if (feedingCategory.equals("Breastfeeding")) {
                viewHolder.amountView.setText(formatDuration(babyActivity.getDuration()));
                viewHolder.amountView.setTextColor(color);
            } else {
                double intake = babyActivity.getIntake();
                @SuppressLint("DefaultLocale") String formattedIntake;
                formattedIntake = String.format("%dml", (int) intake);
                viewHolder.amountView.setText(formattedIntake);
                viewHolder.amountView.setTextColor(color);
            }
        } else if (category.equals("DIAPER_CHANGE")) {
            @SuppressLint("DefaultLocale") String Check;
            if (babyActivity.getDiaper_check()) {
                Check = "OK";
            } else {
                Check = "⚠";
            }
            viewHolder.amountView.setText(Check);
            viewHolder.amountView.setTextColor(color);
        } else {
            @SuppressLint("DefaultLocale") String Check;
            if (babyActivity.getMedicine_check()) {
                Check = "OK";
            } else {
                Check = "⚠";
            }
            viewHolder.amountView.setText(Check);
            viewHolder.amountView.setTextColor(color);
        }
    }

    @Override
    public int getItemCount() {
        return babyActivities.size();
    }
}
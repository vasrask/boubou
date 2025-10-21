package com.vasrask.boubou.repositories;

import android.util.Log;

import com.vasrask.boubou.entities.BabyActivityType;
import com.vasrask.boubou.entities.BabyActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.vasrask.boubou.entities.FeedingType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class BabyActivityRepository {

    private final String TAG = "BabyActivityRepository";
    private final FirebaseFirestore db;
    private final CollectionReference BabyActivitiesRef;
    private final FirebaseAuth mAuth;

    public BabyActivityRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();

        this.BabyActivitiesRef = db.collection("users").document(Objects.requireNonNull(mAuth.getUid())).collection("babyActivities");
    }


    private BabyActivityType mapBabyActivityType(String category) {
        if (category == null) return BabyActivityType.OTHER;

        switch (category.toLowerCase().trim()) {
            case "sleep":
                return BabyActivityType.SLEEP;
            case "feeding":
                return BabyActivityType.FEEDING;
            case "diaperChange":
                return BabyActivityType.DIAPER_CHANGE;
            case "medicine":
                return BabyActivityType.MEDICINE;
            case "playtime":
                return BabyActivityType.PLAYTIME;
            default:
                return BabyActivityType.OTHER;
        }
    }
    private FeedingType mapFeedingType(String category) {
        if (category == null) return FeedingType.NO_FEEDING;

        switch (category.toLowerCase().trim()) {
            case "Breastfeeding":
                return FeedingType.BREASTFEEDING;
            case "Pumped Breast Milk":
                return FeedingType.PUMPED_BREAST_MILK;
            case "Formula":
                return FeedingType.FORMULA;
            default:
                return FeedingType.NO_FEEDING;
        }
    }

    public Task<List<BabyActivity>> getBabyActivities(int limit, String field) {

        Query query = BabyActivitiesRef.orderBy(field, Query.Direction.DESCENDING);

        if (limit != 0) {
            query = query.limit(limit);
        }

        return BabyActivitiesRef.get().continueWith(task -> {
            if (task.isSuccessful()) {
                Log.w(TAG, "BabyActivities fetched");
                List<BabyActivity> BabyActivities = task.getResult().toObjects(BabyActivity.class);
                Log.d(TAG, "Added " + BabyActivities.size() + " BabyActivities");

                return BabyActivities;
            } else {
                Log.e(TAG, "Error getting BabyActivities ", task.getException());
                throw task.getException();
            }
        });
    }

    public Task<List<BabyActivity>> getFilteredBabyActivities(String category) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        return db.collection("users").document(currentUser.getUid()).collection("babyActivities").whereEqualTo("category", category.toUpperCase().trim()).orderBy("timestamp", Query.Direction.DESCENDING).get().continueWith(task -> {
            if (task.isSuccessful()) {
                List<BabyActivity> BabyActivities = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    BabyActivity t = doc.toObject(BabyActivity.class);
                    if (t != null) {
                        t.setId(doc.getId());
                        BabyActivities.add(t);
                    }
                }
                return BabyActivities;
            } else {
                throw task.getException();
            }
        });
    }

    public Task<List<BabyActivity>> getSortedBabyActivities(String field, boolean ascending) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        List<String> validFields = Arrays.asList("duration", "timestamp", "category");

        if (!validFields.contains(field)) {
            field = "timestamp";
        }

        Query.Direction direction = ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;

        return db.collection("users").document(currentUser.getUid()).collection("babyActivities").orderBy(field, direction).get().continueWith(task -> {
            if (task.isSuccessful()) {
                List<BabyActivity> BabyActivities = new ArrayList<>();

                for (DocumentSnapshot doc : task.getResult()) {
                    BabyActivity babyActivity = doc.toObject(BabyActivity.class);
                    if (babyActivity != null) {
                        babyActivity.setId(doc.getId());
                        BabyActivities.add(babyActivity);
                    }
                }
                return BabyActivities;
            } else {
                throw task.getException();
            }

        });
    }

    public Task<Void> storeBabyActivity(double amount, boolean check, String selectedCategory, String feedingType, String notes) {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        if (amount <= 0) {
            return Tasks.forException(new IllegalArgumentException("Duration must be greater than 0"));
        }

        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        return userRef.get().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            DocumentSnapshot document = task.getResult();
            BabyActivity babyActivity;
            if (amount > 0) {
                babyActivity = new BabyActivity(UUID.randomUUID().toString(), userId, amount, mapBabyActivityType(selectedCategory), mapFeedingType(feedingType), notes);
            } else {
                babyActivity = new BabyActivity(UUID.randomUUID().toString(), userId, check, mapBabyActivityType(selectedCategory), notes);
            }
            WriteBatch batch = db.batch();

            DocumentReference txRef = BabyActivitiesRef.document(babyActivity.getId());
            Map<String, Object> txMap = new HashMap<>();
            txMap.put("id", babyActivity.getId());
            txMap.put("userId", userId);
            txMap.put("category", babyActivity.getCategory());
            switch (babyActivity.getCategory()){
                case "SLEEP":
                case "PLAYTIME":
                    txMap.put("duration", babyActivity.getDuration());
                    break;
                case "FEEDING":
                    txMap.put("intake", babyActivity.getIntake());
                    break;
                case "DIAPER_CHANGE":
                    txMap.put("diaper_check", babyActivity.getDiaper_check());
                    break;
                case "MEDICINE":
                    txMap.put("medicine_check", babyActivity.getMedicine_check());
                    break;
                default:
                    Log.i(TAG, "Invalid input");
            }
            txMap.put("timestamp", FieldValue.serverTimestamp());
            txMap.put("notes", babyActivity.getNotes());

            batch.set(txRef, txMap);

            return batch.commit();
        });

    }

    public Task<Void> deleteBabyActivity(String babyActivityId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        String userId = currentUser.getUid();
        DocumentReference txRef = db.collection("users")
                .document(userId)
                .collection("babyActivities")
                .document(babyActivityId);

        return txRef.get().continueWithTask(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                throw new Exception("BabyActivity not found");
            }

            BabyActivity babyActivity = task.getResult().toObject(BabyActivity.class);
            if (babyActivity == null) {
                throw new Exception("Failed to parse babyActivity");
            }

            WriteBatch batch = db.batch();

            DocumentReference userRef = db.collection("users").document(userId);

            batch.delete(txRef);

            return batch.commit();
        });
    }
}

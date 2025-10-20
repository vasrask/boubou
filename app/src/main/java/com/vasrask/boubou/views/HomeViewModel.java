package com.vasrask.boubou.views;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.SetOptions;
import com.vasrask.boubou.entities.BabyActivityType;
import com.vasrask.boubou.entities.BabyActivity;
import com.vasrask.boubou.entities.BabyActivityType;
import com.vasrask.boubou.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<BabyActivity>> babyActivities = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private ListenerRegistration userListener;
    private ListenerRegistration babyActivitiesListener;

    private final String TAG = "HomeViewModel";

    public HomeViewModel() {
        loadBabyActivities();
        loadUserProfile();
    }

    public LiveData<List<BabyActivity>> getBabyActivities() {
        return this.babyActivities;
    }

    public LiveData<User> getUserProfile() {
        return this.user;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    private void loadBabyActivities() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        babyActivitiesListener = db.collection("users").document(currentUser.getUid()).collection("babyActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        babyActivities.setValue(new ArrayList<>());
                        return;
                    }

                    List<BabyActivity> babyActivityList = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        BabyActivity babyActivity = doc.toObject(BabyActivity.class);

                        if (babyActivity != null) {
                            babyActivity.setId(doc.getId());
                            babyActivityList.add(babyActivity);
                        }
                    }
                    babyActivities.setValue(babyActivityList);
                });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        userListener = db.collection("users").document(currentUser.getUid()).addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists()) return;

            User userObj = snapshot.toObject(User.class);
            user.setValue(userObj);
        });
    }

    private BabyActivityType mapBabyActivityType(String category) {
        if (category == null) return BabyActivityType.OTHER;

        switch (category.toLowerCase().trim()) {
            case "sleeping":
                return BabyActivityType.SLEEPING;
            case "eating":
                return BabyActivityType.EATING;
            case "pooping":
                return BabyActivityType.POOPING;
            case "vitamins":
                return BabyActivityType.VITAMINS;
            case "exercising":
                return BabyActivityType.EXERCISING;
            default:
                return BabyActivityType.OTHER;
        }
    }
    public void storeBabyActivity(double amount, boolean check, String selectedCategory, String notes) {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in.");
            return;
        }

        DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());


        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
        BabyActivity babyActivity;
        if (amount != 0) {
            babyActivity = new BabyActivity(UUID.randomUUID().toString(), currentUser.getUid(), amount, mapBabyActivityType(selectedCategory), notes);
        } else {
            babyActivity = new BabyActivity(UUID.randomUUID().toString(), currentUser.getUid(), check, mapBabyActivityType(selectedCategory), notes);
        }
            Map<String, Object> txMap = new HashMap<>();
            txMap.put("id", babyActivity.getId());
            txMap.put("userId", babyActivity.getUserId());
            txMap.put("category", babyActivity.getCategory());
            switch (babyActivity.getCategory()){
                case "SLEEPING":
                case "EXERCISING":
                    txMap.put("duration", babyActivity.getDuration());
                    break;
                case "EATING":
                    txMap.put("intake", babyActivity.getIntake());
                    break;
                case "POOPING":
                    txMap.put("poop_check", babyActivity.getPoop_check());
                    break;
                case "VITAMINS":
                    txMap.put("vitamin_check", babyActivity.getVitamin_check());
                    break;
                default:
                    Log.i(TAG, "Invalid input");
            }
            txMap.put("timestamp", FieldValue.serverTimestamp());
            txMap.put("notes", babyActivity.getNotes());

            DocumentReference txRef = db.collection("users").document(currentUser.getUid()).collection("babyActivities").document(babyActivity.getId());

            db.collection("users").document(currentUser.getUid()).collection("babyActivities").document(babyActivity.getId()).set(txMap).addOnSuccessListener(v -> {
                Log.i(TAG, "Baby activity added");

            }).addOnFailureListener(e -> {
                Log.e(TAG, "BabyActivity failed ", e);
                errorLiveData.setValue("BabyActivity failed!");
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load user data");
            errorLiveData.setValue("Failed to load user data.");
        });
    }

    public LiveData<Boolean> deleteBabyActivity(String babyActivityId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            result.setValue(false);
            return result;
        }

        String userId = currentUser.getUid();
        DocumentReference txRef = db.collection("users")
                .document(userId)
                .collection("babyActivities")
                .document(babyActivityId);

        txRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                result.setValue(false);
                return;
            }

            BabyActivity babyActivity = task.getResult().toObject(BabyActivity.class);
            if (babyActivity == null) {
                result.setValue(false);
                return;
            }

            WriteBatch batch = db.batch();

            DocumentReference userRef = db.collection("users").document(userId);
            batch.delete(txRef);

            batch.commit()
                    .addOnSuccessListener(unused -> {
                        getBabyActivities();
                        getUserProfile();
                        result.setValue(true);
                    })
                    .addOnFailureListener(e -> {
                        result.setValue(false);
                    });
        });

        return result;
    }


    @Override
    protected void onCleared() {
        super.onCleared();

        if (userListener != null) userListener.remove();
        if (babyActivitiesListener != null) babyActivitiesListener.remove();
    }
}

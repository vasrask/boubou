package com.vasrask.boubou.views;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vasrask.boubou.BabyActivityAdapter;
import com.vasrask.boubou.entities.BabyActivity;
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
import java.util.List;

public class BabyActivitiesViewModel extends ViewModel {


    private final MutableLiveData<List<BabyActivity>> babyActivities = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final String TAG = "BabyActivitiesViewModel";
    private ListenerRegistration babyActivitiesListener;

    private BabyActivityAdapter babyActivityAdapter;

    public BabyActivitiesViewModel() {
        loadAllBabyActivities();
    }

    public LiveData<List<BabyActivity>> getBabyActivities() {
        return babyActivities;
    }

    public LiveData<String> getErrorLiveData() {
        return this.errorLiveData;
    }


    public void loadAllBabyActivities() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        babyActivitiesListener = db.collection("users").document(currentUser.getUid()).collection("babyActivities").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed ", error);
                        return;
                    }
                    if (value == null || value.isEmpty()) {
                        babyActivities.setValue(new ArrayList<>());
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


    /**
     * If limit is 0, all babyActivities will be fetched, default case is order by timestamp (Firestore timestamp) by descending order (Newest first)
     *
     * @param field
     * @param ascending
     */
    public void fetchSortedBabyActivities(String field, boolean ascending, int limit) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        Log.d(TAG, "Fetching sorted babyActivities");

        Query.Direction direction = ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;

        Query query = db.collection("users").document(currentUser.getUid())
                .collection("babyActivities")
                .orderBy(field, direction);

        if (limit > 0) {
            query = query.limit(limit);
        }

        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BabyActivity> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        BabyActivity t = doc.toObject(BabyActivity.class);
                        if (t != null) list.add(t);
                    }

                    Log.d(TAG, "Filtered babyActivities " + list.toString());

                    babyActivities.setValue(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading sorted babyActivities ", e);
                    this.babyActivities.setValue(Collections.emptyList());
                    errorLiveData.setValue(e.getMessage());
                });
    }


    /**
     * Category can be  - SLEEP, MEDICINE, OTHER etc
     * We check in the database and filter out the  documents
     *
     * @param type
     */
    public void fetchBabyActivitiesByType(String type) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        Log.d(TAG, "Type is " + type);

        Query query = db.collection("users").document(currentUser.getUid()).collection("babyActivities")
                .whereEqualTo("baby_activity_type", type);


        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<BabyActivity> filteredList = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                BabyActivity t = doc.toObject(BabyActivity.class);
                if (t != null) {
                    t.setId(doc.getId());
                    filteredList.add(t);
                }
            }
            Log.d(TAG, "Filtered babyActivities: " + filteredList);
            babyActivities.setValue(filteredList);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to filter babyActivities ", e);
            this.errorLiveData.setValue(e.getMessage());
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
                        loadAllBabyActivities();
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
        if (babyActivitiesListener != null) {
            babyActivitiesListener.remove();
        }
    }
}

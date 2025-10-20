package com.vasrask.boubou.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vasrask.boubou.R;
import com.vasrask.boubou.BabyActivityAdapter;
import com.vasrask.boubou.views.BabyActivitiesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

public class BabyActivitiesFragment extends Fragment {

    private BabyActivitiesViewModel BabyActivitiesViewModel;
    private BabyActivityAdapter babyActivityAdapter;
    private AutoCompleteTextView filterMenu;
    private FloatingActionButton backTopButton;
    private RecyclerView recyclerView;
    private final String TAG = "BabyActivitiesFragment";

    public BabyActivitiesFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BabyActivitiesViewModel = new ViewModelProvider(this).get(BabyActivitiesViewModel.class);
        babyActivityAdapter = new BabyActivityAdapter();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_baby_activities, container, false);

        recyclerView = view.findViewById(R.id.filteredBabyActivitiesRecyclerView);

        setupFilterDropdown(view);

        setupViews(view);

        backTopButton = view.findViewById(R.id.scrollToTopButton);

        BabyActivitiesViewModel.getBabyActivities().observe(getViewLifecycleOwner(), BabyActivitiesList -> {
            Log.d(TAG, "All BabyActivities are : " + BabyActivitiesList.toString());

            babyActivityAdapter.setBabyActivities(BabyActivitiesList);
        });

        backTopButton.setOnClickListener(v -> recyclerView.scrollToPosition(0));

        // show / hide button on scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem > 3) {
                        backTopButton.show();
                    } else {
                        backTopButton.hide();
                    }
                }
            }
        });

        babyActivityAdapter.setOnDeleteListener(babyActivity -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete babyActivity")
                    .setMessage("Are you sure you want to delete this babyActivity ?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        BabyActivitiesViewModel.deleteBabyActivity(babyActivity.getId())
                                .observe(getViewLifecycleOwner(), success -> {
                                    if (success) {
                                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        return view;
    }

    private void setupViews(View view) {
        recyclerView = view.findViewById(R.id.filteredBabyActivitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(babyActivityAdapter);
        recyclerView.setHasFixedSize(true);
    }


    private void setupFilterDropdown(View view) {
        filterMenu = view.findViewById(R.id.filterMenu);
        String[] filterOptions = getResources().getStringArray(R.array.sort_array);

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, R.id.dropdownItemText, filterOptions);

        filterMenu.setAdapter(dropdownAdapter);

        filterMenu.setDropDownBackgroundResource(R.drawable.filter_dropdown_menu);

        filterMenu.setOnItemClickListener((parent, view1, position, id) -> {

            String selectedFilter = parent.getItemAtPosition(position).toString();

            Log.e(TAG, "Filter is " + selectedFilter);

            handleFilterSelection(selectedFilter);
        });
    }

    private void handleFilterSelection(String selectedFilter) {
        recyclerView.scrollToPosition(0);


        List<String> babyActivityCategories = Arrays.asList("SLEEPING", "EATING", "POOPING", "VITAMINS", "EXERCISING", "OTHER");

        String upperCaseFilter = selectedFilter.toUpperCase();

        if (babyActivityCategories.contains(upperCaseFilter)) {
            BabyActivitiesViewModel.fetchBabyActivitiesByType(upperCaseFilter);
            return;
        }

        String field = "timestamp";
        boolean ascending = selectedFilter.contains("↑");

        if (selectedFilter.contains("duration")) {
            field = "duration";
        }

        BabyActivitiesViewModel.fetchSortedBabyActivities(field, ascending, 0);
    }

    private void resetSortDropdown() {
        String[] sort = getResources().getStringArray(R.array.sort_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.sort_item,
                R.id.sortTextView,
                sort
        );

        filterMenu.setAdapter(adapter);
        filterMenu.setText("", false);
    }


    @Override
    public void onResume() {
        super.onResume();
        resetSortDropdown();

        BabyActivitiesViewModel.loadAllBabyActivities();
    }
}

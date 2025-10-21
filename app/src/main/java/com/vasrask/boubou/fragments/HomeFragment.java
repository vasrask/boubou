package com.vasrask.boubou.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vasrask.boubou.R;
import com.vasrask.boubou.BabyActivityAdapter;
import com.vasrask.boubou.views.HomeViewModel;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private TextView usernameTextView;
    private TextView welcomeMessage;
    private Button makeBabyActivityButton;
    private AutoCompleteTextView categoriesDropdown;
    private TextInputLayout categoriesMenu;
    private TextInputEditText notesTextInput;
    private LinearLayout dynamicInputContainer;
    private LinearLayout notesInputContainer;

    private RecyclerView recyclerView;
    private BabyActivityAdapter babyActivityAdapter;
    private final String TAG = "HomeFragment";
    private View activeInputView;
    private String activeCategory;
    private String feedingActiveType = "";


    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        babyActivityAdapter = new BabyActivityAdapter();
    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setupViews(view);

        setupUI(view);

        setupObservers();

        ConstraintLayout rootLayout = view.findViewById(R.id.homeRootLayout);

        categoriesDropdown.setDropDownBackgroundResource(R.drawable.filter_dropdown_menu);

        String[] categories = getResources().getStringArray(R.array.categories_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, R.id.dropdownItemText, categories);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categoriesDropdown.setOnItemClickListener((parent, _view, position, id) -> {
            String selectedCategory = (String) parent.getItemAtPosition(position);
            updateActivityInputField(selectedCategory);
        });

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets systemBars = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
                }

                // apply top inset as padding
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }

                return insets;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, insets) -> {

            int bottomInset = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                bottomInset = insets.getInsets(WindowInsets.Type.statusBars()).bottom;
            }
            int left = v.getPaddingLeft();
            int right = v.getPaddingRight();
            int top = v.getPaddingTop();

            v.setPadding(left, top, right, bottomInset);

            return insets;
        });

        makeBabyActivityButton.setOnClickListener(v -> onSubmitBabyActivity());

        makeBabyActivityButton.setText(R.string.add_baby_activity);
        categoriesMenu.setVisibility(View.VISIBLE);

        return view;
    }

    private double getDurationInMinutes() {
        String input = ((TextInputEditText) activeInputView).getText().toString().trim();
        Log.i(TAG, input);
        if (input.isEmpty()) return 0;

        double value;
        try {
            value = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 0;
        }

        return value * 60;
    }
    private void setupUI(View view) {

        recyclerView = view.findViewById(R.id.babyActivitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        usernameTextView = view.findViewById(R.id.usernameTextView);
        makeBabyActivityButton = view.findViewById(R.id.makeBabyActivityButton);
        welcomeMessage = view.findViewById(R.id.welcome);
        dynamicInputContainer = view.findViewById(R.id.dynamicInputContainer);
        notesInputContainer = view.findViewById(R.id.notesInputContainer);
        categoriesMenu = view.findViewById(R.id.categoriesMenu);
        categoriesDropdown = view.findViewById(R.id.categoriesDropdown);
        categoriesDropdown.setDropDownBackgroundResource(R.drawable.filter_dropdown_menu);
    }

    private void setupViews(View view) {

        recyclerView = view.findViewById(R.id.babyActivitiesRecyclerView);
        recyclerView.setAdapter(babyActivityAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    @SuppressLint("DefaultLocale")
    public void setupObservers() {
        homeViewModel.getBabyActivities().observe(getViewLifecycleOwner(), BabyActivities -> {
            Log.d(TAG, "BabyActivities updated: " + BabyActivities.size());
            babyActivityAdapter.setBabyActivities(BabyActivities);
            babyActivityAdapter.notifyDataSetChanged();

            if (!BabyActivities.isEmpty()) {
                recyclerView.scrollToPosition(0);
            }
        });

        homeViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            usernameTextView.setText(user.getUsername());
            welcomeMessage.setText("Welcome");
            welcomeMessage.setTextColor(Color.CYAN);
        });

        homeViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        babyActivityAdapter.setOnDeleteListener(babyActivity -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete babyActivity")
                    .setMessage("Are you sure you want to delete this babyActivity ?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        homeViewModel.deleteBabyActivity(babyActivity.getId())
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
    }

    private void updateActivityInputField(String category) {
        dynamicInputContainer.removeAllViews();
        notesInputContainer.removeAllViews();
        dynamicInputContainer = requireView().findViewById(R.id.dynamicInputContainer);
        dynamicInputContainer.removeAllViews();
        notesInputContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        activeCategory = category;


        switch (category) {
            case "Sleep":
            case "Playtime":
                TextInputLayout activityTextInputLayout = (TextInputLayout) inflater.inflate(R.layout.input_metric_layout, dynamicInputContainer, false);
                activityTextInputLayout.setHint("Duration (hours)");
                dynamicInputContainer.addView(activityTextInputLayout);
                activeInputView = activityTextInputLayout.findViewById(R.id.metricInput);
                break;
            case "Feeding":
                Spinner feedingTypeDropdown = new Spinner(requireContext());
                ArrayAdapter<CharSequence> feedingAdapter = ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.feeding_types_array,
                        android.R.layout.simple_spinner_dropdown_item
                );
                feedingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                feedingTypeDropdown.setAdapter(feedingAdapter);
                dynamicInputContainer.addView(feedingTypeDropdown);
               feedingTypeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                   @Override
                   public void onItemSelected(AdapterView<?> parent, View _view, int position, long id) {
                       String selectedFeedingType = parent.getItemAtPosition(position).toString();
                       feedingActiveType = selectedFeedingType;
                      if (dynamicInputContainer.getChildCount() > 1) {
                           dynamicInputContainer.removeViewAt(1);
                       }

                       LayoutInflater inflater2 = LayoutInflater.from(requireContext());
                       TextInputLayout feedingTextinputLayout = (TextInputLayout) inflater2.inflate(
                               R.layout.input_metric_layout, dynamicInputContainer, false);

                       if (selectedFeedingType.equals("Breastfeeding")) {
                           feedingTextinputLayout.setHint("Duration (minutes)");
                       } else {
                           feedingTextinputLayout.setHint("Intake (ml)");
                       }

                       dynamicInputContainer.addView(feedingTextinputLayout);
                       activeInputView = feedingTextinputLayout.findViewById(R.id.metricInput);
                   }

                   @Override
                   public void onNothingSelected(AdapterView<?> parent) {

                   }
               });
                break;
            case "Diaper Change":
                MaterialSwitch diaperSwitch = new MaterialSwitch(requireContext());
                diaperSwitch.setText("Did baby change diaper?");
                dynamicInputContainer.addView(diaperSwitch);
                activeInputView = diaperSwitch;
                break;
            case "Medicine":
                MaterialSwitch medicineSwitch = new MaterialSwitch(requireContext());
                medicineSwitch.setText("Did baby take medicine?");
                dynamicInputContainer.addView(medicineSwitch);
                activeInputView = medicineSwitch;
                break;
            default:
                TextInputLayout otherTextInputLayout = (TextInputLayout) inflater.inflate(R.layout.input_metric_layout, dynamicInputContainer, false);
                otherTextInputLayout.setHint("Other");
                dynamicInputContainer.addView(otherTextInputLayout);
                activeInputView = otherTextInputLayout.findViewById(R.id.metricInput);
                break;
        }
        TextInputLayout notesLayout = (TextInputLayout) inflater.inflate(
                R.layout.input_notes_layout, notesInputContainer, false);
        notesLayout.setHint("Notes");
        notesInputContainer.addView(notesLayout);
        notesTextInput = notesLayout.findViewById(R.id.notesEditText);
    }
    private void onSubmitBabyActivity() {

        String selectedCategory = activeCategory;
        if (selectedCategory == null || activeInputView == null) {
            Toast.makeText(requireContext(), "Please select an activity.", Toast.LENGTH_SHORT).show();
            return;
        }
        String notes = notesTextInput.getText().toString();

        String feedingType = feedingActiveType;
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("category", selectedCategory);
        if (!feedingType.isEmpty()) {
            activityData.put("feeding_category", feedingType);
        }
        activityData.put("notes", notes);
        activityData.put("timestamp", System.currentTimeMillis());

        switch (selectedCategory) {
            case "Sleep":
            case "Playtime":
                double duration = getDurationInMinutes();
                activityData.put("amount", duration);
                activityData.put("unit", "minutes");
                homeViewModel.storeBabyActivity(duration, false, selectedCategory, "", notes);
                break;
            case "Feeding":
                if (feedingType.equals("Pumped Breast Milk") || feedingType.equals("Formula")) {
                    double intake = Double.parseDouble(((TextInputEditText) activeInputView).getText().toString().trim());
                    activityData.put("amount", intake);
                    activityData.put("unit", "ml");
                    homeViewModel.storeBabyActivity(intake, false, selectedCategory, feedingType, notes);
                } else if (feedingType.equals("Breastfeeding")) {
                    double feeding_duration = Double.parseDouble(((TextInputEditText) activeInputView).getText().toString().trim());
                    activityData.put("amount", feeding_duration);
                    activityData.put("unit", "m");
                    homeViewModel.storeBabyActivity(feeding_duration, false, selectedCategory, feedingType, notes);
                }
                break;
            case "Diaper Change":
            case "Medicine":
                if (dynamicInputContainer.getChildCount() > 0) {
                    MaterialSwitch checkSwitch = (MaterialSwitch) dynamicInputContainer.getChildAt(0);
                    activityData.put("check", checkSwitch.isChecked());
                }
                homeViewModel.storeBabyActivity(0, true, selectedCategory, "", notes);
                break;
        }
        notesTextInput.setText("");
        categoriesDropdown.setText("", false);
        dynamicInputContainer.removeAllViews();
        notesInputContainer.removeAllViews();
        recyclerView.scrollToPosition(0);

        Toast.makeText(requireContext(), "Baby activity added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetCategoriesDropdown();
    }


    private void resetCategoriesDropdown() {
        String[] categories = getResources().getStringArray(R.array.categories_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, R.id.dropdownItemText, categories);

        categoriesDropdown.setAdapter(adapter);
        categoriesDropdown.setText("", false);
    }

    public void onDeleteBabyActivity(String babyActivityId) {
        homeViewModel.deleteBabyActivity(babyActivityId).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "BabyActivity deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to delete babyActivity", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

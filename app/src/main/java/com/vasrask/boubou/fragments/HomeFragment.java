package com.vasrask.boubou.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
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
import com.vasrask.boubou.entities.BabyActivityType;
import com.vasrask.boubou.entities.FeedingType;
import com.vasrask.boubou.views.HomeViewModel;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
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
        babyActivityAdapter = new BabyActivityAdapter(requireContext());
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
            String selectedCategory = parent.getItemAtPosition(position).toString();
            updateActivityInputField(BabyActivityType.fromString(selectedCategory));
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

        makeBabyActivityButton = view.findViewById(R.id.makeBabyActivityButton);
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


        homeViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        babyActivityAdapter.setOnDeleteListener(babyActivity -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete babyActivity")
                    .setMessage(getString(R.string.confirm_delete))
                    .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                        homeViewModel.deleteBabyActivity(babyActivity.getId())
                                .observe(getViewLifecycleOwner(), success -> {
                                    if (success) {
                                        Toast.makeText(requireContext(), getString(R.string.successful_delete), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), getString(R.string.failed_delete), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });
    }

    private void updateActivityInputField(BabyActivityType category) {
        dynamicInputContainer.removeAllViews();
        notesInputContainer.removeAllViews();
        dynamicInputContainer = requireView().findViewById(R.id.dynamicInputContainer);
        dynamicInputContainer.removeAllViews();
        notesInputContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        activeCategory = getString(category.getId());


        switch (category) {
            case SLEEP:
            case PLAYTIME:
                TextInputLayout activityTextInputLayout = (TextInputLayout) inflater.inflate(R.layout.input_metric_layout, dynamicInputContainer, false);
                activityTextInputLayout.setHint(getString(R.string.duration) + " (" + getString(R.string.hours) +")");
                dynamicInputContainer.addView(activityTextInputLayout);
                activeInputView = activityTextInputLayout.findViewById(R.id.metricInput);
                break;
            case FEEDING:
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
                       feedingActiveType = parent.getItemAtPosition(position).toString();
                      if (dynamicInputContainer.getChildCount() > 1) {
                           dynamicInputContainer.removeViewAt(1);
                       }

                       LayoutInflater inflater2 = LayoutInflater.from(requireContext());
                       TextInputLayout feedingTextinputLayout = (TextInputLayout) inflater2.inflate(
                               R.layout.input_metric_layout, dynamicInputContainer, false);

                       if (feedingActiveType.equals(getString(R.string.breastfeeding))) {
                           feedingTextinputLayout.setHint(getString(R.string.duration) + " (" + getString(R.string.minutes) +")");
                       } else {
                           feedingTextinputLayout.setHint(getString(R.string.intake) + " (ml)");
                       }

                       dynamicInputContainer.addView(feedingTextinputLayout);
                       activeInputView = feedingTextinputLayout.findViewById(R.id.metricInput);
                   }

                   @Override
                   public void onNothingSelected(AdapterView<?> parent) {

                   }
               });
                break;
            case DIAPER_CHANGE:
                MaterialSwitch diaperSwitch = new MaterialSwitch(requireContext());
                diaperSwitch.setText(getString(R.string.diaper_change_question));
                dynamicInputContainer.addView(diaperSwitch);
                activeInputView = diaperSwitch;
                break;
            case MEDICINE:
                MaterialSwitch medicineSwitch = new MaterialSwitch(requireContext());
                medicineSwitch.setText(getString(R.string.medicine_question));
                dynamicInputContainer.addView(medicineSwitch);
                activeInputView = medicineSwitch;
                break;
            default:
                TextInputLayout otherTextInputLayout = (TextInputLayout) inflater.inflate(R.layout.input_metric_layout, dynamicInputContainer, false);
                otherTextInputLayout.setHint(getString(R.string.other));
                dynamicInputContainer.addView(otherTextInputLayout);
                activeInputView = otherTextInputLayout.findViewById(R.id.metricInput);
                break;
        }
        TextInputLayout notesLayout = (TextInputLayout) inflater.inflate(
                R.layout.input_notes_layout, notesInputContainer, false);
        notesLayout.setHint(getString(R.string.notes));
        notesInputContainer.addView(notesLayout);
        notesTextInput = notesLayout.findViewById(R.id.notesEditText);
    }
    private void onSubmitBabyActivity() {


        BabyActivityType selectedCategory = BabyActivityType.fromString(activeCategory);
        if (selectedCategory == null || activeInputView == null) {
            Toast.makeText(requireContext(), "Please select an activity.", Toast.LENGTH_SHORT).show();
            return;
        }
        String notes = notesTextInput.getText().toString();

        Map<String, Object> activityData = new HashMap<>();
        activityData.put("baby_activity_type", selectedCategory);
        FeedingType selectedFeedingType = FeedingType.fromString(feedingActiveType);

        if (selectedFeedingType != null) {
            activityData.put("feeding_type", selectedFeedingType);
        }
        activityData.put("notes", notes);
        activityData.put("timestamp", System.currentTimeMillis());

        switch (selectedCategory) {
            case SLEEP:
            case PLAYTIME:
                double duration = getDurationInMinutes();
                activityData.put("amount", duration);
                activityData.put("unit", "minutes");
                homeViewModel.storeBabyActivity(duration, false, selectedCategory, FeedingType.NO_FEEDING, notes);
                break;
            case FEEDING:
                if (selectedFeedingType == FeedingType.PUMPED_BREAST_MILK|| selectedFeedingType == FeedingType.FORMULA) {
                    double intake = Double.parseDouble(((TextInputEditText) activeInputView).getText().toString().trim());
                    activityData.put("amount", intake);
                    activityData.put("unit", "ml");
                    homeViewModel.storeBabyActivity(intake, false, selectedCategory, selectedFeedingType, notes);
                } else if (selectedFeedingType == FeedingType.BREASTFEEDING) {
                    double feeding_duration = Double.parseDouble(((TextInputEditText) activeInputView).getText().toString().trim());
                    activityData.put("amount", feeding_duration);
                    activityData.put("unit", "m");
                    Log.d("FEEDING", "here");
                    homeViewModel.storeBabyActivity(feeding_duration, false, selectedCategory, selectedFeedingType, notes);
                }
                break;
            case DIAPER_CHANGE:
            case MEDICINE:
                if (dynamicInputContainer.getChildCount() > 0) {
                    MaterialSwitch checkSwitch = (MaterialSwitch) dynamicInputContainer.getChildAt(0);
                    activityData.put("check", checkSwitch.isChecked());
                }
                homeViewModel.storeBabyActivity(0, true, selectedCategory, FeedingType.NO_FEEDING, notes);
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

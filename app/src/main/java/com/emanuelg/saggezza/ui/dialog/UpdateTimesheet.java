package com.emanuelg.saggezza.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.emanuelg.saggezza.DateValidatorWeekdays;
import com.emanuelg.saggezza.R;
import com.emanuelg.saggezza.TimesheetApi;
import com.emanuelg.saggezza.model.Project;
import com.emanuelg.saggezza.model.Task;
import com.emanuelg.saggezza.model.Timesheet;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public class UpdateTimesheet extends DialogFragment {

    private static final String TAG = "UpdateTimesheet";
    private final TimesheetApi api = TimesheetApi.getInstance();
    MaterialAutoCompleteTextView autocompleteProject;
    MaterialAutoCompleteTextView autocompleteTask;
    private Toolbar toolbar;
    LinearProgressIndicator progressIndicator;
    private EditText inputDatePicker;
    private EditText inputHours;
    private Project selectedProject;
    private Task selectedTask;
    private long startDate;
    private Long endDate;
    Timesheet current;
    int index;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.Theme_MaterialComponents_DayNight_Dialog);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            current = bundle.getParcelable("Timesheet");
            index = bundle.getInt("index");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.SaggezzaTimesheets_Slide);
        }

        selectedProject = current.getProject();
        selectedTask = current.getTask();
        startDate = current.getStartDate();
        endDate = current.getEndDate();
        progressIndicator.setVisibility(View.GONE);
        autocompleteProject.setOnItemClickListener((parentView, view, position, id) -> {
            Object item = parentView.getItemAtPosition(position);
            if (item instanceof Project)
            {
                autocompleteProject.setError(null);
                //dropdownTask.setEnabled(true);
                selectedProject = (Project) parentView.getItemAtPosition(position);
                if(api.getTasksByProjectId(selectedProject.getId()).size() == 0)
                {
                    autocompleteTask.setError("No Tasks Available");
                }else{
                    ArrayAdapter<Task> currentTaskAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, api.getTasksByProjectId(selectedProject.getId()));
                    autocompleteTask.setAdapter(currentTaskAdapter);
                    autocompleteTask.setEnabled(true);
                }
            }else throw new RuntimeException();
        });

        autocompleteTask.setOnItemClickListener((parent, view, position, id) ->
        {
            autocompleteTask.setError(null);
            // dropdownTask.setError(null);
            selectedTask = (Task) parent.getItemAtPosition(position);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_update_timesheet, container, false);

            toolbar = view.findViewById(R.id.toolbar);

            progressIndicator = view.findViewById(R.id.linearProgressIndicator_dialog);

            progressIndicator.setVisibility(View.VISIBLE);
            if(current != null)
            {
                autocompleteProject= view.findViewById(R.id.autoCompleteProject);
                autocompleteTask= view.findViewById(R.id.autoCompleteTask);
                UpdateFields(view, current);
            }


            return view;
        }

    private void UpdateFields(View view, Timesheet current) {
        //region Variable Declarations

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        final MaterialDatePicker<Pair<Long, Long>> materialDatePicker;
        inputDatePicker = view.findViewById(R.id.inputDateRange);
        inputHours = view.findViewById(R.id.inputHours);
        //endregion

        //region Project and Task handling
        autocompleteProject.setText(current.getProject().getName());
        autocompleteTask.setText(current.getTask().getName());


        ArrayAdapter<Project> pAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, api.getMyProjectsList());
        ArrayAdapter<Task> tAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, api.getMyTasksList());

        // Specify the layout to use when the list of choices appears
        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the array adapter to the dropdown
        autocompleteProject.setAdapter(pAdapter);
        autocompleteTask.setAdapter(tAdapter);


        autocompleteProject.setOnDismissListener(() -> autocompleteTask.getText().clear());

        //endregion

        //region Date Range Picker handling

        //Calendar Constraints to limit the range to weekdays only
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder();
        constraintBuilder.setValidator(new DateValidatorWeekdays());

        builder.setTitleText("SELECT A DATE RANGE");
        builder.setSelection(new Pair<>(current.getStartDate(), current.getEndDate()));
        builder.setCalendarConstraints(constraintBuilder.build());
        materialDatePicker = builder.build();

        inputDatePicker.setText(current.getTxtDateRange());

        inputDatePicker.setOnClickListener(v -> {
            inputDatePicker.setEnabled(false);
            materialDatePicker.show(requireActivity().getSupportFragmentManager(),"DATE_PICKER");
        });

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            assert selection.first != null;
            assert selection.second != null;
            long difference = ChronoUnit.DAYS.between(toLocalDateTime(selection.first),toLocalDateTime(selection.second));
            if(difference>=6)
            {
                inputDatePicker.setError("Invalid Selection");
            }
            else
            {
                startDate = selection.first;
                endDate = selection.second;
                inputDatePicker.setText(materialDatePicker.getHeaderText());
                inputDatePicker.setEnabled(true);
            }
        });

        //endregion

        //region Input Hours
        inputHours.setText(current.getHours());
        inputHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(!s.toString().trim().isEmpty() && Integer.parseInt(s.toString()) > 40)
                {
                    inputHours.setError("Invalid Input");
                    inputHours.getText().clear();
                }
            }
        });
        //endregion
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setNavigationOnClickListener(v -> dismiss());

        toolbar.setTitle("Submit your timesheet");

        toolbar.setCollapseIcon(R.drawable.ic_close_24);

        toolbar.setOnMenuItemClickListener(item -> {

            if(item.getItemId() == R.id.action_save)
            {
                updateTimesheet();
            }
            dismiss();
            return true;
        });
    }

    private void updateTimesheet() {
        progressIndicator.setVisibility(View.VISIBLE);
        if(isValid())
        {
            current.setProjectId(selectedProject.getId());
            current.setHours(inputHours.getText().toString());
            current.setTaskId(selectedTask.getId());
            current.setTxtDateRange(inputDatePicker.getText().toString());
            current.setDateRange(toLocalDateTime(startDate).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + toLocalDateTime(endDate).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            current.setStartDate(startDate);
            current.setEndDate(endDate);
            current.setProjectRef(selectedProject.getDocReference());
            current.setTaskRef(selectedTask.getDocReference());
            current.setProject(selectedProject);
            current.setTask(selectedTask);

            Submit(current);
        }else{
            Toast.makeText(getContext(), "Make sure all fields are filled!", Toast.LENGTH_LONG).show();
            progressIndicator.setVisibility(View.GONE);
        }
    }

    private void Submit(Timesheet current) {
        DocumentReference ref = FirebaseFirestore.getInstance().collection("Timesheets").document(current.getId());
        if (    !TextUtils.isEmpty(current.getHours()) && !TextUtils.isEmpty(current.getUid())
                && !TextUtils.isEmpty(current.getTxtDateRange()) && !TextUtils.isEmpty(current.getDateRange())
                && !TextUtils.isEmpty(current.getProjectId()) && !TextUtils.isEmpty(current.getTaskId())
                &&  current.getProjectRef()!= null && current.getTaskRef()!=null
        ){
            ref.update(
                    "projectId", current.getProjectId(),
                    "hours", current.getHours(),
                    "taskId", current.getTaskId(),
                    "txtDateRange", current.getTxtDateRange(),
                    "dateRange" , current.getDateRange(),
                    "startDate", current.getStartDate(),
                    "endDate", current.getEndDate(),
                    "projectRef", current.getProjectRef(),
                    "taskRef", current.getTaskRef()
            ).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Entry successfully updated!", Toast.LENGTH_LONG).show();
                api.getTimesheetList().set(index,current);
            });
        }
    }

    private boolean isValid() {
        if(TextUtils.isEmpty(autocompleteProject.getText()))
        {
            autocompleteProject.setError("Empty field");
            return false;
        }
        if(TextUtils.isEmpty(autocompleteTask.getText()))
        {
            autocompleteTask.setError("Empty field");
            return false;
        }
        if(TextUtils.isEmpty(inputDatePicker.getText()))
        {
            inputDatePicker.setError("Empty field");
            return false;
        }
        if(TextUtils.isEmpty(inputHours.getText()))
        {
            inputHours.setError("Empty field");
            return false;
        }

        return true;
    }

    //note that week starts with Monday
    public static LocalDateTime startOfWeek() {
        return LocalDateTime.now(ZoneId.systemDefault())
                .with(LocalTime.MIN)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static long toMills(final LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.of("GMT")).toInstant().toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(final Long epoch)
    {
        return Instant.ofEpochMilli(epoch).atZone(ZoneId.of("GMT")).toLocalDateTime();
    }
    //note that week ends with Friday
    public static LocalDateTime endOfWeek() {
        return startOfWeek()
                .with(LocalTime.MAX)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();
    }
}


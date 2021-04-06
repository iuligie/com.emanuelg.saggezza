package com.emanuelg.saggezza;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;

import com.emanuelg.saggezza.model.Employee;
import com.emanuelg.saggezza.model.Project;
import com.emanuelg.saggezza.model.Task;
import com.emanuelg.saggezza.model.Timesheet;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.TimeZone;

public class AddTimesheetActivity extends AppCompatActivity {

    private Project selectedProject;
    private Task selectedTask;
    private long startDate;
    private Long endDate;
    private AutoCompleteTextView autocompleteProject;
    private AutoCompleteTextView autocompleteTask;
    private TextInputLayout dropdownTask;
    private EditText inputDatePicker;
    private EditText inputHours;
    private LinearProgressIndicator progressIndicator;
    private final TimesheetApi api = TimesheetApi.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timesheet);
        progressIndicator = findViewById(R.id.linearProgressIndicator_AddTimesheet);
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        final MaterialDatePicker<Pair<Long, Long>> materialDatePicker;
        autocompleteProject = findViewById(R.id.autoCompleteProject);
        autocompleteTask = findViewById(R.id.autoCompleteTask);
        dropdownTask = findViewById(R.id.dropdownTask);
        inputDatePicker = findViewById(R.id.inputDateRange);
        inputHours = findViewById(R.id.inputHours);

        inputHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals("") && Integer.parseInt(s.toString()) > 40)
                {
                    inputHours.setError("Invalid Input");
                    inputHours.getText().clear();
                }
            }
        });

        ActionBar toolbar= getSupportActionBar();
        toolbar.setTitle("Submit your timesheet");
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setHomeButtonEnabled(true);
        toolbar.setHomeAsUpIndicator(R.drawable.ic_close_24);


        //CalendarConstraints
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder();
        constraintBuilder.setValidator(new DateValidatorWeekdays());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.clear();

        long today = MaterialDatePicker.todayInUtcMilliseconds();
        calendar.setTimeInMillis(today);

        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        long january = calendar.getTimeInMillis();

        calendar.set(Calendar.MONTH, Calendar.MARCH);
        long march = calendar.getTimeInMillis();

        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        long december = calendar.getTimeInMillis();

        //region Material Date Picker
        Long startOfCurrentWeek = toMills(startOfWeek());
        Long endOfCurrentWeek = toMills(endOfWeek());
        //MaterialDatePicker

        builder.setTitleText("SELECT A DATE RANGE");
        builder.setSelection(new Pair<>(startOfCurrentWeek, endOfCurrentWeek));
        builder.setCalendarConstraints(constraintBuilder.build());
        materialDatePicker = builder.build();

        inputDatePicker.setOnClickListener(v -> {
            inputDatePicker.setEnabled(false);
            materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");
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



        ArrayAdapter<Project> pAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, api.getMyProjectsList());
        ArrayAdapter<Task> tAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, api.getMyTasksList());

        // Specify the layout to use when the list of choices appears
        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the array adapter to the dropdown
        autocompleteProject.setAdapter(pAdapter);
        autocompleteTask.setAdapter(tAdapter);

        // Disable task dropdown when no projects are selected
        autocompleteTask.setEnabled(false);
        dropdownTask.setEnabled(false);

        if(api.getMyProjectsList().size() == 0) {
            autocompleteProject.setError("No Projects Available");
            autocompleteTask.setError("No Tasks Available");
        }
        autocompleteProject.setOnItemClickListener((parentView, view, position, id) -> {
            Object item = parentView.getItemAtPosition(position);
            if (item instanceof Project)
            {
                autocompleteProject.setError(null);
                dropdownTask.setEnabled(true);
                selectedProject = (Project) parentView.getItemAtPosition(position);
                if(api.getTasksByProjectId(selectedProject.getId()).size() == 0)
                {
                    autocompleteTask.setError("No Tasks Available");
                }else{
                    ArrayAdapter<Task> currentTaskAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, api.getTasksByProjectId(selectedProject.getId()));
                    autocompleteTask.setAdapter(currentTaskAdapter);
                    autocompleteTask.setEnabled(true);
                }
            }else throw new RuntimeException();
        });

        autocompleteTask.setOnItemClickListener((parent, view, position, id) ->
        {
            autocompleteTask.setError(null);
            dropdownTask.setError(null);
            selectedTask = (Task) parent.getItemAtPosition(position);
        });
        autocompleteProject.setOnDismissListener(() -> autocompleteTask.getText().clear());

   }

    private void Save() {
            progressIndicator.setVisibility(View.VISIBLE);
            if(isValid())
            {
                Timesheet current= new Timesheet();
                current.setProjectId(selectedProject.getId());
                current.setHours(inputHours.getText().toString());
                current.setTaskId(selectedTask.getId());
                current.setTxtDateRange(inputDatePicker.getText().toString());
                current.setDateRange(toLocalDateTime(startDate).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + toLocalDateTime(endDate).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                current.setStartDate(startDate);
                current.setEndDate(endDate);
                current.setApprovedOn(Timestamp.now());
                current.setSubmittedOn(Timestamp.now());
                current.setUid(Employee.getInstance().getAccount().getUid());
                current.setApprovedBy(Employee.getInstance().getSupervisorId());
                current.setProjectRef(selectedProject.getDocReference());
                current.setTaskRef(selectedTask.getDocReference());
                current.setProject(selectedProject);
                current.setTask(selectedTask);
                current.setOnTime(LocalDateTime.now().isBefore(toLocalDateTime(endDate).with(LocalTime.MAX)));

                Submit(current);
            }else{
                Toast.makeText(this, "Make sure all fields are filled!", Toast.LENGTH_LONG).show();
                progressIndicator.setVisibility(View.GONE);
            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dialog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            //Toast.makeText(this, "Submission was successful", Toast.LENGTH_LONG).show();
        Save();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        progressIndicator.setVisibility(View.VISIBLE);
        return super.onSupportNavigateUp();
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

    private void Submit(Timesheet current) {
        CollectionReference ref = FirebaseFirestore.getInstance().collection("Timesheets");
        if (    !TextUtils.isEmpty(current.getHours()) && !TextUtils.isEmpty(current.getUid())
             && !TextUtils.isEmpty(current.getTxtDateRange()) && !TextUtils.isEmpty(current.getDateRange())
             && !TextUtils.isEmpty(current.getProjectId()) && !TextUtils.isEmpty(current.getTaskId())
             &&  current.getProjectRef()!= null && current.getTaskRef()!=null
        )
        {
            ref.add(current)
                    .addOnSuccessListener(documentReference ->
                    {
                        current.setId(documentReference.getId());
                        TimesheetApi.getInstance().getTimesheetList().add(0,current);
                    })
                    .addOnFailureListener(e -> Log.d("TAG", "OnFailure" + e.getMessage()));

            Employee.getInstance().incrementScore(current.isOnTime());

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        }else{
            throw new RuntimeException("Null attribute was found");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        progressIndicator.setVisibility(View.GONE);
    }
}
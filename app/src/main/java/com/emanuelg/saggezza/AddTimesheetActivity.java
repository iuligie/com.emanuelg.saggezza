package com.emanuelg.saggezza;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.emanuelg.saggezza.model.Employee;
import com.emanuelg.saggezza.model.Project;
import com.emanuelg.saggezza.model.Task;
import com.emanuelg.saggezza.model.Timesheet;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AddTimesheetActivity extends AppCompatActivity {

    Project selectedProject;
    Task selectedTask;
    final TimesheetApi api = TimesheetApi.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timesheet);
        final Calendar cal = Calendar.getInstance();
        EditText beginDatePicker = findViewById(R.id.inputDateRange);
        Button btnSubmitTimesheet = findViewById(R.id.btnSubmitTimesheet);
        EditText inputHours = findViewById(R.id.inputHours);

        //region Material Date Picker
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

        Long startOfCurrentWeek = toMills(startOfWeek());
        Long endOfCurrentWeek = toMills(endOfWeek());

        //CalendarConstraints
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder();
        constraintBuilder.setValidator(new DateValidatorWeekdays());
        //MaterialDatePicker
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("SELECT A DATE RANGE");
        builder.setSelection(new Pair(startOfCurrentWeek,endOfCurrentWeek));
        //builder.setSelection(today);
        builder.setCalendarConstraints(constraintBuilder.build());
        final MaterialDatePicker materialDatePicker = builder.build();

        beginDatePicker.setOnClickListener(v -> {
            beginDatePicker.setEnabled(false);
            materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");
        });


        materialDatePicker.addOnPositiveButtonClickListener(selection -> beginDatePicker.setText(materialDatePicker.getHeaderText()));
        //endregion

        /*DatePickerDialog.OnDateSetListener beginDate = (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH,month);
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateLabel(beginDatePicker,cal);
        };*/


        AutoCompleteTextView autocompleteProject = findViewById(R.id.autoCompleteProject);
        AutoCompleteTextView autocompleteTask = findViewById(R.id.autoCompleteTask);
        TextInputLayout dropdownProject = findViewById(R.id.dropdownProject);
        TextInputLayout dropdownTask = findViewById(R.id.dropdownTask);

        ArrayAdapter<Project> pAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, api.getMyProjectsList());
        ArrayAdapter<Task> tAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, api.getMyTasksList());
        // Specify the layout to use when the list of choices appears
        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        // Apply the array adapter to the dropdown
        autocompleteProject.setAdapter(pAdapter);
        autocompleteTask.setAdapter(tAdapter);


        autocompleteTask.setEnabled(false);
        dropdownTask.setEnabled(false);

        autocompleteProject.setOnItemClickListener((parentView, view, position, id) -> {
            Object item = parentView.getItemAtPosition(position);
            if (item instanceof Project)
            {
                dropdownTask.setEnabled(true);
                selectedProject = (Project) parentView.getItemAtPosition(position);
                ArrayAdapter<Task> currentTaskAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, api.getTasksByProjectId(selectedProject.getId()));
                autocompleteTask.setAdapter(currentTaskAdapter);
                autocompleteTask.setEnabled(true);
            }else throw new RuntimeException();
        });

        autocompleteTask.setOnItemClickListener((parent, view, position, id) -> selectedTask = (Task) parent.getSelectedItem());

        autocompleteTask.setOnItemClickListener((parent, view, position, id) -> selectedTask = (Task) parent.getItemAtPosition(position));
        autocompleteProject.setOnDismissListener(() -> autocompleteTask.getText().clear());

        btnSubmitTimesheet.setOnClickListener(v -> {
            //progressBar.setVisibility(View.VISIBLE);
            Timesheet current= new Timesheet();
            current.setProjectId(selectedProject.getId());
            current.setHours(inputHours.getText().toString());
            current.setTaskId(selectedTask.getId());
            current.setBeginDate(beginDatePicker.getText().toString());
            //TODO change end date to actual end date
            current.setEndDate(beginDatePicker.getText().toString());
            current.setApprovedOn(Timestamp.now());
            current.setSubmittedOn(Timestamp.now());
            current.setUid(Employee.getInstance().getAccount().getUid());
            current.setApprovedBy(Employee.getInstance().getSupervisorId());
            current.setApprovedBy("ayWEHufN52UhWKHcbOHJ");
            current.setProjectRef(selectedProject.getDocReference());
            current.setTaskRef(selectedTask.getDocReference());
            current.setProject(selectedProject);
            current.setTask(selectedTask);
            Submit(current);
        });

    }
    //note that week starts with Monday
    public static LocalDateTime startOfWeek() {
        return LocalDateTime.now(ZoneId.of("GMT"))
                .with(LocalTime.MIN)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
    public static long toMills(final LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.of("GMT")).toInstant().toEpochMilli();
    }
    //note that week ends with Friday
    public static LocalDateTime endOfWeek() {
        return startOfWeek()
                .with(LocalTime.MAX)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
    }

    private void Submit(Timesheet current) {
        CollectionReference ref = FirebaseFirestore.getInstance().collection("Timesheets");
        if (!TextUtils.isEmpty(current.getHours()) && !TextUtils.isEmpty(current.getBeginDate()) && !TextUtils.isEmpty(current.getEndDate()))
            ref.add(current)
                    .addOnSuccessListener(documentReference ->
                    {
                        current.setId(documentReference.getId());
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Log.d("TAG","OnFailure"+ e.getMessage()));

        //progressBar.setVisibility(View.INVISIBLE);

    }
    private void updateLabel(EditText input, Calendar cal) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        input.setText(sdf.format(cal.getTime()));
    }
}
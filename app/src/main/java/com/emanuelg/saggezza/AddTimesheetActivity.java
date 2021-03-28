package com.emanuelg.saggezza;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.emanuelg.saggezza.model.Employee;
import com.emanuelg.saggezza.model.Project;
import com.emanuelg.saggezza.model.Task;
import com.emanuelg.saggezza.model.Timesheet;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTimesheetActivity extends AppCompatActivity {

    Project selectedProject;
    Task selectedTask;
    final TimesheetApi api = TimesheetApi.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timesheet);
        final Calendar cal = Calendar.getInstance();
        EditText beginDatePicker = findViewById(R.id.BeginDatePicker);
        EditText endDatePicker = findViewById(R.id.EndDatePicker);
        Button btnSubmitTimesheet = findViewById(R.id.btnSubmitTimesheet);
        EditText inputHours = findViewById(R.id.inputHours);

        DatePickerDialog.OnDateSetListener beginDate = (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH,month);
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateLabel(beginDatePicker,cal);
        };
        DatePickerDialog.OnDateSetListener endDate = (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH,month);
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateLabel(endDatePicker,cal);
        };
        beginDatePicker.setOnClickListener(v -> new DatePickerDialog(this, beginDate, cal
                .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show());
        endDatePicker.setOnClickListener(v -> new DatePickerDialog(this, endDate, cal
                .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show());

        Spinner spnProjects = findViewById(R.id.spnProjects);
        Spinner spnTasks = findViewById(R.id.spnTasks);

        AutoCompleteTextView autocompleteProject = findViewById(R.id.autoCompleteProject);
        AutoCompleteTextView autocompleteTask = findViewById(R.id.autoCompleteTask);
        TextInputLayout dropdownProject = findViewById(R.id.dropdownProject);
        TextInputLayout dropdownTask = findViewById(R.id.dropdownTask);

        ArrayAdapter<Project> pAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, api.getMyProjectsList());
        ArrayAdapter<Task> tAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, api.getMyTasksList());

        autocompleteProject.setAdapter(pAdapter);
        autocompleteTask.setAdapter(tAdapter);
        // Specify the layout to use when the list of choices appears
        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the arrAdapter to the snpProjects
        spnProjects.setAdapter(pAdapter);
        spnTasks.setAdapter(tAdapter);
        autocompleteTask.setEnabled(false);

        autocompleteProject.setOnItemClickListener((parentView, view, position, id) -> {
            Object item = parentView.getItemAtPosition(position);
            if (item instanceof Project)
            {
                selectedProject = (Project) parentView.getItemAtPosition(position);
                ArrayAdapter<Task> currentTaskAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, api.getTasksByProjectId(selectedProject.getId()));
                autocompleteTask.setAdapter(currentTaskAdapter);
                autocompleteTask.setEnabled(true);
            }else try {
                throw new Exception("Error when selecting a dropdown item");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        autocompleteTask.setOnItemClickListener((parent, view, position, id) -> selectedTask = (Task) parent.getSelectedItem());

        autocompleteProject.setOnDismissListener(() -> autocompleteTask.getText().clear());



        btnSubmitTimesheet.setOnClickListener(v -> {
            //progressBar.setVisibility(View.VISIBLE);
            Timesheet current= new Timesheet();
            current.setProjectId(selectedProject.getId());
            current.setHours(inputHours.getText().toString());
            current.setTaskId(selectedTask.getId());
            current.setBeginDate(beginDatePicker.getText().toString());
            current.setEndDate(endDatePicker.getText().toString());
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

    private void Submit(Timesheet current) {
        CollectionReference ref = FirebaseFirestore.getInstance().collection("Timesheets");
        if (!TextUtils.isEmpty(current.getHours()) && !TextUtils.isEmpty(current.getBeginDate()) && !TextUtils.isEmpty(current.getEndDate()))
            ref.add(current)
                    .addOnSuccessListener(documentReference -> current.setId(documentReference.getId())).addOnFailureListener(e -> Log.d("TAG","OnFailure"+ e.getMessage()));

        //progressBar.setVisibility(View.INVISIBLE);
    }
    private void updateLabel(EditText input, Calendar cal) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        input.setText(sdf.format(cal.getTime()));
    }
}
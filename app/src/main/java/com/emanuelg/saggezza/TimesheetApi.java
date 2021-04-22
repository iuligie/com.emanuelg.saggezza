package com.emanuelg.saggezza;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.emanuelg.saggezza.model.Employee;
import com.emanuelg.saggezza.model.Project;
import com.emanuelg.saggezza.model.Task;
import com.emanuelg.saggezza.model.Timesheet;
import com.google.firebase.firestore.BuildConfig;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rollbar.android.Rollbar;

import java.util.ArrayList;
import java.util.List;

public class TimesheetApi extends Application {

    //region Variables
    private static TimesheetApi instance;
    private List<Timesheet> timesheetList;
    private List<Project> myProjectsList;
    private List<Task> myTasksList;
    private List<Employee> employeeList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //endregion

    public TimesheetApi() {
        timesheetList = new ArrayList<>();
        myProjectsList = new ArrayList<>();
        myTasksList = new ArrayList<>();
            loadMyTasks();
            loadMyProjects();
            loadMyTimesheets();
    }

    public static synchronized TimesheetApi getInstance() {
        if(instance == null)
            instance = new TimesheetApi();
        return instance;
    }


    //region Employee List

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    //endregion

    //region Load data from Cloud Firestore
    public void loadMyTimesheets() {
        CollectionReference collectionReference = db.collection("Timesheets");
        collectionReference.orderBy("submittedOn", Query.Direction.DESCENDING)
                .whereEqualTo("uid", Employee.getInstance().getAccount().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot items : queryDocumentSnapshots) {
                            Timesheet item = items.toObject(Timesheet.class);
                            item.setId(items.getId());
                            if(!timesheetList.contains(item))
                                timesheetList.add(item);
                        }

                    } else {
                        System.out.println("query was empty");
                    }
                })
                .addOnFailureListener(e -> Log.d("DB-LOG", "onFailure: " + e.getMessage()));
    }
    public void loadMyProjects() {

        CollectionReference collectionReference = db.collection("Projects");
        collectionReference.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot items : queryDocumentSnapshots) {
                            Project item = items.toObject(Project.class);
                            item.setId(items.getId());
                            if(item.getResources().contains(Employee.getInstance().getMyReference()))
                            {
                                item.setDocReference(items.getReference());
                                myProjectsList.add(item);
                            }
                        }
                        instance.setMyProjectsList(myProjectsList);

                    } else {
                        System.out.println("query was empty");
                    }
                })
                .addOnFailureListener(e -> Log.d("DB-LOG", "onFailure: " + e.getMessage()));
    }
    public void loadMyTasks() {

        CollectionReference collectionReference = db.collection("Tasks");
        collectionReference.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot items : queryDocumentSnapshots) {
                            Task item = items.toObject(Task.class);
                            item.setId(items.getId());
                            if(item.getResources().contains(Employee.getInstance().getMyReference()))
                            {
                                item.setDocReference(items.getReference());
                                myTasksList.add(item);
                            }
                        }
                        instance.setMyTasksList(myTasksList);

                    } else {
                        System.out.println("query was empty");
                    }
                })
                .addOnFailureListener(e -> Log.d("DB-LOG", "onFailure: " + e.getMessage()));
    }
    //endregion

    //region Local Data Repository and Filters
    public Project getProjectById(String id)
    {
        for (Project item: myProjectsList) {
            if(item.getId().equals(id)) return item;
        }
        return null;
    }
    public Task getTaskById(String id) {

        if (BuildConfig.DEBUG && !(myTasksList.size() > 0 && id != null)) {
            throw new AssertionError("myTasksList is EMPTY");
        }

        for (Task item : myTasksList) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }
    public List<Task> getTasksByProjectId(String projectId)
    {
        List<Task>result = new ArrayList<>();
        for (Task temp:myTasksList) {
            if(temp.getProjectId().equals(projectId))
            {
                result.add(temp);
            }
        }
        return result;
    }
    //endregion

    //region Set Lists

    public void setTimesheetList(List<Timesheet> timesheetList) {
        this.timesheetList = timesheetList;
    }

    public void setMyProjectsList(List<Project> myProjectsList) {
        this.myProjectsList = myProjectsList;
    }

    public void setMyTasksList(List<Task> myTasksList) {
        this.myTasksList = myTasksList;
    }

    //endregion

    //region Get Lists
    public List<Timesheet> getTimesheetList() {

        return timesheetList;
    }

    public List<Project> getMyProjectsList() {
        if (BuildConfig.DEBUG && myProjectsList.size() == 0) {
            Rollbar.instance().error(new Exception("Something is wrong with my Projects List"));
            throw new AssertionError("Assertion failed");
        }
        return myProjectsList;
    }

    public List<Task> getMyTasksList() {
        if (BuildConfig.DEBUG && myTasksList.size() == 0) {
            Rollbar.instance().error(new Exception("Something is wrong with my Tasks List"));
            throw new AssertionError("Assertion failed");
        }
        return myTasksList;
    }
    //endregion

    //region Achievements
    public List<Uri> getAchievements(int total) {

        List<Uri> result = new ArrayList<>();

        // Create a Cloud Storage reference from the app
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl("gs://emanuel-dissertation.appspot.com");
        for (int i = 1; i <= total; i++) {

                String strImg = "achievement" + i + ".png";

                com.google.android.gms.tasks.Task<Uri> task = storageRef.child(strImg).getDownloadUrl();
                while (!task.isComplete()) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    }, 1);
                }

                if (task.isSuccessful()) {
                    result.add(task.getResult());
                }

        }
        if (result.size() == 0) {

            result.add(Uri.parse("gs://emanuel-dissertation.appspot.com/achievement3.png"));
        }
        return result;
    }
    //endregion

    //region Delete Timesheet
    public void deleteItem(Timesheet item)
    {
        Employee.getInstance().getMyReference().update("score", FieldValue.increment(-10));

        if(item.isOnTime()) {
            Employee.getInstance().getMyReference().update("badgesCount", FieldValue.increment(-1));
            Employee.getInstance().setBadgesCount(Employee.getInstance().getBadgesCount()-1);
        }
        else {
            Employee.getInstance().getMyReference().update("penaltyBadgesCount", FieldValue.increment(-1));
            Employee.getInstance().setPenaltyBadgesCount(Employee.getInstance().getPenaltyBadgesCount()-1);
        }db.collection("Timesheets").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("DELETE-LOG", "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w("DELETE-LOG", "Error deleting document", e));
    }
    //endregion
    public void clearInstance() {

        timesheetList.clear();
        myProjectsList.clear();
        myTasksList.clear();
        employeeList.clear();
        instance=null;

    }
}
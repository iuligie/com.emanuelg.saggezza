package com.emanuelg.saggezza;

import android.app.Application;
import android.util.Log;

import com.emanuelg.saggezza.model.Employee;
import com.emanuelg.saggezza.model.Project;
import com.emanuelg.saggezza.model.Task;
import com.emanuelg.saggezza.model.Timesheet;
import com.google.firebase.firestore.BuildConfig;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rollbar.android.Rollbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TimesheetApi extends Application {

    //region Variables
    private static TimesheetApi instance = new TimesheetApi();
    //private Employee employee;// = Employee.getInstance();
    private String currentTimesheetPos;
    private List<Timesheet> timesheetList;
    private List<Project> myProjectsList;
    private List<Task> myTasksList;
    private List<Employee> employeeList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //endregion

    public TimesheetApi() {
        //Employee = Employee.getInstance();
        timesheetList = new ArrayList<>();
        myProjectsList = new ArrayList<>();
        myTasksList = new ArrayList<>();
        //if (Employee.getInstance().getAccount().getUid() == null) throw new AssertionError();
        loadMyTasks();
        loadMyProjects();
        loadMyTimesheets();


    }

    public static synchronized TimesheetApi getInstance() {
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
        collectionReference.whereEqualTo("uid", Employee.getInstance().getAccount().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot items : queryDocumentSnapshots) {
                            Timesheet item = items.toObject(Timesheet.class);
                            item.setId(items.getId());
                            if(!timesheetList.contains(item))
                                timesheetList.add(item);
                        }

                        //setTimesheetList(timesheetList);
                        //instance.setTimesheetList(timesheetList);

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
                                item.setDocReference(items.getReference());
                            myProjectsList.add(item);
                        }

                        //setMyProjectsList(myProjectsList);
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
                                item.setDocReference(items.getReference());
                            myTasksList.add(item);
                        }

                        //setMyTasksList(myTasksList);
                        instance.setMyTasksList(myTasksList);

                    } else {
                        System.out.println("query was empty");
                    }
                })
                .addOnFailureListener(e -> Log.d("DB-LOG", "onFailure: " + e.getMessage()));
    }
    public void MyTimesheetListener()
    {
        ListenerRegistration registration;
        Query query =db.collection("Timesheets")
                .whereEqualTo("uid", Employee.getInstance().getAccount().getUid());
        registration = query.addSnapshotListener((value, error) -> {
            if(error!=null)throw new AssertionError("Error: "+error.getMessage());
            for(QueryDocumentSnapshot doc: Objects.requireNonNull(value))
            {
                Timesheet item = doc.toObject(Timesheet.class);
                if(!timesheetList.contains(item))
                    timesheetList.add(item);
                //timesheetRecyclerAdapter.notifyDataSetChanged();
                //Toast.makeText(getContext(), "Items: " + TimesheetApi.getInstance().getTimesheetList().size(), Toast.LENGTH_SHORT).show();
            }
        });
        registration.remove();
    }
    //endregion

    //region Local Data Repository and Filters
    public Project getProjectById(String id)
    {
        for (Project item: myProjectsList) {
            if(item.getId().equals(id)) return item;
        }
        return null;
        /*
        DocumentReference doc=FirebaseFirestore.getInstance().collection("Projects").document(id);
       List<Project> resultList = new ArrayList<>();
        doc.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Project project = documentSnapshot.toObject(Project.class);
                    resultList.add(project);
                });
        return resultList.size() == 0 ? fetchProjectFromLocal(id) :  resultList.get(0);//resultList.get(0);
        */
        //return fetchProjectFromLocal(id);
    }
    public Task getTaskById(String id) {
        if (BuildConfig.DEBUG && !(myTasksList.size() > 0 && id != null)) {
            throw new AssertionError("myTasksList is EMPTY");
        }
        for (Task item : myTasksList) {
            if (item.getId().equals(id)) return item;
        }
        return null;
        //return fetchTaskFromLocal(id);
        /*
        CollectionReference collectionReference = db.collection("Tasks");

        List<Task> resultList = new ArrayList<>();
        collectionReference.document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Task task = documentSnapshot.toObject(Task.class);
                    resultList.add(task);
                });
        return  resultList.size() == 0 ? fetchTaskFromLocal(id) :  resultList.get(0);
        */
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

    //region Current Adapter Position
    public String getCurrentTimesheetPos() {
        return currentTimesheetPos;
    }
    public void setCurrentTimesheetPos(String currentTimesheetPos) {
        this.currentTimesheetPos = currentTimesheetPos;
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
        // if (BuildConfig.DEBUG && timesheetList.size() == 0) {
        //     throw new AssertionError("Assertion failed");
        // }
        //if (BuildConfig.DEBUG && timesheetList.size() > 3) {
        //    Rollbar.instance().error(new Exception("Timesheet List has DUPLICATES"));
        //throw new AssertionError("Assertion failed");
        // }
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



}
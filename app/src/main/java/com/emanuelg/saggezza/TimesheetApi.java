package com.emanuelg.saggezza;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.emanuelg.saggezza.model.Employee;
import com.emanuelg.saggezza.model.Project;
import com.emanuelg.saggezza.model.Task;
import com.emanuelg.saggezza.model.Timesheet;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.BuildConfig;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rollbar.android.Rollbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TimesheetApi extends Application {

    //region Variables
    private static TimesheetApi instance;// = new TimesheetApi();
    //private Employee employee;// = Employee.getInstance();
    private String currentTimesheetPos;
    private List<Timesheet> timesheetList;
    private List<Project> myProjectsList;
    private List<Task> myTasksList;
    private List<Employee> employeeList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentSnapshot lastVisible;
    //endregion

    public TimesheetApi() {
        //Employee = Employee.getInstance();
        timesheetList = new ArrayList<>();
        myProjectsList = new ArrayList<>();
        myTasksList = new ArrayList<>();
        //Employee.getInstance().getAccount().getUid();
        //if(Employee.getInstance().getMyReference()!=null) {
            loadMyTasks();
            loadMyProjects();
            loadMyTimesheets();
        //}//else throw new RuntimeException("Employee document reference unavailable");

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
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() -1);
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
                            {
                                item.setDocReference(items.getReference());
                                myTasksList.add(item);
                            }
                        }

                        //setMyTasksList(myTasksList);
                        instance.setMyTasksList(myTasksList);

                    } else {
                        System.out.println("query was empty");
                    }
                })
                .addOnFailureListener(e -> Log.d("DB-LOG", "onFailure: " + e.getMessage()));
    }
    public void loadNextPage()
    {
        //Construct query for next 10 timesheet
        Query next = db.collection("Timesheets")
                .orderBy("submittedOn", Query.Direction.DESCENDING)
                .whereEqualTo("uid", Employee.getInstance().getAccount().getUid())
                .startAfter(lastVisible)
                .limit(10);

        next.get()
                .addOnSuccessListener(documentSnapshots -> {

                    if (!documentSnapshots.isEmpty()) {
                        // Get the last visible document
                        lastVisible = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() -1);
                        for (QueryDocumentSnapshot items : documentSnapshots) {
                            Timesheet item = items.toObject(Timesheet.class);
                            item.setId(items.getId());
                            if(!timesheetList.contains(item))
                                addEndOfList(timesheetList,item);
                        }
                    } else {

                        System.out.println("query was empty");
                    }
                });

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

    //region Rank and Avatar
    public Pair<String, Integer> getRankAndAvatar(int score)
    {
        Pair<String, Integer> result;

        if(score >= 100)
        {
            result = new Pair<>("Level 2 - Visionary ", R.drawable.rank2);
        }else if(score >= 200)
        {
            result = new Pair<>("Level 3 - Aspiring Explorer ", R.drawable.rank3);
        }
        else if(score >= 300)
        {
            result = new Pair<>("Level 4 - Established Explorer ", R.drawable.rank4);
        }
        else if(score >= 500)
        {
            result = new Pair<>("Guru", R.drawable.rank5);
        }else {
            result = new Pair<>("Level 1 - Engager ", R.drawable.rank1);
        }
        
        
        return result;
    }
    //endregion

    //region Achievements
    public List<Uri> getAchievements(int total) {
        List<Uri> result = new ArrayList<>();
        // Create a Cloud Storage reference from the app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        //StorageReference storageRef = storage.getReference();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://emanuel-dissertation.appspot.com");
        for (int i = 1; i <= total; i++) {
            String strImg = "achievement" + i + ".png";
            storageRef.child("achievement1.png")
                    .getDownloadUrl()
                    .addOnSuccessListener(result::add);

        }
        if (result.size() == 0) {
            result.add(Uri.parse("gs://emanuel-dissertation.appspot.com/achievement3.png"));
            //throw new AssertionError("Unable to load achievements");
        }
        return result;
    }
    //endregion

    //region Delete Timesheet
    public void deleteItem(String itemId)
    {
        Employee.getInstance().getMyReference().update("score", FieldValue.increment(-10));
        db.collection("Timesheets").document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("DELETE-LOG", "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w("DELETE-LOG", "Error deleting document", e));
    }

    public void clearInstance() {

        timesheetList.clear();
        myProjectsList.clear();
        myTasksList.clear();
        employeeList.clear();
        instance=null;
    }
    //endregion

    //region Util Methods
    private static void addEndOfList(List<Timesheet> list, Timesheet item){
        try{
            list.add(getEndOfList(list), item);
        } catch (IndexOutOfBoundsException e){
            System.out.println(e.toString());
        }
    }

    private static int getEndOfList(List<Timesheet> list){
        if(list != null) {
            return list.size();
        }
        return -1;
    }
    //endregion
}
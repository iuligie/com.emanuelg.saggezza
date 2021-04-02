package com.emanuelg.saggezza.model;

import com.emanuelg.saggezza.TimesheetApi;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

public class Timesheet {
    //region Variables
    @Exclude
    private String id;
    private DocumentReference projectRef;
    private DocumentReference taskRef;
    private String taskId;
    private String projectId;
    private String txtDateRange;
    private String dateRange;
    private String hours;
    private Timestamp submittedOn;
    private Timestamp approvedOn;
    private String approvedBy;
    private String uid;
    private Project project;
    private Task task;
    private boolean onTime;
    //endregion

    public Timesheet() {
    }

    //region ID
    @Exclude
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    //endregion
    //region Task
    @Exclude
    public Task getTask() {
        assert taskId != null;
        setTask(TimesheetApi.getInstance().getTaskById(taskId));
        if(task == null) {
            //task = new Task();
            throw new RuntimeException();
           // task.setName("DATABASE ERROR");

        }
        return task;
    }
    public String getTaskId() {
        return taskId;
    }
    public DocumentReference getTaskRef() {
        return taskRef;
    }
    public void setTaskRef(DocumentReference taskRef) {
        this.taskRef = taskRef;
    }
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    public void setTask(Task task) {
        this.task = task;
        setTaskRef(task.getDocReference());
    }
    //endregion
    //region Project
    public String getProjectId() {
        return projectId;
    }
    public DocumentReference getProjectRef() {
        return projectRef;
    }

    public void setProjectRef(DocumentReference projectRef) {
        //setProject(TimesheetApi.getInstance().getProjectByRef(projectRef));
        this.projectRef = projectRef;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    public void setProject(Project project) {
        this.project = project;
        setProjectRef(project.getDocReference());
    }
    @Exclude
    public Project getProject() {
        assert projectId != null;
        setProject(TimesheetApi.getInstance().getProjectById(projectId));
        if(project == null)
        {
            throw new RuntimeException();
            //project = new Project();
            //project.setName("DATABASE ERROR");
        }
        return project;
    }
    //endregion
    //region Begin Date
    public String getTxtDateRange() {
        return txtDateRange;
    }
    public void setTxtDateRange(String txtDateRange) {
        this.txtDateRange = txtDateRange;
    }
    //endregion
    //region End Date
    public String getDateRange() {
        return dateRange;
    }
    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }
    //endregion
    //region Hours
    public String getHours() {
        return hours;
    }
    public void setHours(String hours) {
        this.hours = hours;
    }
    //endregion
    //region Submitted On
    public Timestamp getSubmittedOn() {
        return submittedOn;
    }
    public void setSubmittedOn(Timestamp submittedOn) {
        this.submittedOn = submittedOn;
    }
    //endregion
    //region Approved On
    public Timestamp getApprovedOn() {
        return approvedOn;
    }
    public void setApprovedOn(Timestamp approvedOn) {
        this.approvedOn = approvedOn;
    }
    //endregion
    //region Approved By
    public String getApprovedBy() {
        return approvedBy;
    }
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    //endregion
    //region User ID
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    //endregion
    //region OnTime

    public boolean isOnTime() {
        return onTime;
    }

    public void setOnTime(boolean onTime) {
        this.onTime = onTime;
    }

    //endregion
}

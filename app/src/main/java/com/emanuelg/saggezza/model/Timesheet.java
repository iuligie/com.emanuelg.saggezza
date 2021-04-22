package com.emanuelg.saggezza.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.emanuelg.saggezza.TimesheetApi;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

public class Timesheet implements Parcelable {

    //region Variables
    @Exclude
    private String id;
    private DocumentReference projectRef;
    private DocumentReference taskRef;
    private String taskId;
    private String projectId;
    private String txtDateRange;
    private String dateRange;
    private Long startDate;
    private Long endDate;
    private String hours;
    private Timestamp submittedOn;
    private String uid;
    private Project project;
    private Task task;
    private boolean onTime;
    //endregion

    public Timesheet() {
    }

    protected Timesheet(Parcel in) {
        id = in.readString();
        taskId = in.readString();
        projectId = in.readString();
        txtDateRange = in.readString();
        dateRange = in.readString();

        if (in.readByte() == 0) {
            startDate = null;
        } else {
            startDate = in.readLong();
        }
        if (in.readByte() == 0) {
            endDate = null;
        } else {
            endDate = in.readLong();
        }

        hours = in.readString();
        submittedOn = in.readParcelable(Timestamp.class.getClassLoader());
        uid = in.readString();
        onTime = in.readByte() != 0;
    }

    public static final Creator<Timesheet> CREATOR = new Creator<Timesheet>() {
        @Override
        public Timesheet createFromParcel(Parcel in) {
            return new Timesheet(in);
        }

        @Override
        public Timesheet[] newArray(int size) {
            return new Timesheet[size];
        }
    };

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
            throw new RuntimeException();
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
        }
        return project;
    }
    //endregion

    //region Date Range in user friendly text
    public String getTxtDateRange() {
        return txtDateRange;
    }

    public void setTxtDateRange(String txtDateRange) {
        this.txtDateRange = txtDateRange;
    }
    //endregion

    //region Date Range unformatted
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

    //region Computer Friendly Dates

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    //endregion

    //region Interface specific methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(taskId);
        dest.writeString(projectId);
        dest.writeString(txtDateRange);
        dest.writeString(dateRange);
        if (startDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(startDate);
        }
        if (endDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(endDate);
        }
        dest.writeString(hours);
        dest.writeParcelable(submittedOn, flags);
        dest.writeString(uid);
        dest.writeByte((byte) (onTime ? 1 : 0));
    }

    //endregion

}

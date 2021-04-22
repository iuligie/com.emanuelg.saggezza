package com.emanuelg.saggezza.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Task {

    //region Variables
    private String id;
    private String name;
    private String projectId;
    private List<DocumentReference> resources;
    private DocumentReference docReference;
    //endregion

    public Task() {
    }
    //region Id
    @Exclude
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    //endregion

    //region Task Reference
    @Exclude
    public DocumentReference getDocReference() {
        return docReference;
    }

    public void setDocReference(DocumentReference docReference) {
        this.docReference = docReference;
    }

    //endregion

    //region Name
    public String getName() {
        if(name == null)
            return "NOT AVAILABLE";
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String toString() {
        return this.name;
    }

    //endregion

    //region Project Id
    public String getProjectId() {
        return projectId;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    //endregion

    //region Resources
    public List<DocumentReference> getResources() {
        return resources;
    }
    public void setResources(List<DocumentReference> resources) {
        this.resources = resources;
    }
    //endregion
}

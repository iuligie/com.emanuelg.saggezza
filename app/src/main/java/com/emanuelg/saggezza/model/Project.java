package com.emanuelg.saggezza.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String id;
    private String name;
    private String companyName;
    private boolean isBillable;
    private List<DocumentReference> resources = new ArrayList<>();
    private DocumentReference docReference;

    public Project() {
    }
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
        return this.name; // What to display in the Spinner list.
    }

    //endregion
    //region Project Reference
    @Exclude
    public DocumentReference getDocReference() {
        return docReference;
    }

    public void setDocReference(DocumentReference docReference) {
        this.docReference = docReference;
    }

    //endregion
    //region Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    //region Company Name
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    //endregion

    //region isBillable
    public boolean isBillable() {
        return isBillable;
    }

    public void setBillable(boolean isBillable) {
        this.isBillable = isBillable;
    }

    //endregion

}

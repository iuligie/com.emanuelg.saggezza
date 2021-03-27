package com.emanuelg.saggezza.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Employee {

    //region Variables
    public boolean isSupervisor;
    @Exclude
    private FirebaseUser account;

    private String supervisorId;
    private String email;
    private DocumentReference myReference;
    private static Employee instance;
    private int score;
    //endregion
    public Employee() {
    }
    //region Account

    public static Employee getInstance() {
        if (instance == null)

            instance = new Employee();
        return instance;

    }

    @NotNull
    @Exclude
    public FirebaseUser getAccount() {
       // assert FirebaseAuth.getInstance().getCurrentUser() != null;
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
    }
    private void setAccount(FirebaseUser account) {
        this.account = account;
    }
    //endregion
    //region Supervisor
    public String getSupervisorId() {
        return supervisorId;
    }
    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }
    public boolean isSupervisor() {
        return isSupervisor;
    }
    public void setSupervisor(boolean isSupervisor) {
        this.isSupervisor = isSupervisor;
    }
    //endregion
    //region Email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    //endregion
    //region My Reference
    @Exclude
    public DocumentReference getMyReference() {
        return myReference;
    }
    public void setMyReference(DocumentReference myReference) {
        this.myReference = myReference;
    }
    //endregion

    //region Score

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    //endregion
}
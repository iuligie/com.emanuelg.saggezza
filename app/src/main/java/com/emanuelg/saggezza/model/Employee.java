package com.emanuelg.saggezza.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;

public class Employee {

    //region Variables
    public boolean supervisor;
    @Exclude
    private FirebaseUser account;

    private String supervisorId;
    private String email;
    private DocumentReference myReference;
    private static Employee instance;
    private int score;
    private String name;
    private int achievementsTotal;
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
    public void setAccount(FirebaseUser account) {
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
        return supervisor;
    }
    public void setSupervisor(boolean supervisor) {
        this.supervisor = supervisor;
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
    //@Exclude
    public DocumentReference getMyReference() {
       // assert myReference != null;
        return myReference;
    }
    public void setMyReference(DocumentReference myReference) {
        this.myReference = myReference;
    }
    //endregion
    //region Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //endregion
    //region Score

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void incrementScore(boolean onTime)
    {
        if (onTime)
        myReference.update("score", FieldValue.increment(10));
        else myReference.update("score", FieldValue.increment(-10));
    }

    //endregion
    //region Achievements

    public int getAchievementsTotal() {
        return achievementsTotal;
    }

    public void setAchievementsTotal(int achievementsTotal) {
        this.achievementsTotal = achievementsTotal;
    }
    public void incrementAchievementsTotal()
    {
        achievementsTotal++;
    }

    //endregion
}

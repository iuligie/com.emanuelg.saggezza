package com.emanuelg.saggezza.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Employee {

    private String email;
    private static Employee instance;
    private int score;
    private String name;
    private int badgesCount;
    private int penaltyBadgesCount;
    private int achievementsTotal;
    @Exclude
    private DocumentReference myReference;
    //endregion
    public Employee() {
    }
    //region Account and Instance

    public static Employee getInstance() {
        if (instance == null)

            instance = new Employee();
        return instance;
    }
    @NotNull
    @Exclude
    public FirebaseUser getAccount() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
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
        {
            myReference.update("score", FieldValue.increment(10));
            myReference.update("badgesCount",FieldValue.increment(1));
            badgesCount++;
        }
        else {
            myReference.update("score", FieldValue.increment(-10));
            myReference.update("penaltyBadgesCount",FieldValue.increment(1));
            penaltyBadgesCount++;
        }
    }

    //endregion
    //region Achievements

    public int getAchievementsTotal() {
        return achievementsTotal;
    }

    public void setAchievementsTotal(int achievementsTotal) {
        this.achievementsTotal = achievementsTotal;
    }

    //endregion

    //region Number of Badges

    public int getBadgesCount() {
        return badgesCount;
    }

    public void setBadgesCount(int badgesCount) {
        this.badgesCount = badgesCount;
    }

    public int getPenaltyBadgesCount() {
        return penaltyBadgesCount;
    }

    public void setPenaltyBadgesCount(int PenaltyBadgesCount) {
        this.penaltyBadgesCount = PenaltyBadgesCount;
    }


    //endregion
}

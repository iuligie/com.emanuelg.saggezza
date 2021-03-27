package com.emanuelg.saggezza.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

public class LeaderboardItem {
    //region Variables
    private String name;
    private int score;

    public LeaderboardItem() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
//endregion
}

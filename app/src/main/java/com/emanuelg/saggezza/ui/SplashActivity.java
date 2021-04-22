package com.emanuelg.saggezza.ui;
import android.content.Intent;
import android.os.Handler;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.emanuelg.saggezza.Authentication;
import com.emanuelg.saggezza.MainActivity;
import com.emanuelg.saggezza.R;
import com.emanuelg.saggezza.TimesheetApi;
import com.emanuelg.saggezza.model.Employee;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rollbar.android.Rollbar;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class SplashActivity extends Activity {

    private final Handler mWaitHandler = new Handler();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    TimesheetApi api;
    FirebaseAuth mAuth;
    boolean isAuthenticated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        findViewById(R.id.btnSignIn).setVisibility(View.GONE);
        TextView txtWelcome = findViewById(R.id.fullscreen_content);
        txtWelcome.setText(R.string.strWelcome);
        ImageView imgLogIn = findViewById(R.id.imgLogInPage);

        Picasso.get()
                .load(R.drawable.icon_saggezza)
                .into(imgLogIn);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        mAuth = FirebaseAuth.getInstance();

        if(account!=null && mAuth.getCurrentUser()!=null) {

            isAuthenticated = true;
            updateDatabase(mAuth.getCurrentUser());
        }
        else {

            isAuthenticated = false;
        }
        mWaitHandler.postDelayed(() -> {

            try {
                Intent intent;
                if(isAuthenticated)
                {   //Go to Main Activity
                    intent = new Intent(getApplicationContext(), MainActivity.class);

                }
                else
                {   //Go to Authentication Activity
                    intent = new Intent(getApplicationContext(), Authentication.class);
                }
                startActivity(intent);
                //Finish Splash Activity since we do not want to show this when user press back button.
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5000);  // Give a 5 seconds delay.
    }
    private void  updateDatabase(FirebaseUser user) {

        //region  Document retrieving method
        db.collection("Employees").document(Objects.requireNonNull(user.getUid()))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                            Employee temp = Employee.getInstance();
                            temp.setEmail(Objects.requireNonNull(document.toObject(Employee.class)).getEmail());
                            temp.setScore(Objects.requireNonNull(document.toObject(Employee.class)).getScore());
                            temp.setMyReference(Objects.requireNonNull(document.getReference()));
                            temp.setAchievementsTotal(Objects.requireNonNull(document.toObject(Employee.class)).getAchievementsTotal());
                            temp.setName(Objects.requireNonNull(document.toObject(Employee.class)).getName());
                            temp.setBadgesCount(Objects.requireNonNull(document.toObject(Employee.class)).getBadgesCount());
                            temp.setPenaltyBadgesCount(Objects.requireNonNull(document.toObject(Employee.class)).getPenaltyBadgesCount());
                            api = TimesheetApi.getInstance();

                    } else {
                       Rollbar.instance().error("Can not load employees");
                    }
                })
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Employee can not be loaded from server");
                });
        //endregion
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Remove all the callbacks otherwise navigation will execute even after activity is killed or closed.
        mWaitHandler.removeCallbacksAndMessages(null);
    }
}
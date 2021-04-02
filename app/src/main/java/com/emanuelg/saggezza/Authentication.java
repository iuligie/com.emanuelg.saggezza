package com.emanuelg.saggezza;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.emanuelg.saggezza.model.Employee;
import com.emanuelg.saggezza.model.Timesheet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rollbar.android.Rollbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Authentication extends AppCompatActivity {
    private static final String TAG = "AUTHENTICATION";
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    GoogleSignInClient mGoogleSignInClient;
    TimesheetApi api;
    FirebaseUser current;
    private static final int RC_SIGN_IN = 9001;
    private ProgressBar authLoadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        authLoadingBar = findViewById(R.id.authLoadingBar);
        authLoadingBar.setVisibility(View.INVISIBLE);
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this::onClick);

        ImageView imgLogIn = findViewById (R.id.imgLogInPage);
        Picasso.get()
                .load(R.drawable.icon_saggezza)
                .into(imgLogIn);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //if(account.getIdToken() != null)
        //firebaseAuthWithGoogle(account.getIdToken());


        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null) {
            current = mAuth.getCurrentUser();
            signIn();
            updateUI(current, false);
        }else{
            authLoadingBar.setVisibility(View.INVISIBLE);
        }
    }


    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
            signIn();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            assert account != null;
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            throw new RuntimeException();
            //updateUI(null);
        }
    }
    private void updateUI(FirebaseUser account, boolean isNew) {
        if(account != null)
        {
            updateDatabase(account, isNew);
            if(Employee.getInstance().getMyReference()!=null) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
           // else{ Toast.makeText(this, "Something went wrong! Please try again!", Toast.LENGTH_LONG).show();}
            authLoadingBar.setVisibility(View.INVISIBLE);
        }
    }

    private void updateDatabase(FirebaseUser user, boolean isNew) {

        db.collection("Employees").document(Objects.requireNonNull(user.getUid()))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (isNew) {
                            addEmployee(user);
                        } else {
                            Employee temp = Employee.getInstance();
                            temp.setEmail(Objects.requireNonNull(document.toObject(Employee.class)).getEmail());
                            temp.setSupervisor(Objects.requireNonNull(document.toObject(Employee.class)).isSupervisor());
                            temp.setSupervisorId(Objects.requireNonNull(document.toObject(Employee.class)).getSupervisorId());
                            temp.setScore(Objects.requireNonNull(document.toObject(Employee.class)).getScore());
                            temp.setMyReference(Objects.requireNonNull(document.getReference()));
                            temp.setAccount(Objects.requireNonNull(document.toObject(Employee.class)).getAccount());
                            temp.setAchievementsTotal(Objects.requireNonNull(document.toObject(Employee.class)).getAchievementsTotal());
                            temp.setName(Objects.requireNonNull(document.toObject(Employee.class)).getName());
                            api = TimesheetApi.getInstance();
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                        Rollbar.instance().error("Can not load employees");
                    }
                });
    }

    private void addEmployee(FirebaseUser user) {
        Employee employee = Employee.getInstance();
        employee.setEmail(user.getEmail());
        employee.setSupervisor(true);
        employee.setSupervisorId("ZQHSOfw8JdhsmXPDWGnKX86Mj5n1");
        employee.setScore(0);
        employee.setName(user.getDisplayName());
        employee.setAchievementsTotal(1);
        db.collection("Employees").document(Objects.requireNonNull(user.getUid())).set(employee)
                .addOnSuccessListener(aVoid -> Log.d("DB", "DocumentSnapshot added or updated with ID: " + user.getUid()))
                .addOnFailureListener(e -> Log.w("DB", "Error adding document", e));
        DocumentReference myRef = db.collection("Employees").document(Objects.requireNonNull(user.getUid()));
        employee.setMyReference(Objects.requireNonNull(myRef));
        //set user's default tasks and projects
        db.collection("Projects").document("3WB5FEVV2zVCB7qYSFiZ").update("resources",FieldValue.arrayUnion(myRef));
        db.collection("Projects").document("U4eMDAZewD0hd7gJZ8Ga").update("resources",FieldValue.arrayUnion(myRef));
        db.collection("Tasks").document("p6MBUQJSMtTd1gckwCuM").update("resources",FieldValue.arrayUnion(myRef));
        db.collection("Tasks").document("CbFmOW3LoRoOe9Se2SAP").update("resources",FieldValue.arrayUnion(myRef));

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()==null)
        authLoadingBar.setVisibility(View.INVISIBLE);

        else authLoadingBar.setVisibility(View.VISIBLE);

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean isNew = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser();
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user, isNew);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                       Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
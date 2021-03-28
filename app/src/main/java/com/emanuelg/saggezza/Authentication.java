package com.emanuelg.saggezza;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rollbar.android.Rollbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Authentication extends AppCompatActivity {
    private static final String TAG = "AUTHENTICATION";
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //TimesheetApi api;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    //private List<Employee> employeeList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);


        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
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
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //if(account.getIdToken() != null)
        //firebaseAuthWithGoogle(account.getIdToken());


        mAuth = FirebaseAuth.getInstance();


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
    private void updateUI(FirebaseUser account) {
        if(account != null)
        {
            updateDatabase(account);
             TimesheetApi api = TimesheetApi.getInstance();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void updateDatabase(FirebaseUser user) {
        final boolean[] newUser = {false};
        db.collection("Employees").document(Objects.requireNonNull(user.getUid()))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (!document.exists()) {
                            addEmployee(user);
                        } else {
                            Employee temp = Employee.getInstance();
                            temp.setEmail(Objects.requireNonNull(document.toObject(Employee.class)).getEmail());
                            temp.setSupervisor(Objects.requireNonNull(document.toObject(Employee.class)).isSupervisor());
                            temp.setSupervisorId(Objects.requireNonNull(document.toObject(Employee.class)).getSupervisorId());
                            temp.setScore(Objects.requireNonNull(document.toObject(Employee.class)).getScore());
                            temp.setMyReference(Objects.requireNonNull(document.getReference()));
                            temp.setAccount(Objects.requireNonNull(document.toObject(Employee.class)).getAccount());
                            //assert temp != null;
                            //temp.setAccount(user);
                            //temp.setMyReference(document.getReference());
                            //loadExistingUser(temp);
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                        Rollbar.instance().error("Can not load employees");
                    }
                });
    }

    private void loadExistingUser(Employee temp) {
        Employee employee = Employee.getInstance();
        employee.setEmail(temp.getEmail());
        employee.setSupervisor(true);
        employee.setSupervisorId(temp.getSupervisorId());
        employee.setScore(temp.getScore());
        employee.setMyReference(temp.getMyReference());
        employee.setAccount(temp.getAccount());
    }

    private void addEmployee(FirebaseUser user) {
        Employee employee = Employee.getInstance();
        employee.setEmail(user.getEmail());
        employee.setSupervisor(true);
        employee.setSupervisorId("ZQHSOfw8JdhsmXPDWGnKX86Mj5n1");
        employee.setScore(0);
        db.collection("Employees").document(Objects.requireNonNull(user.getUid())).set(employee)
                .addOnSuccessListener(aVoid -> Log.d("DB", "DocumentSnapshot added or updated with ID: " + user.getUid()))
                .addOnFailureListener(e -> Log.w("DB", "Error adding document", e));
        DocumentReference myRef = db.collection("Employees").document(Objects.requireNonNull(user.getUid()));
        employee.setMyReference(myRef);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        updateUI(currentUser);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                       // Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        throw new RuntimeException();
                        //updateUI(null);
                    }

                    // ...
                });
    }
}
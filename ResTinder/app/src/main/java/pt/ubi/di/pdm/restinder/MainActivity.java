package pt.ubi.di.pdm.restinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//Firebase imports


public class MainActivity extends Activity {
    /*Variable to save the logIn state*/
    public static SharedPreferences keepLogIn;
    public static SharedPreferences.Editor keepLogInEditor;
    public static Boolean keepLogInState;

    private EditText emailET;
    private EditText passwordET;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private SharedPreferences loginPF;
    private SharedPreferences.Editor loginEditor;
    private CheckBox saveLoginBox;
    private Button loginbtnId;
    private Boolean saveLogin;
    private ProgressBar loading;
    private String email;
    private String password;

    User userProfile;
    //Permissions api
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setPrefs();


        if(keepLogInState){
            //redirect to home/match/settings with login
            /*create a different xml for this action*/
            setContentView(R.layout.loading);
            keepLogIn();
        }else{
            setContentView(R.layout.activity_main);
            emailET = findViewById(R.id.emailFieldID);
            passwordET = findViewById(R.id.passwordFieldID);
            saveLoginBox = findViewById(R.id.saveLoginBox);
            loginbtnId = findViewById(R.id.loginbtnId);
            loginbtnId.setPressed(false);
            loading = findViewById(R.id.loading);
            loading.setVisibility(View.GONE);
            /*Request permissions*/
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET
                        },
                        1);
            }
            if(saveLogin){
                //put email + password + checkbox on view
                emailET.setText(email);
                passwordET.setText(password);
                saveLoginBox.setChecked(saveLogin);

            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                return;
            }
        }
    }

    public void setPrefs()
    {
        /*Pref: email + password + checkbox
         * */
        loginPF = getSharedPreferences("loginPrefs",MODE_PRIVATE);
        loginEditor = loginPF.edit();
        saveLogin = loginPF.getBoolean("loginState",false);
        if(saveLogin){
            email = loginPF.getString("username","");
            password = loginPF.getString("password","");
        }
        /*Pref: keep log in state
         * */
        keepLogIn = getSharedPreferences("keepLogInPref",MODE_PRIVATE);
        keepLogInEditor = keepLogIn.edit();
        keepLogInState = keepLogIn.getBoolean("keepLogInState",false);
    }
    public void registerUser(View v){
        super.finish();
        startActivity(new Intent(this,Register.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void keepLogIn() {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.d("SUPERMETHODS","keepLogin true");
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    login();
                }else {
                    Log.d("SUPERMETHODS","keepLogin false");
                    Toast.makeText(MainActivity.this,"Failed to login!",Toast.LENGTH_LONG).show();
                    keepLogInEditor.putBoolean("keepLogInState",false);
                    keepLogInEditor.commit();
                    finish();
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });

    }

    public void userLogin(View v) {
        loginbtnId.setPressed(true);
        email = emailET.getText().toString().trim();
        password = passwordET.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()) {
            emailET.setError("Invalid email!");
            emailET.requestFocus();
            return;

        }
        if(password.isEmpty()) {
            passwordET.setError("Password is empty!");
            passwordET.requestFocus();
            return;
        }
        loading.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()) {
                        //save credentials of login => if checkbox is checked
                        if(saveLoginBox.isChecked()){
                            loginEditor.putBoolean("loginState",true);
                            loginEditor.putString("username",emailET.getText().toString().trim());
                            loginEditor.putString("password",passwordET.getText().toString().trim());
                            //user login
                            keepLogInEditor.putBoolean("keepLogInState",true);

                        }
                        else{
                            loginEditor.clear();
                            keepLogInEditor.putBoolean("keepLogInState",false);
                        }
                        loginEditor.commit();
                        keepLogInEditor.commit();
                        login();

                    }else{
                        user.sendEmailVerification();
                        Toast.makeText(MainActivity.this,"Check your email to verify your account!",Toast.LENGTH_LONG).show();
                        loginbtnId.setPressed(false);
                    }

                }else {
                    Toast.makeText(MainActivity.this,"Failed to login!",Toast.LENGTH_LONG).show();
                    loginbtnId.setPressed(false);
                }
            }
        });
        loading.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        finish();
        //overridePendingTransition(0, R.anim.slide_down);
    }

    public void login(){
        //loading.setVisibility(View.VISIBLE);

        //user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        FirebaseDatabase.getInstance().getReference("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            /**
             * Vai buscar os dados no realtime database do user que inicou sessão
             */
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
                if (userProfile != null) {
                    checkFirstLogIn();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(MainActivity.this, "Failed to get user data5!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void checkFirstLogIn()
    {
        if(userProfile.firstLogIn)
        {
            super.finish();
            startActivity(new Intent(this, Settings.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }else if(userProfile.matchPending){
            super.finish();
            startActivity(new Intent(this, Match.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }else{
            super.finish();
            startActivity(new Intent(this, Home.class));
            //overridePendingTransition(0,0);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }


}
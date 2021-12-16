package pt.ubi.di.pdm.restinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
            checkifGPSisEnnable();
            keepLogIn();
        }else{
            setContentView(R.layout.activity_main);
            emailET = findViewById(R.id.emailFieldID);
            passwordET = findViewById(R.id.passwordFieldID);
            saveLoginBox = findViewById(R.id.saveLoginBox);
            loginbtnId = findViewById(R.id.loginbtnId);
            loginbtnId.setEnabled(true);
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
            checkifGPSisEnnable();


        if(saveLogin){
            //put email + password + checkbox on view
            emailET.setText(email);
            passwordET.setText(password);
            saveLoginBox.setChecked(saveLogin);
            checkifGPSisEnnable();
        }
    }


    }

    public void checkifGPSisEnnable(){
        LocationManager manager2 =(LocationManager) getSystemService(LOCATION_SERVICE);
        if(!manager2.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(MainActivity.this,"You need to activate the location!",Toast.LENGTH_LONG).show();
            finish();
        }
    }
    /**
     * Check if the user accept the permission. if he didn't accept the app is closed.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * Set the login and keep login preferences
     */
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

    /**
     * if the user click register, he will be redirect to the register activity
     * @param v
     */
    public void registerUser(View v){
        super.finish();
        startActivity(new Intent(this,Register.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    /**
     * if the user has keeplogin active, he will be redirected for the home page or the match depending on the match pending.
     */
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

    /**
     * if the user click's "Login", the system will check if the credencials are valid and if they are, he will be redirect to the home page or the match page depending on the match pending value
     * @param v
     */
    public void userLogin(View v) {
        loginbtnId.setEnabled(false);
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
                        loginbtnId.setEnabled(true);
                    }

                }else {
                    Toast.makeText(MainActivity.this,"Failed to login!",Toast.LENGTH_LONG).show();
                    loginbtnId.setEnabled(true);
                }
            }
        });

    }

    /**
     * if back is pressed, the app closes
     */
    @Override
    public void onBackPressed() {
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
        finish();
    }

    /**
     * login the user in the firebase authentication panel. And get the user unique id
     */
    public void login(){
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
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Check if it is the first login of the user. if it is, he will be redirect for the settings activity.if it isn´t: if the user has match pending, will be redirected for the match activity; if the user doesn't have match pending, will be redirected for the home activity
     */
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
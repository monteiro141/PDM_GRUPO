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
import android.widget.CheckBox;
import android.widget.EditText;
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
    private EditText emailET;
    private EditText passwordET;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private SharedPreferences loginPF;
    private SharedPreferences.Editor loginEditor;
    private CheckBox saveLoginBox;
    private Boolean saveLogin;
    User userProfile;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        emailET = findViewById(R.id.emailFieldID);
        passwordET = findViewById(R.id.passwordFieldID);
        saveLoginBox = findViewById(R.id.saveLoginBox);

        //object of sharedPreferences to save the values on login
        loginPF = getSharedPreferences("loginPrefs",MODE_PRIVATE);
        loginEditor = loginPF.edit();

        //initialize boolean value to false and set it under "loginPrefs"
        saveLogin = loginPF.getBoolean("loginState",false);

        if(saveLogin){
            emailET.setText(loginPF.getString("username",""));
            passwordET.setText(loginPF.getString("password",""));
            saveLoginBox.setChecked(true);
        }

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            requestPermissions(new String[]{Manifest.permission.INTERNET},1);
        }

    }
    public void registerUser(View v){
        super.finish();
        startActivity(new Intent(this,Register.class));
    }
    public void userLogin(View v) {
        String email = emailET.getText().toString().trim(); // editTextEmail.getText().toString().trim();
        String password = passwordET.getText().toString().trim();;

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()) {
            //System.out.println("Invalid email");
            emailET.setError("Invalid email!");
            emailET.requestFocus();
            return;

        }
        if(password.isEmpty()) {
            //System.out.println("Password is empty");
            passwordET.setError("Password is empty!");
            passwordET.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()) {

                        //save credentials of login => if checkbox is checked
                        if(saveLoginBox.isChecked()){
                            loginEditor.putBoolean("loginState",true);
                            loginEditor.putString("username",emailET.getText().toString());
                            loginEditor.putString("password",passwordET.getText().toString());
                            loginEditor.commit();
                        }

                        else{
                            loginEditor.clear();
                            loginEditor.commit();
                        }

                        //user login
                        login();

                    }else{
                        user.sendEmailVerification();
                        Toast.makeText(MainActivity.this,"Check your email to verify your account!",Toast.LENGTH_LONG).show();
                    }

                }else {
                    Toast.makeText(MainActivity.this,"Failed to login!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
    public void login(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            /**
             * Vai buscar os dados no realtime database do user que inicou sessão
             */
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
                System.out.println(userProfile.toString());
                if (userProfile != null) {
                    checkFirstLogIn();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to get user data!", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void checkFirstLogIn()
    {
        if(userProfile.firstLogIn.equals("yes"))
        {
            super.finish();
            startActivity(new Intent(this, Settings.class));
        }else{
            super.finish();
            startActivity(new Intent(this, Home.class));
        }
    }
}
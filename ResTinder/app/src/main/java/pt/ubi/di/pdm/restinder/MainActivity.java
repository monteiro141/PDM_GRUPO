package pt.ubi.di.pdm.restinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//Firebase imports


public class MainActivity extends Activity {

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();



    }

    private void userLogin() {
        String email = "test@email.com"; // editTextEmail.getText().toString().trim();
        String password = "test123456";
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()) {
            System.out.println("Invalid email");
            /*
             * editTextEmail.setError("Invalid email!");
             * editTextEmail.requestFocus();
             * return;
             * */
        }
        if(password.isEmpty()) {
            System.out.println("Password is empty");
            /*
             * editTextPassword.setError("Password is empty!");
             * editTextPassword.requestFocus();
             * return;
             * */
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()) {
                        startActivity(new Intent(MainActivity.this, Home.class));
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



}
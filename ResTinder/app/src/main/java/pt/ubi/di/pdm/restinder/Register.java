package pt.ubi.di.pdm.restinder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends Activity
{

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mAuth = FirebaseAuth.getInstance();


    }

    private void registerUser()
    {
        String email = "test@email.com"; // editTextEmail.getText().toString().trim();
        String password = "test123456";
        String firstName = "firstNameTest";
        String lastName = "lastNameTest";
        String birthday = "25/11/2021";
        String state = "Single";
        String gender = "MaleFemale";
        String phone = "912345678";
        String interestedIn = "FemaleMale";

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            System.out.println("Invalid email");
            /*
             * editTextEmail.setError("Invalid email!");
             * editTextEmail.requestFocus();
             * return;
             * */
        }
        if(password.isEmpty())
        {
            System.out.println("Password is empty");
            /*
             * editTextPassword.setError("Password is empty!");
             * editTextPassword.requestFocus();
             * return;
             * */
        }

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            User user = new User(email,firstName,lastName,birthday,state,gender,phone,interestedIn);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(Register.this,"User has been registered!",Toast.LENGTH_LONG).show();

                                    }else
                                    {
                                        Toast.makeText(Register.this,"Failed to register! Try again!",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }else
                            {
                                Toast.makeText(Register.this,"Failed to register! Try again!",Toast.LENGTH_LONG).show();
                            }
                    }
                });
    }
}


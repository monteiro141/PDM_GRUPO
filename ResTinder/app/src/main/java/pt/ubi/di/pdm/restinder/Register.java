package pt.ubi.di.pdm.restinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class Register extends Activity implements View.OnClickListener
{
    private EditText emailRegister;
    private EditText passwordRegister;
    private EditText firstNameRegister;
    private EditText lastNameRegister;
    private EditText birthDateRegister;
    private EditText civilStateRegister;
    private EditText genderRegister;
    private EditText cellphoneRegister;
    private EditText preferencesRegister;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mAuth = FirebaseAuth.getInstance();

        emailRegister  = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        firstNameRegister = findViewById(R.id.firstNameRegister);
        lastNameRegister = findViewById(R.id.lastNameRegister);
        birthDateRegister = findViewById(R.id.birthDateRegister);
        civilStateRegister = findViewById(R.id.civilStateRegister);
        genderRegister = findViewById(R.id.genderRegister);
        cellphoneRegister = findViewById(R.id.cellphoneRegister);
        preferencesRegister = findViewById(R.id.preferencesRegister);
    }

    public void registerUser(View v)
    {
        String email = emailRegister.getText().toString().trim();// editTextEmail.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();
        String firstName = firstNameRegister.getText().toString().trim();
        String lastName = lastNameRegister.getText().toString().trim();
        String birthday = birthDateRegister.getText().toString().trim();
        String state = civilStateRegister.getText().toString().trim();
        String gender = genderRegister.getText().toString().trim();
        String phone = cellphoneRegister.getText().toString().trim();
        String interestedIn = preferencesRegister.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailRegister.setError("Invalid email!");
            emailRegister.requestFocus();
            return;
        }
        if(password.isEmpty())
        {
            passwordRegister.setError("Password is empty!");
            passwordRegister.requestFocus();
            return;
        }
        if(firstName.isEmpty())
        {
            firstNameRegister.setError("First name is empty!");
            firstNameRegister.requestFocus();
            return;
        }
        if(lastName.isEmpty())
        {
            firstNameRegister.setError("Last name is empty!");
            firstNameRegister.requestFocus();
            return;
        }
        if(birthday.isEmpty())
        {
            firstNameRegister.setError("Birthday is wrong!");
            firstNameRegister.requestFocus();
            return;
        }
        if(Integer.parseInt(birthday)<=18)
        {
            firstNameRegister.setError("Needs to be 18 or older!");
            firstNameRegister.requestFocus();
            return;
        }
        if(state.isEmpty())
        {
            firstNameRegister.setError("State is empty!");
            firstNameRegister.requestFocus();
            return;
        }
        if(gender.isEmpty())
        {
            firstNameRegister.setError("Gender is empty!");
            firstNameRegister.requestFocus();
            return;
        }
        if(phone.isEmpty())
        {
            firstNameRegister.setError("Phone is empty!");
            firstNameRegister.requestFocus();
            return;
        }
        if(interestedIn.isEmpty())
        {
            firstNameRegister.setError("Interested in is empty!");
            firstNameRegister.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            User user = new User(email,firstName,lastName,birthday,state,gender,phone,interestedIn);
                            /*System.out.println("Arroz");
                            System.out.println(user);*/
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        goToMainActivity();
                                        Toast.makeText(Register.this,"User has been registered!",Toast.LENGTH_LONG).show();

                                    }else
                                    {
                                        Toast.makeText(Register.this,"Failed to register! Try again!",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }else
                            {
                                Toast.makeText(Register.this,"Failed to register!",Toast.LENGTH_LONG).show();
                            }
                    }
                });
    }

    @Override
    public void onClick(View view) {

    }
    @Override
    public void onBackPressed() {
        FirebaseAuth.getInstance().signOut();
        goToMainActivity();
    }
    public void goToMainActivity(){
        finish();
        startActivity(new Intent(this,MainActivity.class));
    }

}


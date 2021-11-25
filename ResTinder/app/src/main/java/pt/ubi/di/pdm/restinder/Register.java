package pt.ubi.di.pdm.restinder;

import android.app.Activity;
import android.util.Patterns;

public class Register extends Activity
{
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
    }
}


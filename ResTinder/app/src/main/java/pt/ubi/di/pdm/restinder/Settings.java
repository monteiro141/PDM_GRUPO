package pt.ubi.di.pdm.restinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends Activity
{
    EditText textnome,textapelido,textmailparceiro,texttelemovel,textradius,pickDate;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private Integer currentAge;
    User userProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //Inicialize layout variables
        textnome = findViewById(R.id.textnome);
        textapelido = findViewById(R.id.textapelido);
        textmailparceiro = findViewById(R.id.textmailparceiro);
        texttelemovel = findViewById(R.id.texttelemovel);
        textradius = findViewById(R.id.textradius);
        pickDate = findViewById(R.id.pickDate);

        //Get the instance of firebase
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            /**
             * Put the settings saved, by the logged-in user, on screen
             */
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
                if (userProfile != null) {
                    textnome.setText(userProfile.firstName);
                    textapelido.setText(userProfile.lastName);
                    texttelemovel.setText(String.valueOf(userProfile.phone));
                    if(!userProfile.state.equals("Single"))
                        textmailparceiro.setText(userProfile.state);
                    pickDate.setText(String.valueOf(userProfile.birthday));
                    if(userProfile.radius != 0)
                        textradius.setText(String.valueOf(userProfile.radius/1000));
                    currentAge = userProfile.birthday;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Settings.this, "Failed to get user data!", Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * When the user click "Confirm Changes", the parameters are checked and if there is no error the changes are confirmed.
     * The user´s setting's are updated in the database
     * @param v the view
     */
    public void changeUser(View v)
    {
        String emailP = textmailparceiro.getText().toString().trim();
        String phone = texttelemovel.getText().toString().trim();
        String radius = textradius.getText().toString().trim();
        String age = pickDate.getText().toString().trim();
        boolean firstTimeInSettings= userProfile.firstLogIn;
        if(!Patterns.EMAIL_ADDRESS.matcher(emailP).matches() && !emailP.isEmpty())
        {
            textmailparceiro.setError("Invalid email!");
            textmailparceiro.requestFocus();
            return;
        }
        if(phone.isEmpty())
        {
            texttelemovel.setError("Phone is empty!");
            texttelemovel.requestFocus();
            return;
        }
        if(radius.isEmpty() || Integer.parseInt(radius) <=0)
        {
            textradius.setError("Radius is invalid!");
            textradius.requestFocus();
            return;
        }
        if(age.isEmpty() || Integer.parseInt(age) < currentAge)
        {
            pickDate.setError("Age is invalid!");
            pickDate.requestFocus();
            return;
        }
        userProfile.firstLogIn = false;
        userProfile.phone = Integer.parseInt(phone);
        userProfile.radius = Integer.parseInt(radius)*1000;
        userProfile.birthday = Integer.parseInt(age);
        if(emailP.isEmpty()){
            userProfile.state = "Single";
        }else{
            userProfile.state = emailP;
        }

        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(Settings.this,"User has been updated!",Toast.LENGTH_LONG).show();
                    if(firstTimeInSettings){
                        goToHome();
                    }else{
                        finish();
                        overridePendingTransition(0, 0);
                    }


                }else
                {
                    Toast.makeText(Settings.this,"Failed to update!",Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    /**
     * If back is pressed go to the previous activity
     */
    @Override
    public void onBackPressed() {
        if(!userProfile.firstLogIn){
            super.finish();
            overridePendingTransition(0, 0);
        }
    }

    /**
     * Go to Home activity
     */
    public void goToHome()
    {
        super.finish();
        startActivity(new Intent(this,Home.class));
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    /**
     * Ask the user if he want to log-out. If he wants, he goes to the login activity.
     * if he doesn't want to, he stays in the application.
     * @param v
     */
    public void onLogout(View v){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Log Out");
        alertDialog.setMessage("Do you wish to log out from your account?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "You have been logged out.", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                MainActivity.keepLogInEditor.putBoolean("keepLogInState",false);
                MainActivity.keepLogInEditor.commit();
                finish();
                startActivity(new Intent(Settings.this,MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }

    /**
     * When the user click on the heart:
     * if it is the first login, a message appears for him to complete the settings
     * if the user has match pending, the user go to the match activity
     * In another cases, a message appear saying that you don´t hava pending match
     * @param v
     */
    public void onMatch(View v){
        if(userProfile.firstLogIn){
            Toast.makeText(Settings.this,"You need to complete the settings!",Toast.LENGTH_SHORT).show();
        }
        else if(userProfile.matchPending){
            super.finish();
            //startActivity(new Intent(this,Match.class));
            overridePendingTransition(0, 0);
        }else{
            Toast.makeText(Settings.this,"You have no pending match!",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Will redirect the user for the home activity or not depending if the user has a match pending and if it is his first login
     * @param v
     */
    public void onHome(View v){
        if(!userProfile.matchPending){
            if(!userProfile.firstLogIn){
                super.finish();
                overridePendingTransition(0, 0);
            }else{
                Toast.makeText(Settings.this,"You need to complete the settings!",Toast.LENGTH_SHORT).show();
            }
        }else{
            if(!userProfile.firstLogIn){
                Toast.makeText(Settings.this,"You have a pending match!",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(Settings.this,"You need to complete the settings!",Toast.LENGTH_SHORT).show();
            }
        }
    }

}

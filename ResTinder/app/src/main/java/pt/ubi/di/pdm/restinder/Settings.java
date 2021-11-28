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

        textnome = findViewById(R.id.textnome);
        textapelido = findViewById(R.id.textapelido);
        textmailparceiro = findViewById(R.id.textmailparceiro);
        texttelemovel = findViewById(R.id.texttelemovel);
        textradius = findViewById(R.id.textradius);
        pickDate = findViewById(R.id.pickDate);

        mAuth = FirebaseAuth.getInstance();
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

    public void changeUser(View v)
    {
        String emailP = textmailparceiro.getText().toString().trim();
        String phone = texttelemovel.getText().toString().trim();
        String radius = textradius.getText().toString().trim();
        String age = pickDate.getText().toString().trim();

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
        userProfile.state = emailP;
        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(Settings.this,"User has been updated!",Toast.LENGTH_LONG).show();
                    goToHome();

                }else
                {
                    Toast.makeText(Settings.this,"Failed to update!",Toast.LENGTH_LONG).show();
                }
            }
        });


    }
    @Override
    public void onBackPressed() {
        /*finish();
        startActivity(new Intent(this,Home.class));*/
    }

    public void goToHome()
    {
        super.finish();
        startActivity(new Intent(this,Home.class));
    }
}

package pt.ubi.di.pdm.restinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class Match extends Activity
{
    public static final String CHANNEL_ID = "restinderServiceChannel";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference, matchReference, partnerReference;
    private String userID;
    private TextView nameR, locR, phP, nameT, locT, phT, text;
    private Button contact, conclude_cancel;
    User userProfile;
    private SharedPreferences boundedServ;
    private SharedPreferences.Editor boundedServEditor;

    public String name;
    public String lat;
    public String lng;
    public String partnerOne;
    public String partnerTwo;
    public String address;
    RestinderService mService;
    boolean mBound;
    boolean loggedOut = false;
    boolean firstNotification;
    boolean isPaused = false;
    boolean changeToHome=false;

    private ServiceConnection connection;
    private Intent serviceIntent;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.match);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        nameR = findViewById(R.id.nameRestaurant);
        locR = findViewById(R.id.locationRestaurant);
        phP = findViewById(R.id.phonePerson);
        nameT = findViewById(R.id.nomeMatched);
        locT = findViewById(R.id.localizacaoMatched);
        phT = findViewById(R.id.telemovelMatched);

        contact = findViewById(R.id.contactarMatched);
        conclude_cancel = findViewById(R.id.cancel);

        text = findViewById(R.id.textoMatched);

        contact.setVisibility(View.INVISIBLE);

        nameR.setVisibility(View.INVISIBLE);
        locR.setVisibility(View.INVISIBLE);
        phP.setVisibility(View.INVISIBLE);
        nameT.setVisibility(View.INVISIBLE);
        locT.setVisibility(View.INVISIBLE);
        phT.setVisibility(View.INVISIBLE);

        boundedServ = getSharedPreferences("boundedServPref",MODE_PRIVATE);
        boundedServEditor = boundedServ.edit();
        mBound = boundedServ.getBoolean("isBounded",false);
        firstNotification = boundedServ.getBoolean("firstNotification",false);
        createNotificationChannel();
        if(mBound){
            serviceIntent = new Intent(getApplicationContext(),RestinderService.class);
            stopService(serviceIntent);
            boundedServEditor.putBoolean("isBounded",false);
            boundedServEditor.commit();
            mBound = false;
            Log.d("SUPERMETHODS","I'm unbinded onCreate");
        }


        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            /**
             * Vai buscar os dados no realtime database do user que inicou sessão
             */
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
                if(userProfile != null){
                    if(!(userProfile.matchPending)){
                        System.out.println("GOTOHOME");
                        if(!isPaused){
                            System.out.println("GOTOHOME1");
                            clearNotificationPref();
                            goToHome();
                        }else{
                            System.out.println("GOTOHOME2");
                            changeToHome = true;
                        }

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(Match.this, "Failed to get user data!", Toast.LENGTH_SHORT).show();
            }
        });

        matchReference = FirebaseDatabase.getInstance().getReference("Match");
        matchReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //System.out.println(snapshot.child("partnerTwo").getValue().toString());
                Match SS = new Match();
                if(snapshot.getValue() != null && (snapshot.child("partnerOne").getValue().toString().equals(userID) || snapshot.child("partnerTwo").getValue().toString().equals(userID))) {
                    SS.lat = snapshot.child("lat").getValue().toString();
                    SS.lng = snapshot.child("lng").getValue().toString();
                    SS.name = snapshot.child("name").getValue().toString();
                    SS.partnerOne = snapshot.child("partnerOne").getValue().toString();
                    SS.partnerTwo = snapshot.child("partnerTwo").getValue().toString();
                    SS.address = snapshot.child("address").getValue().toString();

                    nameR.setVisibility(View.VISIBLE);
                    locR.setVisibility(View.VISIBLE);
                    phP.setVisibility(View.VISIBLE);
                    nameT.setVisibility(View.VISIBLE);
                    locT.setVisibility(View.VISIBLE);
                    phT.setVisibility(View.VISIBLE);
                    contact.setVisibility(View.VISIBLE);

                    text.setText("Matched!");

                    conclude_cancel.setText("Conclude Match");

                    if (SS.partnerOne != null && SS.partnerTwo != null) {
                        if (SS.partnerOne.equals(userID) || SS.partnerTwo.equals(userID)) {
                            System.out.println("matched on class");

                            System.out.println("Lat: " + Double.parseDouble(SS.lat) + "; SEM: " + SS.lat);
                            System.out.println("Lng: " + Double.parseDouble(SS.lng) + "; SEM: " + SS.lng);
                            locR.setText(SS.address);
                            nameR.setText(SS.name);
                            if (SS.partnerOne.equals(userID)) {
                                partnerReference = FirebaseDatabase.getInstance().getReference("Users");
                                partnerReference.child(SS.partnerTwo).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User partner = snapshot.getValue(User.class);
                                        if (partner != null) {
                                            phP.setText(partner.phone.toString());
                                            if(!firstNotification){
                                                firstNotification = true;
                                                boundedServEditor.putBoolean("firstNotification",true);
                                                boundedServEditor.commit();
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else{
                                partnerReference = FirebaseDatabase.getInstance().getReference("Users");
                                partnerReference.child(SS.partnerOne).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User partner = snapshot.getValue(User.class);
                                        if (partner != null) {
                                            phP.setText(partner.phone.toString());
                                            if(!firstNotification){
                                                firstNotification = true;
                                                boundedServEditor.putBoolean("firstNotification",true);
                                                boundedServEditor.commit();
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }

                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "restinderServiceChannel",
                    NotificationManager.IMPORTANCE_NONE

            );
            serviceChannel.setSound(null,null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Empty constructor for the match
     */
    public Match(){

    }

    /**
     * Constructor for the match
     * @param name name of the restaurant
     * @param lat latitude of the restaurant
     * @param lng longitude of the restaurant
     * @param partnerOne partner one
     * @param partnerTwo partner two
     * @param address address of the restaurant
     */
    public Match(String name, String lat, String lng, String partnerOne, String partnerTwo, String address)
    {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.partnerOne = partnerOne;
        this.partnerTwo = partnerTwo;
        this.address = address;
    }


    public void clearNotificationPref(){
        boolean firstNotification = boundedServ.getBoolean("firstNotification",false);
        if(firstNotification){
            boundedServEditor.putBoolean("firstNotification",false);
            boundedServEditor.commit();
        }
    }

    /**
     * if the user click Cancel or "Conclude Match", the pending match or the actual match will be cancel/concluded and a message of sucess or insucess will appear.
     * @param V
     */
    public void cancelCompleteMatch(View V){
        if(conclude_cancel.getText().equals("CANCEL")){
            OnCancel onCancel = new OnCancel(userProfile.state,userID);
            FirebaseDatabase.getInstance().getReference("OnCancel")
                    .child(userID)
                    .setValue(onCancel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(Match.this,"Match was cancelled!",Toast.LENGTH_LONG).show();
                        clearNotificationPref();

                    }else
                    {
                        Toast.makeText(Match.this,"Failed to cancel! Try again!",Toast.LENGTH_LONG).show();
                    }
                }

            });

        }else{
            OnComplete onComplete = new OnComplete(userID);
            FirebaseDatabase.getInstance().getReference("OnComplete")
                    .child(userID)
                    .setValue(onComplete).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(Match.this,"Match was concluded!",Toast.LENGTH_LONG).show();
                        clearNotificationPref();
                    }else
                    {
                        Toast.makeText(Match.this,"Failed to conclude! Try again!",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    /**
     * if the user select the option "Contact", he will be ask to choose one menssage app from his phone and then he will be redirect to that app.
     * A pre-menssage is shown on the app.
     * @param v
     */
    public void contactPerson(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + Integer.parseInt(phP.getText().toString())));
        intent.putExtra("sms_body", "Hi! We have a match on "+nameR.getText().toString()+". When do you wanna meet?");
        startActivity(intent);
    }

    /**
     * This function verify if the back is pressed 2 times. if it is, the user is redirect to the home of the operating system
     * Esta função verifica se a pessoa carregou 2x no "back"
     */
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
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
                loggedOut = true;
                finish();
                startActivity(new Intent(Match.this,MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }

    public void onSettings(View v){
        startActivity(new Intent(this, Settings.class));
        overridePendingTransition(0,0);
    }

    public void onHome(View v){
        Toast.makeText(Match.this, "You have a pending match!", Toast.LENGTH_SHORT).show();
    }

    public void goToHome(){
        super.finish();
        startActivity(new Intent(this,Home.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * SERVICES
     */

    /*@Override
    protected void onPause() {
        Log.d("SUPERMETHODS","onPauseCalled");
        isPaused = true;
        mBound = boundedServ.getBoolean("isBounded",false);
        if((loggedOut || !userProfile.matchPending)&& mBound){
            boundedServEditor.putBoolean("isBounded",false);
            boundedServEditor.commit();
            serviceIntent = new Intent(getApplicationContext(),RestinderService.class);
            stopService(serviceIntent);
            System.out.println("I'm unbinded.");
            mBound = false;
        }else if (!loggedOut && userProfile.matchPending && !mBound ){
            serviceIntent = new Intent(getApplicationContext(),RestinderService.class);
            serviceIntent.putExtra("userid",userID);
            startService(serviceIntent);
            boundedServEditor.putBoolean("isBounded",true);
            boundedServEditor.commit();
            mBound = true;
            System.out.println("I'm binded.");
        }
        super.onPause();
    }*/

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("SUPERMETHODS","onStopCalled");
        isPaused = true;
        mBound = boundedServ.getBoolean("isBounded",false);
        if((loggedOut || !userProfile.matchPending)&& mBound){
            boundedServEditor.putBoolean("isBounded",false);
            boundedServEditor.commit();
            serviceIntent = new Intent(getApplicationContext(),RestinderService.class);
            stopService(serviceIntent);
            Log.d("SUPERMETHODS","onStop Im unbinded");
            mBound = false;
        }else if (!loggedOut && userProfile.matchPending && !mBound && text.getText().toString().equals("Pending!")){
            serviceIntent = new Intent(getApplicationContext(),RestinderService.class);
            serviceIntent.putExtra("userid",userID);
            ContextCompat.startForegroundService(this,serviceIntent);
            boundedServEditor.putBoolean("isBounded",true);
            boundedServEditor.commit();
            mBound = true;
            Log.d("SUPERMETHODS","onStop Im binded");
        }
        Log.d("SUPERMETHODS","onStopCalledEnd");

    }

    /*@Override
    protected void onDestroy() {
        Log.d("SUPERMETHODS","onDestroyCalled");
        isPaused = true;
        if((loggedOut || !userProfile.matchPending)&& mBound){
            Log.d("SUPERMETHODS","I'm unbinded destroyCalled");
            boundedServEditor.putBoolean("isBounded",false);
            boundedServEditor.commit();
            serviceIntent = new Intent(getApplicationContext(),RestinderService.class);
            stopService(serviceIntent);

            mBound = false;
        }else if (!loggedOut && userProfile.matchPending && !mBound ){
            Log.d("SUPERMETHODS","I'm binded destroyCalled");
            serviceIntent = new Intent(getApplicationContext(),RestinderService.class);
            serviceIntent.putExtra("userid",userID);
            startService(serviceIntent);
            boundedServEditor.putBoolean("isBounded",true);
            boundedServEditor.commit();
            mBound = true;
        }
        Log.d("SUPERMETHODS","onDestroyCalledEnd");
        super.onDestroy();
    }*/

    @Override
    protected void onRestart() {
        super.onRestart();
        isPaused = false;
        if(mBound){
            serviceIntent = new Intent(getApplicationContext(),RestinderService.class);
            serviceIntent.putExtra("userid",userID);
            stopService(serviceIntent);
            boundedServEditor.putBoolean("isBounded",false);
            boundedServEditor.commit();
            mBound = false;
            System.out.println("I'm unbinded.");
        }
        if(changeToHome){
            goToHome();
        }
    }
}

package pt.ubi.di.pdm.restinder;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RestinderService extends Service {
    // binder given to clients
    private String userID;
    private final IBinder binder = new LocalBinder();
    User userProfile;
    boolean firstNotification;
    private SharedPreferences boundedServ;
    private SharedPreferences.Editor boundedServEditor;

    // class used for the client binder. because we know this service always
    // runs in the same process as its clients, we don't need to deal with IPC
    public class LocalBinder extends Binder {
        RestinderService getService () {
            // return this instance of LocalService so clients can call public methods
            return RestinderService.this;
        }
    }

    @Override
    public IBinder onBind (Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boundedServ = getSharedPreferences("boundedServPref",MODE_PRIVATE);
        boundedServEditor = boundedServ.edit();
        firstNotification = boundedServ.getBoolean("firstNotification",false);
        userID = intent.getStringExtra("userid");
        FirebaseDatabase.getInstance().getReference("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            /**
             * Vai buscar os dados no realtime database do user que inicou sessão
             */
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(Match.this, "Failed to get user data!", Toast.LENGTH_SHORT).show();
            }
        });
        FirebaseDatabase.getInstance().getReference("Match").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Match SS = new Match();
                if(snapshot.getValue() != null && (snapshot.child("partnerOne").getValue().toString().equals(userID) || snapshot.child("partnerTwo").getValue().toString().equals(userID))) {
                    SS.lat = snapshot.child("lat").getValue().toString();
                    SS.lng = snapshot.child("lng").getValue().toString();
                    SS.name = snapshot.child("name").getValue().toString();
                    SS.partnerOne = snapshot.child("partnerOne").getValue().toString();
                    SS.partnerTwo = snapshot.child("partnerTwo").getValue().toString();
                    SS.address = snapshot.child("address").getValue().toString();
                    if (SS.partnerOne != null && SS.partnerTwo != null) {
                        if (SS.partnerOne.equals(userID) || SS.partnerTwo.equals(userID)) {
                            if (SS.partnerOne.equals(userID)) {
                                FirebaseDatabase.getInstance().getReference("Users").child(SS.partnerTwo).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User partner = snapshot.getValue(User.class);
                                        //ADD NOTIFICATION HERE
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else{
                                FirebaseDatabase.getInstance().getReference("Users").child(SS.partnerOne).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User partner = snapshot.getValue(User.class);
                                        //ADD NOTIFICATION HERE
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        if(!firstNotification){
                            firstNotification = true;
                            boundedServEditor.putBoolean("firstNotification",true);
                            boundedServEditor.commit();
                            toastAnywhere("SIGUIA YEEET");
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
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public void toastAnywhere(final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(RestinderService.this.getApplicationContext(), text,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}

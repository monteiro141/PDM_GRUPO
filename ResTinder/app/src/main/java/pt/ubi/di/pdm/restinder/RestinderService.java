package pt.ubi.di.pdm.restinder;
import static pt.ubi.di.pdm.restinder.Match.CHANNEL_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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

    /**
     * Class for the client binder.
     */
    public class LocalBinder extends Binder {
        RestinderService getService () {
            // return this instance of LocalService so clients can call public methods
            return RestinderService.this;
        }
    }

    /**
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind (Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Function that will be used to start the service. When a match appears in the "Match" table, corresponding to the user,
     * a notification will be displayed to inform him of the match
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,notificationIntent,0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Restinder")
                .setContentText("Finding match...")
                .setSmallIcon(R.drawable.logotipo_nobg_resize)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);
        boundedServ = getSharedPreferences("boundedServPref",MODE_PRIVATE);
        boundedServEditor = boundedServ.edit();
        firstNotification = boundedServ.getBoolean("firstNotification",false);
        userID = intent.getStringExtra("userid");
        FirebaseDatabase.getInstance().getReference("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
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

                            Log.d("SUPERMETHODS","matched on service");
                            if(!firstNotification){
                                firstNotification = true;
                                boundedServEditor.putBoolean("firstNotification",true);
                                boundedServEditor.commit();
                                //toastAnywhere();
                                Intent notificationIntent = new Intent(RestinderService.this,MainActivity.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(RestinderService.this,
                                        0,notificationIntent,0);
                                Notification notification = new NotificationCompat.Builder(RestinderService.this, CHANNEL_ID)
                                        .setContentTitle("You have a match!")
                                        .setContentText("Click to see more details")
                                        .setSmallIcon(R.drawable.logotipo_nobg_resize)
                                        .setContentIntent(pendingIntent)
                                        .build();
                                startForeground(1,notification);

                            }
                            Log.d("SUPERMETHODS","matched on service end");
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

        //stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
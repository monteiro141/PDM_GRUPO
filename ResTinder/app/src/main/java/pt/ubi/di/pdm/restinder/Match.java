package pt.ubi.di.pdm.restinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class Match extends Activity
{
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference, matchReference, partnerReference;
    private String userID;
    private TextView nameR, locR, phP, nameT, locT, phT, text;
    private Button contact, conclude_cancel;
    User userProfile;

    public String name;
    public String lat;
    public String lng;
    public String partnerOne;
    public String partnerTwo;

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


        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            /**
             * Vai buscar os dados no realtime database do user que inicou sessão
             */
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Match.this, "Failed to get user data!", Toast.LENGTH_SHORT).show();
            }
        });

        matchReference = FirebaseDatabase.getInstance().getReference("Match");
        matchReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //System.out.println(snapshot.child("partnerTwo").getValue().toString());
                Match SS = new Match();
                if(snapshot.getValue() != null) {
                    SS.lat = snapshot.child("lat").getValue().toString();
                    SS.lng = snapshot.child("lng").getValue().toString();
                    SS.name = snapshot.child("name").getValue().toString();
                    SS.partnerOne = snapshot.child("partnerOne").getValue().toString();
                    SS.partnerTwo = snapshot.child("partnerTwo").getValue().toString();

                    nameR.setVisibility(View.VISIBLE);
                    locR.setVisibility(View.VISIBLE);
                    phP.setVisibility(View.VISIBLE);
                    nameT.setVisibility(View.VISIBLE);
                    locT.setVisibility(View.VISIBLE);
                    phT.setVisibility(View.VISIBLE);
                    contact.setVisibility(View.VISIBLE);

                    text.setText("Matched!");

                    conclude_cancel.setText("Conclude Match");

                    if (SS != null) {
                        if (SS.partnerOne.equals(userID) || SS.partnerTwo.equals(userID)) {
                            locR.setText(getAddress(Double.parseDouble(SS.lat),Double.parseDouble(SS.lng)));
                            System.out.println("Lat: " + Double.parseDouble(SS.lat) + "; SEM: " + SS.lat);
                            System.out.println("Lng: " + Double.parseDouble(SS.lng) + "; SEM: " + SS.lng);

                            nameR.setText(SS.name);
                            if (SS.partnerOne.equals(userID)) {
                                partnerReference = FirebaseDatabase.getInstance().getReference("Users");
                                partnerReference.child(SS.partnerTwo).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User partner = snapshot.getValue(User.class);
                                        if (partner != null) {
                                            phP.setText(partner.phone.toString());
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

    public Match(){

    }

    public Match(String name, String lat, String lng, String partnerOne, String partnerTwo) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.partnerOne = partnerOne;
        this.partnerTwo = partnerTwo;
    }

    /*public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getPartnerOne() {
        return partnerOne;
    }

    public void setPartnerOne(String partnerOne) {
        this.partnerOne = partnerOne;
    }

    public String getPartnerTwo() {
        return partnerTwo;
    }

    public void setPartnerTwo(String partnerTwo) {
        this.partnerTwo = partnerTwo;
    }*/

    public void cancelMatch(View V){

    }

    @Override
    public void onBackPressed() {
        /*if(!userProfile.matchPending){
            super.finish();
            startActivity();
        }*/
    }

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
                startActivity(new Intent(Match.this,MainActivity.class));
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
    }

    public void onHome(View v){
        Toast.makeText(Match.this, "You have a pending match!", Toast.LENGTH_SHORT).show();
    }

    private String getAddress(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }



}

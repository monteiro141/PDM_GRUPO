package pt.ubi.di.pdm.restinder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Home extends Activity implements LocationListener{
    private ImageButton logout;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private ArrayList<Restaurants> restaurantsList;
    private ImageView restaurantView;
    private TextView nameRestaurantDisplay;
    private double currentLat = 0.0, currentLong = 0.0;
    private double radius;

    private Swipe personSwipes;
    private boolean gotUser;
    private int positionCard;

    /*Teste tinder*/
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;

    User userProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_test_tinder);
        gotUser=false;

        restaurantsList = new ArrayList<>();

        FirebaseInicialized();






        }

    @Override
    public void onBackPressed() {

    }
    public void goToMainActivity(){
        finish();
        startActivity(new Intent(Home.this,MainActivity.class));
    }

    public void addRestaurant(JSONObject jsonObject){
        try {
            restaurantsList.add(new Restaurants(jsonObject.getString("name"),
                    jsonObject.getJSONArray("photos").getJSONObject(0).getString("photo_reference"),
                    jsonObject.getString("business_status"),
                    jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat"),
                    jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLong = location.getLongitude();
        currentLat = location.getLatitude();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        System.out.println("");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        System.out.println("");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("");
    }

    public void tinderSwipe(){
        personSwipes=new Swipe();
        personSwipes.email=userProfile.email;
        personSwipes.state=userProfile.state;

        CardStackView cardStackView = findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
                Log.d("TAG", "onCardDragging: d=" + direction.name() + " ratio=" + ratio);
            }

            @Override
            public void onCardSwiped(Direction direction) {
                //Log.d("TAG", "onCardSwiped: p=" + manager.getTopPosition() + " d=" + direction);
                if (direction == Direction.Right){
                    personSwipes.addElementToAccepted(restaurantsList.get(positionCard));
                    //Toast.makeText(Home.this, "Direction Right", Toast.LENGTH_SHORT).show();
                }
                if (direction == Direction.Left){
                    //Toast.makeText(Home.this, "Direction Left", Toast.LENGTH_SHORT).show();
                }
                // Paginating
                if (manager.getTopPosition() == adapter.getItemCount() - 5){
                    paginate();
                }

                if(manager.getTopPosition()==restaurantsList.size()){
                    addToFirebase();
                }

            }

            @Override
            public void onCardRewound() {
                Log.d("TAG", "onCardRewound: " + manager.getTopPosition());
            }

            @Override
            public void onCardCanceled() {
                Log.d("TAG", "onCardRewound: " + manager.getTopPosition());
            }

            @Override
            public void onCardAppeared(View view, int position) {
                TextView tv = view.findViewById(R.id.item_name);
                Log.d("TAG", "onCardAppeared: " + position + ", nama: " + tv.getText());
                positionCard=position;

            }

            @Override
            public void onCardDisappeared(View view, int position) {
                TextView tv = view.findViewById(R.id.item_name);
                Log.d("TAG", "onCardAppeared: " + position + ", nama: " + tv.getText());
            }
        });
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setSwipeableMethod(SwipeableMethod.Manual);
        manager.setOverlayInterpolator(new LinearInterpolator());
        adapter = new CardStackAdapter(addList());
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
        cardStackView.setItemAnimator(new DefaultItemAnimator());

    }

    private void paginate() {
        List<ItemModel> old = adapter.getItems();
        List<ItemModel> baru = new ArrayList<>(addList());
        CardStackCallback callback = new CardStackCallback(old, baru);
        DiffUtil.DiffResult hasil = DiffUtil.calculateDiff(callback);
        adapter.setItems(baru);
        hasil.dispatchUpdatesTo(adapter);
    }

    private List<ItemModel> addList() {
        List<ItemModel> items = new ArrayList<>();
        for (Restaurants i: restaurantsList) {
            items.add(new ItemModel(i.getName(),i.getImgURl()));
        }


        return items;
    }

    private void FirebaseInicialized(){
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
                if(userProfile.matchPending){
                    goToMatch();
                }else
                    setPlacesAPI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Home.this, "Failed to get user data!", Toast.LENGTH_LONG).show();
            }


        });
    }

    public void setPlacesAPI(){
        //Get the current location of the user. First we need to verify if the permission is granted.
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationManager locationManager= (LocationManager)getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            currentLat=location.getLatitude();
            currentLong=location.getLongitude();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url="https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
                "location="+ currentLat+","+currentLong +
                "&radius=" + userProfile.radius +
                "&type=restaurant"+
                "&sensor=true"+
                "&key=" + getResources().getString(R.string.googlePlacesKey);
        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = response.getJSONArray("results");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                addRestaurant(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        tinderSwipe();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Json", "Impossível ler");
            }

        });

        queue.add(data);
    }

    public void addToFirebase(){
        userProfile.matchPending=true;
        reference.child(userID).setValue(userProfile);
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Swipes");
        userID = user.getUid();

        reference.child(userID)
                .setValue(personSwipes).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            goToMatch();
                        }
                    }
                });
    }

    public void goToMatch(){
        super.finish();
        startActivity(new Intent(this,Match.class));
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
                goToMainActivity();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }

    public void onSettings(View v){
        startActivity(new Intent(this,Settings.class));
    }

    public void onMatch(View v){
        if(!userProfile.matchPending){
            Toast.makeText(Home.this,"You have no pending match!",Toast.LENGTH_SHORT).show();
        }
        else{
            goToMatch();
        }
    }
}

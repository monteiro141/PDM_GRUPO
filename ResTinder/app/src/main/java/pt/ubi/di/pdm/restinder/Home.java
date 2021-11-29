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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

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
    private double radius = 5000;
    private float x,y;
    User userProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        restaurantsList = new ArrayList<>();

        restaurantView = findViewById(R.id.image_restaurant);
        nameRestaurantDisplay = findViewById(R.id.Restaurant_NameID);


        mAuth = FirebaseAuth.getInstance();
        logout = (ImageButton) findViewById(R.id.btn_back);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                goToMainActivity();
            }
        });
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Home.this, "Failed to get user data!", Toast.LENGTH_LONG).show();
            }
        });


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
                    "&radius=" + radius +
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

                            if(restaurantsList.size()==0){
                                restaurantView.setBackgroundResource(R.drawable.no_restaurants);
                            }else{
                                String url2="https://maps.googleapis.com/maps/api/place/photo" +
                                        "?maxwidth=800" +
                                        "&photo_reference=" +restaurantsList.get(0).getImgURl() +
                                        "&key=" + getResources().getString(R.string.googlePlacesKey);
                                Picasso.get().load(url2).into(restaurantView);
                                nameRestaurantDisplay.setText(restaurantsList.get(0).getName());
                            }



                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Json", "Impossível ler");
                }

            });

            queue.add(data);
        /*restaurantView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                LinearLayout.LayoutParams layoutParams =(LinearLayout.LayoutParams) restaurantView.getLayoutParams();

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x=view.getX() - motionEvent.getRawX();
                        y=view.getY() - motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.animate().x(motionEvent.getRawX() +x).y(motionEvent.getRawY()+y).setDuration(0).start();
                        break;
                    default:
                        return false;
                }

                return true;
            }
        });*/
        }

    @Override
    public void onBackPressed() {
        FirebaseAuth.getInstance().signOut();
        goToMainActivity();
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

}

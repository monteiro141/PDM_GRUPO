package pt.ubi.di.pdm.restinder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private boolean wentToMatch=false;
    private int positionCard;
    private int currentRadius = -1;
    private String nextPageToken;
    private boolean hasNextPageToken=false;
    String urlwithToken="";
    String url;
    RequestQueue queue;

    boolean mBound;
    boolean firstNotification;
    private SharedPreferences boundedServ;
    private SharedPreferences.Editor boundedServEditor;

    /*Teste tinder*/
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;

    User userProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        gotUser=false;

        restaurantsList = new ArrayList<>();
        boundedServ = getSharedPreferences("boundedServPref",MODE_PRIVATE);
        boundedServEditor = boundedServ.edit();
        mBound = boundedServ.getBoolean("isBounded",false);
        firstNotification = boundedServ.getBoolean("firstNotification",false);
        if(mBound){
            mBound = false;
            boundedServEditor.putBoolean("isBounded",false);
            boundedServEditor.commit();
        }
        if(firstNotification){
            firstNotification = false;
            boundedServEditor.putBoolean("firstNotification",false);
            boundedServEditor.commit();
        }
        FirebaseInicialized();
    }

    /**
     * Inicialize the firebase with the current user instance
     * If the data changes and the user has match pending, he will be redirected for the match activity
     * If the data changes and the user doesn't have match pending, the cards for the swipes will be displayed
     */
    private void FirebaseInicialized(){
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        FirebaseDatabase.getInstance().getReference("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
                if(userProfile != null){
                    if(userProfile.matchPending && !wentToMatch){
                        goToMatch();
                        wentToMatch = true;
                    }else{
                        if(currentRadius == -1 || currentRadius != userProfile.radius){
                            personSwipes=new Swipe();
                            setPlacesAPI();
                        }
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(Home.this, "Failed to get user data!", Toast.LENGTH_LONG).show();
            }


        });
    }

    /**
     * Go to the match activity
     */
    public void goToMatch(){
        super.finish();
        startActivity(new Intent(this,Match.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Used to get the onsucess callback of VollyCallBAck
     */
    public interface VolleyCallBack {
        void onSuccess();
    }

    /**
     * Add a page of the Places API to the restaurantslist.
     * @param callBack
     */
    public void addAllToQueue(final VolleyCallBack callBack){
        url="https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
                "location="+ currentLat+","+currentLong +
                "&radius=" + userProfile.radius +
                "&type=restaurant"+
                "&sensor=true"+
                "&key=" + getResources().getString(R.string.googlePlacesKey);

        if(urlwithToken.equals("")){
            //The inicial url
            urlwithToken="https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
                    "location="+ currentLat+","+currentLong +
                    "&radius=" + userProfile.radius +
                    "&type=restaurant"+
                    "&sensor=true"+
                    "&key=" + getResources().getString(R.string.googlePlacesKey);
        }else {
            //The url to open another other pages provided by Places API
            urlwithToken = url + "&pagetoken="+nextPageToken;
        }
        queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(new JsonObjectRequest(Request.Method.GET, urlwithToken, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                JSONArray jsonArray = null;

                                try {
                                    jsonArray = response.getJSONArray("results");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }finally {
                                    int i;
                                    JSONObject jsonObject;
                                    for (i = 0; i < jsonArray.length(); i++) {
                                        try {
                                            jsonObject = jsonArray.getJSONObject(i);
                                            addRestaurant(jsonObject);
                                        } catch (JSONException e) {
                                            Log.d("JSONDEBUG","Exception "+e.toString());
                                        }
                                    }
                                }
                                try{
                                    if(response.has("next_page_token")){
                                        nextPageToken = response.getString("next_page_token");
                                        hasNextPageToken = true;
                                        urlwithToken = url + "&pagetoken="+nextPageToken;

                                    }else{
                                        hasNextPageToken = false;
                                    }

                                }catch (JSONException e){
                                    Log.d("JSONDEBUG",e.toString());
                                }finally {
                                    callBack.onSuccess();
                                }


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Json", "Impossible to read");
                    }

                })
        );

    }

    /**
     * Check if the user gave permissions to the app. Then the program will get the current longitude an latitude to be used to get the restaurants near the user (with the radius defined by the user).
     */
    public void setPlacesAPI(){
        if(urlwithToken.equals("")){
            //Get the current location of the user. First we need to verify if the permission is granted.
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                LocationManager locationManager= (LocationManager)getSystemService(LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location == null){
                    LocationManager locationManager2= (LocationManager)getSystemService(LOCATION_SERVICE);
                    locationManager2.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
                    Location location2=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    currentLat=location2.getLatitude();
                    currentLong=location2.getLongitude();
                }else{
                    currentLat=location.getLatitude();
                    currentLong=location.getLongitude();
                }

            }

            currentRadius = userProfile.radius;
        }
        //clear the restaurants list in order to get more restaurants places, if possible
        restaurantsList.clear();
            addAllToQueue(new VolleyCallBack() {
                @Override
                public void onSuccess() {
                        tinderSwipe();
                        Log.d("JSONDEBUG", "TINDERSWIPE");
                    }
            });
    }

    /**
     * If there is no json atribute "photos" or "photo_reference" the restaurant is discarded
     * @param jsonObject
     */
    public void addRestaurant(JSONObject jsonObject){
        try {
            restaurantsList.add(new Restaurants(jsonObject.getString("name"),
                    jsonObject.getJSONArray("photos").getJSONObject(0).getString("photo_reference"),
                    jsonObject.getString("business_status"),
                    jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat"),
                    jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng"),
                    jsonObject.getString("vicinity"))
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the cards to the user. if he swipes left the restaurant is rejected. if he swipes right the restaurant is accepted and saved on a list
     */
    public void tinderSwipe(){

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
                }
                if (direction == Direction.Left){
                }

                //The user has done all the swipes
                if(manager.getTopPosition()==restaurantsList.size()){
                    //if there is no page token the restaurants accepted list is added to firebase.
                    //if there is a page token, the program will display more swipes with more restaurants to the user
                    //if none of the above has occurred, the program tell the user to change radius on the settings
                    if(personSwipes.restaurantAccepted.size() != 0 && !hasNextPageToken)
                    {
                        addToFirebase();
                    }
                    else if(personSwipes.restaurantAccepted.size() != 0 && hasNextPageToken){
                        setPlacesAPI();
                    }
                    else {
                        Toast.makeText(Home.this,"Change radius in settings",Toast.LENGTH_SHORT).show();
                    }
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

    /**
     * Save the user's swipe in the firebase
     */
    public void addToFirebase(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Swipes");
        userID = user.getUid();

        FirebaseDatabase.getInstance().getReference("Swipes").child(userID)
                .setValue(personSwipes).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    userProfile.matchPending=true;
                    FirebaseDatabase.getInstance().getReference("Users").child(userID).setValue(userProfile);
                }
            }
        });
    }




    /**
     * This function verify if the user click 2 times in "back"
     * if he clicks two times, the app will close
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
     * The user is redirected to the home activity
     */
    public void goToMainActivity(){
        finish();
        startActivity(new Intent(Home.this,MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {}

    @Override
    public void onProviderDisabled(@NonNull String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    /**
     * Add all the restaurants to a list to be used on the cards swipe
     * @return
     */
    private List<ItemModel> addList() {
        List<ItemModel> items = new ArrayList<>();
        for (Restaurants i: restaurantsList) {
            items.add(new ItemModel(i.getName(),i.getImgURl()));
        }


        return items;
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
                goToMainActivity();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }

    /**
     * if click settings icon the user go to the settings activity
     * @param v
     */
    public void onSettings(View v){
        startActivity(new Intent(this,Settings.class));
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    /**
     * if the user click match, the system will show a message.
     * @param v
     */
    public void onMatch(View v){
        if(!userProfile.matchPending){
            Toast.makeText(Home.this,"You have no pending match!",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * if the user click Home, the swipes will be reset
     * @param V
     */
    public void onHome(View V){
        nextPageToken="";
        personSwipes=new Swipe();
        setPlacesAPI();

    }
}
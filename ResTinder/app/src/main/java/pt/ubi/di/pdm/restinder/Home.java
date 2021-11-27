package pt.ubi.di.pdm.restinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

public class Home extends Activity
{
    private ImageButton logout;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private ArrayList<Restaurants> restaurantsList;
    private ImageView restaurantView;
    private TextView nameRestaurantDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        restaurantsList=new ArrayList<>();

        restaurantView=findViewById(R.id.image_restaurant);
        nameRestaurantDisplay=findViewById(R.id.Restaurant_NameID);

        mAuth = FirebaseAuth.getInstance();
        logout = (ImageButton) findViewById(R.id.btn_back);

        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
                goToMainActivity();
            }
        });
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID= user.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            /**
             * Vai buscar os dados no realtime database do user que inicou sessão
             */
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User userProfile = dataSnapshot.getValue(User.class);
                if(userProfile != null){
                    String email = userProfile.email;
                    String firstName = userProfile.firstName;
                    String lastName = userProfile.lastName;
                    String birthday = userProfile.birthday;
                    String state = userProfile.state;
                    String gender = userProfile.gender;
                    String phone = userProfile.phone;
                    String interestedIn = userProfile.interestedIn;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Home.this,"Failed to get user data!",Toast.LENGTH_LONG).show();
            }
        });

        RequestQueue queue= Volley.newRequestQueue(this);
        /*String url="https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
                "location=41.44264850991976,-8.303307518099446"+
                "&radius=500" +
                "&type=restaurant"+
                "&key=" + getResources().getString(R.string.googlePlacesKey);*/

        String url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=40.271673892292775,-7.500621264363033&radius=5000&type=restaurant&key=AIzaSyCkPy2xKkFKwz4wr49yUXU9v66Bb7J38-Y";
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
                                //System.out.println(jsonObject.getString("name"));
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


}

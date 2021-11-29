package pt.ubi.di.pdm.restinder;

import java.util.ArrayList;

public class Swipe {
    public String email;
    public String state;
    public ArrayList<Restaurants> restaurantAccepted;

    public Swipe(){
        this.restaurantAccepted=new ArrayList<>();
    }

    public Swipe(String email, String state, ArrayList<Restaurants> restaurantAccepted) {
        this.email = email;
        this.state = state;
        this.restaurantAccepted = restaurantAccepted;
    }

    public void addElementToAccepted(Restaurants restaurant){
        this.restaurantAccepted.add(restaurant);
    }
}

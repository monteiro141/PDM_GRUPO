package pt.ubi.di.pdm.restinder;

public class Restaurants {
    public String name;
    public String imgURl;
    public String businessStatus;
    public String lat;
    public String lng;

    public Restaurants(){

    }
    public Restaurants(String name, String imgURl, String businessStatus, String lat, String lng){
        this.name=name;
        this.imgURl=imgURl;
        this.businessStatus=businessStatus;
        this.lat=lat;
        this.lng=lng;
    }

    public String getName() {
        return name;
    }

    public String getImgURl() {
        return imgURl;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }


}

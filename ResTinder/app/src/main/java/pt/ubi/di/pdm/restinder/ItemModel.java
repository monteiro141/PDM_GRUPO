package pt.ubi.di.pdm.restinder;

public class ItemModel {
    private String name, imageurl;
    public ItemModel()
    {

    }

    /**
     * Constructor for the item model to be used on the swipe card
     * @param name
     * @param imageurl
     */
    public ItemModel(String name,String imageurl)
    {
        this.name = name;
        this.imageurl=imageurl;
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

}

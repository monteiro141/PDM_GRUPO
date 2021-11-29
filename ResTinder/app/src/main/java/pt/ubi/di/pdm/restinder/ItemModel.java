package pt.ubi.di.pdm.restinder;

public class ItemModel {
    private int image;
    private String name, city,age;
    public ItemModel()
    {

    }
    public ItemModel(int image, String name, String city, String age)
    {
        this.image = image;
        this.name = name;
        this.city = city;
        this.age = age;
    }
    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }


}

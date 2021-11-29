package pt.ubi.di.pdm.restinder;


import java.io.Serializable;

public class User implements Serializable
{
    public String email,firstName, lastName, state,gender,interestedIn;
    public Integer birthday;
    public Integer phone;
    public Integer radius;
    public Boolean firstLogIn;
    public boolean matchPending;
    public User()
    {

    }
    public User(String Email,String FirstName, String LastName, Integer Birthday, String State,String Gender,Integer Phone,String InterestedIn)
    {
        this.email = Email;
        this.firstName = FirstName;
        this.lastName = LastName;
        this.birthday = Birthday;
        this.state = State;
        this.gender = Gender;
        this.phone = Phone;
        this.interestedIn = InterestedIn;
        this.matchPending=false;
        firstLogIn = true;
        radius = -1;
    }
}

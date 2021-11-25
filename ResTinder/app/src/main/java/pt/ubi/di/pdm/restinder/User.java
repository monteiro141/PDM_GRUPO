package pt.ubi.di.pdm.restinder;


import java.io.Serializable;

public class User implements Serializable
{
    public String email,firstName, lastName,birthday, state,gender,phone,interestedIn;
    public User()
    {

    }
    public User(String Email,String FirstName, String LastName, String Birthday, String State,String Gender,String Phone,String InterestedIn)
    {
        this.email = Email;
        this.firstName = FirstName;
        this.lastName = LastName;
        this.birthday = Birthday;
        this.state = State;
        this.gender = Gender;
        this.phone = Phone;
        this.interestedIn = InterestedIn;
    }
}

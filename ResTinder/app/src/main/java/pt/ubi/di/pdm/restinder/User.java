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

    /**
     * Empty constructor for the user
     */
    public User()
    {

    }

    /**
     * Constructor for the user
     * @param Email
     * @param FirstName
     * @param LastName
     * @param Birthday
     * @param State
     * @param Gender
     * @param Phone
     * @param InterestedIn
     */
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

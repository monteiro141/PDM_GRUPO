package pt.ubi.di.pdm.restinder;

/**
 * This class is used to send a match to the realtime database that will be completed later
 */
public class OnComplete {
    public String userid;

    /**
     * Empty Constructor for the OnComplete
     */
    public OnComplete() {
    }

    /**
     * Constructor for the Oncomplete
     * @param userid
     */
    public OnComplete(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}

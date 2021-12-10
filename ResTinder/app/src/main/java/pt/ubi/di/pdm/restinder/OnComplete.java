package pt.ubi.di.pdm.restinder;

public class OnComplete {
    public String userid;

    public OnComplete() {
    }

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

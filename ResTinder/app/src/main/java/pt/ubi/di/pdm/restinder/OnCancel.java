package pt.ubi.di.pdm.restinder;

public class OnCancel {
    private String state,userid;

    public OnCancel() {
    }

    public OnCancel(String state,String userid) {
        this.state = state;
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

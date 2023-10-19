package app.simple.inure.models;

import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class Tracker implements Parcelable {
    
    private String name;
    private String trackerId;
    private boolean isActivity = false;
    private boolean isService = false;
    private boolean isReceiver = false;
    private boolean isBlocked = false;
    private boolean isEnabled = true;
    private ActivityInfo activityInfo = null;
    private ServiceInfo serviceInfo = null;
    public static final Creator <Tracker> CREATOR = new Creator <Tracker>() {
        @Override
        public Tracker createFromParcel(Parcel in) {
            return new Tracker(in);
        }
        
        @Override
        public Tracker[] newArray(int size) {
            return new Tracker[size];
        }
    };
    public boolean isLogged = false;
    
    public Tracker() {
    }
    
    public Tracker(String name, String trackerId, boolean isActivity, boolean isService, boolean isReceiver, boolean isBlocked, boolean isEnabled, ActivityInfo activityInfo, ServiceInfo serviceInfo, boolean isLogged) {
        this.name = name;
        this.trackerId = trackerId;
        this.isActivity = isActivity;
        this.isService = isService;
        this.isReceiver = isReceiver;
        this.isBlocked = isBlocked;
        this.isEnabled = isEnabled;
        this.activityInfo = activityInfo;
        this.serviceInfo = serviceInfo;
        this.isLogged = isLogged;
    }
    
    protected Tracker(Parcel in) {
        name = in.readString();
        trackerId = in.readString();
        isActivity = in.readByte() != 0;
        isService = in.readByte() != 0;
        isReceiver = in.readByte() != 0;
        isBlocked = in.readByte() != 0;
        isEnabled = in.readByte() != 0;
        activityInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
        serviceInfo = in.readParcelable(ServiceInfo.class.getClassLoader());
        isLogged = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(trackerId);
        dest.writeByte((byte) (isActivity ? 1 : 0));
        dest.writeByte((byte) (isService ? 1 : 0));
        dest.writeByte((byte) (isReceiver ? 1 : 0));
        dest.writeByte((byte) (isBlocked ? 1 : 0));
        dest.writeByte((byte) (isEnabled ? 1 : 0));
        dest.writeParcelable(activityInfo, flags);
        dest.writeParcelable(serviceInfo, flags);
        dest.writeByte((byte) (isLogged ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTrackerId() {
        return trackerId;
    }
    
    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }
    
    public boolean isActivity() {
        return isActivity;
    }
    
    public void setActivity(boolean activity) {
        isActivity = activity;
    }
    
    public boolean isService() {
        return isService;
    }
    
    public void setService(boolean service) {
        isService = service;
    }
    
    public boolean isReceiver() {
        return isReceiver;
    }
    
    public void setReceiver(boolean receiver) {
        isReceiver = receiver;
    }
    
    public boolean isBlocked() {
        return isBlocked;
    }
    
    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    
    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }
    
    public void setActivityInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }
    
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
    
    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
    public boolean isLogged() {
        return isLogged;
    }
    
    public void setLogged(boolean logged) {
        isLogged = logged;
    }
    
    @Override
    public String toString() {
        return "Tracker{" +
                "name='" + name + '\'' +
                ", trackerId='" + trackerId + '\'' +
                ", isActivity=" + isActivity +
                ", isService=" + isService +
                ", isReceiver=" + isReceiver +
                ", isBlocked=" + isBlocked +
                ", isEnabled=" + isEnabled +
                ", activityInfo=" + activityInfo +
                ", serviceInfo=" + serviceInfo +
                ", isLogged=" + isLogged +
                '}';
    }
}

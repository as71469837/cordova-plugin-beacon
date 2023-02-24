package huayu.cordova.plugin.beacon;

public class BeaconModel {
    public String minor;
    public String major;
    public String uuid;
    public int rssi;

    public BeaconModel() {

    }

    public BeaconModel(String uuid, String major, String minor, int rssi) {
        this.minor = minor;
        this.major = major;
        this.uuid = uuid;
        this.rssi = rssi;
    }


}

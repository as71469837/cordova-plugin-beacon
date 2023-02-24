package huayu.cordova.plugin.beacon;

public class PostBeaconModel {
    public String user_uuid;
    public String beacons;
    /**
     * 是否同步返回定位结果，1表示返回；0表示不返回。默认不返回
     */
    public int synchronize;


    public PostBeaconModel() {

    }

    public PostBeaconModel(String uuid, String beacons, int synchronize) {
        this.user_uuid = uuid;
        this.beacons = beacons;
        this.synchronize = synchronize;
    }
}

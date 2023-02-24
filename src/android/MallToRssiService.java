package huayu.cordova.plugin.beacon;

import java.util.*;

import retrofit2.Call;
import retrofit2.http.*;

public interface MallToRssiService {

    /**
     * 获取beacon数据
     * @param uuid
     * @param appId
     * @param currentTime
     * @param signatureVersion
     * @param signatureNonce
     * @param signature
     * @return
     */
    @Headers("Accept:application/json")
    @GET("api/beacon_uuid")
    Call<List<String>> GetBeaconUUIDs(@Header("UUID") String uuid
            ,@Header("App-Id") String appId
            ,@Header("Timestamp") String currentTime
            ,@Header("Signature-Version") String signatureVersion
            ,@Header("Signature-Nonce") String signatureNonce
            ,@Header("Signature") String signature
    );


    /**
     * 上报beacon数据
     * @param uuid
     * @param appId
     * @param currentTime
     * @param signatureVersion
     * @param signatureNonce
     * @param signature
     * @return
     */
    @Headers("Accept:application/json")
    @POST("api/lbs/location_data")
    Call<Object> PostBeaconData(@Header("UUID") String uuid
            ,@Header("App-Id") String appId
            ,@Header("Timestamp") String currentTime
            ,@Header("Signature-Version") String signatureVersion
            ,@Header("Signature-Nonce") String signatureNonce
            ,@Header("Signature") String signature
            ,@Body PostBeaconModel beaconModel
    );


}


package huayu.cordova.plugin.beacon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import org.altbeacon.beacon.service.ArmaRssiFilter;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.BeaconParser;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;

/**
 * This class echoes a string called from JavaScript.
 */
public class beaconHelper extends CordovaPlugin {

  private static final Queue<Map<String, Object>> BeaconQueue = new ArrayDeque<>();
  private static final Pattern pattern = Pattern.compile("a\\db1\\w+");
  private static CallbackContext excingCallbackContext = null;
  private static final String encoding = "UTF-8";
  private static final int requestScanPermission = 32;
  private static final int requestPositionPermission = 35;

  private String TAG = "Beacon";
  private BeaconManager beaconManager = null;
  public static final Region wildcardRegion = new Region("wildcardRegion", null, null, null);

  private String[] mBluetoothScanPermissionList = new String[] {
      Manifest.permission.BLUETOOTH,
      Manifest.permission.BLUETOOTH_ADMIN,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
  };
  private static final Retrofit retrofit = new Retrofit.Builder()
      .baseUrl("https://test-easy.mall-to.com/")
      // .baseUrl("https://integration-easy.mall-to.com/")
      .addConverterFactory(GsonConverterFactory.create())
      .build();
  private static final MallToRssiService rssiService = retrofit.create(MallToRssiService.class);
  Location location3C = new Location(22.5367032085214, 113.952907056939);
  Location location3D = new Location(22.5367044853623, 113.952848325464);
  Location location38 = new Location(22.536757437687, 113.952908618687);
  Location location37 = new Location(22.5367581069149, 113.952849836473);
  Location locationCenter = new Location(22.536696012, 113.952835325464);

  Point point3C = conveterLocationToPoint(location3C);
  Point point3D = conveterLocationToPoint(location3D);
  Point point38 = conveterLocationToPoint(location38);
  Point point37 = conveterLocationToPoint(location37);
  Point pointCenter = conveterLocationToPoint(locationCenter);
  Gson gson = new Gson();

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    excingCallbackContext = callbackContext;
    if (action.equals("coolMethod")) {
      String message = args.getString(0);
      this.coolMethod(message, callbackContext);
      return true;
    }
    if (action.equals("startListening")) {
      cordova.requestPermissions(this, requestScanPermission, mBluetoothScanPermissionList);
      // this.startListening(callbackContext);
      return true;
    } else if (action.equals("startPositioning")) {
      // this.startPositioning(callbackContext);
      cordova.requestPermissions(this, requestPositionPermission, mBluetoothScanPermissionList);
      return true;
    } else if (action.equals("stopListening")) {
      this.stopListening(callbackContext);
      return true;
    }
    return false;
  }

  private void coolMethod(String message, CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
      callbackContext.success(message);
    } else {
      callbackContext.error("Expected one non-empty string argument.");
    }
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
      throws JSONException {
    if (requestCode == requestScanPermission || requestCode == requestPositionPermission) {
      for (int r : grantResults) {
        if (r == PackageManager.PERMISSION_DENIED) {
          excingCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION));
          return;
        }
      }
      switch (requestCode) {
        case requestScanPermission:
          startListening(excingCallbackContext);
          break;
        case requestPositionPermission:
          startPositioning(excingCallbackContext);
          break;
      }
    }
  }

  private void startListening(CallbackContext callbackContext) {
    if (null != beaconManager) {
      return;
    }
    beaconManager = BeaconManager.getInstanceForApplication(cordova.getContext());
    beaconManager.getBeaconParsers().clear();
    beaconManager.setForegroundScanPeriod(3 * 1000L);
    beaconManager.getBeaconParsers()
        .add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

    RangeNotifier rangeNotifier = new RangeNotifier() {
      @Override
      public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          beacons.forEach(beacon -> {
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("UUID", beacon.getId1().toUuid().toString());
            tempMap.put("Mac", beacon.getBluetoothAddress());
            tempMap.put("Name", beacon.getBluetoothName());
            tempMap.put("Major", beacon.getId2().toString());
            tempMap.put("Minor", beacon.getId3().toString());
            tempMap.put("RSSI", beacon.getRssi());
            tempMap.put("Distance", beacon.getDistance());
            results.add(tempMap);
          });
        }
        if (results.size() > 0) {
          JSONArray jsonArray = new JSONArray(results);
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonArray.toString());
          pluginResult.setKeepCallback(true);
          callbackContext.sendPluginResult(pluginResult);
        }
      }
    };
    beaconManager.addRangeNotifier(rangeNotifier);
    beaconManager.startRangingBeacons(wildcardRegion);
  }

  private void startPositioning(CallbackContext callbackContext) {
    if (null != beaconManager) {
      return;
    }
    BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
    beaconManager = BeaconManager.getInstanceForApplication(cordova.getContext());
    beaconManager.getBeaconParsers().clear();
    beaconManager.setForegroundScanPeriod(600L);
    beaconManager.getBeaconParsers()
        .add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

    RangeNotifier rangeNotifier = new RangeNotifier() {
      @Override
      public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        List<BeaconModel> results = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          beacons.forEach(beacon -> {
            String filterUuid = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825";
            String currentBeaconUuid = beacon.getId1().toUuid().toString();
            if (currentBeaconUuid.equalsIgnoreCase(filterUuid)) {
              BeaconModel tempModel = new BeaconModel(currentBeaconUuid, beacon.getId2().toString(),
                  beacon.getId3().toString(), beacon.getRssi());
              results.add(tempModel);
            }
          });
        }
        if (results.size() > 3) {
          String beaconsString = "";
          try {
            beaconsString = gson.toJson(results);
            Log.i("Beacon", beaconsString);
          } catch (Exception exc) {
            exc.printStackTrace();
            Log.e("startListening", exc.getMessage(), exc);
            CallJs(false,"解析beacons数据失败",callbackContext);
            return;
          }

          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String uuid = "1000194";
          String appId = "999";
          String currentTime = dateFormat.format(System.currentTimeMillis());
          String signatureVersion = "4";
          String signatureNonce = UUID.randomUUID().toString().replace("-", "");

          PostBeaconModel beaconModel = new PostBeaconModel();
          beaconModel.user_uuid = "123123123";
          beaconModel.beacons = beaconsString;
          beaconModel.synchronize = 1;

          TreeMap<String, Object> params = new TreeMap<>();
          params.put("app_id", appId);
          params.put("uuid", uuid);
          params.put("timestamp", currentTime);
          params.put("signature_nonce", signatureNonce);
          params.put("user_uuid", beaconModel.user_uuid);
          params.put("beacons", beaconsString);
          params.put("synchronize", 1);
          String signature = "";
          try {
            signature = buildSignature(params, "testsecret");
          } catch (Exception e) {
            signatureVersion = "999";
            e.printStackTrace();
          }

          String finalSignature = signature;
          String finalSignatureVersion = signatureVersion;
          Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                Call<Object> repos = rssiService.PostBeaconData(uuid, appId, currentTime, finalSignatureVersion,
                    signatureNonce, finalSignature, beaconModel);
                repos.enqueue(new Callback<Object>() {

                  @Override
                  public void onResponse(Call<Object> call, Response<Object> response) {

                    if (response.isSuccessful()) {
                      String responseBodyString=response.body().toString();
                      Log.i("postedResult", responseBodyString);
                      JSONObject result= null;
                      try {
                        result = new JSONObject(responseBodyString);
                        JSONArray pointArray=  result.getJSONArray("smooth_xy");
                        // Point position = new Point(pointArray.getDouble(0), pointArray.getDouble(1));
                        // double positionX=position.getX()-pointCenter.getX();
                        // double positionY=-(position.getY()-pointCenter.getY());
                        double positionX = pointArray.getDouble(0) - pointCenter.getX();
                        double positionY = -(pointArray.getDouble(1) - pointCenter.getY());
                        Log.i("Position Result", "X:" + positionX + "   Y:" + positionY);
                        Point changedPosition = new Point(positionX, positionY);
                        CallJs(true,gson.toJson(changedPosition),callbackContext);
                      } catch (JSONException e) {
                        e.printStackTrace();
                        CallJs(false,"repos 序列化JSON对象时发送异常",callbackContext);
                      }

                    } else {
                      Log.e("repos.onResponse", response.errorBody().toString());
                    }
                  }

                  @Override
                  public void onFailure(Call<Object> call, Throwable t) {
                    Log.e("repos.Callback", "发生错误", t);
                  }
                });

              } catch (Exception e) {
                e.printStackTrace();
                Log.e("startListening", e.getMessage(), e);

              }

            }
          });
          httpThread.run();
        }

      }
    };
    beaconManager.addRangeNotifier(rangeNotifier);
    beaconManager.startRangingBeacons(wildcardRegion);
  }

  private void stopListening(CallbackContext callbackContext) {
    if (null == beaconManager) {
      callbackContext.success("stoped");
      return;
    }
    beaconManager.stopRangingBeacons(wildcardRegion);
    beaconManager.removeAllRangeNotifiers();
    callbackContext.success("stoped");
  }

  private String buildSignature(TreeMap<String, Object> params, String secret) throws Exception {
    if (params == null || params.isEmpty()) {
      return "";
    }
    int paramCount = params.size();
    int paramIndex = 1;
    StringBuilder stringBuilder = new StringBuilder();
    Set<Map.Entry<String, Object>> sets = params.entrySet();
    for (Map.Entry<String, Object> item : sets) {
      if (paramIndex < paramCount) {
        stringBuilder.append(item.getKey().replace("-", "_").toLowerCase() + "=" + item.getValue().toString() + "&");
      } else {
        stringBuilder.append(item.getKey().replace("-", "_").toLowerCase() + "=" + item.getValue().toString());
      }
      paramIndex++;
    }
    String paramString = stringBuilder.toString();
    String paramUrlString = UrlEncode(paramString);
    String encryptedString = HmacSHA1Encrypt(paramUrlString, secret);
    return UrlEncode(encryptedString);
  }

  private String UrlEncode(String text) throws UnsupportedEncodingException {
    return URLEncoder.encode(text, encoding)
        .replace("+", "%20")
        .replace("*", "%2A")
        .replace("%7E", "~");
  }

  private String HmacSHA1Encrypt(String text, String secret) throws Exception {
    String algorithm = "HmacSHA1";
    Mac hmacSHA1 = Mac.getInstance(algorithm);
    byte[] secretBytes = secret.getBytes(encoding);
    SecretKey key = new SecretKeySpec(secretBytes, algorithm);
    hmacSHA1.init(key);
    byte[] textBytes = text.getBytes(encoding);
    byte[] hmacData = hmacSHA1.doFinal(textBytes);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      return Base64.getEncoder().encodeToString(hmacData);
    } else {
      return android.util.Base64.encodeToString(hmacData, android.util.Base64.DEFAULT);
    }
  }

  private Point conveterLocationToPoint(Location location) {
    return new Point(SphericalMercatorProjection.longitudeToX(location.getLongitude()),
        SphericalMercatorProjection.latitudeToY(location.getLatitude()));
  }

  private void CallJs(boolean isSuccessful, String message, CallbackContext callbackContext) {
    PluginResult pluginResult;
    if (isSuccessful) {
      pluginResult = new PluginResult(PluginResult.Status.OK, message);
    } else {
      pluginResult = new PluginResult(PluginResult.Status.ERROR, message);
    }
    pluginResult.setKeepCallback(true);
    callbackContext.sendPluginResult(pluginResult);
  }

}

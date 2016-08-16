package com.syarul.rnlocation;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNLocationModule extends ReactContextBaseJavaModule implements LocationProvider.LocationCallback
{

  // React Class Name as called from JS
  public static final String REACT_CLASS = "RNLocation";
  public static final String TAG = RNLocationModule.class.getSimpleName();

  private LocationProvider mLocationProvider;
  private Activity mActivity;

  // Constructor Method as called in Package
  public RNLocationModule(ReactApplicationContext reactContext, Activity activity)
  {
    super(reactContext);

    mActivity = activity;
  }

  @Override
  public String getName()
  {
    return REACT_CLASS;
  }

  /*
   * Location permission request (Not implemented yet)
   */
  @ReactMethod
  public void requestWhenInUseAuthorization()
  {
    Log.i(TAG, "Requesting authorization");
  }

  /*
   * Location Callback as called by JS
   */
  @ReactMethod
  public void startUpdatingLocation()
  {
    Log.d(TAG, "Starting location updates...");
    mLocationProvider = new LocationProvider(mActivity, this);

    // Check if all went well and the Google Play Service are available...
    if (!mLocationProvider.checkPlayServices())
    {
      Log.i(TAG, "Location Provider not available...");
    }
    else
    {
      // Connect to Play Services
      mLocationProvider.connect();
      Log.i(TAG, "Location Provider successfully created.");
    }
  }

  @Override
  public void handleNewLocation(Location loc) {
    Log.d(TAG, "Location changed");
    try
    {
      double longitude;
      double latitude;
      double speed;
      double altitude;
      float accuracy;

      // Receive Longitude / Latitude from (updated) Last Location
      longitude = loc.getLongitude();
      latitude = loc.getLatitude();
      speed = loc.getSpeed();
      altitude = loc.getAltitude();
      accuracy = loc.getAccuracy();

      Log.i(TAG, "Got new location. Lng: " + longitude + " Lat: " + latitude);

      // Create Map with Parameters to send to JS
      WritableMap params = Arguments.createMap();
      params.putDouble("longitude", longitude);
      params.putDouble("latitude", latitude);
      params.putDouble("speed", speed);
      params.putDouble("altitude", altitude);
      params.putDouble("accuracy", accuracy);

      // Send Event to JS to update Location
      sendEvent(getReactApplicationContext(), "locationUpdated", params);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.i(TAG, "Location services disconnected.");
    }
  }

  @ReactMethod
  public void stopUpdatingLocation()
  {
    Log.d(TAG, "Stopping location updates.");
    mLocationProvider.disconnect();
  }

  /*
   * Internal function for communicating with JS
   */
  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params)
  {
    if (reactContext.hasActiveCatalystInstance())
    {
      reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    }
    else
    {
      Log.i(TAG, "Waiting for CatalystInstance...");
    }
  }
}

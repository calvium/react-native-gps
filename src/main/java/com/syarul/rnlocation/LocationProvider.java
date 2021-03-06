package com.syarul.rnlocation;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by benjakuben on 12/17/14.
 */
public class LocationProvider implements
  GoogleApiClient.ConnectionCallbacks,
  GoogleApiClient.OnConnectionFailedListener,
  LocationListener {

  /**
   * Location Callback interface to be defined in Module
   */
  public abstract interface LocationCallback {
    public abstract void handleNewLocation(Location location);
    public abstract void handleConnectionStatus(String status);
  }

  // Unique Name for Log TAG
  public static final String TAG = LocationProvider.class.getSimpleName();
  /*
   * Define a request code to send to Google Play services
   * This code is returned in Activity.onActivityResult
   */
  private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
  // Location Callback for later use
  private LocationCallback mLocationCallback;
  // Context for later use
  private Activity mActivity;
  // Main Google API CLient (Google Play Services API)
  private GoogleApiClient mGoogleApiClient;
  // Location Request for later use
  private LocationRequest mLocationRequest;
  // Are we Connected?
  public Boolean connected;

  public LocationProvider(Activity activity, LocationCallback updateCallback) {
    // Save current Context
    mActivity = activity;
    // Save Location Callback
    this.mLocationCallback = updateCallback;
    // Initialize connection "state"
    connected = false;

    // First we need to check availability of play services
    if (checkPlayServices()) {
      mGoogleApiClient = new GoogleApiClient.Builder(activity)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();

      // Create the LocationRequest object
      mLocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(10 * 1000)        // 10 seconds, in milliseconds
        .setFastestInterval(1000);     // 1 second, in milliseconds
    }
  }

  /**
   * Method to verify google play services on the device
   * */
  public boolean checkPlayServices() {
    int resultCode = GooglePlayServicesUtil
      .isGooglePlayServicesAvailable(mActivity);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
        switch (resultCode) {
          case ConnectionResult.SERVICE_DISABLED:
          case ConnectionResult.SERVICE_INVALID:
          case ConnectionResult.SERVICE_MISSING:
          case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity, 0);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
              @Override
              public void onCancel(DialogInterface dialogInterface) {
                Log.e(TAG, "user cancelled Google play services dialog");
                mLocationCallback.handleConnectionStatus("googleServicesRequired");
              }
            });
            dialog.show();
        }
        Log.i(TAG, GooglePlayServicesUtil.getErrorString(resultCode));
      } else {
        Log.i(TAG, "This device is not supported.");
        mLocationCallback.handleConnectionStatus("deviceNotSupported");
      }
      return false;
    }
    return true;
  }

  /**
   * Connects to Google Play Services - Location
   */
  public void connect() {
    mGoogleApiClient.connect();
  }

  /**
   * Disconnects to Google Play Services - Location
   */
  public void disconnect() {
    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
      LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
      mGoogleApiClient.disconnect();
    }
  }

  @Override
  public void onConnected(Bundle bundle) {
    Log.i(TAG, "Location services connected.");
    // We are Connected!
    connected = true;

    try
    {
      // First, get Last Location and return it to Callback
      Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
      if (location != null)
      {
        mLocationCallback.handleNewLocation(location);
      }
      // Now request continuous Location Updates
      LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
      mLocationCallback.handleConnectionStatus("authorized");
    }
    catch (Exception e) {
      Log.e(TAG, "Couldn't start updating location: " + e.getLocalizedMessage());
      e.printStackTrace();
      mLocationCallback.handleConnectionStatus("denied");
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.i(TAG, "Location services suspended...");
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
    if (connectionResult.hasResolution() && mActivity instanceof Activity) {
      try {
        Activity activity = (Activity)mActivity;
        // Start an Activity that tries to resolve the error
        connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
      } catch (IntentSender.SendIntentException e) {
        // Log the error
        e.printStackTrace();
        mLocationCallback.handleConnectionStatus("connectionFailed");
      }
    } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
      Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
      mLocationCallback.handleConnectionStatus("connectionFailed");
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    Log.i(TAG, "Location Changed!");
    // Callback as defined in Module.
    mLocationCallback.handleNewLocation(location);
  }
}
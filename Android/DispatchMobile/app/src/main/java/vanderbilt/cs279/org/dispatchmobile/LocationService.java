package vanderbilt.cs279.org.dispatchmobile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.location.LocationListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * todo : untested
 * todo : so far only set up to update web api. Messaging needed for local updates?
 */
public class LocationService extends Service {

    public static final int SERVICE_STARTED = 1;

    public static final String SESSION_STRING = "sess_string";

    private static final String TAG = LOCATION_SERVICE.getClass().getCanonicalName();
    private LocationManager mLocationManager = null;
    private static final int LOCATION_UPDATE_INTERVAL = 1000; // in millisec
    private static final float LOCATION_UPDATE_DISTANCE = 0;

    private String mSession = null;

    private class CustomLocListener implements LocationListener {

        private String mSession = null;

        public CustomLocListener(String session){
            mSession = session;
        }

        @Override
        public void onLocationChanged(Location location) {

            Log.i(TAG, "Location update recieved: " + location.getLatitude() + " " + location.getLongitude());

//            // todo update web api
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl("http://10.0.2.2:8000/") // todo
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//
//            // prepare call in Retrofit 2.0
//            GlowAPI glowAPI = retrofit.create(GlowAPI.class);
//
//            Call<Object> call = glowAPI.updateLocation(mSession,
//                                                    location.getTime(),
//                                                    location.getLatitude(),
//                                                    location.getLongitude());
//
//
//            //asynchronous call
//            call.enqueue(new Callback<Object>() {
//                @Override
//                public void onResponse(Call<Object> call, Response<Object> response) {
//                    if (response.isSuccessful()) {
//                        Log.i(TAG, "Location update successful");
//                    } else {
//                        Log.i(TAG, "Location update unsuccessful");
//                    }
//                }
//                @Override
//                public void onFailure(Call<Object> call, Throwable t) {
//                    Log.e(TAG, "Location update failure: " + t.getMessage());
//                }
//            });
//
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private LocationListener mLocationListener = null;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        // get the session id from the caller
        String session = intent.getStringExtra(SESSION_STRING);

        Log.i(TAG, "Service started with session: " + session);


        // create the location listener
        mLocationListener = new CustomLocListener(session);

        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    LOCATION_UPDATE_DISTANCE,
                    mLocationListener);
        }catch (java.lang.SecurityException ex) {
            Log.i(TAG, "location permission error");
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLocationManager();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mLocationManager != null){
            try{
                mLocationManager.removeUpdates(mLocationListener);
            }catch (java.lang.SecurityException ex){
                Log.i(TAG, "location permission error");
            }
        }
    }

    private void initializeLocationManager(){
        if(mLocationManager == null){
            mLocationManager = (LocationManager) getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
        }
    }
}

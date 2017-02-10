package vanderbilt.cs279.org.dispatchmobile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.location.LocationListener;
import android.util.Log;

/**
 * todo : untested
 * todo : so far only set up to update web api. Messaging needed for local updates?
 */
public class LocationService extends Service {

    private static final String TAG = LOCATION_SERVICE.getClass().getCanonicalName();
    private LocationManager mLocationManager = null;
    private static final int LOCATION_UPDATE_INTERVAL = 1000; // in millisec
    private static final float LOCATION_UPDATE_DISTANCE = 10f;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // todo update web api
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
    };

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLocationManager();

        // todo move to onBind ... need to get worker id / session key

        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    LOCATION_UPDATE_DISTANCE,
                    mLocationListener);
        }catch (java.lang.SecurityException ex) {
            Log.i(TAG, "location permission error");
        }
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

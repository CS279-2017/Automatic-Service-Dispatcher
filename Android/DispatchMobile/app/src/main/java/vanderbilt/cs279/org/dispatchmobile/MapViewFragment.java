package vanderbilt.cs279.org.dispatchmobile;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.widget.TextView;

import java.util.ArrayList;
//package vanderbilt.cs279.org.dispatchmobile;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.MapsInitializer;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//

/**
 * Created by gpettet on 2017-02-23.
 */

public class MapViewFragment extends Fragment
    implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, DirectionCallback {


    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    LatLng latLng;

    Marker currLocMarker;

    private String serverKey = "AIzaSyDEzbQHvv2frw-KIiiS7yCIOro7-NM9vTI";

    private static final String TAG = vanderbilt.cs279.org.dispatchmobile.MapViewFragment.class.getCanonicalName();
    public static final String LAT_DEST_KEY = "lat";
    public static final String LONG_DEST_KEY = "long";

    MapView mMapView;
    FloatingActionButton mBtn_Cancel;
    FloatingActionButton mBtn_Confirm;
    TextView mJobLocation, mVolume, mWage, mPinCode;

    private GoogleMap googleMap;

    LatLng mDestination = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.map_fragment_view, container, false);

        Bundle args = getArguments();

        if (args != null && args.containsKey(LAT_DEST_KEY) && args.containsKey(LONG_DEST_KEY)){

            Log.i(TAG, "getting dirctions");
            mDestination = new LatLng((double)args.get(LAT_DEST_KEY),
                                    (double)args.get(LONG_DEST_KEY));

        }

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mJobLocation = (TextView) rootView.findViewById((R.id.jobInfo));
        mVolume = (TextView) rootView.findViewById((R.id.jobInfo));
        mWage = (TextView) rootView.findViewById((R.id.jobInfo));
        mPinCode = (TextView) rootView.findViewById((R.id.jobInfo));

        mBtn_Cancel = (FloatingActionButton) rootView.findViewById(R.id.btn_cancel_task);
        mBtn_Confirm = (FloatingActionButton) rootView.findViewById(R.id.btn_confirm_task);
        mBtn_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // confirm the task

            }
        });
        mBtn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the task

            }
        });
        mMapView.onCreate(savedInstanceState);



        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;

        // For showing a move to my location button
        try {
            googleMap.setMyLocationEnabled(true);
        }catch (SecurityException e){
            Log.e(TAG, "Location Permission needed");
        }

        buildGoogleApiClient();

        mGoogleApiClient.connect();



        // For dropping a marker at a point on the Map
//                LatLng sydney = new LatLng(-34, 151);
//                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

        // For zooming automatically to the location of the marker
//        CameraPosition cameraPosition = new CameraPosition.Builder().target(googleMap).zoom(12).build();
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient= new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                //place marker at current position
                //mGoogleMap.clear();
                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                currLocMarker = googleMap.addMarker(markerOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
            }

            if(mDestination != null){

                Log.i(TAG, "directions: connected");

                GoogleDirection.withServerKey(serverKey)
                        .from(currLocMarker.getPosition())
                        .to(mDestination)
                        .transportMode(TransportMode.DRIVING)
                        .execute(this);

            }

            // TODO: 2017-02-23 check that this is how often to update
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000); //5 seconds
            mLocationRequest.setFastestInterval(3000); //3 seconds
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch(SecurityException e){
            Log.e(TAG, "Permission Error");
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "connection failed");

    }

    @Override
    public void onLocationChanged(Location location) {
        //place marker at current position
        googleMap.clear();
        if (currLocMarker != null) {
            currLocMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currLocMarker = googleMap.addMarker(markerOptions);

        if(mDestination != null){
            GoogleDirection.withServerKey(serverKey)
                    .from(currLocMarker.getPosition())
                    .to(mDestination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);

        }

        Log.i(TAG, "Updating location");

//        Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();

        //zoom to current position:
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));

        //If you only need one location, unregister the listener
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        Log.i(TAG, "directions: success");

        Log.i(TAG, "directions: status: "+ direction.getStatus() );

        if (direction.isOK()) {
            Log.i(TAG, "directions: is Okay");

            //googleMap.addMarker(new MarkerOptions().position(currLocMarker.getPosition()));
            googleMap.addMarker(new MarkerOptions().position(mDestination));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(getContext(), directionPositionList, 5, Color.RED));


        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast toast = Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG);
        toast.show();
    }
}

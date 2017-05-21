package vanderbilt.cs279.org.dispatchmobile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
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

    Retrofit retrofit;
    GlowAPI glowAPI;
    SharedPreferences mSharedPreferences;

    Task mCurrentTask;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    LatLng latLng;

    Marker currLocMarker;

    private String serverKey = "AIzaSyDEzbQHvv2frw-KIiiS7yCIOro7-NM9vTI";

    // Shared Preferences key used to store the user's session id
    private static final String mPREFERENCES = "GlowPrefs";

    // the user's session id
    private static final String mSessionId = "sessionKey";

    private static final String mDeviceId = "deviceId";

    private static final String TAG = vanderbilt.cs279.org.dispatchmobile.MapViewFragment.class.getCanonicalName();

    MapView mMapView;
    FloatingActionButton mBtn_Cancel;
    FloatingActionButton mBtn_Confirm;
    TextView mJobLocation, mVolume, mWageInfo, mPinCode;
    LinearLayout mTaskInfoLinearLayout;


    private GoogleMap googleMap;

    LatLng mDestination = null;


    String mWage = "default";
    String mPin = "default";


    String sessionId;
    String deviceId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.map_fragment_view, container, false);

        Bundle args = getArguments();

        mSharedPreferences = this.getActivity().getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);

        sessionId = mSharedPreferences.getString(mSessionId, "N/A");

        deviceId = mSharedPreferences.getString(mDeviceId, "N/A");

/*
        if (args != null && args.containsKey(LAT_DEST_KEY) && args.containsKey(LONG_DEST_KEY)){

            Log.i(TAG, "getting dirctions");
            mDestination = new LatLng((double)args.get(LAT_DEST_KEY),
                                    (double)args.get(LONG_DEST_KEY));

            mWage = args.getString(WAGE_KEY);
            mPin = args.getString(PIN_CODE_KEY);

        }
*/
        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        glowAPI = retrofit.create(GlowAPI.class);

        mSharedPreferences = this.getActivity().getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mJobLocation = (TextView) rootView.findViewById((R.id.jobInfo));
        mVolume = (TextView) rootView.findViewById((R.id.waterVolume));
        mWageInfo = (TextView) rootView.findViewById((R.id.jobWage));
        mPinCode = (TextView) rootView.findViewById((R.id.jobPinCode));

        mBtn_Cancel = (FloatingActionButton) rootView.findViewById(R.id.btn_cancel_task);
        mBtn_Confirm = (FloatingActionButton) rootView.findViewById(R.id.btn_confirm_task);
        mTaskInfoLinearLayout = (LinearLayout) rootView.findViewById(R.id.taskInfoLinearLayout);

        if(!sessionId.equals("N/A")){
            checkCurrentTask(sessionId, deviceId);
        }


        mBtn_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // confirm the task
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//getActivity());
                builder.setMessage("Are you sure that you complete the task?").setTitle("Confirm the task")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                  confirm_task(sessionId, mCurrentTask.taskId);
                            }
                        });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        mBtn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the task
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//getActivity());
                builder.setMessage("Are you sure to cancel the task?").setTitle("Cancel the task")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                  cancel_task(sessionId, mCurrentTask.taskId, mDeviceId);
                            }
                        });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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

    private void checkCurrentTask(String session, String deviceId){

        // check the current task and show the info
        Call<Task> call = glowAPI.getMyTask(session, deviceId);
        call.enqueue(new Callback<Task>() {
        @Override
        public void onResponse(Call<Task> call, Response<Task> response) {
            if (response.isSuccessful()) {
                mCurrentTask = response.body();
                mJobLocation.setText(mCurrentTask.name+" at Pad "+mCurrentTask.sensor);
                mVolume.setText("Tank Level:"+ mCurrentTask.levelAtRequest+" of "+mCurrentTask.tankCapacity);
                mPinCode.setText("PinCode:"+ mCurrentTask.pinCode);
                mWageInfo.setText("$"+mCurrentTask.wage);
                mDestination = new LatLng(mCurrentTask.lattitude,
                        mCurrentTask.longitude);
                Log.e("Error", response.body().toString());
                //Sam Added
                if(currLocMarker != null){
                    setupDestMarker(mDestination);
                }
            } else {
                // No Session
                mTaskInfoLinearLayout.setVisibility(View.GONE);
            }
        }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });

    }

    private void confirm_task(String session, long taskId) {
        // confirm the task is completed
        Call<TaskList> call = glowAPI.completeTask(session, taskId);
        call.enqueue(new Callback<TaskList>() {
            @Override
            public void onResponse(Call<TaskList> call, Response<TaskList> response) {
                if (response.isSuccessful()) {
                   // Log.v("res", response.body().toString());
                    mTaskInfoLinearLayout.setVisibility(View.GONE);
                } else {
                    // No Session
                    Log.v("res", response.body().toString());
                }
            }
            @Override
            public void onFailure(Call<TaskList> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void cancel_task(String session, long taskId, String deviceId) {
        // cancel the current task
        Call<Object> call = glowAPI.cancelTask(session, taskId, deviceId);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                  //  Log.v("res", response.body().toString());
                    mTaskInfoLinearLayout.setVisibility(View.GONE);
                } else {
                    // No Session
                    Log.v("res", response.body().toString());
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });

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

    private void setupDestMarker(LatLng destination){
        GoogleDirection.withServerKey(serverKey)
                .from(currLocMarker.getPosition())
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
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

                setupDestMarker(mDestination);

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
                    .from(mDestination)
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

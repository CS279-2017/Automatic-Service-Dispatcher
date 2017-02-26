package vanderbilt.cs279.org.dispatchmobile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vanderbilt.cs279.org.dispatchmobile.R;

public class NavDrawAct extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = NavDrawAct.class.getCanonicalName();
    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION=1;

    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";
    private static final String mDeviceId = "deviceId";

    //////////////////////////////////////////////////////////////////////////////
    // Location Service
    private void setupLocationService(){

        if(ContextCompat.checkSelfPermission(this,
                                            Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            }

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_ACCESS_FINE_LOCATION);

        } else {
            // TODO: 2017-02-23  
            //startLocationService();
        }

        //startLocationService();

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("login", "Permission Granted");
                    //startLocationService(); // TODO: 2017-02-23
                } else {
                    Log.i("login", "Permission Denied");
                }
        }
    }

    private void startLocationService(){
        Log.i("login", "Starting Service");

        Intent locIntent = new Intent(this, LocationService.class);
        locIntent.putExtra(LocationService.SESSION_STRING, "test_session");

        startService(locIntent);
    }

    private void stopLocationService() {
        Intent locIntent = new Intent(this, LocationService.class);
        stopService(locIntent);
    }
    //
    ////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_draw);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeActiveFragment(new MapViewFragment());

        // TODO: 2017-02-23 activate for deployment
        setupLocationService();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopLocationService();
        Log.i(TAG, "service stopped");
    }

    @Override
    protected  void onResume(){
        super.onResume();
        // TODO: 2017-02-23 checkSession
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_draw, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Toast toast = Toast.makeText(getApplicationContext(), "Not Yet Implemented", Toast.LENGTH_SHORT);
            toast.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            // Handle the camera action
            changeActiveFragment(new MapViewFragment());
        } else if (id == R.id.nav_get_list) {
            changeActiveFragment(new TaskListFrag());
        } else if (id == R.id.nav_past_jobs) {
            changeActiveFragment(new CompletedTaskListFrag());
            Toast toast = Toast.makeText(getApplicationContext(), "Not Yet Implemented", Toast.LENGTH_SHORT);
            toast.show();
        } else if (id == R.id.nav_settings) {
            changeActiveFragment(new SettingsFragment());
            /*Toast toast = Toast.makeText(getApplicationContext(), "Not Yet Implemented", Toast.LENGTH_SHORT);
            toast.show();

            MapViewFragment mapDirTest = new MapViewFragment();

            Bundle args = new Bundle();
            args.putDouble(MapViewFragment.LAT_DEST_KEY, 36.203177);
            args.putDouble(MapViewFragment.LONG_DEST_KEY, -86.738602);

            mapDirTest.setArguments(args);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_nav_draw, mapDirTest);
            transaction.commit();*/

        } else if (id == R.id.nav_logout) {
            String sessionId = getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE).getString(mSessionId, "N/A");
            logout(sessionId);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeActiveFragment(Fragment newFrag){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_nav_draw, newFrag);
        transaction.commit();
    }

    private void logout(String session){
        //https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GlowAPI glow = retrofit.create(GlowAPI.class);

        Call<Object> call = glow.logout(session);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(mSessionId, "N/A");
                    editor.apply();
                    openLoginView();
                } else {
                    //nothing happens at failure
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void openLoginView(){
        Intent myIntent = new Intent(this, LoginActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);
    }
}

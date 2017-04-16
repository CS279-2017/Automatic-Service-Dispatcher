package vanderbilt.cs279.org.dispatchmobile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Main UI activity. This contains the Navigation drawer implementation and
 * gives the application fragments a framework to display in
 */
public class NavDrawAct extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Log Tag
    public static final String TAG = NavDrawAct.class.getCanonicalName();

    // constant used to ensure that the app has queried the user to enable the
    // location permission
    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION=1;

    // Preferences and session information
    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";

    //////////////////////////////////////////////////////////////////////////////
    // Location Service

    /**
     * This checks if the user has enabled location permissions, and if not asks them
     * to enable them.
     */
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

    /**
     * callback method used once user has responded to location permission request
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * Activity lifecycle method called when activity is created. Initializes activity state
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup UI layout
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

        // set the default fragment to a mapViewFragment with no task argument
        changeActiveFragment(new MapViewFragment());

        // TODO: 2017-02-23 activate for deployment
        setupLocationService();
    }

    /**
     * Activity lifecycle method called when activity is destroyed. Cleans up location service
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopLocationService();
        Log.i(TAG, "service stopped");
    }

    /**
     * Activity lifecycle method called when unpaused.
     */
    @Override
    protected  void onResume(){
        super.onResume();
        // TODO: 2017-02-23 checkSession
    }

    /**
     * Navigation drawer callback invoked when the back button is pressed.
     * Currently closes the drawer if open. Otherwise calls base back button
     * implementation
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Hook method that creates the options menu for the activity. Simply inflates
     * the layout resource.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_draw, menu);
        return true;
    }

    /**
     * Hook method called when an options menu item is selected. Checks item id, and
     * performs appropriate action for the pressed item. Right now shell implementation
     * @param item
     * @return
     */
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

    /**
     * Hook method called when an item in the navigation drawer is selected.
     * Changes active fragment based on which item is selected.
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Change the active fragment to the map view
        if (id == R.id.nav_map) {
            changeActiveFragment(new MapViewFragment());

        // Change active fragment to the NextTaskFrag
        } else if (id == R.id.nav_get_list) {
            changeActiveFragment(new NextTaskFrag());

        // change to the job history fragment
        } else if (id == R.id.nav_past_jobs) {
            changeActiveFragment(new CompletedTaskListFrag());

        // Change to the profile fragment.
        } else if (id == R.id.nav_profile) {
            changeActiveFragment(new ProfileFragment());

        // Log the user out
        } else if (id == R.id.nav_logout) {
            String sessionId = getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE).getString(mSessionId, "N/A");
            logout(sessionId);
        }

        // Close the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Helper method that changes the active fragment of the navigation drawer activity.
     * @param newFrag
     */
    private void changeActiveFragment(Fragment newFrag){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_nav_draw, newFrag);
        transaction.commit();
    }

    /**
     * Helper method that logs the user out
     * @param session
     */
    private void logout(String session){
        //https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GlowAPI glow = retrofit.create(GlowAPI.class);

        // retrofit api call to log the user out
        Call<Object> call = glow.logout(session);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {

                    // Clear the sessionID sharedPreference
                    SharedPreferences.Editor editor = getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(mSessionId, "N/A");
                    editor.apply();

                    // Force the user to log in
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

    /**
     * Helper method that forces the user to log in
     */
    private void openLoginView(){
        Intent myIntent = new Intent(this, LoginActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);
    }
}

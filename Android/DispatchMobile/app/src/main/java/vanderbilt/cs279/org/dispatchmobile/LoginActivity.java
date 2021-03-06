package vanderbilt.cs279.org.dispatchmobile;

import android.*;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{//} implements LoaderCallbacks<Cursor> {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    // Shared Preferences for Session
    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";
    private static final String mDeviceId = "deviceId";

    SharedPreferences mSharedPreferences;
    Retrofit retrofit;
    GlowAPI glowAPI;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        glowAPI = retrofit.create(GlowAPI.class);

        checkSession();
    }

    /*
    Redirects to task view if authenticated
    */
    private void openTaskView(){
        Intent myIntent = new Intent(this, NavDrawAct.class);
        finish();
        startActivity(myIntent);
    }

    // TODO: 2017-02-23 should checkSession,
    
    /*
    Checks shared preferences for session id. If set, then validate against server
     */
    private void checkSession(){
        mSharedPreferences = getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);
        String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
        String deviceId = mSharedPreferences.getString(mDeviceId, "N/A");
        if(!sessionId.equals("N/A")){
            checkSession(sessionId, deviceId);
        }
    }

    private void checkSession(String session, String deviceId){
        //https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api
        Call<LoginResult> call = glowAPI.getSession(session, deviceId);
        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                if (response.isSuccessful()) {
                    System.out.println(response.body().toString());
                    openTaskView();
                } else {
                    System.out.println("error");
                }
                showProgress(false);
            }
            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mSharedPreferences = getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);
            String deviceId = mSharedPreferences.getString(mDeviceId, "N/A");
            executeLogin(email, password, deviceId);
            //checkSession();
            showProgress(true);
        }
    }

    private void executeLogin(String email, String password, String deviceId){
        //https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api
        Call<LoginResult> call = glowAPI.getUserLogin(email, password, deviceId);
        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                if (response.isSuccessful()) {
                    //System.out.println(response.body().toString());
                    //System.out.println(response.body().sessionId);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(mSessionId, response.body().sessionId);
                    editor.apply();
                    openTaskView();
                } else {
                    toast("error");
                    System.out.println("error");
                    // error response, no access to resource?
                }
                showProgress(false);
            }
            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void toast(String message){
        Toast.makeText(this, "Login Failed!",
                Toast.LENGTH_LONG).show();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}


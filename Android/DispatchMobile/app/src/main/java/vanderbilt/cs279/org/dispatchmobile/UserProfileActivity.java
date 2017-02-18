package vanderbilt.cs279.org.dispatchmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserProfileActivity extends AppCompatActivity {

    private EditText mFirstName, mLastName, mEmail;
    private String firstName, lastName, email;
    private Button mUpdateButton;

    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";
    private static final String mDeviceId = "deviceId";

    SharedPreferences mSharedPreferences;
    Retrofit retrofit;
    GlowAPI glowAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mFirstName = (EditText) findViewById(R.id.firstNameEdit);
        mFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                firstName = s.toString();
            }
            @Override
            public void afterTextChanged(Editable s) {
                // do stuff
            }
        });
        mLastName = (EditText) findViewById(R.id.lastNameEdit);
        mLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {lastName=s.toString();}

            @Override
            public void afterTextChanged(Editable s) {
                // do stuff
            }
        });
        mEmail = (EditText) findViewById(R.id.emailEdit);
        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {email=s.toString();}

            @Override
            public void afterTextChanged(Editable s) {
                // do stuff
            }
        });

        mSharedPreferences = getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);

        mUpdateButton = (Button) findViewById(R.id.updateUser);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
                if(!sessionId.equals("N/A")){
                    updateUser(sessionId);
                }
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        glowAPI = retrofit.create(GlowAPI.class);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
        if(!sessionId.equals("N/A")){
            getUser(sessionId);
        }
    }

    private void getUser(String session){
        Call<LoginResult> call = glowAPI.getUserInfo(session);
        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                if (response.isSuccessful()) {
                    mFirstName.setText(response.body().firstName);
                    mLastName.setText(response.body().lastName);
                    mEmail.setText(response.body().email);
                } else {
                    openLoginView();
                }
            }
            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void updateUser(String session){
        Call<LoginResult> call = glowAPI.updateUserInfo(session, firstName, lastName, email);
        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                if (response.isSuccessful()) {
                    makeToast("User Updated!");
                    mFirstName.setText(response.body().firstName);
                    mLastName.setText(response.body().lastName);
                    mEmail.setText(response.body().email);
                } else {
                    openLoginView();
                }
            }
            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void openLoginView(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(mSessionId, "N/A");
        editor.apply();

        Intent myIntent = new Intent(this, LoginActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);
    }

    public void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

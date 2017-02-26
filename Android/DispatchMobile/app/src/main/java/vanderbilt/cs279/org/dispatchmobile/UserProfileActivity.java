/*package vanderbilt.cs279.org.dispatchmobile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
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

import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserProfileActivity extends AppCompatActivity {

    private EditText mFirstName, mLastName, mEmail, mProfession;
    private TextView mSkills;
    private String firstName, lastName, email, profession;
    private Button mUpdateButton, mUpdateSkills;

    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";
    private static final String mDeviceId = "deviceId";

    UserInformation mUser;
    Set<String> mSkillsSet;

    SharedPreferences mSharedPreferences;
    Retrofit retrofit;
    GlowAPI glowAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_profile);
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
        mProfession = (EditText) findViewById(R.id.professionEdit);
        mProfession.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                profession = s.toString();
            }
            @Override
            public void afterTextChanged(Editable s) {
                // do stuff
            }
        });

        mSkills = (TextView)findViewById(R.id.skillsText);

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
        mUpdateSkills = (Button) findViewById(R.id.updateSkills);
        mUpdateSkills.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("update");
                openDialog();
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
        Call<UserInformation> call = glowAPI.getUserInfo(session);
        call.enqueue(new Callback<UserInformation>() {
            @Override
            public void onResponse(Call<UserInformation> call, Response<UserInformation> response) {
                if (response.isSuccessful()) {
                    mUser = response.body();
                    mSkillsSet = mUser.initializeSkillSet();
                    mSkills.setText(mUser.getSkills(mSkillsSet));
                    mFirstName.setText(mUser.firstName);
                    mLastName.setText(mUser.lastName);
                    mEmail.setText(mUser.email);
                    mProfession.setText(mUser.profession);
                } else {
                    openLoginView();
                }
            }
            @Override
            public void onFailure(Call<UserInformation> call, Throwable t) {
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void updateUser(String session){
        Call<UserInformation> call = glowAPI.updateUserInfo(session, firstName, lastName, email, profession, mUser.getSkillsIds(mSkillsSet));
        call.enqueue(new Callback<UserInformation>() {
            @Override
            public void onResponse(Call<UserInformation> call, Response<UserInformation> response) {
                if (response.isSuccessful()) {
                    makeToast("User Updated!");
                    mUser = response.body();
                    mSkillsSet = mUser.initializeSkillSet();
                    mSkills.setText(mUser.getSkills(mSkillsSet));
                    mFirstName.setText(mUser.firstName);
                    mLastName.setText(mUser.lastName);
                    mEmail.setText(mUser.email);
                    mProfession.setText(mUser.profession);
                } else {
                    openLoginView();
                }
            }
            @Override
            public void onFailure(Call<UserInformation> call, Throwable t) {
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

    public void openDialog(){
        final String[] options = mUser.possibleSkillsArray();
        boolean[] defaults = mUser.createAlreadyChecked(mSkillsSet);
        final Set<String> cache = new HashSet<String>(mSkillsSet);

        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Select Skills").setMultiChoiceItems(options, defaults,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            mSkillsSet.add(options[which]);
                        } else if (mSkillsSet.contains(options[which])) {
                            mSkillsSet.remove(options[which]);
                        }
                    }
                })
                // Set the action buttons
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mSkills.setText(mUser.getSkills(mSkillsSet));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mSkillsSet = cache;
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
*/
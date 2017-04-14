package vanderbilt.cs279.org.dispatchmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gpettet on 2017-02-23.
 *
 * This Fragment displays a user's Profile information
 */

public class ProfileFragment extends Fragment {
    private TextView mFirstName;
    private TextView mLastName;
    private TextView mEmail;
    private TextView mProfession;
    private TextView mSkills;
    private ImageView mProfileImage;

    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";
    private static final String mDeviceId = "deviceId";

    UserInformation mUser;
    Set<String> mSkillsSet;

    SharedPreferences mSharedPreferences;
    Retrofit retrofit;
    GlowAPI glowAPI;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirstName = (TextView) view.findViewById(R.id.firstNameText);
        mLastName = (TextView) view.findViewById(R.id.lastNameText);
        mEmail = (TextView) view.findViewById(R.id.emailText);
        mProfession = (TextView) view.findViewById(R.id.professionText);
        mSkills = (TextView) view.findViewById(R.id.skillsText);
        mProfileImage = (ImageView)view.findViewById(R.id.profileImage);

        mSharedPreferences = this.getActivity().getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);
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
                    new DownloadImageTask(mProfileImage).execute("https://www.gravatar.com/avatar/"+mUser.emailHash+"?d=identicon&s=600");
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

        Intent myIntent = new Intent(this.getActivity(), LoginActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);
    }

    public void makeToast(String message){
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_LONG).show();
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
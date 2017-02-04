package vanderbilt.cs279.org.dispatchmobile;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends ListActivity {

    private TextView mText;
    private List<String> listValues;

    // Shared Preferences for Session
    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";

    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView) findViewById(R.id.mainText);

        listValues = new ArrayList<String>();
        listValues.add("Android");
        listValues.add("Andid");
        listValues.add("Andr");

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.task_row, R.id.taskText, listValues);

        setListAdapter(myAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        checkSession();
    }


    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        String selectedItem = (String) getListView().getItemAtPosition(position);
        mText.setText("You clicked " + selectedItem + " at position " + position);
        logout(getSession());
    }

    /*
    Checks shared preferences for session id. If set, then validate against server
     */
    private String getSession(){
        mSharedPreferences = getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(mSessionId, "N/A");
    }

    private void checkSession(){
        String sessionId = getSession();
        if(!sessionId.equals("N/A")){
            checkSession(sessionId);
        }
    }

    private void checkSession(String session){
        //https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // prepare call in Retrofit 2.0
        GlowAPI glowAPI = retrofit.create(GlowAPI.class);
        //Call<TaskList> call = glowAPI.loadQuestions("android");
        Call<LoginResult> call = glowAPI.getSession(session);
        //asynchronous call
        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                if (response.isSuccessful()) {
                    System.out.println(response.body().toString());
                } else {
                    // No Session
                    openLoginView();
                }
            }
            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void logout(String session){
        //https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // prepare call in Retrofit 2.0
        GlowAPI glowAPI = retrofit.create(GlowAPI.class);
        //Call<TaskList> call = glowAPI.loadQuestions("android");
        Call<Object> call = glowAPI.logout(session);
        //asynchronous call
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
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
        finish();
        startActivity(myIntent);
    }
}

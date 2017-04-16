package vanderbilt.cs279.org.dispatchmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gpettet on 2017-02-23.
 */

public class NextTaskFrag extends Fragment {
    //private TasksAdapter mAdapter;
    private ProgressBar mTankProgress;
    private TextView mTaskTitle, mTankProgressWords, mTime, mWage;
    private ImageView mImage;
    private Button mAcceptButton;

    // Shared Preferences for Session
    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";
    private static final String mDeviceId = "deviceId";

    private String message = "";

    SharedPreferences mSharedPreferences;
    Retrofit retrofit;
    GlowAPI glowAPI;

    Task mCurrentTask;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.next_task_frag, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTaskTitle = (TextView) view.findViewById(R.id.taskTitle);
        mTankProgress = (ProgressBar) view.findViewById(R.id.tankProgress);
        mTankProgressWords = (TextView) view.findViewById(R.id.tankProgressWords);
        mTime = (TextView) view.findViewById(R.id.time);
        mWage = (TextView) view.findViewById(R.id.jobExpectedWage);
        mImage = (ImageView) view.findViewById(R.id.locationImage);
        mAcceptButton = (Button) view.findViewById(R.id.acceptButton);
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//getActivity());
                builder.setMessage(mCurrentTask.name +" at Pad "+mCurrentTask.sensor).setTitle(mCurrentTask.name)
                        .setPositiveButton("Start Task", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
                                startTask(sessionId, mCurrentTask);
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

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        glowAPI = retrofit.create(GlowAPI.class);

        mSharedPreferences = this.getActivity().getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getNextTask();
    }


    private void getNextTask(){
        String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
        //TODO: only needed for login? if they are logged in to multiple devices this would
        // indicate the current device they are using
        String deviceId = mSharedPreferences.getString(mDeviceId, "N/A");
        if(!sessionId.equals("N/A")){
            getNextTask(sessionId, deviceId);
        } else {
            openLoginView();
        }
    }

    private void getNextTask(String session, String deviceId){
        Call<Task> call = glowAPI.loadNextTask(session, deviceId);
        call.enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    //TODO: Add to view
                    mCurrentTask = response.body();

                    byte[] decodedString = Base64.decode(mCurrentTask.image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    mImage.setImageBitmap(decodedByte);

                    mTaskTitle.setText(mCurrentTask.name+" at Pad "+mCurrentTask.sensor);
                    mTime.setText(getTime(mCurrentTask.date));
                    // @TODO task.getWage()?
                    mWage.setText("$215");

                    mTankProgress.setProgress(100*mCurrentTask.levelAtRequest/mCurrentTask.tankCapacity);
                    mTankProgressWords.setText("Tank Level: "+ mCurrentTask.levelAtRequest+" of "+mCurrentTask.tankCapacity);

                } else {
                    // No Session
                    openLoginView();
                }
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void openLoginView(){
        Intent myIntent = new Intent(this.getActivity(), LoginActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);
    }

    private void startTask(String session, final Task task){
        Call<Task> call = glowAPI.startTask(session, task.taskId);
        call.enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    //TODO: plug into array adapter
                    Toast.makeText(getActivity(), "Task Started", Toast.LENGTH_SHORT).show();

                    MapViewFragment mapDirTest = new MapViewFragment();

                    Bundle args = new Bundle();

                    args.putDouble(MapViewFragment.LAT_DEST_KEY, (double)task.lattitude);
                    args.putDouble(MapViewFragment.LONG_DEST_KEY, (double)task.longitude);


                    mapDirTest.setArguments(args);

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_nav_draw, mapDirTest);
                    transaction.commit();

                    Toast.makeText(getActivity(), "frag updated", Toast.LENGTH_SHORT).show();

                } else {
                    // No Session
                    openLoginView();
                }
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error", t.getMessage());
            }
        });
    }

    private String getTime(String date){
        String time = "";
        Log.e("Date", date);
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        try {
            SimpleDateFormat localDateFormat = new SimpleDateFormat("h:mm a");
            time = localDateFormat.format(m_ISO8601Local.parse(date));
        }catch(Exception e){
            return "Incorrect";
        }
        return time;
    }
}
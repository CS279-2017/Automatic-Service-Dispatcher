package vanderbilt.cs279.org.dispatchmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.Fragment;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gpettet on 2017-02-23.
 */

public class TaskListFrag extends ListFragment implements AdapterView.OnItemClickListener {
    //private TasksAdapter mAdapter;
    private TasksAdapter mAdapter;
    private ProgressBar mProgressView;
    private ListView mListView;
    private ExpandableListView mExpandable;

    // Shared Preferences for Session
    private static final String mPREFERENCES = "GlowPrefs";
    private static final String mSessionId = "sessionKey";
    private static final String mDeviceId = "deviceId";

    SharedPreferences mSharedPreferences;
    Retrofit retrofit;
    GlowAPI glowAPI;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.possible_task_list_frag, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProgressView = (ProgressBar) view.findViewById(R.id.possible_task_progress);
        //mListView = (ListView) view.findViewById(android.R.id.list);

        //expListView = (ExpandableListView) view.findViewById(R.id.lvExp);
        ArrayList<Task> arrayOfUsers = new ArrayList<Task>();
        mAdapter = new TasksAdapter(this.getActivity(), arrayOfUsers);
        setListAdapter(mAdapter);


        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        glowAPI = retrofit.create(GlowAPI.class);

        mSharedPreferences = this.getActivity().getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);
        showProgress(true);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("onresume", "taskfrag");
        getTasks();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        //Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
        final Task selectedItem = (Task) getListView().getItemAtPosition(position);
        String message = selectedItem.name+" at sensor "+selectedItem.sensor;

        TextView textView = (TextView) view.findViewById(R.id.additionalData);
        textView.setVisibility(textView.getVisibility()==View.VISIBLE ? View.GONE : View.VISIBLE);
        /*if ( textView.getVisibility() == View.GONE) {
            //expandedChildList.set(arg2, true);
            textView.setVisibility(View.VISIBLE);
        }
        else
        {
            //expandedChildList.set(arg2, false);
            textView.setVisibility(View.GONE);
        }*/
        //TODO: finish task
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());//getActivity());
        builder.setMessage(message).setTitle("Complete Task")
                .setPositiveButton("Complete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
                        //completeTask(sessionId, selectedItem.taskId);
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();*/
    }

    private void getTasks(){
        String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
        //TODO: only needed for login? if they are logged in to multiple devices this would
        // indicate the current device they are using
        String deviceId = mSharedPreferences.getString(mDeviceId, "N/A");
        //Log.e('Session');
        if(!sessionId.equals("N/A")){
            getTasks(sessionId, deviceId);
        } else {
            openLoginView();
        }
    }

    private void getTasks(String session, String deviceId){
        Call<TaskList> call = glowAPI.loadActiveTasks(session, deviceId);
        call.enqueue(new Callback<TaskList>() {
            @Override
            public void onResponse(Call<TaskList> call, Response<TaskList> response) {
                if (response.isSuccessful()) {
                    //TODO: plug into array adapter
                    mAdapter.clear();
                    mAdapter.addAll(response.body().active_tasks);
                    showProgress(false);
                    getListView().getEmptyView().setVisibility(View.GONE);
                    if(response.body().active_tasks.size() == 0){
                        getListView().getEmptyView().setVisibility(View.VISIBLE);
                    }
                } else {
                    // No Session
                    openLoginView();
                }
            }
            @Override
            public void onFailure(Call<TaskList> call, Throwable t) {
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

    private void startTask(String session, long taskId){
        Call<Task> call = glowAPI.startTask(session, taskId);
        call.enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    //TODO: plug into array adapter
                    Toast.makeText(getActivity(), "Task Started", Toast.LENGTH_SHORT).show();
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

    public class TasksAdapter extends ArrayAdapter<Task> {

        public TasksAdapter(Context context, ArrayList<Task> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final Task task = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_row, parent, false);
            }

            TextView taskName = (TextView) convertView.findViewById(R.id.taskTitle);
            TextView hours = (TextView) convertView.findViewById(R.id.time);
            Button additionalData = (Button) convertView.findViewById(R.id.additionalData);
            additionalData.setVisibility(View.GONE);
            additionalData.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String message = task.name+" at sensor "+task.sensor;
                    //TODO: finish task
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//getActivity());
                    builder.setMessage(message).setTitle(task.name)
                            .setPositiveButton("Start Task", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
                                    startTask(sessionId, task.taskId);
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

            ImageView image = (ImageView) convertView.findViewById(R.id.locationImage);
            byte[] decodedString = Base64.decode(task.image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            image.setImageBitmap(decodedByte);

            taskName.setText(task.name+" at Pad "+task.sensor);
            hours.setText(getTime(task.date));

            return convertView;

        }

        private String getTime(String date){
            String time = "";
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

            getListView().getEmptyView().setVisibility(show ? View.GONE : View.VISIBLE);
            /*mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

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
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
//package vanderbilt.cs279.org.dispatchmobile;
//
//import android.app.Fragment;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.support.v7.app.AlertDialog;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
///**
// * Created by gpettet on 2017-02-23.
// */
//
//public class TaskListFrag extends Fragment {
//
//    private TextView mText;
//    private Button mLogoutButton, mSettingsButton;
//    private TaskListFrag.TasksAdapter mAdapter;
//
//    // Shared Preferences for Session
//    private static final String mPREFERENCES = "GlowPrefs";
//    private static final String mSessionId = "sessionKey";
//    private static final String mDeviceId = "deviceId";
//
//    SharedPreferences mSharedPreferences;
//    Retrofit retrofit;
//    GlowAPI glowAPI;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        View mView = inflater.inflate(R.layout.activity_main, container, false);
//
//        mText = (TextView) mView.findViewById(R.id.mainText);
//        mLogoutButton = (Button) mView.findViewById(R.id.logout);
//        mLogoutButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mSharedPreferences = getContext().getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);
//                String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
//                logout(sessionId);
//            }
//        });
//        mSettingsButton = (Button) mView.findViewById(R.id.settings_button);
//        mSettingsButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                openSettings();
//            }
//        });
//
//        ArrayList<Task> arrayOfUsers = new ArrayList<Task>();
//        mAdapter = new TaskListFrag.TasksAdapter(getContext(), arrayOfUsers);
//
//        ListView mListView = mView.findViewById(R.id.list);
//
//        mListView.setAdapter(mAdapter);
//
//        retrofit = new Retrofit.Builder()
//                .baseUrl("http://10.0.2.2:8000/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        glowAPI = retrofit.create(GlowAPI.class);
//
//        mSharedPreferences = getContext().getSharedPreferences(mPREFERENCES, Context.MODE_PRIVATE);
//
//        return mView;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();  // Always call the superclass method first
//        getTasks();
//    }
//
//
//    @Override
//    protected void onListItemClick(ListView list, View view, int position, long id) {
//        super.onListItemClick(list, view, position, id);
//        final Task selectedItem = (Task) getListView().getItemAtPosition(position);
//        String message = selectedItem.name+" at sensor "+selectedItem.sensor;
//        //TODO: finish task
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);//getActivity());
//        builder.setMessage(message).setTitle("Complete Task")
//                .setPositiveButton("Complete", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
//                        completeTask(sessionId, selectedItem.taskId);
//                    }
//                });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                // User cancelled the dialog
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//
//    private void getTasks(){
//        String sessionId = mSharedPreferences.getString(mSessionId, "N/A");
//        //TODO: only needed for login? if they are logged in to multiple devices this would
//        // indicate the current device they are using
//        String deviceId = mSharedPreferences.getString(mDeviceId, "N/A");
//        if(!sessionId.equals("N/A")){
//            getTasks(sessionId, deviceId);
//        }
//    }
//
//    private void getTasks(String session, String deviceId){
//        Call<TaskList> call = glowAPI.loadActiveTasks(session, deviceId);
//        call.enqueue(new Callback<TaskList>() {
//            @Override
//            public void onResponse(Call<TaskList> call, Response<TaskList> response) {
//                if (response.isSuccessful()) {
//                    //TODO: plug into array adapter
//                    mAdapter.clear();
//                    mAdapter.addAll(response.body().active_tasks);
//                } else {
//                    // No Session
//                    openLoginView();
//                }
//            }
//            @Override
//            public void onFailure(Call<TaskList> call, Throwable t) {
//                // something went completely south (like no internet connection)
//                Log.e("Error", t.getMessage());
//            }
//        });
//    }
//
//    private void completeTask(String session, long taskId){
//        Call<TaskList> call = glowAPI.completeTask(session, taskId);
//        call.enqueue(new Callback<TaskList>() {
//            @Override
//            public void onResponse(Call<TaskList> call, Response<TaskList> response) {
//                if (response.isSuccessful()) {
//                    //TODO: plug into array adapter
//                    mAdapter.clear();
//                    mAdapter.addAll(response.body().active_tasks);
//                } else {
//                    // No Session
//                    openLoginView();
//                }
//            }
//            @Override
//            public void onFailure(Call<TaskList> call, Throwable t) {
//                // something went completely south (like no internet connection)
//                Log.e("Error", t.getMessage());
//            }
//        });
//    }
//
//    private void logout(String session){
//        //https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api
//        Call<Object> call = glowAPI.logout(session);
//        call.enqueue(new Callback<Object>() {
//            @Override
//            public void onResponse(Call<Object> call, Response<Object> response) {
//                if (response.isSuccessful()) {
//                    SharedPreferences.Editor editor = mSharedPreferences.edit();
//                    editor.putString(mSessionId, "N/A");
//                    editor.apply();
//                    openLoginView();
//                } else {
//                    //nothing happens at failure
//                }
//            }
//            @Override
//            public void onFailure(Call<Object> call, Throwable t) {
//                // something went completely south (like no internet connection)
//                Log.e("Error", t.getMessage());
//            }
//        });
//    }
//
//    private void openLoginView(){
//        Intent myIntent = new Intent(this, LoginActivity.class);
//        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(myIntent);
//    }
//
//    private void openSettings(){
//        Intent myIntent = new Intent(this, UserProfileActivity.class);
//        startActivity(myIntent);
//    }
//
//    public class TasksAdapter extends ArrayAdapter<Task> {
//
//        public TasksAdapter(Context context, ArrayList<Task> users) {
//            super(context, 0, users);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            // Get the data item for this position
//            Task task = getItem(position);
//            // Check if an existing view is being reused, otherwise inflate the view
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_row, parent, false);
//            }
//
//            TextView taskName = (TextView) convertView.findViewById(R.id.taskTitle);
//            TextView date = (TextView) convertView.findViewById(R.id.dateTitle);
//            TextView hours = (TextView) convertView.findViewById(R.id.time);
//
//            taskName.setText(task.name);
//            date.setText(getDateString(task.date));
//            hours.setText(getTime(task.date));
//
//            return convertView;
//
//        }
//
//        private String getDateString(String date){
//            String year = date.substring(0, 4);
//            String month = date.substring(5,7);
//            String day = date.substring(8,10);
//            return month+"/"+day+"/"+year;
//        }
//        private String getTime(String date){
//            String time = "";
//            DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
//            try {
//                SimpleDateFormat localDateFormat = new SimpleDateFormat("h:mm a");
//                time = localDateFormat.format(m_ISO8601Local.parse(date));
//            }catch(Exception e){
//                return "Incorrect";
//            }
//            return time;
//        }
//    }
//
//}

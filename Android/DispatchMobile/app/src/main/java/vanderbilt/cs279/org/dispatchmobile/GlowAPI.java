package vanderbilt.cs279.org.dispatchmobile;


import java.security.Timestamp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Sam on 1/31/2017.
 */

public interface GlowAPI {

    //http://stackoverflow.com/questions/28426154/retrofit-post-parameter
    //http://stackoverflow.com/questions/36703737/simple-login-form-with-retrofit
    @FormUrlEncoded
    @POST("/accounts/android_login/")
    Call<LoginResult> getUserLogin(@Field("username")String uname, @Field("password")String password, @Field("deviceId")String deviceId);

    @FormUrlEncoded
    @POST("/accounts/check_session/")
    Call<LoginResult> getSession(@Field("session")String session, @Field("deviceId")String deviceId);

    @FormUrlEncoded
    @POST("/accounts/android_logout/")
    Call<Object> logout(@Field("session")String session);

    //Todo: some sort of session key
    @GET("https://maps.googleapis.com/maps/api/staticmap")
    Call<TaskList> loadMapIcon(@Query("center") String center, @Query("zoom") String zoom,
                                   @Query("size") String size, @Query("maptype") String maptype,
                                   @Query("key") String key);

    // Location Update
    @FormUrlEncoded
    @POST("/api/android/update_location") // todo web server location api
    Call<Object> updateLocation(@Field("session") String session,
                                @Field("timeStamp") long timestamp,
                                @Field("latitude") double latitude,
                                @Field("longitude") double longitude);


    @FormUrlEncoded
    @POST("/api/android/possible_tasks/")
    Call<TaskList> loadPossibleTasks(@Field("session")String session, @Field("deviceId")String deviceId);

    @FormUrlEncoded
    @POST("/api/android/previous_tasks/")
    Call<TaskList> loadPreviousTasks(@Field("session")String session, @Field("deviceId")String deviceId);

    @FormUrlEncoded
    @POST("/api/android/get_user/")
    Call<UserInformation> getUserInfo(@Field("session")String session);

    @FormUrlEncoded
    @POST("/api/android/update_user/")
    Call<UserInformation> updateUserInfo(@Field("session")String session,
                                         @Field("firstName")String firstName,
                                         @Field("lastName")String lastName,
                                         @Field("email")String email,
                                         @Field("profession") String profession,
                                         @Field("jobs")long[] jobs);

    @FormUrlEncoded
    @POST("/api/android/complete_task/")
    Call<TaskList> completeTask(@Field("session")String session, @Field("taskId")long taskId);

    @FormUrlEncoded
    @POST("/api/android/start_task/")
    Call<Task> startTask(@Field("session")String session, @Field("taskId")long taskId);
}

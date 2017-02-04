package vanderbilt.cs279.org.dispatchmobile;


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
    Call<LoginResult> getUserLogin(@Field("username")String uname, @Field("password")String password);

    @FormUrlEncoded
    @POST("/accounts/check_session/")
    Call<LoginResult> getSession(@Field("session")String session);

    @FormUrlEncoded
    @POST("/accounts/android_logout/")
    Call<Object> logout(@Field("session")String session);

    //Todo: some sort of session key
    @GET("/api/android/activetasks/")
    Call<TaskList> loadActiveTasks();
}

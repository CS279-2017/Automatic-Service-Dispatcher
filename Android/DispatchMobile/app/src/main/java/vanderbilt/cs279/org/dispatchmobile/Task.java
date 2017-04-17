package vanderbilt.cs279.org.dispatchmobile;


/**
 * Created by Sam on 1/31/2017.
 *
 * This is a POJO that contains all of a Task's information.
 *
 * This is built to be used with the Retrofit library, which will automatically create
 * this POJO using the Task JSON response from the server. Reference the
 * GlowAPI class for the Retrofit API
 */

public class Task {

    /*
        Task information
     */
    long taskId;
    long workerId;
    String name;
    String date;
    long sensor;
    String dateCompleted;
    String image;
    double lattitude;
    double longitude;
    int hoursOpen;
    int minutesOpen;
    int levelAtRequest;
    int tankCapacity;
    String pinCode;
    String wage;

    /**
     * Returns a String containing this task's name
     * @return
     */
    @Override
    public String toString() {
        return(name);
    }
}

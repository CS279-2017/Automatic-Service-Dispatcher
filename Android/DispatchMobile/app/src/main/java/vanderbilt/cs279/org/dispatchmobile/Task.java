package vanderbilt.cs279.org.dispatchmobile;

import java.security.Timestamp;

/**
 * Created by Sam on 1/31/2017.
 */

public class Task {
    long taskId;
    long workerId;
    String name;
    String date;
    long sensor;
    String dateCompleted;

    int hoursOpen;

    @Override
    public String toString() {
        return(name);
    }
}

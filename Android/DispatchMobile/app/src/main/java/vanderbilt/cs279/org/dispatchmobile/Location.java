package vanderbilt.cs279.org.dispatchmobile;

import java.security.Timestamp;

/**
 * Created by gpettet on 2017-02-04.
 */
public class Location {
    long workerId;
    Timestamp date;
    long Latitude;
    long Longitude;

    @Override
    public String toString() {
        return workerId + ": " + Latitude + " " + Longitude;
    }
}

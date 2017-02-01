package vanderbilt.cs279.org.dispatchmobile;

/**
 * Created by Sam on 1/31/2017.
 */

public class LoginResult {
    String firstName;
    String lastName;
    String email;
    String id;
    String profession;
    int numActive;
    int numDone;

    @Override
    public String toString() {
        return(firstName+" "+lastName);
    }
}

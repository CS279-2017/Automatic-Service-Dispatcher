package vanderbilt.cs279.org.dispatchmobile;

/**
 * Created by Sam on 2/18/2017.
 *
 * This is a POJO that contains a Skill object's information
 *
 * This is built to be used with the Retrofit library, which will automatically create
 * this POJO using the Skill JSON response from the server. Reference the
 * GlowAPI class for the Retrofit API
 */

public class Skill {

    /*
        Skill information
     */
    String name;
    String title;
    long id;

    /**
     * Returns a string containing this skill's name
     * @return
     */
    @Override
    public String toString(){
        return name;
    }
}

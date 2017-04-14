package vanderbilt.cs279.org.dispatchmobile;

import java.util.List;

/**
 * Created by Sam on 1/31/2017.
 *
 * This is a POJO containing a list of active tasks.
 *
 * This is built to be used with the Retrofit library, which will automatically create
 * this POJO using the TaskList JSON response from the server. Reference the GlowAPI
 * class for the Retrofit API
 */

public class TaskList {

    // List of tasks
    List<Task> active_tasks;
    //List<Task> completed_tasks;


    /**
     * Returns a String containing the names of each task held by this list
     * @return
     */
    @Override
    public String toString() {
        String str = "";
        for(int i =0;i<active_tasks.size(); i++){
            str+=active_tasks.get(i).name;
        }
        return(str);
    }
}

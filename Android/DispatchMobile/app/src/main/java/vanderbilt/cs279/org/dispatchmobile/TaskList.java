package vanderbilt.cs279.org.dispatchmobile;

import java.util.List;

/**
 * Created by Sam on 1/31/2017.
 */

public class TaskList {
    List<Task> active_tasks;
    //List<Task> completed_tasks;


    @Override
    public String toString() {
        String str = "";
        for(int i =0;i<active_tasks.size(); i++){
            str+=active_tasks.get(i).name;
        }
        return(str);
    }
}

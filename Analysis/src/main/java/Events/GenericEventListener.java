package Events;

import Data.AlertData;
import Data.Data;
import Data.Tag;
import Utils.RestUtils;
import com.espertech.esper.client.EventBean;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by gpettet on 2017-02-09.
 */
public class GenericEventListener implements EsperEventListener{

    /*
    todo: needs:
        - Log tag
        - Job Id
        - Esper Statement ~ select statement: (basic)
            * data to select
            * sensor to monitor (or all sensors?)
            * time window? other esper constriants?
            * "where" statement
       - alert to generate - tag and what triggered alert
     */


    private String mEventDescription;
    private int mEventId;
    private String mEsperStatement;
    private List<String> mElementsOfInterest;

    public GenericEventListener(String eventDescription,
                                int eventId,
                                String esperStatement,
                                List<String> elementsOfInterest) {

        mEventDescription = eventDescription;
        mEventId = eventId;
        mEsperStatement = esperStatement;
        mElementsOfInterest = elementsOfInterest;
    }


    public String getEsperStatement() {
        return mEsperStatement;
    }

    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        for(EventBean event :newEvents){
            System.out.println("Event " + mEventId + " Triggered: " + mEventDescription); //TODO: 2017-02-09  log statment

        }
    }

    private void sendAlert(EventBean event) {

        Gson gson = new Gson();

        AlertData alert = new AlertData(
                new Data(
                        (String) event.get("sensorID"),
                        (Double) event.get("temperature"),
                        (Double) event.get("pressure"),
                        (Double) event.get("voltage")),
                new Tag(mEventDescription),
                gson.toJson(mElementsOfInterest));
        
        String jsonString = gson.toJson(alert);

        System.out.println(jsonString);

        RestUtils.httpPostJson(RestUtils.DISPATCH_URL,
                new JSONObject(jsonString));

    }
}

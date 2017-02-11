package Events;

import Data.OldAlertData;
import Data.Data;
import Data.Tag;
import Utils.RestUtils;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.Gson;
import org.json.JSONObject;

/**
 * Created by gpettet on 2016-12-01.
 */
public class LowPressureListener implements EsperEventListener {

    public static final String JOB_ID = "LOW_PRESSURE";

    public LowPressureListener(String sensorID) {
        this.sensorID = sensorID;
    }

    private String sensorID;

    public void update(EventBean[] newEvents, EventBean[] oldEvents){
        EventBean event = newEvents[0];

        if(event.get("pressure") != null) {

            System.out.println("\tError: low pressure");
            sendAlert(event);

        }
    }

    public String getEsperStatement() {
        return "select * from Data.Data(sensorID=\'" + sensorID +"\').win:time(5 sec) where pressure < 3";
    }

    private void sendAlert(EventBean event) {
        OldAlertData alert = new OldAlertData(
                new Data(
                        sensorID,
                        (Double) event.get("temperature"),
                        (Double) event.get("pressure"),
                        (Double) event.get("voltage")),
                new Tag(
                        JOB_ID
                ));
        String jsonString = new Gson().toJson(alert);

        System.out.println(jsonString);

        RestUtils.httpPostJson(RestUtils.DISPATCH_URL,
                new JSONObject(jsonString));

    }
}

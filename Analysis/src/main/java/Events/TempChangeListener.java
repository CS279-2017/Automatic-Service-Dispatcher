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
public class TempChangeListener implements EsperEventListener {

    public static final String JOB_ID = "TEMPERATURE_CHANGE";

    private String sensorID;

    public TempChangeListener(String sensorID) {
        this.sensorID = sensorID;
    }

    public void update(EventBean[] newEvents, EventBean[] oldEvents){
        EventBean event = newEvents[0];

        if(event.get("max(temperature)") != null) {

            double maxTemp = (Double) event.get("max(temperature)");
            double minTemp = (Double) event.get("min(temperature)");

            double tempFluc = maxTemp - minTemp;

            //System.out.println("TempFluc=" + tempFluc);

            if (tempFluc > 40) {
                System.out.println("\tError: temp change for sensor" + sensorID);
                sendAlert(event, tempFluc);
            }
        }
    }

    public String getEsperStatement() {
        return "select max(temperature), min(temperature) from Data.Data(sensorID=\'" + sensorID +"\').win:time(5 sec)";
    }

    private void sendAlert(EventBean event, double tempFluc) {
        OldAlertData alert = new OldAlertData(
                new Data(
                        sensorID,
                        tempFluc,
                        0.0,
                        0.0),
                new Tag(
                        JOB_ID
                ));
        String jsonString = new Gson().toJson(alert);

        System.out.println(jsonString);

        RestUtils.httpPostJson(RestUtils.DISPATCH_URL,
                new JSONObject(jsonString));

    }
}

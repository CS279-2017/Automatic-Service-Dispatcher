package Events;

import com.espertech.esper.client.EventBean;

/**
 * Created by gpettet on 2017-02-09.
 */
public class TestListener implements EsperEventListener {
    public String getEsperStatement() {
        return null;
    }

    public void update(EventBean[] newEvents, EventBean[] oldEvents) {

    }
}

package Data;

import com.google.gson.JsonArray;

/**
 * Created by gpettet on 2016-12-01.
 */
public class OldAlertData {

    private Data data;
    private Tag tag;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public OldAlertData(Data data, Tag tag) {
        this.data = data;
        this.tag = tag;
    }
}


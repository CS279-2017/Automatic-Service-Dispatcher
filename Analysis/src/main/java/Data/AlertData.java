package Data;

import com.google.gson.JsonArray;

/**
 * Created by gpettet on 2016-12-01.
 */
public class AlertData {

    private Data data;
    private Tag tag;
    private String interestElements;

    public String getInterestElements() {
        return interestElements;
    }

    public void setInterestElements(String interestElements) {
        this.interestElements = interestElements;
    }


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

    public AlertData(Data data, Tag tag, String interestElements) {
        this.data = data;
        this.tag = tag;
        this.interestElements = interestElements;
    }
}

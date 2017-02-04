package vanderbilt.cs279.org.dispatchmobile;

import android.app.ListActivity;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {

    private TextView mText;
    private List<String> listValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView) findViewById(R.id.mainText);

        listValues = new ArrayList<String>();
        listValues.add("Android");
        listValues.add("Andid");
        listValues.add("Andr");

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.task_row, R.id.taskText, listValues);

        setListAdapter(myAdapter);
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        String selectedItem = (String) getListView().getItemAtPosition(position);

        mText.setText("You clicked " + selectedItem + " at position " + position);
    }
}

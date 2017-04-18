package vanderbilt.cs279.org.dispatchmobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;

public class ReviewActivity extends AppCompatActivity {

    // UI references.
    private ImageView mUserPhoto;
    private TextView mUserInfo;
    private EditText mReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        mUserPhoto = (ImageView) findViewById(R.id.userPhoto);
        mUserInfo = (TextView) findViewById(R.id.userInfo);
        mReview = (EditText) findViewById(R.id.userReview);
    }

    private void loadUserInfoStatistics() {
        // load user info to show in the activity like photo, info
    }

}

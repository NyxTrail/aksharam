package in.digistorm.aksharam;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    String logTag = "HelpActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(logTag, "Starting help activity...");
        setContentView(R.layout.activity_help);

        ((TextView) findViewById(R.id.help_activity_tv))
                .setText(Html.fromHtml(getString(R.string.help_text)));
    }
}

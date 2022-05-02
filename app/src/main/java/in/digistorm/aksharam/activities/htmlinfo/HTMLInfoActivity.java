package in.digistorm.aksharam.activities.htmlinfo;

/*
 * Copyright (c) 2022 Alan M Varghese <alan@digistorm.in>
 *
 * This files is part of Aksharam, a script teaching app for Indic
 * languages.
 *
 * Aksharam is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aksharam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even teh implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import in.digistorm.aksharam.R;
import in.digistorm.aksharam.util.Log;

/* A full screen TextView activity to display information in html for
   help, privacy policy etc
 */
public class HTMLInfoActivity extends AppCompatActivity {
    String logTag = "HelpActivity";

    public static String EXTRA_NAME = "HTMLINFO_EXTRA";
    public enum EXTRA_VALUES {
        HELP,
        PRIVACY
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(logTag, "Starting HTMLInfoActivity...");
        setContentView(R.layout.activity_help);

        Object extra = getIntent().getExtras().get(EXTRA_NAME);

        TextView htmlInfoActivityTV = findViewById(R.id.htmlinfo_activity_tv);
        htmlInfoActivityTV.setMovementMethod(LinkMovementMethod.getInstance());
        if(extra == EXTRA_VALUES.HELP) {
            Log.d(logTag, "Displaying help");
            String text = "<a href='http://www.google.com'>Google</a> this is a link";

            htmlInfoActivityTV.setText(Html.fromHtml(getString(R.string.help_text)));
        }
        else if(extra == EXTRA_VALUES.PRIVACY) {
            Log.d(logTag, "Displaying privacy");
            htmlInfoActivityTV.setText(Html.fromHtml(getString(R.string.privacy_text)));
        }
    }
}
